package gov.nic.esdt.vismai.fraudanalytics.service;

import gov.nic.esdt.vismai.fraudanalytics.domain.entity.FraudRule;
import gov.nic.esdt.vismai.fraudanalytics.domain.enums.RiskLevel;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Computes the composite risk score from triggered rule weights and maps it
 * to a risk level (SRS 5.3).
 *
 * Score bands: 0-30 Normal, 31-60 Medium, >60 High. Weights themselves live on
 * {@link FraudRule#getWeight()} and are externally configurable.
 */
@Service
public class RiskScoringService {

    private static final int MAX_SCORE = 100;

    public int computeScore(List<FraudRule> triggeredRules) {
        int score = triggeredRules.stream().mapToInt(FraudRule::getWeight).sum();
        return Math.min(score, MAX_SCORE);
    }

    public RiskLevel classify(int score) {
        return RiskLevel.fromScore(score);
    }
}
