import java.util.*;
import java.util.concurrent.*;

public class problem3 {

    // DNS Entry class
    private static class DNSEntry {
        String domain;
        String ipAddress;
        long expiryTime; // in milliseconds

        DNSEntry(String domain, String ipAddress, long ttlSeconds) {
            this.domain = domain;
            this.ipAddress = ipAddress;
            this.expiryTime = System.currentTimeMillis() + ttlSeconds * 1000;
        }

        boolean isExpired() {
            return System.currentTimeMillis() > expiryTime;
        }
    }

    private final int maxCacheSize;
    private final Map<String, DNSEntry> cache;
    private final Deque<String> lruQueue; // For LRU eviction
    private final ScheduledExecutorService cleaner;
    private long hits = 0;
    private long misses = 0;

    public problem3(int maxCacheSize) {
        this.maxCacheSize = maxCacheSize;
        this.cache = new ConcurrentHashMap<>();
        this.lruQueue = new ConcurrentLinkedDeque<>();

        // Background cleaner thread to remove expired entries every second
        this.cleaner = Executors.newSingleThreadScheduledExecutor();
        cleaner.scheduleAtFixedRate(this::cleanExpiredEntries, 1, 1, TimeUnit.SECONDS);
    }

    // Resolve domain (returns IP)
    public String resolve(String domain, long ttlSeconds) {
        long start = System.nanoTime();

        DNSEntry entry = cache.get(domain);
        if (entry != null && !entry.isExpired()) {
            // Cache HIT
            hits++;
            touchLRU(domain);
            long duration = (System.nanoTime() - start) / 1_000_000;
            System.out.println("Cache HIT → " + entry.ipAddress + " (retrieved in " + duration + "ms)");
            return entry.ipAddress;
        }

        // Cache MISS
        misses++;
        String ip = queryUpstreamDNS(domain);
        addToCache(domain, ip, ttlSeconds);
        long duration = (System.nanoTime() - start) / 1_000_000;
        System.out.println("Cache MISS → Query upstream → " + ip + " (TTL: " + ttlSeconds + "s, " + duration + "ms)");
        return ip;
    }

    // Add entry to cache with LRU eviction
    private synchronized void addToCache(String domain, String ipAddress, long ttlSeconds) {
        if (cache.size() >= maxCacheSize) {
            // Evict least recently used
            String lruDomain = lruQueue.pollLast();
            if (lruDomain != null) {
                cache.remove(lruDomain);
            }
        }
        DNSEntry newEntry = new DNSEntry(domain, ipAddress, ttlSeconds);
        cache.put(domain, newEntry);
        touchLRU(domain);
    }

    // Update LRU queue
    private synchronized void touchLRU(String domain) {
        lruQueue.remove(domain);
        lruQueue.addFirst(domain);
    }

    // Remove expired entries
    private void cleanExpiredEntries() {
        for (String domain : cache.keySet()) {
            DNSEntry entry = cache.get(domain);
            if (entry != null && entry.isExpired()) {
                cache.remove(domain);
                lruQueue.remove(domain);
            }
        }
    }

    // Simulated upstream DNS query
    private String queryUpstreamDNS(String domain) {
        // For simulation, generate a pseudo IP
        int lastOctet = new Random().nextInt(256);
        return "172.217.14." + lastOctet;
    }

    // Cache statistics
    public void getCacheStats() {
        long total = hits + misses;
        double hitRate = total == 0 ? 0 : (hits * 100.0 / total);
        System.out.println("Cache Stats → Hits: " + hits + ", Misses: " + misses + ", Hit Rate: " + String.format("%.2f", hitRate) + "%");
    }

    // Shutdown cleaner thread
    public void shutdown() {
        cleaner.shutdown();
    }

    // Example usage
    public static void main(String[] args) throws InterruptedException {
        problem3 dnsCache = new problem3(5); // max 5 entries in cache

        dnsCache.resolve("google.com", 3); // TTL 3s
        dnsCache.resolve("example.com", 5);
        dnsCache.resolve("google.com", 3); // HIT
        dnsCache.resolve("openai.com", 4);
        Thread.sleep(3100); // wait for google.com to expire
        dnsCache.resolve("google.com", 3); // Cache EXPIRED → MISS
        dnsCache.getCacheStats();

        dnsCache.shutdown();
    }
}