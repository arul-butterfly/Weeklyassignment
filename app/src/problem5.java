import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class problem5 {

    // Page URL -> total visit count
    private Map<String, AtomicInteger> pageViews = new ConcurrentHashMap<>();

    // Page URL -> unique user IDs
    private Map<String, Set<String>> uniqueVisitors = new ConcurrentHashMap<>();

    // Traffic source -> visit count
    private Map<String, AtomicInteger> trafficSources = new ConcurrentHashMap<>();

    // For top N pages (we maintain a PriorityQueue for top 10)
    private final int TOP_N = 10;

    // Scheduled executor for dashboard updates
    private ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

    public problem5() {
        // Schedule dashboard updates every 5 seconds
        scheduler.scheduleAtFixedRate(this::getDashboard, 5, 5, TimeUnit.SECONDS);
    }

    // Process a page view event
    public void processEvent(String url, String userId, String source) {
        pageViews.computeIfAbsent(url, k -> new AtomicInteger(0)).incrementAndGet();
        uniqueVisitors.computeIfAbsent(url, k -> ConcurrentHashMap.newKeySet()).add(userId);
        trafficSources.computeIfAbsent(source, k -> new AtomicInteger(0)).incrementAndGet();
    }

    // Generate dashboard output
    public void getDashboard() {
        System.out.println("\n--- Real-Time Dashboard ---");

        // Top pages by total views
        List<Map.Entry<String, AtomicInteger>> topPages = pageViews.entrySet()
                .stream()
                .sorted((a, b) -> b.getValue().get() - a.getValue().get())
                .limit(TOP_N)
                .collect(Collectors.toList());

        System.out.println("Top Pages:");
        int rank = 1;
        for (Map.Entry<String, AtomicInteger> entry : topPages) {
            String url = entry.getKey();
            int views = entry.getValue().get();
            int unique = uniqueVisitors.getOrDefault(url, Collections.emptySet()).size();
            System.out.printf("%d. %s - %d views (%d unique)\n", rank++, url, views, unique);
        }

        // Traffic sources
        System.out.println("\nTraffic Sources:");
        for (Map.Entry<String, AtomicInteger> entry : trafficSources.entrySet()) {
            System.out.printf("%s: %d visits\n", entry.getKey(), entry.getValue().get());
        }
    }

    // Shutdown scheduler
    public void shutdown() {
        scheduler.shutdown();
    }

    // Example usage
    public static void main(String[] args) throws InterruptedException {
        problem5 analytics = new problem5();

        // Simulate events
        analytics.processEvent("/article/breaking-news", "user_123", "google");
        analytics.processEvent("/article/breaking-news", "user_456", "facebook");
        analytics.processEvent("/sports/championship", "user_123", "direct");
        analytics.processEvent("/article/breaking-news", "user_123", "google"); // same user revisits

        // Simulate more events
        for (int i = 0; i < 50; i++) {
            analytics.processEvent("/article/breaking-news", "user_" + i, "google");
            analytics.processEvent("/sports/championship", "user_" + i, "facebook");
        }

        // Let dashboard print a couple of times
        Thread.sleep(12000);

        analytics.shutdown();
    }
}