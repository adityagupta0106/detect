package gov.nic.esdt.vismai.fraudanalytics.domain.entity;

import java.time.Instant;
import java.util.UUID;

import gov.nic.esdt.vismai.fraudanalytics.domain.enums.AuthResult;
import gov.nic.esdt.vismai.fraudanalytics.domain.enums.AuthType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;

/**
 * One row per Aadhaar authentication transaction routed through VismAI.
 *
 * IMPORTANT (SRS 5.1 / 6.3 — compliance-critical):
 *  - {@link #aadhaarRefToken} holds ONLY the Aadhaar Data Vault (ADV) reference
 *    token, never a raw Aadhaar number.
 *  - This entity NEVER stores biometric templates or OTP values, consistent
 *    with UIDAI's prohibition on permanent storage of such data. Only
 *    authentication response metadata is captured here.
 */
@Entity
@Table(name = "auth_transaction", indexes = {
        @Index(name = "idx_auth_txn_adv_token", columnList = "aadhaar_ref_token"),
        @Index(name = "idx_auth_txn_source_ip", columnList = "source_ip"),
        @Index(name = "idx_auth_txn_user_id", columnList = "requesting_user_id"),
        @Index(name = "idx_auth_txn_timestamp", columnList = "event_timestamp"),
        @Index(name = "idx_auth_txn_dept_state", columnList = "department, state")
})

public class AuthTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    /** VismAI-generated correlation ID for the authentication attempt. */
    @Column(name = "transaction_id", nullable = false, unique = true, length = 100)
    private String transactionId;

    /** Aadhaar Data Vault (ADV) reference token — NEVER the raw Aadhaar number. */
    @Column(name = "aadhaar_ref_token", nullable = false, length = 128)
    private String aadhaarRefToken;

    @Enumerated(EnumType.STRING)
    @Column(name = "auth_type", nullable = false, length = 16)
    private AuthType authType;

    @Column(name = "event_timestamp", nullable = false)
    private Instant eventTimestamp;

    @Column(name = "source_ip", nullable = false, length = 45)
    private String sourceIp;

    /** Hashed device/user-agent fingerprint — not the raw device identifier. */
    @Column(name = "device_fingerprint", length = 128)
    private String deviceFingerprint;

    /** VismAI application-level user performing the request (not the Aadhaar holder). */
    @Column(name = "requesting_user_id", nullable = false, length = 64)
    private String requestingUserId;

    @Column(name = "service_id", length = 64)
    private String serviceId;

    @Column(name = "department", length = 64)
    private String department;

    @Column(name = "state", length = 64)
    private String state;

    @Enumerated(EnumType.STRING)
    @Column(name = "result", nullable = false, length = 16)
    private AuthResult result;

    @Column(name = "uidai_error_code", length = 16)
    private String uidaiErrorCode;

    @Column(name = "response_time_ms")
    private Long responseTimeMs;

    @Column(name = "ingested_at", nullable = false)
    private Instant ingestedAt = Instant.now();

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

	public String getAadhaarRefToken() {
		return aadhaarRefToken;
	}

	public void setAadhaarRefToken(String aadhaarRefToken) {
		this.aadhaarRefToken = aadhaarRefToken;
	}

	public AuthType getAuthType() {
		return authType;
	}

	public void setAuthType(AuthType authType) {
		this.authType = authType;
	}

	public Instant getEventTimestamp() {
		return eventTimestamp;
	}

	public void setEventTimestamp(Instant eventTimestamp) {
		this.eventTimestamp = eventTimestamp;
	}

	public String getSourceIp() {
		return sourceIp;
	}

	public void setSourceIp(String sourceIp) {
		this.sourceIp = sourceIp;
	}

	public String getDeviceFingerprint() {
		return deviceFingerprint;
	}

	public void setDeviceFingerprint(String deviceFingerprint) {
		this.deviceFingerprint = deviceFingerprint;
	}

	public String getRequestingUserId() {
		return requestingUserId;
	}

	public void setRequestingUserId(String requestingUserId) {
		this.requestingUserId = requestingUserId;
	}

	public String getServiceId() {
		return serviceId;
	}

	public void setServiceId(String serviceId) {
		this.serviceId = serviceId;
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

	public AuthResult getResult() {
		return result;
	}

	public void setResult(AuthResult result) {
		this.result = result;
	}

	public String getUidaiErrorCode() {
		return uidaiErrorCode;
	}

	public void setUidaiErrorCode(String uidaiErrorCode) {
		this.uidaiErrorCode = uidaiErrorCode;
	}

	public Long getResponseTimeMs() {
		return responseTimeMs;
	}

	public void setResponseTimeMs(Long responseTimeMs) {
		this.responseTimeMs = responseTimeMs;
	}

	public Instant getIngestedAt() {
		return ingestedAt;
	}

	public void setIngestedAt(Instant ingestedAt) {
		this.ingestedAt = ingestedAt;
	}

	public AuthTransaction() {
	}

	public AuthTransaction(UUID id, String transactionId, String aadhaarRefToken, AuthType authType,
			Instant eventTimestamp, String sourceIp, String deviceFingerprint, String requestingUserId,
			String serviceId, String department, String state, AuthResult result, String uidaiErrorCode,
			Long responseTimeMs, Instant ingestedAt) {
		this.id = id;
		this.transactionId = transactionId;
		this.aadhaarRefToken = aadhaarRefToken;
		this.authType = authType;
		this.eventTimestamp = eventTimestamp;
		this.sourceIp = sourceIp;
		this.deviceFingerprint = deviceFingerprint;
		this.requestingUserId = requestingUserId;
		this.serviceId = serviceId;
		this.department = department;
		this.state = state;
		this.result = result;
		this.uidaiErrorCode = uidaiErrorCode;
		this.responseTimeMs = responseTimeMs;
		this.ingestedAt = ingestedAt;
	}
    
    
}
