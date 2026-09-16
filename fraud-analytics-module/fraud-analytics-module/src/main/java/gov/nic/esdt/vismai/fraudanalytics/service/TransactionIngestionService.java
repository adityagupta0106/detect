package gov.nic.esdt.vismai.fraudanalytics.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import gov.nic.esdt.vismai.fraudanalytics.domain.entity.AuthTransaction;
import gov.nic.esdt.vismai.fraudanalytics.domain.entity.FraudRule;
import gov.nic.esdt.vismai.fraudanalytics.domain.enums.RiskLevel;
import gov.nic.esdt.vismai.fraudanalytics.dto.AuthEventDto;
import gov.nic.esdt.vismai.fraudanalytics.repository.AuthTransactionRepository;

/**
 * Orchestrates the end-to-end pipeline for a single authentication event:
 * persist to the Authentication Transaction Repository (SRS 5.1), evaluate
 * fraud rules (SRS 5.2), score risk (SRS 5.3), and raise an alert if warranted
 * (SRS 5.5).
 *
 * This is the single entry point used by both the Kafka consumer (primary
 * path) and the fallback REST ingestion endpoint (SRS Section 9), so the two
 * paths can never diverge in behaviour.
 */
@Service
public class TransactionIngestionService {


    private final AuthTransactionRepository authTransactionRepository;
    private final RuleEngineService ruleEngineService;
    private final RiskScoringService riskScoringService;
    private final AlertService alertService;
    
    public TransactionIngestionService(AuthTransactionRepository authTransactionRepository,
			RuleEngineService ruleEngineService, RiskScoringService riskScoringService, AlertService alertService) {
		this.authTransactionRepository = authTransactionRepository;
		this.ruleEngineService = ruleEngineService;
		this.riskScoringService = riskScoringService;
		this.alertService = alertService;
	}

	@Transactional
    public void ingest(AuthEventDto event) {
        persistTransaction(event);

        RuleEngineService.EvaluationResult evaluation = ruleEngineService.evaluate(event);
        List<FraudRule> triggered = evaluation.triggeredRules();

        if (triggered.isEmpty()) {
            return;
        }

        int score = riskScoringService.computeScore(triggered);
        RiskLevel level = riskScoringService.classify(score);

        alertService.raiseAlert(event, triggered, score, level);
    }

    private void persistTransaction(AuthEventDto event) {
    	AuthTransaction txn = new AuthTransaction();
    	txn.setTransactionId(event.transactionId());
    	txn.setAadhaarRefToken(event.aadhaarRefToken());
    	txn.setAuthType(event.authType());
    	txn.setEventTimestamp(event.eventTimestamp());
    	txn.setSourceIp(event.sourceIp());
    	txn.setDeviceFingerprint(event.deviceFingerprint());
		txn.setRequestingUserId(event.requestingUserId());
    	txn.setServiceId(event.serviceId());
    	txn.setDepartment(event.department());
    	txn.setState(event.state());
    	txn.setResult(event.result());
    	txn.setUidaiErrorCode(event.uidaiErrorCode());
    	txn.setResponseTimeMs(event.responseTimeMs());

        authTransactionRepository.save(txn);
    }
}
