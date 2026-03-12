import java.util.*;

public class problem10 {

    // Video data class (simplified)
    static class VideoData {
        String videoId;
        String content; // placeholder for actual video data

        VideoData(String videoId, String content) {
            this.videoId = videoId;
            this.content = content;
        }
    }

    // L1 Cache: in-memory LRU (LinkedHashMap with access-order)
    private final int L1_CAPACITY = 10000;
    private LinkedHashMap<String, VideoData> l1Cache = new LinkedHashMap<>(L1_CAPACITY, 0.75f, true) {
        protected boolean removeEldestEntry(Map.Entry<String, VideoData> eldest) {
            return size() > L1_CAPACITY;
        }
    };

    // L2 Cache: SSD-backed simulation with access count
    private final int L2_CAPACITY = 100000;
    private Map<String, VideoData> l2Cache = new HashMap<>();
    private Map<String, Integer> l2AccessCount = new HashMap<>();
    private final int PROMOTION_THRESHOLD = 5; // promote to L1 after 5 accesses

    // L3 Database: slow access simulation
    private Map<String, VideoData> database = new HashMap<>();

    // Cache statistics
    private int l1Hits = 0, l2Hits = 0, l3Hits = 0, totalRequests = 0;

    // Access times (ms)
    private final double L1_TIME = 0.5, L2_TIME = 5, L3_TIME = 150;

    // Add video to database (L3)
    public void addToDatabase(VideoData video) {
        database.put(video.videoId, video);
    }

    // Get video with multi-level cache logic
    public VideoData getVideo(String videoId) {
        totalRequests++;

        // L1 cache check
        if (l1Cache.containsKey(videoId)) {
            l1Hits++;
            return l1Cache.get(videoId);
        }

        // L2 cache check
        if (l2Cache.containsKey(videoId)) {
            l2Hits++;
            int count = l2AccessCount.getOrDefault(videoId, 0) + 1;
            l2AccessCount.put(videoId, count);

            // Promote to L1 if threshold exceeded
            if (count >= PROMOTION_THRESHOLD) {
                l1Cache.put(videoId, l2Cache.get(videoId));
            }
            return l2Cache.get(videoId);
        }

        // L3 database access
        l3Hits++;
        VideoData video = database.get(videoId);
        if (video != null) {
            // Add to L2 cache
            if (l2Cache.size() >= L2_CAPACITY) {
                // Simple eviction: remove random entry (could be LRU on disk)
                String firstKey = l2Cache.keySet().iterator().next();
                l2Cache.remove(firstKey);
                l2AccessCount.remove(firstKey);
            }
            l2Cache.put(videoId, video);
            l2AccessCount.put(videoId, 1);
        }
        return video;
    }

    // Get cache statistics
    public void getStatistics() {
        double l1HitRate = totalRequests == 0 ? 0 : (l1Hits * 100.0 / totalRequests);
        double l2HitRate = totalRequests == 0 ? 0 : (l2Hits * 100.0 / totalRequests);
        double l3HitRate = totalRequests == 0 ? 0 : (l3Hits * 100.0 / totalRequests);

        double avgTime = totalRequests == 0 ? 0 :
                (l1Hits * L1_TIME + l2Hits * L2_TIME + l3Hits * L3_TIME) / totalRequests;

        System.out.printf("Cache Statistics:\nL1 Hit Rate: %.1f%%, Avg Time: %.1fms\n", l1HitRate, L1_TIME);
        System.out.printf("L2 Hit Rate: %.1f%%, Avg Time: %.1fms\n", l2HitRate, L2_TIME);
        System.out.printf("L3 Hit Rate: %.1f%%, Avg Time: %.1fms\n", l3HitRate, L3_TIME);
        System.out.printf("Overall Hit Rate: %.1f%%, Avg Time: %.1fms\n",
                l1HitRate + l2HitRate + l3HitRate, avgTime);
    }

    // Example usage
    public static void main(String[] args) {
        problem10 cacheSystem = new problem10();

        // Populate database
        for (int i = 1; i <= 100; i++) {
            cacheSystem.addToDatabase(new VideoData("video_" + i, "Content_" + i));
        }

        // Simulate requests
        cacheSystem.getVideo("video_1");  // L3 miss -> L2 add
        cacheSystem.getVideo("video_1");  // L2 hit
        cacheSystem.getVideo("video_1");  // L2 hit
        cacheSystem.getVideo("video_1");  // L2 hit
        cacheSystem.getVideo("video_1");  // L2 hit -> promotion threshold reached -> L1 add
        cacheSystem.getVideo("video_1");  // L1 hit

        cacheSystem.getVideo("video_2");  // L3 miss -> L2 add
        cacheSystem.getVideo("video_3");  // L3 miss -> L2 add

        // Print statistics
        cacheSystem.getStatistics();
    }
}