package gov.nic.esdt.vismai.fraudanalytics.service;

import java.time.Duration;
import java.util.Set;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

/**
 * Rolling window counters and set-membership tracking used to evaluate rules
 * such as "excessive OTP requests" or "same Aadhaar token used from multiple
 * IPs" without hitting PostgreSQL on every event.
 *
 * Reuses Redis, already part of the ServicePlus stack (SRS 4.2 / 7.1).
 * Keys are namespaced under {@code fam:} and expire automatically via TTL,
 * so no separate cleanup job is required.
 */
@Service
public class RollingCounterService {

    private final StringRedisTemplate redis;

    public RollingCounterService(StringRedisTemplate redis) {
		this.redis = redis;
	}

	private static final String NS = "fam:";

    /** Increments and returns the count of events for {@code key} within the given window. */
    public long incrementAndGetCount(String bucket, String key, Duration window) {
        String redisKey = NS + bucket + ":" + key;
        Long count = redis.opsForValue().increment(redisKey);
        if (count != null && count == 1L) {
            redis.expire(redisKey, window);
        }
        return count == null ? 0 : count;
    }

    /** Adds {@code member} to a rolling set (e.g. distinct IPs per Aadhaar token) and returns its size. */
    public long addToSetAndGetSize(String bucket, String key, String member, Duration window) {
        String redisKey = NS + "set:" + bucket + ":" + key;
        redis.opsForSet().add(redisKey, member);
        redis.expire(redisKey, window);
        return redis.opsForSet().size(redisKey) == null ? 0 : redis.opsForSet().size(redisKey);
    }

    public Set<String> membersOf(String bucket, String key) {
        return redis.opsForSet().members(NS + "set:" + bucket + ":" + key);
    }

    public long currentCount(String bucket, String key) {
        String value = redis.opsForValue().get(NS + bucket + ":" + key);
        return value == null ? 0 : Long.parseLong(value);
    }
}
