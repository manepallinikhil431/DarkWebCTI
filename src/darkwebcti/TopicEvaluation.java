package darkwebcti;

import java.util.*;

public class TopicEvaluation {

    // Topic diversity = unique selected top terms / all selected top terms.
    public double calculateTopicDiversity(
            KMeansCluster.ClusterResult clusteringResult,
            Map<String, Map<String, Double>> tfidf,
            int topN) {

        if (clusteringResult == null || tfidf == null || topN <= 0) {
            return 0.0;
        }

        Set<String> uniqueTerms = new HashSet<>();
        int totalTerms = 0;

        for (Map.Entry<Integer, List<String>> clusterEntry
                : clusteringResult.clusters.entrySet()) {

            Map<String, Double> termScores = new HashMap<>();

            for (String documentName : clusterEntry.getValue()) {
                Map<String, Double> scores = tfidf.get(documentName);
                if (scores == null) continue;

                for (Map.Entry<String, Double> term : scores.entrySet()) {
                    termScores.merge(term.getKey(), term.getValue(), Double::sum);
                }
            }

            List<Map.Entry<String, Double>> sortedTerms =
                    new ArrayList<>(termScores.entrySet());
            sortedTerms.sort((a, b) -> Double.compare(b.getValue(), a.getValue()));

            int limit = Math.min(topN, sortedTerms.size());
            for (int i = 0; i < limit; i++) {
                uniqueTerms.add(sortedTerms.get(i).getKey());
                totalTerms++;
            }
        }

        return totalTerms == 0 ? 0.0 : (double) uniqueTerms.size() / totalTerms;
    }

    // Mean silhouette score using cosine distance.
    public double calculateSilhouetteScore(
            KMeansCluster.ClusterResult result,
            Map<String, Map<String, Double>> documents) {

        if (result == null || documents == null || result.clusters.size() < 2) {
            return 0.0;
        }

        double totalScore = 0.0;
        int documentCount = 0;

        for (Map.Entry<Integer, List<String>> entry : result.clusters.entrySet()) {
            List<String> ownCluster = entry.getValue();

            for (String document : ownCluster) {
                Map<String, Double> vector = documents.get(document);
                if (vector == null) continue;

                // Standard silhouette convention: singleton cluster score is zero.
                if (ownCluster.size() <= 1) {
                    documentCount++;
                    continue;
                }

                double aSum = 0.0;
                int aCount = 0;
                for (String other : ownCluster) {
                    if (!document.equals(other)) {
                        Map<String, Double> otherVector = documents.get(other);
                        if (otherVector != null) {
                            aSum += cosineDistance(vector, otherVector);
                            aCount++;
                        }
                    }
                }
                if (aCount == 0) {
                    documentCount++;
                    continue;
                }
                double a = aSum / aCount;

                double b = Double.POSITIVE_INFINITY;
                for (Map.Entry<Integer, List<String>> otherEntry
                        : result.clusters.entrySet()) {
                    if (otherEntry.getKey().equals(entry.getKey())
                            || otherEntry.getValue().isEmpty()) {
                        continue;
                    }

                    double distanceSum = 0.0;
                    int count = 0;
                    for (String other : otherEntry.getValue()) {
                        Map<String, Double> otherVector = documents.get(other);
                        if (otherVector != null) {
                            distanceSum += cosineDistance(vector, otherVector);
                            count++;
                        }
                    }
                    if (count > 0) b = Math.min(b, distanceSum / count);
                }

                if (Double.isFinite(b)) {
                    double denominator = Math.max(a, b);
                    if (denominator > 0.0) totalScore += (b - a) / denominator;
                }
                documentCount++;
            }
        }

        return documentCount == 0 ? 0.0 : totalScore / documentCount;
    }

    private double cosineDistance(
            Map<String, Double> a,
            Map<String, Double> b) {

        if (a == null || b == null || a.isEmpty() || b.isEmpty()) return 1.0;

        double dot = 0.0;
        double normA = 0.0;
        double normB = 0.0;

        for (double value : a.values()) normA += value * value;
        for (double value : b.values()) normB += value * value;
        for (Map.Entry<String, Double> term : a.entrySet()) {
            dot += term.getValue() * b.getOrDefault(term.getKey(), 0.0);
        }

        if (normA == 0.0 || normB == 0.0) return 1.0;
        double similarity = dot / (Math.sqrt(normA) * Math.sqrt(normB));
        similarity = Math.max(-1.0, Math.min(1.0, similarity));
        return 1.0 - similarity;
    }
}
