
package darkwebcti;

import java.util.*;

public class TopicCoherence {

    public double calculateCoherence(
            List<String> topicWords,
            List<String> documents) {

        if (topicWords == null || topicWords.size() < 2
                || documents == null || documents.isEmpty()) {
            return 0.0;
        }

        int n = documents.size();
        double total = 0.0;
        int pairs = 0;

        for (int i = 0; i < topicWords.size(); i++) {

            for (int j = i + 1; j < topicWords.size(); j++) {

                String first = topicWords.get(i).toLowerCase();
                String second = topicWords.get(j).toLowerCase();

                int countA = 0;
                int countB = 0;
                int countBoth = 0;

                for (String document : documents) {

                    Set<String> words = new HashSet<>(
                        Arrays.asList(
                            document.toLowerCase()
                                .replaceAll("[^a-z0-9 ]", " ")
                                .split("\\s+")
                        )
                    );

                    boolean a = words.contains(first);
                    boolean b = words.contains(second);

                    if (a) countA++;
                    if (b) countB++;
                    if (a && b) countBoth++;
                }

                if (countBoth == 0) {
                    total -= 1.0;
                } else {

                    double pA = (double) countA / n;
                    double pB = (double) countB / n;
                    double pAB = (double) countBoth / n;

                    double pmi = Math.log(
                        pAB / (pA * pB)
                    );

                    double npmi = pmi / -Math.log(pAB);

                    total += npmi;
                }

                pairs++;
            }
        }

        return pairs == 0 ? 0.0 : total / pairs;
    }
}
