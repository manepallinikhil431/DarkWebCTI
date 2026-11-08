package darkwebcti;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * Compares extracted entities with manually labeled expected entities.
 * Scores apply only to the supplied labeled evaluation sample.
 */
public class EntityEvaluation {

    public static class MetricResult {
        public int truePositive;
        public int falsePositive;
        public int falseNegative;
        public double precision;
        public double recall;
        public double f1Score;
    }

    public Map<String, MetricResult> evaluate(
            Map<String, List<String>> actual,
            Map<String, List<String>> predicted) {

        Map<String, MetricResult> results = new LinkedHashMap<>();
        Set<String> categories = new LinkedHashSet<>();
        categories.addAll(actual.keySet());
        categories.addAll(predicted.keySet());

        for (String category : categories) {
            Set<String> actualSet = normalize(
                actual.getOrDefault(category, Collections.<String>emptyList()));
            Set<String> predictedSet = normalize(
                predicted.getOrDefault(category, Collections.<String>emptyList()));

            MetricResult metric = new MetricResult();

            for (String value : predictedSet) {
                if (actualSet.contains(value)) {
                    metric.truePositive++;
                } else {
                    metric.falsePositive++;
                }
            }

            for (String value : actualSet) {
                if (!predictedSet.contains(value)) {
                    metric.falseNegative++;
                }
            }

            metric.precision = ratio(metric.truePositive,
                metric.truePositive + metric.falsePositive);
            metric.recall = ratio(metric.truePositive,
                metric.truePositive + metric.falseNegative);
            metric.f1Score = harmonicMean(metric.precision, metric.recall);

            results.put(category, metric);
        }
        return results;
    }

    private Set<String> normalize(List<String> values) {
        Set<String> normalized = new LinkedHashSet<>();
        for (String value : values) {
            if (value != null && !value.trim().isEmpty()) {
                normalized.add(value.trim().toLowerCase(Locale.ROOT));
            }
        }
        return normalized;
    }

    private double ratio(int numerator, int denominator) {
        return denominator == 0 ? 0.0 : (double) numerator / denominator;
    }

    private double harmonicMean(double precision, double recall) {
        return precision + recall == 0.0 ? 0.0
            : 2.0 * precision * recall / (precision + recall);
    }

    public String generateReport(Map<String, MetricResult> results) {
        StringBuilder report = new StringBuilder();
        report.append("ENTITY EXTRACTION EVALUATION\n");
        report.append("============================\n\n");

        int totalTP = 0;
        int totalFP = 0;
        int totalFN = 0;

        for (Map.Entry<String, MetricResult> entry : results.entrySet()) {
            MetricResult metric = entry.getValue();
            report.append(entry.getKey()).append('\n');
            report.append("----------------------------\n");
            report.append("True Positive: ").append(metric.truePositive).append('\n');
            report.append("False Positive: ").append(metric.falsePositive).append('\n');
            report.append("False Negative: ").append(metric.falseNegative).append('\n');
            report.append(String.format(Locale.US,
                "Precision: %.2f%%\n", metric.precision * 100.0));
            report.append(String.format(Locale.US,
                "Recall: %.2f%%\n", metric.recall * 100.0));
            report.append(String.format(Locale.US,
                "F1-Score: %.2f%%\n\n", metric.f1Score * 100.0));

            totalTP += metric.truePositive;
            totalFP += metric.falsePositive;
            totalFN += metric.falseNegative;
        }

        double precision = ratio(totalTP, totalTP + totalFP);
        double recall = ratio(totalTP, totalTP + totalFN);
        double f1 = harmonicMean(precision, recall);

        report.append("OVERALL MICRO-AVERAGED RESULTS\n");
        report.append("==============================\n");
        report.append("True Positive: ").append(totalTP).append('\n');
        report.append("False Positive: ").append(totalFP).append('\n');
        report.append("False Negative: ").append(totalFN).append('\n');
        report.append(String.format(Locale.US,
            "Precision: %.2f%%\n", precision * 100.0));
        report.append(String.format(Locale.US,
            "Recall: %.2f%%\n", recall * 100.0));
        report.append(String.format(Locale.US,
            "F1-Score: %.2f%%\n", f1 * 100.0));

        return report.toString();
    }
}
