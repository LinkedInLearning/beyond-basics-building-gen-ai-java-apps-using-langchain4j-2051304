import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.model.openai.OpenAiChatModelName;

public class PromptTechniques1_bonus {
    /*
    Background:
    [Insert all relevant background information, quotes, or any additional information.]

    Task:
    [Clearly state the specific action or question.]
     */
    final static String example = """
            Background:
            The user is a first-year law student studying contract law. They have read sections on offer, acceptance, and consideration.
            
            Task:
            What are three real-world examples of consideration in contract law?
            """;

    public static void main(String[] args) {
        ChatModel cmodel = OpenAiChatModel.builder()
                .apiKey(System.getenv("OPENAI_API_KEY"))
                .modelName(OpenAiChatModelName.GPT_4_O)
                .build();

        System.out.println(cmodel.chat(example));
    }
}
