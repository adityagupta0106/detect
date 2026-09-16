package gov.nic.esdt.vismai.fraudanalytics.dto;

import java.util.List;
import java.util.Map;

/** Dashboard summary payload (SRS 5.4). */
public record DashboardSummaryDto(
        long totalAuthentications,
        long successfulAuthentications,
        long failedAuthentications,
        double failureRatePercent,
        long alertsToday,
        long openHighRiskAlerts,
        List<TopIp> topSourceIps
) {
    public record TopIp(String ip, long count) {}

    public static DashboardSummaryDto of(long total, long success, long failed,
                                          long alertsToday, long openHighRisk,
                                          List<TopIp> topIps) {
        double failureRate = total == 0 ? 0.0 : (failed * 100.0) / total;
        return new DashboardSummaryDto(total, success, failed, failureRate, alertsToday, openHighRisk, topIps);
    }
}
