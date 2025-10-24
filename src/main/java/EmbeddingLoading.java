import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.openai.OpenAiEmbeddingModel;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.inmemory.InMemoryEmbeddingStore;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;

import static dev.langchain4j.model.openai.OpenAiEmbeddingModelName.TEXT_EMBEDDING_3_SMALL;

public class EmbeddingLoading {
    public static void main(String[] args) {
        String resDir = System.getProperty("user.dir") + "/src/main/resources";     // dir of files to be ingested

        EmbeddingStore<TextSegment> estore = new InMemoryEmbeddingStore<>();

        EmbeddingModel embeddingModel = OpenAiEmbeddingModel.builder()
                .apiKey(System.getenv("OPENAI_API_KEY"))
                .modelName(TEXT_EMBEDDING_3_SMALL)
                .build();

        try (Stream<String> lines = Files.lines(Path.of(resDir + "/" + "growing_vegetables.txt"))) {        // myfile is target file

            lines.filter(line -> !line.trim().isEmpty())                 // avoid empty lines
                    .forEach(line -> {
                        TextSegment ts = TextSegment.from(line);                // Create a TextSegment
                        Embedding emb = embeddingModel.embed(ts).content();     // Get the embedding vector
                        estore.add(emb, ts);                                    // Save it
                    });
            System.out.println("Embedding and Loading completed...");

        } catch (IOException iox) {
            iox.printStackTrace();
        }
    }
}
