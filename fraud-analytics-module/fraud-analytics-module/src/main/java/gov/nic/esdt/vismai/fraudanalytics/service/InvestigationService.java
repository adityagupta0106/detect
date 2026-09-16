package gov.nic.esdt.vismai.fraudanalytics.service;

import java.time.Instant;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import gov.nic.esdt.vismai.fraudanalytics.domain.entity.AuthTransaction;
import gov.nic.esdt.vismai.fraudanalytics.domain.entity.FraudAlert;
import gov.nic.esdt.vismai.fraudanalytics.domain.entity.InvestigationRemark;
import gov.nic.esdt.vismai.fraudanalytics.domain.enums.AlertStatus;
import gov.nic.esdt.vismai.fraudanalytics.dto.InvestigationRequests.AddRemarkRequest;
import gov.nic.esdt.vismai.fraudanalytics.dto.InvestigationRequests.CloseAlertRequest;
import gov.nic.esdt.vismai.fraudanalytics.exception.ResourceNotFoundException;
import gov.nic.esdt.vismai.fraudanalytics.repository.AuthTransactionRepository;
import gov.nic.esdt.vismai.fraudanalytics.repository.FraudAlertRepository;

/**
 * Investigation Workbench (SRS 5.6) — search, timeline view, remarks, and
 * alert closure with mandatory disposition. Every action here is retained as
 * audit evidence (SRS 6.3 / Section 10).
 */
@Service
public class InvestigationService {

    private final FraudAlertRepository fraudAlertRepository;
    private final AuthTransactionRepository authTransactionRepository;
    
    public InvestigationService(FraudAlertRepository fraudAlertRepository,
			AuthTransactionRepository authTransactionRepository) {
		this.fraudAlertRepository = fraudAlertRepository;
		this.authTransactionRepository = authTransactionRepository;
	}

	public FraudAlert getAlert(UUID alertId) {
        return fraudAlertRepository.findById(alertId)
                .orElseThrow(() -> new ResourceNotFoundException("Alert not found: " + alertId));
    }

    public Page<AuthTransaction> userTimeline(String requestingUserId, int page, int size) {
        return authTransactionRepository.findByRequestingUserIdOrderByEventTimestampDesc(
                requestingUserId, PageRequest.of(page, size));
    }

    public Page<AuthTransaction> ipTimeline(String sourceIp, int page, int size) {
        return authTransactionRepository.findBySourceIpOrderByEventTimestampDesc(
                sourceIp, PageRequest.of(page, size));
    }

    @Transactional
    public InvestigationRemark addRemark(UUID alertId, AddRemarkRequest request) {
        FraudAlert alert = getAlert(alertId);
        InvestigationRemark remark = new InvestigationRemark();
        remark.setAlert(alert);
        remark.setAuthor(request.author());
        remark.setRemark(request.remark());
        alert.getRemarks().add(remark);
        fraudAlertRepository.save(alert);
        return remark;
    }

    @Transactional
    public FraudAlert closeAlert(UUID alertId, CloseAlertRequest request) {
        FraudAlert alert = getAlert(alertId);
        alert.setStatus(AlertStatus.CLOSED);
        alert.setDisposition(request.disposition());
        alert.setClosedAt(Instant.now());

        if (request.closingRemark() != null && !request.closingRemark().isBlank()) {
        	InvestigationRemark remark = new InvestigationRemark();
        	remark.setAlert(alert);
        	remark.setAuthor(request.closedBy());
        	remark.setRemark("[Closing remark] " + request.closingRemark());
            alert.getRemarks().add(remark);
        }

        return fraudAlertRepository.save(alert);
    }
}
