import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.model.openai.OpenAiStreamingChatModel;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.TokenStream;
import dev.langchain4j.service.UserMessage;

import java.io.Console;
import java.time.Duration;
import java.util.Scanner;
import java.util.Set;
import java.util.concurrent.CountDownLatch;

import static dev.langchain4j.model.openai.OpenAiChatModelName.GPT_4_O_MINI;

public class ServicesStreamLatch {
    private static String question;

    interface Assistant {
        @SystemMessage("Respond as a professional enterprise consultant without using markdown or numbered bullets.")
        TokenStream chat(@UserMessage String message);
    }

    public static void main(String[] args) throws InterruptedException {

        StreamingChatModel model = OpenAiStreamingChatModel.builder()
                .apiKey(System.getenv("OPENAI_API_KEY"))
                .modelName(GPT_4_O_MINI)
                .timeout(Duration.ofSeconds(120))
                //.logRequests(true)
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

            var latch = new CountDownLatch(1);

            consultant.chat(question)
                    .onPartialResponse(System.out::print)                 // tokens/chunks as they arrive
                    .onCompleteResponse(t -> latch.countDown())
                    .onError(e -> {
                        e.printStackTrace();
                        latch.countDown();
                    })
                    .start();

            latch.await();
        }
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
