import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.model.openai.OpenAiEmbeddingModel;
import dev.langchain4j.model.output.Response;

import static dev.langchain4j.model.openai.OpenAiEmbeddingModelName.TEXT_EMBEDDING_3_SMALL;

/**
 * GetEmbedding - Lab 6 - retrieving an embedding vector from an embedding service
 */
public class GetEmbedding {
    public static void main(String[] argv) {
        String apiKey = System.getenv("OPENAI_API_KEY");

        EmbeddingModel model = OpenAiEmbeddingModel.builder()
                .apiKey(apiKey)
                .modelName(TEXT_EMBEDDING_3_SMALL)
                .build();

        Response<Embedding> response = model.embed("Java is perfectly suitable for GenAI applications.");
        Embedding embedding = response.content();

        System.out.println(embedding.dimension() + " elements: " + embedding);
    }
}
