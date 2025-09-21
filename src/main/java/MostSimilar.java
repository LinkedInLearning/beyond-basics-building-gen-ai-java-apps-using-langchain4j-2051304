import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.openai.OpenAiEmbeddingModel;
import dev.langchain4j.model.output.Response;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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
        List<String> fstrings = fileToListStrings(DEFAULT_DATA);
        List<Float> similarities = new ArrayList<>();

        // Iterate through List and calculate the cosine similarity for each line in the file compared to the user's input
        for (String fs : fstrings) {
            if (fs.isEmpty())           // skip empty lines
                continue;

            List<Float> fsembedding = getEmbeddingVec(model, fs);
            double similarity = cosineSimilarity(FloatList2doubleArray(one), FloatList2doubleArray(fsembedding));
            similarities.add(1 - (float) similarity);     // Save the cosine similarities for sorting later
        }

        Collections.sort(similarities);     // remember sort() sorts in ascending order
        System.out.println(similarities);

        showtop(similarities, 3);
    }

    public static List<Float> getEmbeddingVec(EmbeddingModel model, String input) {
        Response<dev.langchain4j.data.embedding.Embedding> response = model.embed(input);
        return response.content().vectorAsList();
    }

    public static void showtop(List<Float> sim, int max) {
        System.out.println("Top " + max + " =====================");
        for (int i = 0; i < max; i++) {
            System.out.println(sim.get(i));
        }
    }

    /**
     * fileToListStrings() - read a text file into a List of Strings
     * @param fname
     * @return
     */
    public static List<String> fileToListStrings(String fname) {
        List<String> lines = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(fname))) {
            String line;
            while ((line = reader.readLine()) != null) {
                lines.add(line);
            }
        } catch (IOException e) {
            System.err.println("Error reading file: " + e.getMessage());
        }
        return lines;
    }
    public static double dotProduct(double[] vec1, double[] vec2) {
        double dotProduct = 0;
        for (int i = 0; i < vec1.length; i++) {
            dotProduct += vec1[i] * vec2[i];
        }
        return dotProduct;
    }

    // Function to calculate magnitude of a vector
    public static double magnitude(double[] vec) {
        double sum = 0;
        for (double v : vec) {
            sum += v * v;
        }
        return Math.sqrt(sum);
    }

    // Function to calculate cosine similarity between two embedding vectors
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
    public static double[] FloatList2doubleArray(List<Float> floatList) {
        double[] result = new double[floatList.size()];
        for (int i = 0; i < floatList.size(); i++) {
            result[i] = floatList.get(i);
        }
        return result;
    }
}
