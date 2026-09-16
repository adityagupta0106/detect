package gov.nic.esdt.vismai.fraudanalytics.controller;

import gov.nic.esdt.vismai.fraudanalytics.domain.entity.AuthTransaction;
import gov.nic.esdt.vismai.fraudanalytics.domain.entity.FraudAlert;
import gov.nic.esdt.vismai.fraudanalytics.domain.entity.InvestigationRemark;
import gov.nic.esdt.vismai.fraudanalytics.domain.enums.AlertStatus;
import gov.nic.esdt.vismai.fraudanalytics.dto.AlertDto;
import gov.nic.esdt.vismai.fraudanalytics.dto.InvestigationRequests.AddRemarkRequest;
import gov.nic.esdt.vismai.fraudanalytics.dto.InvestigationRequests.CloseAlertRequest;
import gov.nic.esdt.vismai.fraudanalytics.repository.FraudAlertRepository;
import gov.nic.esdt.vismai.fraudanalytics.service.InvestigationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * Alert search/detail (SRS 5.5) and the Investigation Workbench (SRS 5.6):
 * timeline views, remarks, and closure with mandatory disposition.
 */
@RestController
@RequestMapping("/api/fraud/alerts")
public class AlertController {

    private final FraudAlertRepository fraudAlertRepository;
    private final InvestigationService investigationService;
    
	public AlertController(FraudAlertRepository fraudAlertRepository, InvestigationService investigationService) {
		this.fraudAlertRepository = fraudAlertRepository;
		this.investigationService = investigationService;
	}

	@GetMapping
    public Page<AlertDto> listAlerts(
            @RequestParam(required = false) AlertStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Page<FraudAlert> alerts = status != null
                ? fraudAlertRepository.findByStatus(status, PageRequest.of(page, size))
                : fraudAlertRepository.findAll(PageRequest.of(page, size));
        return alerts.map(AlertDto::from);
    }

    @GetMapping("/{id}")
    public AlertDto getAlert(@PathVariable UUID id) {
        return AlertDto.from(investigationService.getAlert(id));
    }

    @GetMapping("/timeline/user/{userId}")
    public Page<AuthTransaction> userTimeline(@PathVariable String userId,
                                               @RequestParam(defaultValue = "0") int page,
                                               @RequestParam(defaultValue = "20") int size) {
        return investigationService.userTimeline(userId, page, size);
    }

    @GetMapping("/timeline/ip/{ip}")
    public Page<AuthTransaction> ipTimeline(@PathVariable String ip,
                                             @RequestParam(defaultValue = "0") int page,
                                             @RequestParam(defaultValue = "20") int size) {
        return investigationService.ipTimeline(ip, page, size);
    }

    @PostMapping("/{id}/remarks")
    public InvestigationRemark addRemark(@PathVariable UUID id, @Valid @RequestBody AddRemarkRequest request) {
        return investigationService.addRemark(id, request);
    }

    @PostMapping("/{id}/close")
    public AlertDto closeAlert(@PathVariable UUID id, @Valid @RequestBody CloseAlertRequest request) {
        return AlertDto.from(investigationService.closeAlert(id, request));
    }
}
