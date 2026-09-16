package gov.nic.esdt.vismai.fraudanalytics.service;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import gov.nic.esdt.vismai.fraudanalytics.domain.entity.FraudAlert;
import gov.nic.esdt.vismai.fraudanalytics.dto.AuthEventDto;

/**
 * Dispatches alert notifications to configured channels (SRS 5.5).
 *
 * Dashboard notification requires no action here — persisting the
 * {@link FraudAlert} already makes it visible via the dashboard/investigation
 * APIs. Email is wired to the platform's mail sender; SMS is a stub pending
 * integration with VismAI's existing SMS gateway (per-department recipients
 * are expected to come from platform configuration, not this module).
 */
@Service
public class NotificationDispatcher {

	private static final Logger log = LogManager.getLogger("famLogger");

    private final JavaMailSender mailSender;

    @Value("${fam.alerting.email.enabled:false}")
    private boolean emailEnabled;

    @Value("${fam.alerting.email.recipients:}")
    private String defaultRecipients;

    @Value("${fam.alerting.sms.enabled:false}")
    private boolean smsEnabled;

    public NotificationDispatcher(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void dispatch(FraudAlert alert, AuthEventDto event) {
        if (emailEnabled && !defaultRecipients.isBlank()) {
            sendEmail(alert);
        }
        if (smsEnabled) {
            sendSms(alert);
        }
    }

    private void sendEmail(FraudAlert alert) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(defaultRecipients.split(","));
            message.setSubject(String.format("[FAM] %s risk fraud alert — txn %s",
                    alert.getRiskLevel(), alert.getTransactionId()));
            message.setText(String.format(
                    "Alert ID: %s%nTransaction: %s%nRisk score: %d (%s)%nTriggered rules: %s%nDepartment/State: %s / %s%n",
                    alert.getId(), alert.getTransactionId(), alert.getRiskScore(), alert.getRiskLevel(),
                    alert.getTriggeredRuleCodes(), alert.getDepartment(), alert.getState()));
            mailSender.send(message);
        } catch (Exception ex) {
            // Notification failures must never fail the fraud pipeline itself.
            log.error("Failed to send email notification for alert {}", alert.getId(), ex);
        }
    }

    private void sendSms(FraudAlert alert) {
        // Integration point: wire to VismAI's existing SMS gateway.
        log.info("SMS dispatch stub — alert {} (integrate with VismAI SMS gateway)", alert.getId());
    }
}
