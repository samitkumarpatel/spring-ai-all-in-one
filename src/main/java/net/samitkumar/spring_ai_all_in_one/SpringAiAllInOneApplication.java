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
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.reactive.HandlerMapping;
import org.springframework.web.reactive.handler.SimpleUrlHandlerMapping;
import org.springframework.web.reactive.socket.WebSocketHandler;
import org.springframework.web.reactive.socket.WebSocketMessage;
import org.springframework.web.reactive.socket.WebSocketSession;
import org.springframework.web.reactive.socket.server.support.WebSocketHandlerAdapter;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;


@SpringBootApplication
@Slf4j
public class SpringAiAllInOneApplication {

	public static void main(String[] args) {
		SpringApplication.run(SpringAiAllInOneApplication.class, args);
	}

	@Bean
	Map<String, PromptChatMemoryAdvisor> promptStorage() {
		return new ConcurrentHashMap<>();
	}

	@Bean
	ChatClient chatClient(ChatClient.Builder builder) {
		return builder
				.build();
	}
}

@RequiredArgsConstructor
@Controller
@Slf4j
class PromptController {
	private final ChatClient chatClient;
	private final Map<String, PromptChatMemoryAdvisor> promptStorage;

	@GetMapping(value = "/{id}/chat/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
	@ResponseBody
	public Flux<String> getHrResponse(@PathVariable String id, @RequestParam("prompt") String prompt) {
		log.info("##streaming##");
		var chatMemory = promptStorage.computeIfAbsent(id, k -> new PromptChatMemoryAdvisor(new InMemoryChatMemory()));
		return chatClient
				.prompt()
				.user(prompt)
				.advisors(advisorSpec -> advisorSpec
						.advisors(chatMemory)
						.param(AbstractChatMemoryAdvisor.CHAT_MEMORY_CONVERSATION_ID_KEY, id)
						.param(AbstractChatMemoryAdvisor.CHAT_MEMORY_RETRIEVE_SIZE_KEY, 100))
				.stream()
				.content();
	}
}

@Component
@RequiredArgsConstructor
@Slf4j
class BotWebSocketHandler implements WebSocketHandler {
	private final ChatClient chatClient;
	private final Map<String, PromptChatMemoryAdvisor> promptStorage;

	@Override
	public Mono<Void> handle(WebSocketSession session) {
		var chatMemory = promptStorage.computeIfAbsent(session.getId(), k -> new PromptChatMemoryAdvisor(new InMemoryChatMemory()));
		//echo the message back to the client
		return session.send(
				session.receive()
						.map(WebSocketMessage::getPayloadAsText)
						.flatMap(message -> {
							log.info("##websocket##");
							return chatClient
									.prompt()
									.user(message)
									.advisors(advisorSpec -> advisorSpec
											.advisors(chatMemory)
											.param(AbstractChatMemoryAdvisor.CHAT_MEMORY_CONVERSATION_ID_KEY, session.getId())
											.param(AbstractChatMemoryAdvisor.CHAT_MEMORY_RETRIEVE_SIZE_KEY, 100))
									.stream()
									.content()
									.map(session::textMessage);
						})
						.delayElements(Duration.ofMillis(100))
		);

	}
}

@Configuration
@RequiredArgsConstructor
class WebSocketConfiguration {
	private final BotWebSocketHandler botWebSocketHandler;
	@Bean
	public HandlerMapping handlerMapping() {
		Map<String, WebSocketHandler> map = new HashMap<>();
		map.put("/ws/bot", botWebSocketHandler);
		int order = -1; // before annotated controllers

		return new SimpleUrlHandlerMapping(map, order);
	}
}