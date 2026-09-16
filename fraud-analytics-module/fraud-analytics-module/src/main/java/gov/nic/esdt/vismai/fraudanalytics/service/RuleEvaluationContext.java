package gov.nic.esdt.vismai.fraudanalytics.service;

import gov.nic.esdt.vismai.fraudanalytics.dto.AuthEventDto;

/**
 * The variable set exposed to MVEL fraud-rule expressions (SRS 5.2).
 *
 * Field names here are deliberately stable and documented, since they form
 * the "API" that department administrators write rule expressions against
 * (e.g. {@code "otpRequestCountLast10Min > 5"}).
 */

public class RuleEvaluationContext {

    /** The event currently being evaluated. */
    private final AuthEventDto event;

    // -- Rolling counters, populated from Redis before rule evaluation --

    private final long otpRequestCountLast10Min;
    private final long failureCountLast30Min;
    private final long distinctIpCountLast15MinForToken;
    private final long distinctUserCountLast1HourForIp;
    private final long ekycAttemptCountLast5Min;
    private final long totalAttemptsLast1Hour;
    private final long failuresLast1Hour;
    private final boolean newDevice;
    private final boolean outsideConfiguredHours;
    

    public AuthEventDto getEvent() {
		return event;
	}


	public long getOtpRequestCountLast10Min() {
		return otpRequestCountLast10Min;
	}


	public long getFailureCountLast30Min() {
		return failureCountLast30Min;
	}


	public long getDistinctIpCountLast15MinForToken() {
		return distinctIpCountLast15MinForToken;
	}


	public long getDistinctUserCountLast1HourForIp() {
		return distinctUserCountLast1HourForIp;
	}


	public long getEkycAttemptCountLast5Min() {
		return ekycAttemptCountLast5Min;
	}


	public long getTotalAttemptsLast1Hour() {
		return totalAttemptsLast1Hour;
	}


	public long getFailuresLast1Hour() {
		return failuresLast1Hour;
	}


	public boolean isNewDevice() {
		return newDevice;
	}


	public boolean isOutsideConfiguredHours() {
		return outsideConfiguredHours;
	}


	/** Rolling failure rate over the last hour, 0-100. */
    public double failureRatePercentLast1Hour() {
        if (totalAttemptsLast1Hour == 0) return 0.0;
        return (failuresLast1Hour * 100.0) / totalAttemptsLast1Hour;
    }

	public RuleEvaluationContext(AuthEventDto event, long otpRequestCountLast10Min, long failureCountLast30Min,
			long distinctIpCountLast15MinForToken, long distinctUserCountLast1HourForIp, long ekycAttemptCountLast5Min,
			long totalAttemptsLast1Hour, long failuresLast1Hour, boolean newDevice, boolean outsideConfiguredHours) {
		this.event = event;
		this.otpRequestCountLast10Min = otpRequestCountLast10Min;
		this.failureCountLast30Min = failureCountLast30Min;
		this.distinctIpCountLast15MinForToken = distinctIpCountLast15MinForToken;
		this.distinctUserCountLast1HourForIp = distinctUserCountLast1HourForIp;
		this.ekycAttemptCountLast5Min = ekycAttemptCountLast5Min;
		this.totalAttemptsLast1Hour = totalAttemptsLast1Hour;
		this.failuresLast1Hour = failuresLast1Hour;
		this.newDevice = newDevice;
		this.outsideConfiguredHours = outsideConfiguredHours;
	}
    
}
