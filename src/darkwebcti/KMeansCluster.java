
package darkwebcti;

import java.util.*;

public class KMeansCluster {

    private static final int MAX_ITERATIONS = 100;
    private static final Random RANDOM = new Random(42);

    public static class ClusterResult {
        public Map<Integer, List<String>> clusters =
                new LinkedHashMap<>();
    }

    public ClusterResult performClustering(
            Map<String, Map<String, Double>> documents,
            int k) {

        ClusterResult result = new ClusterResult();

        if (documents == null || documents.isEmpty()) {
            return result;
        }

        if (k < 1 || k > documents.size()) {
            throw new IllegalArgumentException(
                "K must be between 1 and the number of documents."
            );
        }

        List<String> names =
                new ArrayList<>(documents.keySet());

        List<Map<String, Double>> vectors =
                new ArrayList<>();

        for (String name : names) {
            vectors.add(normalize(documents.get(name)));
        }

        List<Map<String, Double>> centroids =
                initializeCentroids(vectors, k);

        int[] assignments = new int[vectors.size()];

        Arrays.fill(assignments, -1);

        for (int iteration = 0;
             iteration < MAX_ITERATIONS;
             iteration++) {

            boolean changed = false;

            // Assign each document to its nearest centroid
            for (int i = 0; i < vectors.size(); i++) {

                int bestCluster = 0;
                double bestDistance = Double.MAX_VALUE;

                for (int c = 0; c < k; c++) {

                    double distance = cosineDistance(
                        vectors.get(i),
                        centroids.get(c)
                    );

                    if (distance < bestDistance) {
                        bestDistance = distance;
                        bestCluster = c;
                    }
                }

                if (assignments[i] != bestCluster) {
                    assignments[i] = bestCluster;
                    changed = true;
                }
            }

            // Calculate new centroids
            List<Map<String, Double>> newCentroids =
                    new ArrayList<>();

            for (int c = 0; c < k; c++) {

                Map<String, Double> sum = new HashMap<>();
                int count = 0;

                for (int i = 0; i < vectors.size(); i++) {

                    if (assignments[i] == c) {

                        count++;

                        for (Map.Entry<String, Double> entry
                                : vectors.get(i).entrySet()) {

                            String word = entry.getKey();

                            sum.put(
                                word,
                                sum.getOrDefault(word, 0.0)
                                + entry.getValue()
                            );
                        }
                    }
                }

                if (count > 0) {

                    for (String word :
                            new ArrayList<>(sum.keySet())) {

                        sum.put(word, sum.get(word) / count);
                    }

                    newCentroids.add(normalize(sum));

                } else {
                    // Reinitialize an empty cluster using
                    // the document farthest from its centroid.
                    int farthestIndex = findFarthestDocument(
                        vectors, centroids, assignments
                    );

                    newCentroids.add(
                        new HashMap<>(vectors.get(farthestIndex))
                    );

                    assignments[farthestIndex] = c;
                    changed = true;
                }
            }

            centroids = newCentroids;

            if (!changed) {
                break;
            }
        }

        // Build final cluster results
        for (int c = 0; c < k; c++) {
            result.clusters.put(c, new ArrayList<>());
        }

        for (int i = 0; i < names.size(); i++) {
            result.clusters.get(assignments[i]).add(names.get(i));
        }

        return result;
    }

    // K-means++ centroid initialization
    private List<Map<String, Double>> initializeCentroids(
            List<Map<String, Double>> vectors, int k) {

        List<Map<String, Double>> centroids =
                new ArrayList<>();

        Set<Integer> selected = new HashSet<>();

        int first = RANDOM.nextInt(vectors.size());

        selected.add(first);
        centroids.add(new HashMap<>(vectors.get(first)));

        while (centroids.size() < k) {

            double[] distances = new double[vectors.size()];
            double total = 0.0;

            for (int i = 0; i < vectors.size(); i++) {

                if (selected.contains(i)) {
                    continue;
                }

                double nearest = Double.MAX_VALUE;

                for (Map<String, Double> centroid : centroids) {

                    double distance = cosineDistance(
                        vectors.get(i), centroid
                    );

                    nearest = Math.min(nearest, distance);
                }

                distances[i] = nearest * nearest;
                total += distances[i];
            }

            int chosen = -1;

            if (total > 0) {

                double target = RANDOM.nextDouble() * total;
                double cumulative = 0.0;

                for (int i = 0; i < vectors.size(); i++) {

                    if (selected.contains(i)) {
                        continue;
                    }

                    cumulative += distances[i];

                    if (cumulative >= target) {
                        chosen = i;
                        break;
                    }
                }
            }

            // Fallback if all remaining distances are zero
            if (chosen == -1) {

                for (int i = 0; i < vectors.size(); i++) {
                    if (!selected.contains(i)) {
                        chosen = i;
                        break;
                    }
                }
            }

            selected.add(chosen);

            centroids.add(
                new HashMap<>(vectors.get(chosen))
            );
        }

        return centroids;
    }

    // Normalize a TF-IDF vector
    private Map<String, Double> normalize(
            Map<String, Double> vector) {

        Map<String, Double> normalized = new HashMap<>();

        if (vector == null || vector.isEmpty()) {
            return normalized;
        }

        double norm = 0.0;

        for (double value : vector.values()) {
            norm += value * value;
        }

        norm = Math.sqrt(norm);

        if (norm == 0.0) {
            return normalized;
        }

        for (Map.Entry<String, Double> entry
                : vector.entrySet()) {

            normalized.put(
                entry.getKey(),
                entry.getValue() / norm
            );
        }

        return normalized;
    }

    // Cosine distance = 1 - cosine similarity
    private double cosineDistance(
            Map<String, Double> a,
            Map<String, Double> b) {

        if (a.isEmpty() || b.isEmpty()) {
            return 1.0;
        }

        double dot = 0.0;

        for (Map.Entry<String, Double> entry : a.entrySet()) {

            dot += entry.getValue()
                * b.getOrDefault(entry.getKey(), 0.0);
        }

        return Math.max(0.0, Math.min(2.0, 1.0 - dot));
    }

    // Find a document that is far from its assigned centroid
    private int findFarthestDocument(
            List<Map<String, Double>> vectors,
            List<Map<String, Double>> centroids,
            int[] assignments) {

        int farthest = 0;
        double maximumDistance = -1.0;

        for (int i = 0; i < vectors.size(); i++) {

            int cluster = assignments[i];

            if (cluster < 0) {
                continue;
            }

            double distance = cosineDistance(
                vectors.get(i),
                centroids.get(cluster)
            );

            if (distance > maximumDistance) {
                maximumDistance = distance;
                farthest = i;
            }
        }

        return farthest;
    }
}
