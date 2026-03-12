import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public class problem6 {

    // Token bucket class per client
    private static class TokenBucket {
        private final int maxTokens;
        private final long refillIntervalMillis; // e.g., 1 hour in ms
        private AtomicInteger tokens;
        private long lastRefillTime;

        public TokenBucket(int maxTokens, long refillIntervalMillis) {
            this.maxTokens = maxTokens;
            this.refillIntervalMillis = refillIntervalMillis;
            this.tokens = new AtomicInteger(maxTokens);
            this.lastRefillTime = System.currentTimeMillis();
        }

        public synchronized boolean allowRequest() {
            refillTokensIfNeeded();
            if (tokens.get() > 0) {
                tokens.decrementAndGet();
                return true;
            } else {
                return false;
            }
        }

        public synchronized int getRemainingTokens() {
            refillTokensIfNeeded();
            return tokens.get();
        }

        public synchronized long getResetTimeSeconds() {
            refillTokensIfNeeded();
            return (lastRefillTime + refillIntervalMillis) / 1000;
        }

        private void refillTokensIfNeeded() {
            long now = System.currentTimeMillis();
            if (now - lastRefillTime >= refillIntervalMillis) {
                tokens.set(maxTokens);
                lastRefillTime = now;
            }
        }
    }

    // Client ID -> TokenBucket
    private Map<String, TokenBucket> clientBuckets = new ConcurrentHashMap<>();
    private final int MAX_REQUESTS = 1000; // per hour
    private final long REFILL_INTERVAL = 60 * 60 * 1000; // 1 hour in milliseconds

    // Check rate limit for a client
    public String checkRateLimit(String clientId) {
        TokenBucket bucket = clientBuckets.computeIfAbsent(clientId, id -> new TokenBucket(MAX_REQUESTS, REFILL_INTERVAL));
        boolean allowed = bucket.allowRequest();
        if (allowed) {
            return "Allowed (" + bucket.getRemainingTokens() + " requests remaining)";
        } else {
            long retryAfter = bucket.getResetTimeSeconds() - System.currentTimeMillis() / 1000;
            return "Denied (0 requests remaining, retry after " + retryAfter + "s)";
        }
    }

    // Get current rate limit status for a client
    public Map<String, Object> getRateLimitStatus(String clientId) {
        TokenBucket bucket = clientBuckets.get(clientId);
        Map<String, Object> status = new HashMap<>();
        if (bucket != null) {
            status.put("used", MAX_REQUESTS - bucket.getRemainingTokens());
            status.put("limit", MAX_REQUESTS);
            status.put("reset", bucket.getResetTimeSeconds());
        } else {
            status.put("used", 0);
            status.put("limit", MAX_REQUESTS);
            status.put("reset", System.currentTimeMillis() / 1000 + REFILL_INTERVAL / 1000);
        }
        return status;
    }

    // Example usage
    public static void main(String[] args) throws InterruptedException {
        problem6 limiter = new problem6();
        String clientId = "abc123";

        // Simulate requests
        for (int i = 0; i < 5; i++) {
            System.out.println(limiter.checkRateLimit(clientId));
        }

        // Check rate limit status
        System.out.println("Rate limit status: " + limiter.getRateLimitStatus(clientId));

        // Simulate exceeding limit
        for (int i = 0; i < 1000; i++) {
            limiter.checkRateLimit(clientId);
        }
        System.out.println(limiter.checkRateLimit(clientId)); // Should be denied
        System.out.println("Rate limit status: " + limiter.getRateLimitStatus(clientId));
    }
}