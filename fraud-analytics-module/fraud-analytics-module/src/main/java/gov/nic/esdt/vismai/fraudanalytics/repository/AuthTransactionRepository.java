package gov.nic.esdt.vismai.fraudanalytics.repository;

import gov.nic.esdt.vismai.fraudanalytics.domain.entity.AuthTransaction;
import gov.nic.esdt.vismai.fraudanalytics.domain.enums.AuthResult;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface AuthTransactionRepository
        extends JpaRepository<AuthTransaction, UUID>, JpaSpecificationExecutor<AuthTransaction> {

    List<AuthTransaction> findByRequestingUserIdAndEventTimestampAfter(String requestingUserId, Instant after);

    List<AuthTransaction> findBySourceIpAndEventTimestampAfter(String sourceIp, Instant after);

    List<AuthTransaction> findByAadhaarRefTokenAndEventTimestampAfter(String aadhaarRefToken, Instant after);

    long countByEventTimestampAfter(Instant after);

    long countByResultAndEventTimestampAfter(AuthResult result, Instant after);

    /** Investigation timeline for a given user, most recent first. */
    Page<AuthTransaction> findByRequestingUserIdOrderByEventTimestampDesc(String requestingUserId, Pageable pageable);

    /** Investigation timeline for a given source IP, most recent first. */
    Page<AuthTransaction> findBySourceIpOrderByEventTimestampDesc(String sourceIp, Pageable pageable);
}
