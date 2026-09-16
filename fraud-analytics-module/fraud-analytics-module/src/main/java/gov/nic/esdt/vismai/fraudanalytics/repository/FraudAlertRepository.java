package gov.nic.esdt.vismai.fraudanalytics.repository;

import gov.nic.esdt.vismai.fraudanalytics.domain.entity.FraudAlert;
import gov.nic.esdt.vismai.fraudanalytics.domain.enums.AlertStatus;
import gov.nic.esdt.vismai.fraudanalytics.domain.enums.RiskLevel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.UUID;

public interface FraudAlertRepository extends JpaRepository<FraudAlert, UUID> {

    Page<FraudAlert> findByStatus(AlertStatus status, Pageable pageable);

    Page<FraudAlert> findByRiskLevel(RiskLevel riskLevel, Pageable pageable);

    Page<FraudAlert> findByDepartmentAndState(String department, String state, Pageable pageable);

    long countByCreatedAtAfter(Instant after);

    long countByStatusAndCreatedAtAfter(AlertStatus status, Instant after);
}
