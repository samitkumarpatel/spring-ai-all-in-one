package net.samitkumar.spring_ai_all_in_one;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.AbstractChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.PromptChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.memory.InMemoryChatMemory;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@SpringBootApplication
public class SpringAiAllInOneApplication {

	public static void main(String[] args) {
		SpringApplication.run(SpringAiAllInOneApplication.class, args);
	}

	@Bean
	Map<String, PromptChatMemoryAdvisor> promptStore() {
		return new ConcurrentHashMap<>();
	}

	@Bean
	ChatClient chatClient(ChatClient.Builder builder, BookingTool bookingTool) {
		return builder
				.defaultSystem("""
						You are a customer chat support agent of an airline named "Funnair". Respond in a friendly,
						helpful, and joyful manner.

						Before providing information about a booking or cancelling a booking, you MUST always
						get the following information from the user: booking number, customer first name and last name.

						Before changing a booking you MUST ensure it is permitted by the terms.

						If there is a charge for the change, you MUST ask the user to consent before proceeding.
						""")
				.defaultAdvisors(
						new MessageChatMemoryAdvisor(new InMemoryChatMemory()),
						new SimpleLoggerAdvisor()
				)
				.defaultTools(bookingTool)
				.build();
	}

	@Bean
	ChatClient calenderChatClient(ChatClient.Builder builder) {
		return builder.build();
	}

	@Bean
	ChatClient hrChatClient(ChatClient.Builder builder) {
		return builder
				.defaultSystem("""
						You are a HR chat support agent of an IT company called "JSONPLACEHOLDER LLC". Respond in a friendly,
						helpful, and joyful manner.

						Before providing information about a employee get the following information from the user: employee id.
						""")
				.build();
	}
}

@RequiredArgsConstructor
@Controller
@Slf4j
class PromptController {
	private final ChatClient chatClient;
	private final ChatClient calenderChatClient;
	private final ChatClient hrChatClient;
	private final Map<String, PromptChatMemoryAdvisor> promptStore;

	@GetMapping("/{id}/booking-agent")
	@ResponseBody
	public String getPromptResponse(@PathVariable String id, @RequestParam String prompt) {
		log.info("##prompt: {}", prompt);
		return chatClient
				.prompt()
				.user(prompt)
				.advisors(advisorSpec -> advisorSpec
						.param(AbstractChatMemoryAdvisor.CHAT_MEMORY_CONVERSATION_ID_KEY, id)
						.param(AbstractChatMemoryAdvisor.CHAT_MEMORY_RETRIEVE_SIZE_KEY, 100))
				.call()
				.content();
	}

	@GetMapping("/{id}/calender-agent")
	@ResponseBody
	public String getDateTimeResponse(@PathVariable String id, @RequestParam("prompt") String prompt) {
		log.info("##datetime-agent");
		return calenderChatClient
				.prompt()
				.user(prompt)
				.advisors(advisorSpec -> advisorSpec
						.param(AbstractChatMemoryAdvisor.CHAT_MEMORY_CONVERSATION_ID_KEY, id)
						.param(AbstractChatMemoryAdvisor.CHAT_MEMORY_RETRIEVE_SIZE_KEY, 100))
				.tools(new DateTimeTools())
				.call()
				.content();
	}

	@GetMapping("/{id}/hr-agent")
	@ResponseBody
	public String getHrResponse(@PathVariable String id, @RequestParam("prompt") String prompt) {
		log.info("##hr-agent");
		return hrChatClient
				.prompt()
				.user(prompt)
				.advisors(advisorSpec -> advisorSpec
						.param(AbstractChatMemoryAdvisor.CHAT_MEMORY_CONVERSATION_ID_KEY, id)
						.param(AbstractChatMemoryAdvisor.CHAT_MEMORY_RETRIEVE_SIZE_KEY, 100))
				.tools(new HrRecordTool())
				.call()
				.content();
	}
}


class DateTimeTools {

	@Tool(description = "Get the current date and time in the user's timezone")
	String getCurrentDateTime() {
		return LocalDateTime.now().atZone(LocaleContextHolder.getTimeZone().toZoneId()).toString();
	}

	@Tool(description = "Get the tomorrow date and time in the user's timezone")
	String getTomorrowDate() {
		return LocalDateTime.now().plusDays(1).atZone(LocaleContextHolder.getTimeZone().toZoneId()).toString();
	}

}

@Component
@Slf4j
class BookingTool {

	@Tool(description = "Get booking details using booking number")
	BookingDetails getBookingDetails(
			@ToolParam(description = "booking number") String bookingNumber,
			@ToolParam(description = "customer first name") String firstName,
			@ToolParam(description = "customer last name") String lastName) {
		log.info("##Get booking details: {}, {}, {}", bookingNumber, firstName, lastName);
		return new BookingDetails("CONFIRMED", "Copenhagen", "New York", "2024-05-10");

	}

	@Tool(description = "Change a flight booking if allowed")
	BookingChangeResponse changeBooking(@ToolParam(description = "booking number") String bookingNumber,
										@ToolParam(description = "customer first name") String firstName,
										@ToolParam(description = "customer last name") String lastName) {
		log.info("##Change booking: {}, {}, {}", bookingNumber, firstName, lastName);
		return new BookingChangeResponse("SUCCESS", "Flight changed to 2024-05-12");
	}

	@Tool(description = "Cancel an existing flight booking")
	CancelResponse cancelBooking(@ToolParam(description = "booking number") String bookingNumber,
								 @ToolParam(description = "customer first name") String firstName,
								 @ToolParam(description = "customer last name") String lastName) {
		return new CancelResponse("CANCELLED", "Your flight has been cancelled.");
	}

}

record BookingDetails(String status, String from, String to, String date) {}
record BookingChangeResponse(String status, String message) {}
record CancelResponse(String status, String message) {}

class HrRecordTool {
	@Tool(description = "Get employee details using employee id")
	EmployeeDetails getEmployeeDetails(@ToolParam(description = "employee id") String employeeId){
		return new EmployeeDetails(
				"Samit",
				"Software Engineer",
				"IT",
				LocalDate.of(2022, 1, 1),
				"New York",
				"skp@jsonplaceholder.net",
				"+1 222 12122"
		);
	}
}
record EmployeeDetails(String name, String position, String department, LocalDate joiningDate, String location, String email, String phoneNumber) {}