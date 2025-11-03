import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.model.openai.OpenAiChatModelName;

public class PromptTechniques2 {
    /*
    Here is the background: [insert context]
    Follow this process:
        1. Identify any relevant details from the background.
        2. Explain how these details affect the task.
        3. Provide the final answer.
    Task: [insert task]
    */
    final static String example = """
            Here is the background:
              Our Java-based payment gateway started failing on certain transactions after a recent update. The error logs show NullPointerExceptions originating from the PaymentProcessor class. The failures occur only for transactions in EUR currency when processed through the new DiscountService. QA confirms the issue is reproducible in staging.
            
            Follow this process:
             1. Identify relevant details from the background.
             2. Explain how these details affect the task.
             3. Provide the final answer.
            
            Task:
            Suggest the most likely cause and the first debugging step.
            """;

    public static void main(String[] args) {
        ChatModel cmodel = OpenAiChatModel.builder()
                .apiKey(System.getenv("OPENAI_API_KEY"))
                .modelName(OpenAiChatModelName.GPT_4_O)
                .build();

        System.out.println(cmodel.chat(example));
    }
}
