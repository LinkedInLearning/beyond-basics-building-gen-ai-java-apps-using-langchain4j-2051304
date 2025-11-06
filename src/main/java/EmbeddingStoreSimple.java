import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.DocumentSplitter;
import dev.langchain4j.data.document.loader.FileSystemDocumentLoader;
import dev.langchain4j.data.document.splitter.DocumentSplitters;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.openai.OpenAiEmbeddingModel;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.EmbeddingStoreIngestor;
import dev.langchain4j.store.embedding.inmemory.InMemoryEmbeddingStore;

import static dev.langchain4j.model.openai.OpenAiEmbeddingModelName.TEXT_EMBEDDING_3_SMALL;

public class EmbeddingStoreSimple {
    public static void main(String[] args) {

        String resDir = System.getProperty("user.dir") + "/src/main/resources";     // dir of files to be ingested

        EmbeddingStore<TextSegment> estore = new InMemoryEmbeddingStore<>();

        EmbeddingModel embeddingModel = OpenAiEmbeddingModel.builder()
                .apiKey(System.getenv("OPENAI_API_KEY"))
                .modelName(TEXT_EMBEDDING_3_SMALL)
                .build();

        Document mydoc = FileSystemDocumentLoader.loadDocument(resDir + "/" + "history_of_tomatoes.txt");
        DocumentSplitter splitter = DocumentSplitters.recursive(500, 50);

        EmbeddingStoreIngestor ingestor = EmbeddingStoreIngestor.builder()
                .embeddingModel(embeddingModel)
                .embeddingStore(estore)
                .documentSplitter(splitter)
                .build();

        ingestor.ingest(mydoc);         // load strings and embeddings into DB
                                        // Need to run in debug mode and inspect the contents of  'estore'
    }
}
