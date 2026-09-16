package gov.nic.esdt.vismai.fraudanalytics.kafka;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

import gov.nic.esdt.vismai.fraudanalytics.dto.AuthEventDto;
import gov.nic.esdt.vismai.fraudanalytics.service.TransactionIngestionService;

/**
 * Primary ingestion path (SRS Section 7): consumes authentication events
 * published by the Aadhaar Authentication Integration Layer to the
 * {@code auth.events} Kafka topic, so fraud detection runs near-real-time
 * without adding latency to the authentication response path (SRS 6.1).
 *
 * Manual acknowledgment + at-least-once semantics: if FAM is briefly
 * unavailable, events remain on the topic and are reconciled on recovery
 * (SRS 6.2 — graceful degradation without affecting authentication itself).
 */
@Component
public class AuthEventConsumer {
	private static final Logger log = LogManager.getLogger("famLogger");
    private final TransactionIngestionService transactionIngestionService;
    
	public AuthEventConsumer(TransactionIngestionService transactionIngestionService) {
		this.transactionIngestionService = transactionIngestionService;
	}

	@KafkaListener(
            topics = "${fam.kafka.topic.auth-events:auth.events}",
            groupId = "${fam.kafka.consumer-group:fraud-analytics-module}",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void onAuthEvent(AuthEventDto event, Acknowledgment ack) {
        try {
            transactionIngestionService.ingest(event);
        } catch (Exception ex) {
            log.error("Failed to process auth event, transactionId={}. Will not ack; message will be redelivered.",
                    event != null ? event.transactionId() : "unknown", ex);
            // Do not acknowledge — let the container's retry/DLT policy handle redelivery.
            return;
        }
        ack.acknowledge();
    }
}
