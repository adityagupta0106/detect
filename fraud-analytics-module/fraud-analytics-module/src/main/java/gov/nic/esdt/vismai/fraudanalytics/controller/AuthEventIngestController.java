package gov.nic.esdt.vismai.fraudanalytics.controller;

import gov.nic.esdt.vismai.fraudanalytics.dto.AuthEventDto;
import gov.nic.esdt.vismai.fraudanalytics.service.TransactionIngestionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Fallback synchronous ingestion endpoint (SRS Section 9). The primary
 * ingestion path is {@link gov.nic.esdt.vismai.fraudanalytics.kafka.AuthEventConsumer};
 * this endpoint exists for deployments or callers that cannot yet publish to
 * Kafka, and delegates to the exact same {@link TransactionIngestionService}
 * so behaviour never diverges between the two paths.
 */
@RestController
@RequestMapping("/api/fraud/events")
public class AuthEventIngestController {

    private final TransactionIngestionService transactionIngestionService;
    
    public AuthEventIngestController(TransactionIngestionService transactionIngestionService) {
		this.transactionIngestionService = transactionIngestionService;
	}

	@PostMapping
    public ResponseEntity<Void> ingest(@Valid @RequestBody AuthEventDto event) {
        transactionIngestionService.ingest(event);
        return ResponseEntity.status(HttpStatus.ACCEPTED).build();
    }
}
