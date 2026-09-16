package gov.nic.esdt.vismai.fraudanalytics.dto;

import gov.nic.esdt.vismai.fraudanalytics.domain.enums.AlertDisposition;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public final class InvestigationRequests {

    private InvestigationRequests() {}

    /** SRS 5.6 — add a free-text investigation remark. */
    public record AddRemarkRequest(@NotBlank String author, @NotBlank String remark) {}

    /** SRS 5.6 — close an alert with a mandatory disposition. */
    public record CloseAlertRequest(@NotNull AlertDisposition disposition, String closingRemark, @NotBlank String closedBy) {}
}
