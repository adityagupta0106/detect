package gov.nic.esdt.vismai.fraudanalytics.domain.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

/**
 * Externally configurable fraud detection rule (SRS 5.2).
 *
 * The {@link #mvelExpression} is evaluated by the Rule Engine (MVEL) against a
 * {@code RuleEvaluationContext} built from the incoming event plus Redis-backed
 * rolling counters. Reuses the same rule/expression engine already standardised
 * in the ServicePlus Workflow Management Module (see SRS 4.2).
 *
 * Example expression (evaluates to boolean):
 *   "otpRequestCountLast10Min > 5"
 *   "failureCountLast30Min > 10"
 *   "distinctIpCountLast15MinForToken >= 2"
 */
@Entity
@Table(name = "fraud_rule")
@Builder
public class FraudRule {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "code", nullable = false, unique = true, length = 64)
    private String code;

    @Column(name = "name", nullable = false, length = 128)
    private String name;

    @Column(name = "description", length = 512)
    private String description;

    /** Boolean MVEL expression evaluated against the rule context. */
    @Column(name = "mvel_expression", nullable = false, length = 1024)
    private String mvelExpression;

    /** Points added to the composite risk score when this rule triggers (SRS 5.3). */
    @Column(name = "weight", nullable = false)
    private Integer weight;

    @Column(name = "enabled", nullable = false)
    @Builder.Default
    private Boolean enabled = true;

    /** Optional scoping — null means platform-wide. */
    @Column(name = "department", length = 64)
    private String department;

    @Column(name = "state", length = 64)
    private String state;

    @Column(name = "created_at", nullable = false)
    @Builder.Default
    private Instant createdAt = Instant.now();

    @Column(name = "updated_at", nullable = false)
    @Builder.Default
    private Instant updatedAt = Instant.now();

	public UUID getId() {
		return id;
	}

	public void setId(UUID id) {
		this.id = id;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getMvelExpression() {
		return mvelExpression;
	}

	public void setMvelExpression(String mvelExpression) {
		this.mvelExpression = mvelExpression;
	}

	public Integer getWeight() {
		return weight;
	}

	public void setWeight(Integer weight) {
		this.weight = weight;
	}

	public Boolean getEnabled() {
		return enabled;
	}

	public void setEnabled(Boolean enabled) {
		this.enabled = enabled;
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

	public Instant getUpdatedAt() {
		return updatedAt;
	}

	public void setUpdatedAt(Instant updatedAt) {
		this.updatedAt = updatedAt;
	}   
}
