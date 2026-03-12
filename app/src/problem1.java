import java.util.*;

public class problem1 {
    // In-memory storage
    private Map<String, Integer> usernameMap = new HashMap<>();
    private Map<String, Integer> attemptFrequency = new HashMap<>();

    // Constructor to initialize some usernames
    public problem1() {
        usernameMap.put("john_doe", 123);
        usernameMap.put("alice99", 456);
        usernameMap.put("admin", 1);
    }

    // Check if a username is available
    public boolean checkAvailability(String username) {
        attemptFrequency.put(username, attemptFrequency.getOrDefault(username, 0) + 1);
        return !usernameMap.containsKey(username);
    }

    // Suggest alternative usernames
    public List<String> suggestAlternatives(String username, int maxSuggestions) {
        List<String> suggestions = new ArrayList<>();
        String base = username;
        int i = 1;
        while (suggestions.size() < maxSuggestions) {
            String candidate = base + i;
            if (!usernameMap.containsKey(candidate)) {
                suggestions.add(candidate);
            }
            i++;
        }

        if (base.contains("_") && suggestions.size() < maxSuggestions) {
            String candidate = base.replace("_", ".");
            if (!usernameMap.containsKey(candidate)) {
                suggestions.add(candidate);
            }
        }

        return suggestions;
    }

    // Get the most attempted username
    public Map.Entry<String, Integer> getMostAttempted() {
        if (attemptFrequency.isEmpty()) {
            return null;
        }
        Map.Entry<String, Integer> maxEntry = null;
        for (Map.Entry<String, Integer> entry : attemptFrequency.entrySet()) {
            if (maxEntry == null || entry.getValue() > maxEntry.getValue()) {
                maxEntry = entry;
            }
        }
        return maxEntry;
    }

    // Example usage
    public static void main(String[] args) {
        problem1 checker = new problem1();

        System.out.println(checker.checkAvailability("john_doe"));    // false
        System.out.println(checker.checkAvailability("jane_smith"));  // true

        System.out.println(checker.suggestAlternatives("john_doe", 5)); // [john_doe1, john_doe2, john_doe3, john_doe4, john_doe5, john.doe]

        Map.Entry<String, Integer> mostAttempted = checker.getMostAttempted();
        if (mostAttempted != null) {
            System.out.println("Most attempted: " + mostAttempted.getKey() + " (" + mostAttempted.getValue() + " attempts)");
        }
    }
}