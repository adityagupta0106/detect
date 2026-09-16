package gov.nic.esdt.vismai.fraudanalytics.service;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.mvel2.MVEL;
import org.springframework.stereotype.Service;

import gov.nic.esdt.vismai.fraudanalytics.domain.entity.FraudRule;
import gov.nic.esdt.vismai.fraudanalytics.dto.AuthEventDto;
import gov.nic.esdt.vismai.fraudanalytics.repository.FraudRuleRepository;

/**
 * Rule Engine (SRS 5.2) — evaluates each incoming authentication event against
 * the configured set of MVEL-expressed {@link FraudRule}s.
 *
 * Reuses MVEL, the same expression engine already standardised in the
 * ServicePlus Workflow Management Module, instead of introducing a second,
 * parallel rule engine (SRS 4.2 "Reuse Before Rebuild").
 *
 * Compiled expressions are cached so repeated evaluation does not re-parse
 * the MVEL source on every event.
 */
@Service
public class RuleEngineService {
	private static final Logger log = LogManager.getLogger("famLogger");

    private final FraudRuleRepository fraudRuleRepository;
    private final RollingCounterService rollingCounterService;

	public RuleEngineService(FraudRuleRepository fraudRuleRepository, RollingCounterService rollingCounterService) {
		this.fraudRuleRepository = fraudRuleRepository;
		this.rollingCounterService = rollingCounterService;
	}

	private final Map<String, Object> compiledExpressionCache = new ConcurrentHashMap<>();

    private static final Duration WINDOW_10_MIN = Duration.ofMinutes(10);
    private static final Duration WINDOW_15_MIN = Duration.ofMinutes(15);
    private static final Duration WINDOW_30_MIN = Duration.ofMinutes(30);
    private static final Duration WINDOW_5_MIN = Duration.ofMinutes(5);
    private static final Duration WINDOW_1_HOUR = Duration.ofHours(1);

    public record EvaluationResult(List<FraudRule> triggeredRules, RuleEvaluationContext context) {}

    /**
     * Builds the rule context from Redis rolling counters and evaluates every
     * enabled, in-scope rule against it.
     */
    public EvaluationResult evaluate(AuthEventDto event) {
        RuleEvaluationContext context = buildContext(event);

        List<FraudRule> triggered = new ArrayList<>();
        for (FraudRule rule : applicableRules(event)) {
            if (safeEvaluate(rule, context)) {
                triggered.add(rule);
            }
        }
        return new EvaluationResult(triggered, context);
    }

    private List<FraudRule> applicableRules(AuthEventDto event) {
        return fraudRuleRepository.findByEnabledTrue().stream()
                .filter(r -> r.getDepartment() == null || r.getDepartment().equalsIgnoreCase(event.department()))
                .filter(r -> r.getState() == null || r.getState().equalsIgnoreCase(event.state()))
                .toList();
    }

    private boolean safeEvaluate(FraudRule rule, RuleEvaluationContext context) {
        try {
            Object compiled = compiledExpressionCache.computeIfAbsent(
                    rule.getCode() + "::" + rule.getMvelExpression(),
                    k -> MVEL.compileExpression(rule.getMvelExpression()));
            Map<String, Object> vars = Map.of("ctx", context);
            Object result = MVEL.executeExpression(compiled, vars);
            return Boolean.TRUE.equals(result);
        } catch (Exception ex) {
            // A malformed rule must never take down the pipeline — log and skip it.
            log.error("Fraud rule '{}' failed to evaluate; expression='{}'. Skipping rule.",
                    rule.getCode(), rule.getMvelExpression(), ex);
            return false;
        }
    }

    private RuleEvaluationContext buildContext(AuthEventDto event) {
        String userKey = event.requestingUserId();
        String ipKey = event.sourceIp();
        String tokenKey = event.aadhaarRefToken();

        long otpCount = event.authType().name().equals("OTP")
                ? rollingCounterService.incrementAndGetCount("otp-req", userKey, WINDOW_10_MIN)
                : rollingCounterService.currentCount("otp-req", userKey);

        long failureCount = "FAILURE".equals(event.result().name())
                ? rollingCounterService.incrementAndGetCount("failures", userKey, WINDOW_30_MIN)
                : rollingCounterService.currentCount("failures", userKey);

        long distinctIpsForToken = rollingCounterService.addToSetAndGetSize(
                "ip-per-token", tokenKey, ipKey, WINDOW_15_MIN);

        long distinctUsersForIp = rollingCounterService.addToSetAndGetSize(
                "user-per-ip", ipKey, userKey, WINDOW_1_HOUR);

        long ekycAttempts = event.authType().name().equals("EKYC")
                ? rollingCounterService.incrementAndGetCount("ekyc-attempts", userKey, WINDOW_5_MIN)
                : rollingCounterService.currentCount("ekyc-attempts", userKey);

        long totalLastHour = rollingCounterService.incrementAndGetCount("total-1h", userKey, WINDOW_1_HOUR);
        long failuresLastHour = "FAILURE".equals(event.result().name())
                ? rollingCounterService.incrementAndGetCount("failures-1h", userKey, WINDOW_1_HOUR)
                : rollingCounterService.currentCount("failures-1h", userKey);

        boolean newDevice = event.deviceFingerprint() != null
                && rollingCounterService.addToSetAndGetSize("devices", userKey, event.deviceFingerprint(), Duration.ofDays(30)) <= 1;

        return new RuleEvaluationContext(
                event,
                otpCount,
                failureCount,
                distinctIpsForToken,
                distinctUsersForIp,
                ekycAttempts,
                totalLastHour,
                failuresLastHour,
                newDevice,
                false // resolved against per-department config; see RuleConfigController
        );
    }
}
