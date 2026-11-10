
package darkwebcti;

import java.util.*;

public class NaiveBayesClassifier {

    private final Map<String, Map<String, Integer>> wordCounts =
        new HashMap<>();

    private final Map<String, Integer> categoryCounts =
        new HashMap<>();

    private final Set<String> vocabulary = new HashSet<>();

    private int totalDocuments = 0;

    public void train(
            List<String> texts,
            List<String> labels) {

        if (texts.size() != labels.size()) {
            throw new IllegalArgumentException(
                "Texts and labels must have equal sizes."
            );
        }

        wordCounts.clear();
        categoryCounts.clear();
        vocabulary.clear();
        totalDocuments = 0;

        for (int i = 0; i < texts.size(); i++) {

            String category = labels.get(i);

            if (category == null || category.trim().isEmpty()) {
                continue;
            }

            category = category.trim();

            categoryCounts.merge(category, 1, Integer::sum);
            totalDocuments++;

            Map<String, Integer> counts =
                wordCounts.computeIfAbsent(
                    category, k -> new HashMap<>()
                );

            for (String word : tokenize(texts.get(i))) {

                vocabulary.add(word);
                counts.merge(word, 1, Integer::sum);
            }
        }
    }

    public String predict(String text) {

        if (totalDocuments == 0 || vocabulary.isEmpty()) {
            return "Model not trained";
        }

        String bestCategory = "";
        double bestScore = Double.NEGATIVE_INFINITY;

        for (String category : categoryCounts.keySet()) {

            double score = Math.log(
                (double) categoryCounts.get(category)
                / totalDocuments
            );

            Map<String, Integer> counts =
                wordCounts.get(category);

            int totalWords = counts.values().stream()
                .mapToInt(Integer::intValue).sum();

            for (String word : tokenize(text)) {

                int frequency = counts.getOrDefault(word, 0);

                score += Math.log(
                    (frequency + 1.0)
                    /
                    (totalWords + vocabulary.size())
                );
            }

            if (score > bestScore) {
                bestScore = score;
                bestCategory = category;
            }
        }

        return bestCategory;
    }

    private List<String> tokenize(String text) {

        List<String> words = new ArrayList<>();

        if (text == null) return words;

        for (String word : text.toLowerCase()
                .replaceAll("[^a-z0-9 ]", " ")
                .split("\\s+")) {

            if (word.length() > 2) {
                words.add(word);
            }
        }

        return words;
    }
}
