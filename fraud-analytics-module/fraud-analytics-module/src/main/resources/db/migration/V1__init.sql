-- SP-SRS-FRAUD-001 v1.0 — Fraud Analytics Module initial schema
-- Note: auth_transaction never holds a raw Aadhaar number, biometric template,
-- or OTP value — only the Aadhaar Data Vault (ADV) reference token. See SRS 5.1/6.3.

CREATE TABLE auth_transaction (
    id                   UUID PRIMARY KEY,
    transaction_id       VARCHAR(100) NOT NULL UNIQUE,
    aadhaar_ref_token      VARCHAR(128) NOT NULL,
    auth_type            VARCHAR(16)  NOT NULL,
    event_timestamp      TIMESTAMPTZ  NOT NULL,
    source_ip            VARCHAR(45)  NOT NULL,
    device_fingerprint   VARCHAR(128),
    requesting_user_id   VARCHAR(64)  NOT NULL,
    service_id           VARCHAR(64),
    department           VARCHAR(64),
    state                VARCHAR(64),
    result                VARCHAR(16)  NOT NULL,
    uidai_error_code     VARCHAR(16),
    response_time_ms     BIGINT,
    ingested_at           TIMESTAMPTZ  NOT NULL DEFAULT now()
);

CREATE INDEX idx_auth_txn_adv_token   ON auth_transaction (aadhaar_ref_token);
CREATE INDEX idx_auth_txn_source_ip   ON auth_transaction (source_ip);
CREATE INDEX idx_auth_txn_user_id     ON auth_transaction (requesting_user_id);
CREATE INDEX idx_auth_txn_timestamp   ON auth_transaction (event_timestamp);
CREATE INDEX idx_auth_txn_dept_state  ON auth_transaction (department, state);

CREATE TABLE fraud_rule (
    id                UUID PRIMARY KEY,
    code              VARCHAR(64)   NOT NULL UNIQUE,
    name              VARCHAR(128)  NOT NULL,
    description       VARCHAR(512),
    mvel_expression   VARCHAR(1024) NOT NULL,
    weight            INTEGER       NOT NULL,
    enabled           BOOLEAN       NOT NULL DEFAULT true,
    department        VARCHAR(64),
    state             VARCHAR(64),
    created_at        TIMESTAMPTZ   NOT NULL DEFAULT now(),
    updated_at        TIMESTAMPTZ   NOT NULL DEFAULT now()
);

CREATE TABLE fraud_alert (
    id                     UUID PRIMARY KEY,
    transaction_id         VARCHAR(100) NOT NULL,
    triggered_rule_codes   VARCHAR(512) NOT NULL,
    risk_score             INTEGER      NOT NULL,
    risk_level             VARCHAR(16)  NOT NULL,
    status                 VARCHAR(16)  NOT NULL DEFAULT 'OPEN',
    disposition            VARCHAR(24)  NOT NULL DEFAULT 'PENDING',
    requesting_user_id     VARCHAR(64),
    department             VARCHAR(64),
    state                  VARCHAR(64),
    created_at             TIMESTAMPTZ  NOT NULL DEFAULT now(),
    closed_at              TIMESTAMPTZ
);

CREATE INDEX idx_fraud_alert_status     ON fraud_alert (status);
CREATE INDEX idx_fraud_alert_risk_level ON fraud_alert (risk_level);
CREATE INDEX idx_fraud_alert_created_at ON fraud_alert (created_at);

CREATE TABLE investigation_remark (
    id           UUID PRIMARY KEY,
    alert_id     UUID NOT NULL REFERENCES fraud_alert (id) ON DELETE CASCADE,
    author       VARCHAR(64)   NOT NULL,
    remark       VARCHAR(2000) NOT NULL,
    created_at   TIMESTAMPTZ   NOT NULL DEFAULT now()
);

CREATE INDEX idx_investigation_remark_alert_id ON investigation_remark (alert_id);

-- Baseline rule set (SRS 5.2) — thresholds are illustrative defaults and are
-- expected to be tuned per department/state via PUT /api/fraud/rules/{id}.
INSERT INTO fraud_rule (id, code, name, description, mvel_expression, weight, enabled) VALUES
    (gen_random_uuid(), 'EXCESSIVE_OTP', 'Excessive OTP requests',
     'More than 5 OTP requests within 10 minutes for the same user',
     'ctx.otpRequestCountLast10Min > 5', 20, true),

    (gen_random_uuid(), 'REPEATED_FAILURES', 'Repeated authentication failures',
     'More than 10 failures within 30 minutes for the same user',
     'ctx.failureCountLast30Min > 10', 30, true),

    (gen_random_uuid(), 'MULTI_IP_SAME_TOKEN', 'Same Aadhaar token used from multiple IPs',
     'Two or more distinct source IPs for the same Aadhaar reference token within 15 minutes',
     'ctx.distinctIpCountLast15MinForToken >= 2', 20, true),

    (gen_random_uuid(), 'IP_FANOUT', 'Same IP authenticating many distinct users',
     'More than 20 distinct users from the same source IP within 1 hour',
     'ctx.distinctUserCountLast1HourForIp > 20', 20, true),

    (gen_random_uuid(), 'RAPID_EKYC', 'Rapid repeated eKYC attempts',
     'Three or more eKYC calls for the same user within 5 minutes',
     'ctx.ekycAttemptCountLast5Min >= 3', 15, true),

    (gen_random_uuid(), 'HIGH_FAILURE_RATIO', 'High failure ratio',
     'Rolling 1-hour failure rate above 80% for the user',
     'ctx.failureRatePercentLast1Hour > 80', 20, true),

    (gen_random_uuid(), 'NEW_DEVICE', 'New / unrecognised device',
     'Authentication from a device fingerprint not seen for this user in the last 30 days',
     'ctx.newDevice == true', 10, true);
