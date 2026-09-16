package gov.nic.esdt.vismai.fraudanalytics;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

/**
 * Smoke test — verifies the Spring context loads with all beans wired.
 * Requires a running PostgreSQL/Redis/Kafka (or testcontainers) in CI;
 * intentionally minimal here as a scaffold, not a full integration suite.
 */
@SpringBootTest
@ActiveProfiles("test")
class FraudAnalyticsModuleApplicationTests {

    @Test
    void contextLoads() {
        // Intentionally empty — a failing context load is the assertion.
    }
}
