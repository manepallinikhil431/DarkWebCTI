
package darkwebcti;

import java.util.*;

public class TemporalStability {

    public double calculateStability(
            List<String> previousTopics,
            List<String> currentTopics) {

        if (previousTopics == null || currentTopics == null) {
            return 0.0;
        }

        Set<String> previous = new HashSet<>();
        Set<String> current = new HashSet<>();

        for (String word : previousTopics) {
            previous.add(word.toLowerCase().trim());
        }

        for (String word : currentTopics) {
            current.add(word.toLowerCase().trim());
        }

        Set<String> intersection = new HashSet<>(previous);
        intersection.retainAll(current);

        Set<String> union = new HashSet<>(previous);
        union.addAll(current);

        if (union.isEmpty()) return 0.0;

        return (double) intersection.size() / union.size();
    }

    public double calculateAverageStability(
            List<List<String>> topicsOverTime) {

        if (topicsOverTime == null
                || topicsOverTime.size() < 2) {
            return 0.0;
        }

        double total = 0.0;
        int comparisons = 0;

        for (int i = 1; i < topicsOverTime.size(); i++) {

            total += calculateStability(
                topicsOverTime.get(i - 1),
                topicsOverTime.get(i)
            );

            comparisons++;
        }

        return total / comparisons;
    }
}

