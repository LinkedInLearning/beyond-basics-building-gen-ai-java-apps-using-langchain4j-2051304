import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.openai.OpenAiEmbeddingModel;
import dev.langchain4j.model.output.Response;
import dev.langchain4j.store.embedding.EmbeddingMatch;
import dev.langchain4j.store.embedding.EmbeddingSearchRequest;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.inmemory.InMemoryEmbeddingStore;
import dev.langchain4j.data.document.Metadata;
import dev.langchain4j.store.embedding.filter.Filter;
import dev.langchain4j.store.embedding.filter.comparison.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Stream;

import static dev.langchain4j.model.openai.OpenAiEmbeddingModelName.TEXT_EMBEDDING_3_SMALL;

public class MetaDataFilter {

    static void main() {
        MetaDataFilter mdf = new MetaDataFilter();

        // Choose an embedding model and an EmbeddingStore
        EmbeddingModel em = mdf.createEmbeddingModel();
        EmbeddingStore<TextSegment> es = mdf.createEmbeddingStore();

        // Load chunks and metadata into the EmbeddingStore
        List<TextSegment> textSegments;

        String resDir = System.getProperty("user.dir") + "/src/main/resources";
        textSegments = mdf.loadTextSegmentsFromFile(resDir + "/" + "history_of_music.txt", "Frank", "hobby");
        mdf.storeTextSegments(em, es, textSegments);
        textSegments = mdf.loadTextSegmentsFromFile(resDir + "/" + "history_of_tomatoes.txt", "Polly", "food");
        mdf.storeTextSegments(em, es, textSegments);

        // main user input loop
        mdf.userInteraction("String> ", em, es);
    }

    /**
     * createEmbeddingModel() - select our embedding model
     * @return
     */
    EmbeddingModel createEmbeddingModel() {
        return OpenAiEmbeddingModel.builder()
                .apiKey(System.getenv("OPENAI_API_KEY"))
                .modelName(TEXT_EMBEDDING_3_SMALL)
                .build();
    }

    /**
     * createEmbeddingStore() - select an EmbeddingStore (ie, a vector store or "vector database")
     * @return
     */
    EmbeddingStore<TextSegment> createEmbeddingStore() {
        return new InMemoryEmbeddingStore<>();
    }

    /**
     * storeTextSegments(EmbeddingModel em, EmbeddingStore<TextSegment> es, List<TextSegment> segments)
     * @param em - embedding model
     * @param es - embedding store - where the vectors and associated text and metadata are stored
     * @param segments - a List of TextSegments to store
     */
    void storeTextSegments(EmbeddingModel em, EmbeddingStore<TextSegment> es, List<TextSegment> segments) {
        Response<List<Embedding>> response = em.embedAll(segments);
        List<Embedding> embeddings = response.content();

        for (int i = 0; i < embeddings.size(); i++) {
            es.add(embeddings.get(i), segments.get(i));
        }
    }

    /**
     * getInput(String userPrompt) - get user input from the command line
     * @param userPrompt - command prompt (unrelated to LLM 'prompt')
     * @return the inputted line
     */
    public String getInput(String userPrompt) {
        System.out.print(userPrompt);
        return new Scanner(System.in).nextLine();
    }

    /**
     * userInteraction(String pstring, EmbeddingModel em, EmbeddingStore<TextSegment> es)
     *    main CLI loop
     *
     * @param pstring - command prompt (unrelated to LLM 'prompt')
     * @param em - EmbeddingModel
     * @param es - EmbeddingStore, ie, the vector store
     */
    public void userInteraction(String pstring, EmbeddingModel em, EmbeddingStore<TextSegment> es) {

        Set<String> set = Set.of("exit", "quit", "bye");
        String question;
        String userfilter;

        while (true) {
            question = getInput(pstring);
            if (set.contains(question.toLowerCase()))
                break;
            Embedding queryEmbedding = em.embed(question).content();    // get vector for user input

            userfilter = getInput("filter> ");
            if (set.contains(question.toLowerCase()))
                break;

            Filter filter = new IsEqualTo("author", userfilter);

            EmbeddingSearchRequest esr = EmbeddingSearchRequest.builder()
                    .queryEmbedding(queryEmbedding)
                    .minScore(.6)
                    .maxResults(3)
                    .filter(filter)
                    .build();

            List<EmbeddingMatch<TextSegment>> matches = es.search(esr).matches();

            for (EmbeddingMatch<TextSegment> match : matches) {
                System.out.println(match.score() + " : " + match.embedded().text());
                System.out.println(match.embedded().metadata());
            }
        }
    }

    /**
     * loadTextSegmentsFromFile(String myfile, String author, String category)
     * @param myfile - text file to be loaded
     * @param author - author to be stored as metadata
     * @param category - category to be stored as metadata
     * @return - List of loaded TextSegments with associated metadata
     */
    public List<TextSegment> loadTextSegmentsFromFile(String myfile, String author, String category) {

        List<TextSegment> textSegments = new ArrayList<>();

        try (Stream<String> lines = Files.lines(Path.of(myfile))) {     // Chunk by line and add metadata to TextSegments
            lines.filter(line -> !line.trim().isEmpty())
                    .forEach(line -> {
                        TextSegment ts = TextSegment.from(
                                line,
                                Metadata.from(Map.of(
                                        "author", author,
                                        "doctype", "text",
                                        "category", category,
                                        "year", "2025"))
                        );
                        textSegments.add(ts);
                    });
        } catch (IOException iox) {
            iox.printStackTrace();
        }
        return textSegments;
    }
}
