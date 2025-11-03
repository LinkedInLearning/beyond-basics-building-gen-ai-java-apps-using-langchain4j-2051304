import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.model.openai.OpenAiChatModelName;

public class PromptTechniques1 {
    /*
       You are [role/expertise level].
       Background: [insert relevant background data]
       Task: [description of user request ]
       Constraints: [style, tone, output format, etc.]
    */
    final static String example = """
            You are a senior legal analyst.
            Background:
            Stambovsky v. Ackley (N.Y. App. Div. 1991) is the famous “Ghostbusters case,” where the court ruled that if a seller has publicly promoted a house as haunted, then for purposes of a real estate dispute, the house is legally haunted. Helen Ackley had repeatedly told national and local media that her Nyack, New York home was occupied by friendly poltergeists and even put it on a local “haunted house” tour. Jeffrey Stambovsky signed a contract to buy the house for $650,000, paid a $32,500 deposit, and then—after hearing Ackley’s ghost stories firsthand—sued to rescind the purchase and get his deposit back, arguing fraudulent misrepresentation.
            
            The appellate court held that while New York followed “caveat emptor” (buyer beware) and the seller and broker had no duty to disclose paranormal activity as a factual defect, equity still allowed rescission. The reason: the haunting claim, which Ackley herself had widely publicized, materially affected the property’s value and reputation in a way a normal inspection could never reveal. The court wrote that Ackley was “estopped to deny” the ghosts and, “as a matter of law, the house is haunted,” so forcing the sale would be unfair. However, the court refused to award money damages for fraud.
            
            One judge dissented, saying caveat emptor should control and courts shouldn’t undo real estate contracts over ghosts. After the ruling, the case became a staple in property and contract law courses, both for its narrow exception to caveat emptor and for its playful language (“plaintiff hasn’t a ghost of a chance,” etc.). Practically, the decision let Stambovsky walk away from the deal without forfeiting the deposit and established that when a seller creates and markets a stigmatizing condition—like “this house is haunted”—they can’t hide from it later.
            
            Task: Summarize the key contractual risks.
            Constraints: Use bullet points and avoid legal jargon.
            """;

    public static void main(String[] args) {
        ChatModel cmodel = OpenAiChatModel.builder()
                .apiKey(System.getenv("OPENAI_API_KEY"))
                .modelName(OpenAiChatModelName.GPT_4_O)
                .build();

        System.out.println(cmodel.chat(example));
    }
}
