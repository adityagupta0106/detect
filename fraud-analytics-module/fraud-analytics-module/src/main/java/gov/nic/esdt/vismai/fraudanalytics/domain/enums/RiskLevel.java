package gov.nic.esdt.vismai.fraudanalytics.domain.enums;

public enum RiskLevel {
    NORMAL,   // 0-30
    MEDIUM,   // 31-60
    HIGH;     // >60

    public static RiskLevel fromScore(int score) {
        if (score > 60) return HIGH;
        if (score > 30) return MEDIUM;
        return NORMAL;
    }
}
