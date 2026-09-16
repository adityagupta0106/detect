package gov.nic.esdt.vismai.fraudanalytics.service;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import gov.nic.esdt.vismai.fraudanalytics.domain.enums.AuthResult;
import gov.nic.esdt.vismai.fraudanalytics.domain.enums.RiskLevel;
import gov.nic.esdt.vismai.fraudanalytics.dto.DashboardSummaryDto;
import gov.nic.esdt.vismai.fraudanalytics.repository.AuthTransactionRepository;
import gov.nic.esdt.vismai.fraudanalytics.repository.FraudAlertRepository;

/** Builds the monitoring dashboard summary (SRS 5.4). */
@Service
public class DashboardService {

    private final AuthTransactionRepository authTransactionRepository;
    private final FraudAlertRepository fraudAlertRepository;

    public DashboardService(AuthTransactionRepository authTransactionRepository,
			FraudAlertRepository fraudAlertRepository) {
		this.authTransactionRepository = authTransactionRepository;
		this.fraudAlertRepository = fraudAlertRepository;
	}

	public DashboardSummaryDto summary(Duration window) {
        Instant since = Instant.now().minus(window);
        Instant todayStart = Instant.now().truncatedTo(ChronoUnit.DAYS);

        long total = authTransactionRepository.countByEventTimestampAfter(since);
        long success = authTransactionRepository.countByResultAndEventTimestampAfter(AuthResult.SUCCESS, since);
        long failed = authTransactionRepository.countByResultAndEventTimestampAfter(AuthResult.FAILURE, since);

        long alertsToday = fraudAlertRepository.countByCreatedAtAfter(todayStart);
        long openHighRisk = fraudAlertRepository.findByRiskLevel(RiskLevel.HIGH, PageRequest.of(0, 1))
                .getTotalElements();

        // Top source IPs by volume would typically be a native aggregate query;
        // represented here as an extension point (see repository layer for a
        // Specification-based or native-query implementation against auth_transaction).
        List<DashboardSummaryDto.TopIp> topIps = List.of();

        return DashboardSummaryDto.of(total, success, failed, alertsToday, openHighRisk, topIps);
    }
}
