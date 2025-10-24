import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.openai.OpenAiEmbeddingModel;
import dev.langchain4j.store.embedding.EmbeddingMatch;
import dev.langchain4j.store.embedding.EmbeddingSearchRequest;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.inmemory.InMemoryEmbeddingStore;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Scanner;
import java.util.stream.Stream;

import static dev.langchain4j.model.openai.OpenAiEmbeddingModelName.TEXT_EMBEDDING_3_SMALL;

/**
 * EmbeddingInMemory - Lab 8 - Creating an in-memory EmbeddingStore and searching for similar text
 */
public class EmbeddingInMemory {

    public static void main(String[] args) {
        EmbeddingStore<TextSegment> embeddingStore = new InMemoryEmbeddingStore<>();   // in-memory embedding store

        EmbeddingModel emodel = OpenAiEmbeddingModel.builder()          // Select the embedding algorithm/service
                .apiKey(System.getenv("OPENAI_API_KEY"))
                .modelName(TEXT_EMBEDDING_3_SMALL)
                .build();

        loadEmbeddingsFromFile(embeddingStore, emodel, "src/main/resources/amateur_astronomy.txt");  // load a list of strings into a local embeddings store [emodel]

        while (true) {
            System.out.print("query> ");
            String cmdline = new Scanner(System.in).nextLine();

            if (cmdline.isEmpty())
                continue;

            Embedding queryEmbedding = emodel.embed(cmdline).content();

            EmbeddingSearchRequest embeddingSearchRequest = EmbeddingSearchRequest.builder()    // Search the local embedding store for related strings
                    .queryEmbedding(queryEmbedding)
                    .maxResults(10)     // at most, find this number of matches
                    .minScore(0.7)      // 0-1, so ignore anything below the midpoint
                    .build();

            List<EmbeddingMatch<TextSegment>> matches = embeddingStore.search(embeddingSearchRequest).matches();

            if (matches.isEmpty()) {
                System.out.println("No matches found");
            } else {
                matches.forEach(em -> System.out.println(em.score() + ":" + em.embedded()));
            }
        }
    }

    /**
     * loadEmbeddingsFromFile() - Given an EmbeddingStore, an EmbeddingModel, and a file of strings, store the embedding vectors and the strings in the EmbeddingStore
     *
     * Uses a manual approach to loading lines (strings terminated with a <CR>) into the DB
     *
     * @param estore   - where you want to store the strings and vectors
     * @param embModel - which particular embedding model you want
     * @param myfile   - file of strings.  Each is delimited by a CR
     */
    public static void loadEmbeddingsFromFile(EmbeddingStore<TextSegment> estore, EmbeddingModel embModel, String myfile) {

        try (Stream<String> lines = Files.lines(Path.of(myfile))) {
            lines.filter(line -> !line.trim().isEmpty())
                    .forEach(line -> {
                        TextSegment ts = TextSegment.from(line);
                        Embedding emb = embModel.embed(ts).content();
                        estore.add(emb, ts);
                    });
            System.out.println("Embedding and Loading completed...");
        } catch (IOException iox) {
            iox.printStackTrace();
        }
    }

    /**
     * loadEmbeddingsFromFile_traditional(estore, embModel, myfile)
     * @param estore - where you want to store the embedding vectors and their associated strings
     * @param embModel - which embedding service you want to use
     * @param myfile - file to be loaded into the embedding-store
     *
     *    In case students are confused by the code above that uses the Java Streaming IO API
     */
    public static void loadEmbeddingsFromFile_traditional(EmbeddingStore<TextSegment> estore, EmbeddingModel embModel, String myfile) {
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader(myfile));
            String line;

            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (!line.isEmpty()) {
                    TextSegment ts = TextSegment.from(line);
                    Embedding emb = embModel.embed(ts).content();
                    estore.add(emb, ts);
                }
            }
            System.out.println("Embedding and Loading completed...");
        } catch (IOException iox) {
            iox.printStackTrace();
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
