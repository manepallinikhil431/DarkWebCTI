
package darkwebcti;

import java.util.*;

public class AnalystRelevance {

    public double calculateAverageRelevance(
            List<Integer> ratings) {

        if (ratings == null || ratings.isEmpty()) {
            return 0.0;
        }

        double total = 0.0;
        int count = 0;

        for (Integer rating : ratings) {

            if (rating == null) continue;

            if (rating < 1 || rating > 5) {
                throw new IllegalArgumentException(
                    "Ratings must be between 1 and 5."
                );
            }

            total += rating;
            count++;
        }

        return count == 0 ? 0.0 : total / count;
    }

    public double calculateRelevancePercentage(
            List<Integer> ratings) {

        return calculateAverageRelevance(ratings) / 5.0 * 100.0;
    }

    public String generateReport(List<Integer> ratings) {

        return "ANALYST RELEVANCE REPORT\n"
            + "-----------------------\n"
            + "Average Rating: "
            + String.format(Locale.US, "%.2f / 5.00",
                calculateAverageRelevance(ratings))
            + "\nRelevance Percentage: "
            + String.format(Locale.US, "%.2f%%",
                calculateRelevancePercentage(ratings));
    }
}
