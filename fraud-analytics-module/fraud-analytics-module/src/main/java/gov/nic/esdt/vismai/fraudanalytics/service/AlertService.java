package gov.nic.esdt.vismai.fraudanalytics.service;

import java.util.List;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;

import gov.nic.esdt.vismai.fraudanalytics.domain.entity.FraudAlert;
import gov.nic.esdt.vismai.fraudanalytics.domain.entity.FraudRule;
import gov.nic.esdt.vismai.fraudanalytics.domain.enums.RiskLevel;
import gov.nic.esdt.vismai.fraudanalytics.dto.AuthEventDto;
import gov.nic.esdt.vismai.fraudanalytics.repository.FraudAlertRepository;

/**
 * Raises fraud alerts and dispatches notifications (SRS 5.5).
 *
 * Dashboard notification is always emitted (alerts are simply persisted and
 * visible via the dashboard/investigation APIs). Email/SMS channels are
 * dispatched for MEDIUM/HIGH risk per department notification config; wiring
 * to the platform's existing notification gateway is left to
 * {@link NotificationDispatcher}, an integration seam rather than a
 * reimplementation of VismAI's notification infrastructure (SRS 5.5 / 11).
 */
@Service
public class AlertService {
	private static final Logger log = LogManager.getLogger("famLogger");

    private final FraudAlertRepository fraudAlertRepository;
    private final NotificationDispatcher notificationDispatcher;
    
    public AlertService(FraudAlertRepository fraudAlertRepository, NotificationDispatcher notificationDispatcher) {
		this.fraudAlertRepository = fraudAlertRepository;
		this.notificationDispatcher = notificationDispatcher;
	}



	public FraudAlert raiseAlert(AuthEventDto event, List<FraudRule> triggeredRules, int riskScore, RiskLevel riskLevel) {
        String ruleCodes = triggeredRules.stream().map(FraudRule::getCode).collect(Collectors.joining(","));

        FraudAlert alert = new FraudAlert();
        alert.setTransactionId(event.transactionId());
        alert.setTriggeredRuleCodes(ruleCodes);
        alert.setRiskScore(riskScore);
        alert.setRiskLevel(riskLevel);
        alert.setRequestingUserId(event.requestingUserId());
        alert.setDepartment(event.department());
        alert.setState(event.state());

        alert = fraudAlertRepository.save(alert);

        // High-risk alerts are surfaced immediately, not deferred to a batch cycle (SRS 5.5).
        if (riskLevel == RiskLevel.HIGH || riskLevel == RiskLevel.MEDIUM) {
            notificationDispatcher.dispatch(alert, event);
        }

        log.info("Fraud alert raised: id={}, txn={}, score={}, level={}, rules={}",
                alert.getId(), alert.getTransactionId(), riskScore, riskLevel, ruleCodes);

        return alert;
    }
}
