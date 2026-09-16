package gov.nic.esdt.vismai.fraudanalytics;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * VismAI (ServicePlus 2.0) — Fraud Analytics Module (FAM).
 *
 * Rule-based detection service for Aadhaar authentication transactions.
 * Ref: SP-SRS-FRAUD-001 v1.0.
 *
 * Design notes (see SRS Section 4.2 — "Reuse Before Rebuild"):
 *  - Rule expressions are evaluated with MVEL, the same expression engine
 *    already standardised in the ServicePlus Workflow Management Module.
 *  - Events are consumed from Kafka (already part of the VismAI ESB/Integration
 *    Layer) rather than polled on a fixed batch cycle.
 *  - Rolling counters/rate limits use Redis (already part of the ServicePlus stack).
 *  - Persistent records use PostgreSQL (the platform's system of record).
 *
 * This module never stores raw Aadhaar numbers, biometric templates, or OTP
 * values — only the Aadhaar Data Vault (ADV) reference token. See SRS 5.1 and 6.3.
 */
@SpringBootApplication
@EnableScheduling
public class FraudAnalyticsModuleApplication {

    public static void main(String[] args) {
        SpringApplication.run(FraudAnalyticsModuleApplication.class, args);
    }
}
