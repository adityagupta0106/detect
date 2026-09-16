package gov.nic.esdt.vismai.fraudanalytics.domain.entity;

import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

/** Free-text investigation remark against an alert (SRS 5.6), retained for audit. */
@Entity
@Table(name = "investigation_remark")
public class InvestigationRemark {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "alert_id", nullable = false)
    private FraudAlert alert;

    @Column(name = "author", nullable = false, length = 64)
    private String author;

    @Column(name = "remark", nullable = false, length = 2000)
    private String remark;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt = Instant.now();

	public UUID getId() {
		return id;
	}

	public void setId(UUID id) {
		this.id = id;
	}

	public FraudAlert getAlert() {
		return alert;
	}

	public void setAlert(FraudAlert alert) {
		this.alert = alert;
	}

	public String getAuthor() {
		return author;
	}

	public void setAuthor(String author) {
		this.author = author;
	}

	public String getRemark() {
		return remark;
	}

	public void setRemark(String remark) {
		this.remark = remark;
	}

	public Instant getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(Instant createdAt) {
		this.createdAt = createdAt;
	}

	public InvestigationRemark(UUID id, FraudAlert alert, String author, String remark, Instant createdAt) {
		this.id = id;
		this.alert = alert;
		this.author = author;
		this.remark = remark;
		this.createdAt = createdAt;
	}

	public InvestigationRemark() {
	}
    
}
