package gov.nic.esdt.vismai.fraudanalytics.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import gov.nic.esdt.vismai.fraudanalytics.domain.enums.AuthResult;
import gov.nic.esdt.vismai.fraudanalytics.domain.enums.AuthType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;

/**
 * Payload published by the Aadhaar Authentication Integration Layer to the
 * {@code auth.events} Kafka topic (SRS Section 7), or posted to the fallback
 * REST ingestion endpoint.
 *
 * Deliberately carries only the ADV reference token for the Aadhaar identity —
 * never a raw Aadhaar number, biometric template, or OTP value (SRS 5.1 / 6.3).
 */
public record AuthEventDto(

        @NotBlank String transactionId,

        @NotBlank String aadhaarRefToken,

        @NotNull AuthType authType,

        @NotNull @JsonFormat(shape = JsonFormat.Shape.STRING) Instant eventTimestamp,

        @NotBlank String sourceIp,

        String deviceFingerprint,

        @NotBlank String requestingUserId,

        String serviceId,

        String department,

        String state,

        @NotNull AuthResult result,

        String uidaiErrorCode,

        Long responseTimeMs
) {
}
