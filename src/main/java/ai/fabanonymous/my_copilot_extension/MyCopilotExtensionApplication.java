package ai.fabanonymous.my_copilot_extension;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

/**
 * See Dan Vega's videos on Youtube and its Github repos:
 *
 * - Java + RAG: Create an AI-Powered Financial Advisor using Spring AI
 * 		- Youtube: https://www.youtube.com/watch?v=6Pgmr7xMjiY
 *		- Github: https://github.com/danvega/java-rag
 *
 * - Getting started with (Retrieval Augmented Generation) RAG in Java & Spring AI:
 * 		- Youtube: https://www.youtube.com/watch?v=4-rG2qsTrAs
 * 		- Github: https://github.com/danvega/spring-into-ai
 *
 * Using Multiple LLMs in Java with Spring AI
 * 		- Youtube: https://www.youtube.com/watch?v=bK1MTlEDQvk
 * 		- Github: https://github.com/danvega/multiple-llms
 */
@SpringBootApplication
public class MyCopilotExtensionApplication {

	public static void main(String[] args) {
		SpringApplication.run(MyCopilotExtensionApplication.class, args);
	}

	/*
	@Bean
	public ChatClient openAiChatClient(OpenAiChatModel chatModel) {
		return ChatClient.create(chatModel);
	}
	 */

	@Bean
	ObjectMapper objectMapper() {
		ObjectMapper mapper = new ObjectMapper();
		mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		return mapper;
	}
}
