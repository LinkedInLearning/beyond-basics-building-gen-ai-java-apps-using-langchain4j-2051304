import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.openai.OpenAiEmbeddingModel;
import dev.langchain4j.model.output.Response;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.IntStream;

import static dev.langchain4j.model.openai.OpenAiEmbeddingModelName.TEXT_EMBEDDING_3_SMALL;

public class MostSimilar {
    private final static String DEFAULT_DATA = "./src/main/resources/history_of_tomatoes.txt";

    public static void main(String[] args) throws IOException {

        EmbeddingModel model = OpenAiEmbeddingModel.builder()
                .apiKey(System.getenv("OPENAI_API_KEY"))
                .modelName(TEXT_EMBEDDING_3_SMALL)
                .build();

        List<Float> one = getEmbeddingVec(model, "I am interested in cooking and good food.");

        // Read file of strings into a list
        List<String> fstrings = Files.readAllLines(Path.of(DEFAULT_DATA), StandardCharsets.UTF_8);
        List<Float> similarities = new ArrayList<>();      // Collection of distances.  Smaller is more similar

        // Iterate through List and calculate the cosine similarity for each line in the file compared to the user's input
        for (String fs : fstrings) {
            if (fs.isEmpty())           // skip empty lines
                continue;

            List<Float> fsembedding = getEmbeddingVec(model, fs);
            double similarity = cosineSimilarity(FloatList2doubleArray(one), FloatList2doubleArray(fsembedding));
            similarities.add((float) similarity);

        }

        Collections.sort(similarities, Collections.reverseOrder());    // by default, sort() sorts in ascending order - smallest distances first
        System.out.println(similarities);

        showtop(similarities, 10);                              // The most similar are at the top
    }

    /**
     * getEmbeddingVec(EmbeddingModel model, String input)
     * @param model - specific EmbeddingModel to use
     * @param input - target string
     * @return - List<Float> - actual embedding vector
     */
    public static List<Float> getEmbeddingVec(EmbeddingModel model, String input) {
        Response<dev.langchain4j.data.embedding.Embedding> response = model.embed(input);
        return response.content().vectorAsList();
    }

    /**
     * showtop(List<Float> sim, int max)
     * @param sim - embedding vector - List<Float>
     * @param max - how many you want to show
     */
    public static void showtop(List<Float> sim, int max) {
        System.out.println("Top " + max + " =====================");
        /*  For those students who have not covered lambdas yet
        for (int i = 0; i < max; i++) {
            System.out.println(sim.get(i));
        }*/
        IntStream.range(0, max)
                .forEach(i -> System.out.println(sim.get(i)));
    }

    /**
     * dotProduct(double[] vec1, double[] vec2) - Calculate dot product for two 1-D arrays
     * @param vec1 - first 1-dimensional array
     * @param vec2 - second 1-dimensional array
     * @return - dot product
     */
    public static double dotProduct(double[] vec1, double[] vec2) {
        double dotProduct = 0;
        for (int i = 0; i < vec1.length; i++) {
            dotProduct += vec1[i] * vec2[i];
        }
        return dotProduct;
    }

    /**
     * magnitude(double[] vec) - Calculate magnitude of a 1-dimensional vector
     * @param vec - desired vector
     * @return magnitude
     */
    public static double magnitude(double[] vec) {
        double sum = 0;
        for (double v : vec) {
            sum += v * v;
        }
        return Math.sqrt(sum);
    }

    /**
     * cosineSimilarity(double[] vec1, double[] vec2) - Calculate cosine similarity between two embedding vectors
     * @param vec1 - first vector
     * @param vec2 - second vector
     * @return - similarity [0 - least similar] to [1 - most similar]
     */
    public static double cosineSimilarity(double[] vec1, double[] vec2) {
        double dotProduct = dotProduct(vec1, vec2);
        double magnitudeVec1 = magnitude(vec1);
        double magnitudeVec2 = magnitude(vec2);

        if (magnitudeVec1 == 0 || magnitudeVec2 == 0) {
            return 0; // To avoid division by zero
        } else {
            return dotProduct / (magnitudeVec1 * magnitudeVec2);
        }
    }

    /**
     * FloatList2doubleArray(List<Float> floatlist) - utility method to convert List of Floats to array of doubles
     * @param floatList - target List of Floats
     * @return equivalent array of doubles
     */
    public static double[] FloatList2doubleArray(List<Float> floatList) {
        double[] result = new double[floatList.size()];
        for (int i = 0; i < floatList.size(); i++) {
            result[i] = floatList.get(i);
        }
        return result;
    }
}
