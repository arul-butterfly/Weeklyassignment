import java.util.*;

public class problem7 {

    // Trie Node
    private static class TrieNode {
        Map<Character, TrieNode> children = new HashMap<>();
        boolean isEndOfQuery = false;
        String query = null;
        int frequency = 0;
    }

    private final TrieNode root = new TrieNode();
    private final int TOP_K = 10;

    // Insert a query with initial frequency
    public void insertQuery(String query, int freq) {
        TrieNode node = root;
        for (char c : query.toCharArray()) {
            node = node.children.computeIfAbsent(c, k -> new TrieNode());
        }
        node.isEndOfQuery = true;
        node.query = query;
        node.frequency = freq;
    }

    // Update frequency for a query (new searches)
    public void updateFrequency(String query) {
        TrieNode node = root;
        for (char c : query.toCharArray()) {
            node = node.children.get(c);
            if (node == null) {
                // New query: insert it
                insertQuery(query, 1);
                return;
            }
        }
        if (node.isEndOfQuery) {
            node.frequency += 1;
        } else {
            node.isEndOfQuery = true;
            node.query = query;
            node.frequency = 1;
        }
    }

    // Get top K suggestions for a prefix
    public List<String> search(String prefix) {
        TrieNode node = root;
        for (char c : prefix.toCharArray()) {
            node = node.children.get(c);
            if (node == null) return Collections.emptyList();
        }

        PriorityQueue<TrieNode> heap = new PriorityQueue<>(TOP_K, (a, b) -> a.frequency - b.frequency);
        dfs(node, heap);

        List<String> result = new ArrayList<>();
        while (!heap.isEmpty()) {
            result.add(heap.poll().query + " (" + heap.peek() + ")");
        }
        Collections.reverse(result); // highest frequency first
        return result;
    }

    // DFS to collect queries under a node
    private void dfs(TrieNode node, PriorityQueue<TrieNode> heap) {
        if (node.isEndOfQuery) {
            if (heap.size() < TOP_K) {
                heap.offer(node);
            } else if (node.frequency > heap.peek().frequency) {
                heap.poll();
                heap.offer(node);
            }
        }
        for (TrieNode child : node.children.values()) {
            dfs(child, heap);
        }
    }

    // Example usage
    public static void main(String[] args) {
        problem7 autocomplete = new problem7();

        // Insert some queries
        autocomplete.insertQuery("java tutorial", 1234567);
        autocomplete.insertQuery("javascript", 987654);
        autocomplete.insertQuery("java download", 456789);
        autocomplete.insertQuery("java 21 features", 10);

        // Search for prefix
        System.out.println("Search results for prefix 'jav':");
        List<String> suggestions = autocomplete.search("jav");
        for (String s : suggestions) {
            System.out.println(s);
        }

        // Update frequency
        autocomplete.updateFrequency("java 21 features");
        autocomplete.updateFrequency("java 21 features");
        System.out.println("\nAfter updating frequency:");
        suggestions = autocomplete.search("jav");
        for (String s : suggestions) {
            System.out.println(s);
        }
    }
}