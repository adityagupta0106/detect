package gov.nic.esdt.vismai.fraudanalytics.domain.entity;

import gov.nic.esdt.vismai.fraudanalytics.domain.enums.AlertDisposition;
import gov.nic.esdt.vismai.fraudanalytics.domain.enums.AlertStatus;
import gov.nic.esdt.vismai.fraudanalytics.domain.enums.RiskLevel;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * A raised fraud alert (SRS 5.5) — the auditable unit that flows into the
 * Investigation Workbench (SRS 5.6) and ultimately into annual audit evidence
 * (SRS 6.3 / Section 10 Compliance Mapping).
 */
@Entity
@Table(name = "fraud_alert", indexes = {
        @Index(name = "idx_fraud_alert_status", columnList = "status"),
        @Index(name = "idx_fraud_alert_risk_level", columnList = "risk_level"),
        @Index(name = "idx_fraud_alert_created_at", columnList = "created_at")
})
public class FraudAlert {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    /** The transaction that triggered this alert. */
    @Column(name = "transaction_id", nullable = false, length = 100)
    private String transactionId;

    /** Comma-separated rule codes that fired (kept simple; could be a child table). */
    @Column(name = "triggered_rule_codes", nullable = false, length = 512)
    private String triggeredRuleCodes;

    @Column(name = "risk_score", nullable = false)
    private Integer riskScore;

    @Enumerated(EnumType.STRING)
    @Column(name = "risk_level", nullable = false, length = 16)
    private RiskLevel riskLevel;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 16)
    private AlertStatus status = AlertStatus.OPEN;

    @Enumerated(EnumType.STRING)
    @Column(name = "disposition", nullable = false, length = 24)
    private AlertDisposition disposition = AlertDisposition.PENDING;

    @Column(name = "requesting_user_id", length = 64)
    private String requestingUserId;

    @Column(name = "department", length = 64)
    private String department;

    @Column(name = "state", length = 64)
    private String state;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt = Instant.now();

    @Column(name = "closed_at")
    private Instant closedAt;

    @OneToMany(mappedBy = "alert", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<InvestigationRemark> remarks = new ArrayList<>();

	public UUID getId() {
		return id;
	}

	public void setId(UUID id) {
		this.id = id;
	}

	public String getTransactionId() {
		return transactionId;
	}

	public void setTransactionId(String transactionId) {
		this.transactionId = transactionId;
	}

	public String getTriggeredRuleCodes() {
		return triggeredRuleCodes;
	}

	public void setTriggeredRuleCodes(String triggeredRuleCodes) {
		this.triggeredRuleCodes = triggeredRuleCodes;
	}

	public Integer getRiskScore() {
		return riskScore;
	}

	public void setRiskScore(Integer riskScore) {
		this.riskScore = riskScore;
	}

	public RiskLevel getRiskLevel() {
		return riskLevel;
	}

	public void setRiskLevel(RiskLevel riskLevel) {
		this.riskLevel = riskLevel;
	}

	public AlertStatus getStatus() {
		return status;
	}

	public void setStatus(AlertStatus status) {
		this.status = status;
	}

	public AlertDisposition getDisposition() {
		return disposition;
	}

	public void setDisposition(AlertDisposition disposition) {
		this.disposition = disposition;
	}

	public String getRequestingUserId() {
		return requestingUserId;
	}

	public void setRequestingUserId(String requestingUserId) {
		this.requestingUserId = requestingUserId;
	}


	public String getDepartment() {
		return department;
	}

	public void setDepartment(String department) {
		this.department = department;
	}

	public String getState() {
		return state;
	}

	public void setState(String state) {
		this.state = state;
	}

	public Instant getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(Instant createdAt) {
		this.createdAt = createdAt;
	}

	public Instant getClosedAt() {
		return closedAt;
	}

	public void setClosedAt(Instant closedAt) {
		this.closedAt = closedAt;
	}

	public List<InvestigationRemark> getRemarks() {
		return remarks;
	}

	public void setRemarks(List<InvestigationRemark> remarks) {
		this.remarks = remarks;
	}    
	public FraudAlert() {
	}

	public FraudAlert(UUID id,
	                  String transactionId,
	                  String triggeredRuleCodes,
	                  Integer riskScore,
	                  RiskLevel riskLevel,
	                  AlertStatus status,
	                  AlertDisposition disposition,
	                  String requestingUserId,
	                  String department,
	                  String state,
	                  Instant createdAt,
	                  Instant closedAt,
	                  List<InvestigationRemark> remarks) {

	    this.id = id;
	    this.transactionId = transactionId;
	    this.triggeredRuleCodes = triggeredRuleCodes;
	    this.riskScore = riskScore;
	    this.riskLevel = riskLevel;
	    this.status = status;
	    this.disposition = disposition;
	    this.requestingUserId = requestingUserId;
	    this.department = department;
	    this.state = state;
	    this.createdAt = createdAt;
	    this.closedAt = closedAt;
	    this.remarks = remarks;
	}
}
