
package darkwebcti;

import java.util.*;

public class TfIdfAnalyzer {

    // Calculate TF-IDF values for a collection of documents
    public Map<String, Map<String, Double>> calculateTfIdf(
            List<String> documents) {

        Map<String, Map<String, Double>> result = new LinkedHashMap<>();

        if (documents == null || documents.isEmpty()) {
            return result;
        }

        List<List<String>> tokenizedDocuments = new ArrayList<>();
        Map<String, Integer> documentFrequency = new HashMap<>();

        // Tokenize documents and calculate document frequency
        for (String document : documents) {

            List<String> words = tokenize(document);
            tokenizedDocuments.add(words);

            Set<String> uniqueWords = new HashSet<>(words);

            for (String word : uniqueWords) {
                documentFrequency.put(
                    word,
                    documentFrequency.getOrDefault(word, 0) + 1
                );
            }
        }

        int totalDocuments = documents.size();

        // Calculate TF-IDF for every document
        for (int i = 0; i < tokenizedDocuments.size(); i++) {

            List<String> words = tokenizedDocuments.get(i);

            Map<String, Integer> frequency = new HashMap<>();

            for (String word : words) {
                frequency.put(
                    word,
                    frequency.getOrDefault(word, 0) + 1
                );
            }

            Map<String, Double> scores = new LinkedHashMap<>();

            int totalWords = words.size();

            if (totalWords > 0) {

                for (String word : frequency.keySet()) {

                    double tf =
                        (double) frequency.get(word) / totalWords;

                    double idf = Math.log(
                        (double) totalDocuments /
                        documentFrequency.get(word)
                    );

                    double tfIdf = tf * idf;

                    scores.put(word, tfIdf);
                }
            }

            result.put("Document " + (i + 1), scores);
        }

        return result;
    }

    // Clean and tokenize text
    private List<String> tokenize(String text) {

        List<String> words = new ArrayList<>();

        if (text == null || text.trim().isEmpty()) {
            return words;
        }

        String cleaned = text.toLowerCase()
                .replaceAll("[^a-z0-9\\s]", " ")
                .replaceAll("\\s+", " ")
                .trim();

        for (String word : cleaned.split("\\s+")) {

            if (word.length() > 2) {
                words.add(word);
            }
        }

        return words;
    }
}
