
package darkwebcti;

import java.util.*;

public class LdaTopicModel {

    private final int topics;
    private final int iterations;
    private final double alpha;
    private final double beta;
    private final Random random = new Random(42);

    private Map<String, Integer> vocabulary;
    private int[][] documentTopic;
    private int[][] topicWord;
    private int[] topicTotals;
    private int[][] assignments;
    private List<List<String>> documents;

    public LdaTopicModel(int topics, int iterations) {
        this.topics = topics;
        this.iterations = iterations;
        this.alpha = 0.1;
        this.beta = 0.01;
    }

    public Map<Integer, List<String>> train(List<String> input) {

        Map<Integer, List<String>> result = new LinkedHashMap<>();

        documents = new ArrayList<>();
        vocabulary = new LinkedHashMap<>();

        for (String text : input) {
            List<String> words = new ArrayList<>();

            if (text != null) {
                for (String word : text.toLowerCase()
                        .replaceAll("[^a-z0-9 ]", " ")
                        .split("\\s+")) {

                    if (word.length() > 2) {
                        words.add(word);
                        vocabulary.putIfAbsent(
                            word, vocabulary.size()
                        );
                    }
                }
            }

            documents.add(words);
        }

        int d = documents.size();
        int v = vocabulary.size();

        if (d == 0 || v == 0) return result;

        documentTopic = new int[d][topics];
        topicWord = new int[topics][v];
        topicTotals = new int[topics];
        assignments = new int[d][];

        for (int i = 0; i < d; i++) {

            assignments[i] = new int[documents.get(i).size()];

            for (int j = 0; j < documents.get(i).size(); j++) {

                int topic = random.nextInt(topics);

                assignments[i][j] = topic;

                int word = vocabulary.get(
                    documents.get(i).get(j)
                );

                documentTopic[i][topic]++;
                topicWord[topic][word]++;
                topicTotals[topic]++;
            }
        }

        for (int iteration = 0;
             iteration < iterations;
             iteration++) {

            for (int i = 0; i < d; i++) {

                for (int j = 0;
                     j < assignments[i].length;
                     j++) {

                    String wordText = documents.get(i).get(j);
                    int word = vocabulary.get(wordText);

                    int oldTopic = assignments[i][j];

                    documentTopic[i][oldTopic]--;
                    topicWord[oldTopic][word]--;
                    topicTotals[oldTopic]--;

                    double[] probabilities = new double[topics];
                    double total = 0;

                    for (int k = 0; k < topics; k++) {

                        probabilities[k] =
                            (documentTopic[i][k] + alpha)
                            *
                            (topicWord[k][word] + beta)
                            /
                            (topicTotals[k] + v * beta);

                        total += probabilities[k];
                    }

                    double sample = random.nextDouble() * total;
                    int newTopic = topics - 1;

                    for (int k = 0; k < topics; k++) {

                        sample -= probabilities[k];

                        if (sample <= 0) {
                            newTopic = k;
                            break;
                        }
                    }

                    assignments[i][j] = newTopic;

                    documentTopic[i][newTopic]++;
                    topicWord[newTopic][word]++;
                    topicTotals[newTopic]++;
                }
            }
        }

        for (int k = 0; k < topics; k++) {

            Map<String, Integer> scores = new HashMap<>();

            for (String word : vocabulary.keySet()) {

                int id = vocabulary.get(word);

                if (topicWord[k][id] > 0) {
                    scores.put(word, topicWord[k][id]);
                }
            }

            List<String> topWords = new ArrayList<>(
                scores.keySet()
            );

            topWords.sort((a, b) ->
                Integer.compare(scores.get(b), scores.get(a))
            );

            result.put(k, topWords);
        }

        return result;
    }
}
