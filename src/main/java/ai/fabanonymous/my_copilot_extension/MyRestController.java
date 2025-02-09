package ai.fabanonymous.my_copilot_extension;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.List;

/**
 * See Dan Vega on YouTube: Java + RAG: Create an AI-Powered Financial Advisor using Spring AI,
 * at https://www.youtube.com/watch?v=6Pgmr7xMjiY
 * Github repo: https://github.com/danvega/java-rag
 *
 * Payload example sent from Copilot:
 *{
 *   "copilot_thread_id": "afb7554a-1fa2-41d2-b648-bd69fe6ddb7c",
 *   "messages": [
 *     {
 *       "role": "user",
 *       "content": "@volkiagent say hello",
 *       "copilot_references": [
 *         {
 *           "type": "github.repository",
 *           "data": {
 *             "type": "repository",
 *             "id": 929733157,
 *             "name": "my-copilot-extension",
 *             "ownerLogin": "fabanonymous",
 *             "ownerType": "User",
 *             "readmePath": "README.md",
 *             "description": "",
 *             "commitOID": "6fd2d1e62518a1605c1debc5cd23db7692c61167",
 *             "ref": "refs/heads/main",
 *             "refInfo": {
 *               "name": "main",
 *               "type": "branch"
 *             },
 *             "visibility": "public",
 *             "languages": [
 *               {
 *                 "name": "Java",
 *                 "percent": 100
 *               }
 *             ]
 *           },
 *           "id": "fabanonymous/my-copilot-extension",
 *           "is_implicit": false,
 *           "metadata": {
 *             "display_name": "fabanonymous/my-copilot-extension",
 *             "display_icon": "",
 *             "display_url": ""
 *           }
 *         }
 *       ],
 *       "copilot_confirmations": null
 *     },
 *     {
 *       "role": "assistant",
 *       "content": "Ahoy there, @fabanonymous! 'Tis I, Blackbeard, the fearsome pirate. How can this ol' seafarin' soul be of assistance to ye today?",
 *       "copilot_references": [],
 *       "copilot_confirmations": null
 *     },
 *     {
 *       "role": "user",
 *       "content": "Current Date and Time (UTC - YYYY-MM-DD HH:MM:SS formatted): 2025-02-09 09:51:05\nCurrent User's Login: fabanonymous\n",
 *       "name": "_session",
 *       "copilot_references": [
 *         {
 *           "type": "github.redacted",
 *           "data": {
 *             "type": "github.current-url"
 *           },
 *           "id": "aa578019-18cd-4de2-9e9d-6d59a61a325a",
 *           "is_implicit": true,
 *           "metadata": {
 *             "display_name": "",
 *             "display_icon": "",
 *             "display_url": ""
 *           }
 *         }
 *       ],
 *       "copilot_confirmations": null
 *     },
 *     {
 *       "role": "user",
 *       "content": "",
 *       "copilot_references": [
 *         {
 *           "type": "github.repository",
 *           "data": {
 *             "type": "repository",
 *             "id": 929733157,
 *             "name": "my-copilot-extension",
 *             "ownerLogin": "fabanonymous",
 *             "ownerType": "User",
 *             "readmePath": "README.md",
 *             "description": "",
 *             "commitOID": "6fd2d1e62518a1605c1debc5cd23db7692c61167",
 *             "ref": "refs/heads/main",
 *             "refInfo": {
 *               "name": "main",
 *               "type": "branch"
 *             },
 *             "visibility": "public",
 *             "languages": [
 *               {
 *                 "name": "Java",
 *                 "percent": 100
 *               }
 *             ]
 *           },
 *           "id": "fabanonymous/my-copilot-extension",
 *           "is_implicit": false,
 *           "metadata": {
 *             "display_name": "fabanonymous/my-copilot-extension",
 *             "display_icon": "",
 *             "display_url": ""
 *           }
 *         }
 *       ],
 *       "copilot_confirmations": null
 *     },
 *     {
 *       "role": "user",
 *       "content": "tell me joke",
 *       "copilot_references": [],
 *       "copilot_confirmations": []
 *     }
 *   ],
 *   "stop": null,
 *   "top_p": 0,
 *   "temperature": 0,
 *   "max_tokens": 0,
 *   "presence_penalty": 0,
 *   "frequency_penalty": 0,
 *   "response_format": null,
 *   "copilot_skills": null,
 *   "agent": "volkiagent",
 *   "client_id": "Iv23ctUoN0llmDvBmYjC",
 *   "tools": [],
 *   "functions": null,
 *   "model": ""
 * }
 */
@RestController
public class MyRestController {

    private static final Logger log = LoggerFactory.getLogger(MyRestController.class);

    private final ChatClient chatClient;
    private final ObjectMapper objectMapper;
    private RestTemplate restTemplate;

    public MyRestController(ChatClient.Builder builder, ObjectMapper objectMapper, RestTemplate restTemplate) {
        this.chatClient = builder
                .defaultSystem("You are a helpful assistant that replies to user messages as if you were the Blackbeard Pirate.")
                .build();
        this.objectMapper = objectMapper;
        this.restTemplate = restTemplate;
    }

    @GetMapping("/")
    public String welcome() {
        return "Welcome to My CoPilot Extension!";
    }

    @PostMapping(value = "/", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public String chat(@RequestBody String bodyAsString, HttpServletRequest httpRequest) throws JsonProcessingException {
        log.info("Payload as String is {}", bodyAsString);

        CopilotRequestBody bodyAsJSON = objectMapper.readValue(bodyAsString, CopilotRequestBody.class);
        log.info("Payload as JSON is {}", bodyAsJSON);

        // Identify the user, using the GitHub API token provided in the request headers.
        String tokenForUser = httpRequest.getHeader("X-GitHub-Token");
        log.info("Token for User is {}", tokenForUser);

        List<CopilotMessage> messages = bodyAsJSON.messages();
        messages.add(new CopilotMessage(
                "system",
                "You are a helpful assistant that replies to user messages as if you were the Blackbeard Pirate."
        ));
        //messages.add(new CopilotMessage(
        //        "system",
        //        "Start every response with the user's name, which is @${user.data.login}"
        //));

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(tokenForUser);
        headers.setContentType(MediaType.APPLICATION_JSON);
        RequestToCopilot requestToCopilot = new RequestToCopilot(messages, false);
        HttpEntity<RequestToCopilot> requestToCopilotAsEntity = new HttpEntity<>(requestToCopilot, headers);
        ResponseEntity<String /*JsonNode*/> responseFromCopilotAsEntity = restTemplate.exchange(
                "https://api.githubcopilot.com/chat/completions",
                HttpMethod.POST,
                requestToCopilotAsEntity,
                String.class /*JsonNode.class*/);
        String /*JsonNode*/ responseFromCopilot = responseFromCopilotAsEntity.getBody();
        log.info("Response from Copilot is {}", responseFromCopilot);
        return responseFromCopilot;
    }

    // http :8081/test/chat message=="Say Hello"
    @GetMapping("/test/chat")
    public String testChatLikePirate(@RequestParam(value = "message", defaultValue = "Say Hello") String message) {
        return chatClient.prompt()
                .user(message)
                .call()
                .content();
    }

    record RequestToCopilot(List<CopilotMessage> messages, boolean stream) {}

    record CopilotRequestBody(
            String copilot_thread_id,
            List<CopilotMessage> messages
            ) {}
    record CopilotMessage(
            String role,
            String content,
            String name,    // for _session
            List<CopilotReferences> copilot_references,
            List<CopilotConfirmations> copilot_confirmations,
            String stop,
            int top_p,
            int temperature,
            int max_tokens,
            int presence_penalty,
            int frequency_penalty,
            String response_format,
            List<CopilotSkills> copilot_skills,
            String agent,
            String client_id,
            List<CopilotTools> tools,
            List<CopilotFunctions> functions,
            String model
            ) {
        public CopilotMessage(String role, String content) {
            this(role, content, null, null, null, null, 0, 0, 0, 0, 0, null, null, null, null, null, null, null);
        }
    }
    record CopilotReferences(
            String type,
            CopilotData data,
            String id,
            boolean is_implicit,
            CopilotMetadata metadata
            ) {}
    record CopilotConfirmations(
            String type,
            CopilotData data,
            String id,
            boolean is_implicit,
            CopilotMetadata metadata
            ) {}
    record CopilotSkills(
            ) {}
    record CopilotTools(
            ) {}
    record CopilotFunctions(
            ) {}
    record CopilotData(
            String type
            ) {}
    record CopilotMetadata(
            String display_name,
            String display_icon,
            String display_url
            ) {}
}
