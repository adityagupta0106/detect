package gov.nic.esdt.vismai.fraudanalytics.controller;

import gov.nic.esdt.vismai.fraudanalytics.dto.DashboardSummaryDto;
import gov.nic.esdt.vismai.fraudanalytics.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Duration;

/** Monitoring dashboard (SRS 5.4). */
@RestController
@RequestMapping("/api/fraud/dashboard")
public class DashboardController {

    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService) {
		this.dashboardService = dashboardService;
	}

	@GetMapping("/summary")
    public DashboardSummaryDto summary(@RequestParam(defaultValue = "24") long windowHours) {
        return dashboardService.summary(Duration.ofHours(windowHours));
    }
}
