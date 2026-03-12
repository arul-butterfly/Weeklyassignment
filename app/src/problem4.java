import java.util.*;
import java.nio.file.*;
import java.io.*;

public class problem4 {

    private final int NGRAM_SIZE = 5; // 5-grams
    // Map: n-gram string → set of document IDs containing it
    private Map<String, Set<String>> ngramIndex = new HashMap<>();

    // Store document ID → total n-grams count for similarity calculation
    private Map<String, Integer> documentNgramCounts = new HashMap<>();

    // Analyze a document and index its n-grams
    public void analyzeDocument(String docId, String content) {
        List<String> words = tokenize(content);
        int ngramsCount = 0;

        for (int i = 0; i <= words.size() - NGRAM_SIZE; i++) {
            String ngram = buildNgram(words, i);
            ngramIndex.computeIfAbsent(ngram, k -> new HashSet<>()).add(docId);
            ngramsCount++;
        }

        documentNgramCounts.put(docId, ngramsCount);
        System.out.println(docId + " → Extracted " + ngramsCount + " n-grams");
    }

    // Tokenize document into words (simple split on whitespace)
    private List<String> tokenize(String content) {
        content = content.toLowerCase().replaceAll("[^a-z0-9\\s]", ""); // remove punctuation
        return Arrays.asList(content.split("\\s+"));
    }

    // Build n-gram starting at index i
    private String buildNgram(List<String> words, int start) {
        StringBuilder sb = new StringBuilder();
        for (int j = 0; j < NGRAM_SIZE; j++) {
            if (j > 0) sb.append(" ");
            sb.append(words.get(start + j));
        }
        return sb.toString();
    }

    // Compare a document with indexed documents to find similarity
    public void compareDocument(String docId, String content) {
        List<String> words = tokenize(content);
        Map<String, Integer> matchCounts = new HashMap<>();

        for (int i = 0; i <= words.size() - NGRAM_SIZE; i++) {
            String ngram = buildNgram(words, i);
            Set<String> matchedDocs = ngramIndex.get(ngram);
            if (matchedDocs != null) {
                for (String matchedDocId : matchedDocs) {
                    if (!matchedDocId.equals(docId)) {
                        matchCounts.put(matchedDocId, matchCounts.getOrDefault(matchedDocId, 0) + 1);
                    }
                }
            }
        }

        // Calculate similarity percentages
        for (Map.Entry<String, Integer> entry : matchCounts.entrySet()) {
            String otherDoc = entry.getKey();
            int matches = entry.getValue();
            int totalNgrams = documentNgramCounts.getOrDefault(otherDoc, 1);
            double similarity = (matches * 100.0) / totalNgrams;
            String status = similarity > 50.0 ? "PLAGIARISM DETECTED" : "suspicious";
            System.out.printf("→ Found %d matching n-grams with \"%s\"\n", matches, otherDoc);
            System.out.printf("→ Similarity: %.1f%% (%s)\n", similarity, status);
        }
    }

    // Example usage
    public static void main(String[] args) {
        problem4 detector = new problem4();

        // Simulated document contents
        String doc1 = "This is a sample essay. It contains multiple sentences for testing plagiarism detection.";
        String doc2 = "This essay is a sample document. It contains multiple sentences to test plagiarism detection system.";
        String doc3 = "Completely unrelated document content that shares no similarity with others.";

        detector.analyzeDocument("essay_089.txt", doc1);
        detector.analyzeDocument("essay_092.txt", doc2);
        detector.analyzeDocument("essay_123.txt", doc3);

        // Compare a new document against existing ones
        String newDoc = "This is a sample essay. It contains multiple sentences for testing plagiarism detection system.";
        detector.compareDocument("essay_123.txt", newDoc);
    }
}