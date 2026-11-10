
package darkwebcti;

import java.util.*;

public class AdjustedRandIndex {

    public double calculate(
            List<String> actual,
            List<String> predicted) {

        if (actual == null || predicted == null
                || actual.size() != predicted.size()) {
            throw new IllegalArgumentException(
                "Input lists must have equal sizes."
            );
        }

        int n = actual.size();

        if (n < 2) return 1.0;

        Map<String, Integer> actualCounts = new HashMap<>();
        Map<String, Integer> predictedCounts = new HashMap<>();
        Map<String, Integer> jointCounts = new HashMap<>();

        for (int i = 0; i < n; i++) {

            String a = String.valueOf(actual.get(i));
            String p = String.valueOf(predicted.get(i));

            actualCounts.merge(a, 1, Integer::sum);
            predictedCounts.merge(p, 1, Integer::sum);

            String key = a + "\u0000" + p;
            jointCounts.merge(key, 1, Integer::sum);
        }

        double index = 0.0;
        double sumActual = 0.0;
        double sumPredicted = 0.0;

        for (int count : jointCounts.values()) {
            index += choose2(count);
        }

        for (int count : actualCounts.values()) {
            sumActual += choose2(count);
        }

        for (int count : predictedCounts.values()) {
            sumPredicted += choose2(count);
        }

        double totalPairs = choose2(n);

        double expected =
            (sumActual * sumPredicted) / totalPairs;

        double maximum =
            0.5 * (sumActual + sumPredicted);

        double denominator = maximum - expected;

        if (Math.abs(denominator) < 1e-12) {
            return Math.abs(index - maximum) < 1e-12 ? 1.0 : 0.0;
        }

        return (index - expected) / denominator;
    }

    private double choose2(int n) {
        return (double) n * (n - 1) / 2.0;
    }
}
