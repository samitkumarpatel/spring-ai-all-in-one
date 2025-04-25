package net.samitkumar.spring_ai_all_in_one;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.AbstractChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.PromptChatMemoryAdvisor;
import org.springframework.ai.chat.memory.InMemoryChatMemory;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@SpringBootApplication
public class SpringAiAllInOneApplication {

	public static void main(String[] args) {
		SpringApplication.run(SpringAiAllInOneApplication.class, args);
	}

	@Bean
	ChatClient chatClient(ChatClient.Builder builder, BookingTool bookingTool) {
		return builder
				.defaultSystem("""
							You are a customer chat support agent of an airline named "Funnair". Respond in a friendly,
							helpful, and joyful manner.

							Before providing information about a booking or cancelling a booking, you MUST always
							get the following information from the user: booking number.

							Before changing a booking you MUST ensure it is permitted by the terms.

							If there is a charge for the change, you MUST ask the user to consent before proceeding.
						""")
				.defaultAdvisors(
						new MessageChatMemoryAdvisor(new InMemoryChatMemory())
				)
				.defaultTools(bookingTool)
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

	@GetMapping("{userId}/ai")
	@ResponseBody
	public String getPromptResponse(@PathVariable("userId")String userId, @RequestParam String prompt) {
		log.info("### prompt: userId={} prompt={}", userId, prompt);

		//var advisor = chatMemoryStorage.computeIfAbsent(userId, k -> new PromptChatMemoryAdvisor(new InMemoryChatMemory()));

		return chatClient
				.prompt()
				.user(prompt)
				/*.advisors(a -> a
						.advisors(advisor)
						.param(AbstractChatMemoryAdvisor.CHAT_MEMORY_CONVERSATION_ID_KEY, userId)
						.param(AbstractChatMemoryAdvisor.CHAT_MEMORY_RETRIEVE_SIZE_KEY, 100)
				)*/
				.call()
				.content();
	}
}

@Component
@Slf4j
class BookingTool {

	@Tool(description = "Get booking details")
	BookingDetails getBookingDetails(@ToolParam String bookingNumber) {
		if (bookingNumber.isEmpty()) {
			throw new InvalidBookingNumberException(bookingNumber);
		} else if (bookingNumber.equalsIgnoreCase("abc123")) {
			return new BookingDetails("CONFIRMED", "Copenhagen", "New York", "2024-05-10");
		} else {
			throw new BookingNotFoundException(bookingNumber);
		}
	}
}

record BookingDetails(String status, String from, String to, String date) {}

@ResponseStatus(HttpStatus.NOT_FOUND)
class BookingNotFoundException extends RuntimeException {
	BookingNotFoundException(String bookingNumber) {
		super("Booking not found: " + bookingNumber);
	}
}

@ResponseStatus(HttpStatus.BAD_REQUEST)
class InvalidBookingNumberException extends RuntimeException {
	InvalidBookingNumberException(String bookingNumber) {
		super("Invalid booking number: " + bookingNumber);
	}
}