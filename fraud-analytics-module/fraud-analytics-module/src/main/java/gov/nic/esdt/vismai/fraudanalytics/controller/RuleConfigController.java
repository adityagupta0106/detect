package gov.nic.esdt.vismai.fraudanalytics.controller;

import gov.nic.esdt.vismai.fraudanalytics.domain.entity.FraudRule;
import gov.nic.esdt.vismai.fraudanalytics.exception.ResourceNotFoundException;
import gov.nic.esdt.vismai.fraudanalytics.repository.FraudRuleRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Rule administration (SRS 5.2 / Section 9). Restricted to {@code fam-admin}
 * (see {@link gov.nic.esdt.vismai.fraudanalytics.config.SecurityConfig}).
 * Rules, weights and thresholds are configurable without a code deployment
 * (SRS 6.4).
 */
@RestController
@RequestMapping("/api/fraud/rules")
@RequiredArgsConstructor
public class RuleConfigController {

    private final FraudRuleRepository fraudRuleRepository;
    
    public RuleConfigController(FraudRuleRepository fraudRuleRepository) {
		this.fraudRuleRepository = fraudRuleRepository;
	}

	@GetMapping
    public List<FraudRule> listRules() {
        return fraudRuleRepository.findAll();
    }

    @PostMapping
    public FraudRule createRule(@Valid @RequestBody FraudRule rule) {
        rule.setId(null);
        rule.setCreatedAt(Instant.now());
        rule.setUpdatedAt(Instant.now());
        return fraudRuleRepository.save(rule);
    }

    @PutMapping("/{id}")
    public FraudRule updateRule(@PathVariable UUID id, @Valid @RequestBody FraudRule update) {
        FraudRule existing = fraudRuleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Rule not found: " + id));

        existing.setName(update.getName());
        existing.setDescription(update.getDescription());
        existing.setMvelExpression(update.getMvelExpression());
        existing.setWeight(update.getWeight());
        existing.setEnabled(update.getEnabled());
        existing.setDepartment(update.getDepartment());
        existing.setState(update.getState());
        existing.setUpdatedAt(Instant.now());

        return fraudRuleRepository.save(existing);
    }

    @DeleteMapping("/{id}")
    public void disableRule(@PathVariable UUID id) {
        FraudRule existing = fraudRuleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Rule not found: " + id));
        existing.setEnabled(false);
        existing.setUpdatedAt(Instant.now());
        fraudRuleRepository.save(existing);
    }
}
