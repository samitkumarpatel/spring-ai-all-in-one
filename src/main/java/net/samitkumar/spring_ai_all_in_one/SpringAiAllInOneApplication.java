package net.samitkumar.spring_ai_all_in_one;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.AbstractChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.PromptChatMemoryAdvisor;
import org.springframework.ai.chat.memory.InMemoryChatMemory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import reactor.core.publisher.Flux;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@SpringBootApplication
public class SpringAiAllInOneApplication {

	public static void main(String[] args) {
		SpringApplication.run(SpringAiAllInOneApplication.class, args);
	}

	@Bean
	ChatClient chatClient(ChatClient.Builder builder) {
		return builder
				.defaultSystem("""
							You are a friendly chat bot that answers question in the voice of a {voice}
						""")
				.build();
	}

	@Bean
	Map<String, PromptChatMemoryAdvisor> chatMemoryStorage() {
		return new ConcurrentHashMap<>();
	}

}

@RequiredArgsConstructor
@Controller
@Slf4j
class PromptController {
	private final ChatClient chatClient;
	private final Map<String, PromptChatMemoryAdvisor> chatMemoryStorage;

	//http :8080/ai prompt=="hi"
	@GetMapping("/ai")
	@ResponseBody
	public String getPromptResponse(@RequestParam String prompt) {
		log.info("### prompt: {}", prompt);
		return chatClient
				.prompt()
				.user(prompt)
				.call()
				.content();
	}

	//http :8080/ai/stream prompt=="hi"
	@GetMapping(value = "/ai/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
	@ResponseBody
	public Flux<String> getPromptResponseInStream(@RequestParam String prompt) {
		log.info("### prompt for stream: {}", prompt);
		return chatClient
				.prompt()
				.user(prompt)
				.stream()
				.content();
	}


	@GetMapping("{userId}/ai")
	@ResponseBody
	public String getPromptResponseV2(@PathVariable("userId")String userId, @RequestParam String prompt, @RequestParam(defaultValue = "Normal man") String voice) {
		log.info("### prompt: userId={} voice={} prompt={}", userId, voice, prompt);

		var advisor = chatMemoryStorage.computeIfAbsent(userId, k -> new PromptChatMemoryAdvisor(new InMemoryChatMemory()));

		return chatClient
				.prompt()
				//default system message
				.system(sp -> sp.param("voice", voice))
				.user(prompt)
				// advisors to remember the conversation for interactions. We have used here InMemoryChatMemory but there are other options too. like RedisChatMemory, MongoChatMemory etc.
				// For more info:: https://docs.spring.io/spring-ai/reference/api/chatclient.html#_advisors & https://docs.spring.io/spring-ai/reference/api/chatclient.html#_chat_memory
				.advisors(a -> a
						.advisors(advisor)
						.param(AbstractChatMemoryAdvisor.CHAT_MEMORY_CONVERSATION_ID_KEY, userId)
						.param(AbstractChatMemoryAdvisor.CHAT_MEMORY_RETRIEVE_SIZE_KEY, 100)
				)
				.call()
				.content();
	}
}