package gov.nic.esdt.vismai.fraudanalytics.dto;

import gov.nic.esdt.vismai.fraudanalytics.domain.entity.FraudAlert;
import gov.nic.esdt.vismai.fraudanalytics.domain.enums.AlertDisposition;
import gov.nic.esdt.vismai.fraudanalytics.domain.enums.AlertStatus;
import gov.nic.esdt.vismai.fraudanalytics.domain.enums.RiskLevel;

import java.time.Instant;
import java.util.UUID;

public record AlertDto(
        UUID id,
        String transactionId,
        String triggeredRuleCodes,
        int riskScore,
        RiskLevel riskLevel,
        AlertStatus status,
        AlertDisposition disposition,
        String requestingUserId,
        String department,
        String state,
        Instant createdAt,
        Instant closedAt
) {
    public static AlertDto from(FraudAlert a) {
        return new AlertDto(a.getId(), a.getTransactionId(), a.getTriggeredRuleCodes(),
                a.getRiskScore(), a.getRiskLevel(), a.getStatus(), a.getDisposition(),
                a.getRequestingUserId(), a.getDepartment(), a.getState(),
                a.getCreatedAt(), a.getClosedAt());
    }
}
