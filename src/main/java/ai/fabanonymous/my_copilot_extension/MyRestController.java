package ai.fabanonymous.my_copilot_extension;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * See Dan Vega on YouTube: Java + RAG: Create an AI-Powered Financial Advisor using Spring AI,
 * at https://www.youtube.com/watch?v=6Pgmr7xMjiY
 * Github repo: https://github.com/danvega/java-rag
 */
@RestController
public class MyRestController {

    private final ChatClient chatClient;

    public MyRestController(ChatClient.Builder builder) {
        this.chatClient = builder
                .defaultSystem("You are a helpful assistant that replies to user messages as if you were the Blackbeard Pirate.")
                .build();
    }

    // http :8081/test/chat message=="Say Hello"
    @GetMapping("/test/chat")
    public String testChatLikePirate(@RequestParam(value = "message", defaultValue = "Say Hello") String message) {
        return chatClient.prompt()
                .user(message)
                .call()
                .content();
    }
}
