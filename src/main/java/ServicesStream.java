import java.io.Console;
import java.time.Duration;
import java.util.Scanner;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.model.chat.response.ChatResponse;
import dev.langchain4j.model.openai.OpenAiStreamingChatModel;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.TokenStream;

import static dev.langchain4j.model.openai.OpenAiChatModelName.GPT_4_O_MINI;

/**
 * ServicesStream - Lab 5 - Handling streamed output (SSE) with an AiService
 */
class ServicesStream {
    private static String question;

    interface Assistant {
        @SystemMessage("Respond as a professional enterprise consultant without using markdown or numbered bullets.")
        TokenStream chat(String message);
    }

    public static void main(String[] args) {

        StreamingChatModel model = OpenAiStreamingChatModel.builder()
                .apiKey(System.getenv("OPENAI_API_KEY"))
                .modelName(GPT_4_O_MINI)
                .timeout(Duration.ofSeconds(120))
                .logRequests(true)
                .maxTokens(1024*10)
                //.logResponses(true)
                .build();

        Assistant consultant = AiServices.builder(Assistant.class)
                .streamingChatModel(model)
                .chatMemory(MessageWindowChatMemory.withMaxMessages(10))
                .build();

        String pstring = "\nCmd> ";
        Set<String> set = Set.of("exit", "quit", "bye");

        while (true) {

            String question = getUserInput(pstring);
            if (set.contains(question.toLowerCase()))
                break;

            if (question.isBlank())       // If nothing, do nothing
                continue;

            CompletableFuture<ChatResponse> future = new CompletableFuture<>();
            TokenStream stream = consultant.chat(question);

            stream  .onPartialResponse(System.out::print)
                    .onCompleteResponse(future::complete)
                    .onError(future::completeExceptionally)
                    .start();

            future.join();
        }

        System.exit(0);
    }

    /**
     * getUserInput(pstring) - return string from  user
     * @return
     */
    public static String getUserInput(String pstring) {
        System.out.print(pstring);
        var userinput = new Scanner(System.in);
        String cmdline = userinput.nextLine();

       return cmdline;
    }
}
