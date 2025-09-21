import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.DocumentSplitter;
import dev.langchain4j.data.document.parser.TextDocumentParser;
import dev.langchain4j.data.document.parser.apache.pdfbox.ApachePdfBoxDocumentParser;
import dev.langchain4j.data.document.splitter.DocumentSplitters;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.internal.Utils;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.model.openai.OpenAiChatModelName;
import dev.langchain4j.model.openai.OpenAiEmbeddingModel;
import dev.langchain4j.rag.content.retriever.ContentRetriever;
import dev.langchain4j.rag.content.retriever.EmbeddingStoreContentRetriever;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.store.embedding.EmbeddingStoreIngestor;
import dev.langchain4j.store.embedding.inmemory.InMemoryEmbeddingStore;

import static dev.langchain4j.data.document.loader.FileSystemDocumentLoader.loadDocuments;
import static dev.langchain4j.model.openai.OpenAiEmbeddingModelName.TEXT_EMBEDDING_3_SMALL;

import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class EmbeddingInMemoryService {
    interface MatchAssistant {
        @SystemMessage("You are a polite conversational assistant")
        String match(String text);
    }
    public static void main(String[] args) {
        String directory = "MyDocuments";       // Folder of my .txt and .pdf files
        MatchAssistant matcher = createMatchAssistant(createChatModel(), directory);

        while (true) {
            System.out.print("query> ");
            String cmdline = new Scanner(System.in).nextLine();
            if (cmdline.isEmpty())
                continue;

            var response = matcher.match(cmdline);        // Send everything to the LLM chatbot style
            System.out.println(response);
        }
    }
    /**
     * createChatModel() - convenience method to instantiate a ChatModel
     * @return specific chat model (for our example, hard-coded to OpenAI)
     */
    public static ChatModel createChatModel() {
        return OpenAiChatModel.builder()
                .apiKey(System.getenv("OPENAI_API_KEY"))
                .modelName(OpenAiChatModelName.GPT_4_O)
                .build();
    }

    public static MatchAssistant createMatchAssistant(ChatModel chatModel, String dir) {
        // Load all .txt and .pdf files in dir
        List<Document> txtDocuments = loadDocuments(toPath(dir), glob("*.txt"), new TextDocumentParser());
        List<Document> pdfDocuments = loadDocuments(toPath(dir), glob("*.pdf"), new ApachePdfBoxDocumentParser());

        // Combine the Documents
        List<Document> allDocs = new ArrayList<>(txtDocuments.size() + pdfDocuments.size());
        allDocs.addAll(txtDocuments);
        allDocs.addAll(pdfDocuments);

        // Split (chunk) the Documents
        DocumentSplitter splitter = DocumentSplitters.recursive(300, 0);
        List<TextSegment> segments = splitter.splitAll(allDocs);

        // Get the embeddings for each chunk
        EmbeddingModel embeddingModel = OpenAiEmbeddingModel.builder()
                .apiKey(System.getenv("OPENAI_API_KEY"))
                .modelName(TEXT_EMBEDDING_3_SMALL)
                .build();
        List<Embedding> embeddings = embeddingModel.embedAll(segments).content();

        // Create an in-memory embedding store
        InMemoryEmbeddingStore<TextSegment> embeddingStore = new InMemoryEmbeddingStore<>();

        // Create a specific ingestor and load the embedding store
        EmbeddingStoreIngestor ingestor = EmbeddingStoreIngestor.builder()
                .documentSplitter(DocumentSplitters.recursive(300, 0))
                .embeddingModel(embeddingModel)
                .embeddingStore(embeddingStore)
                .build();
        ingestor.ingest(allDocs);

        // Create a specific retriever
        ContentRetriever contentRetriever = EmbeddingStoreContentRetriever.builder()
                .embeddingStore(embeddingStore)
                .embeddingModel(embeddingModel)
                .maxResults(10)
                .minScore(0.6)
                .build();

        // Create an AiService with these characteristics and return it
        return AiServices.builder(MatchAssistant.class)
                .chatModel(chatModel)
                .chatMemory(MessageWindowChatMemory.withMaxMessages(10))
                .contentRetriever(contentRetriever) // it should have access to our documents
                .build();
    }

    public static PathMatcher glob(String glob) {
        return FileSystems.getDefault().getPathMatcher("glob:" + glob);
    }

    public static Path toPath(String relativePath) {
        try {
            URL fileUrl = Utils.class.getClassLoader().getResource(relativePath);
            return Paths.get(fileUrl.toURI());
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }
}
