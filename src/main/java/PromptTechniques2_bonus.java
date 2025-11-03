import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.model.openai.OpenAiChatModelName;

public class PromptTechniques2_bonus {
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
              Our e-commerce store saw a 25% drop in sales last month. The biggest losses came from our outdoor furniture line, which dropped 40%. Website analytics show slower page load times on product pages and a spike in abandoned carts. Customer feedback mentions frustration with checkout errors on mobile devices.
            
             Follow this process:
              1. Identify relevant details from the background.
              2. Explain how these details affect the task.
              3. Provide the final answer.
            
            Task:
            Recommend the top two actions to reverse the sales decline.
            """;

    public static void main(String[] args) {
        ChatModel cmodel = OpenAiChatModel.builder()
                .apiKey(System.getenv("OPENAI_API_KEY"))
                .modelName(OpenAiChatModelName.GPT_4_O)
                .build();

        System.out.println(cmodel.chat(example));
    }
}
