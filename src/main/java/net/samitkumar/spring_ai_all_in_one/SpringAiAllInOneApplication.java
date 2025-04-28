package net.samitkumar.spring_ai_all_in_one;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.AbstractChatMemoryAdvisor;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.support.RestClientAdapter;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;


@SpringBootApplication
@Slf4j
public class SpringAiAllInOneApplication {

	public static void main(String[] args) {
		SpringApplication.run(SpringAiAllInOneApplication.class, args);
	}

	@Bean
	JsonPlaceHolderClient jsonPlaceHolderClient(RestClient.Builder restClientBuilder) {
		var restClient= restClientBuilder
				.baseUrl("https://jsonplaceholder.typicode.com")
				.requestInterceptor((request, body, next) -> {
					log.info("## {} {}", request.getMethod(), request.getURI().getPath());
					return next.execute(request, body);
				})
				.build();
		RestClientAdapter adapter = RestClientAdapter.create(restClient);
		HttpServiceProxyFactory factory = HttpServiceProxyFactory.builderFor(adapter).build();
		return factory.createClient(JsonPlaceHolderClient.class);
	}

	@Bean
	ChatClient chatClient(ChatClient.Builder builder) {
		return builder
				.defaultSystem("""
					  You are an HR admin chat support agent for an IT company called "JSONPLACEHOLDER LLC".
					  Always respond in a friendly, helpful, and joyful manner.

					  Your primary task is to provide information about users.

					  Instructions:
					  - Before sharing any user information, you must ask the user for user id.
					  - If the user provides a valid user id, respond by sharing the user's information in JSON format.
					  - If the user does not provide a user id, politely ask them to provide one.
					  - If the user provides an invalid user id, inform them that it is invalid and kindly ask them to provide a valid user id.
					  - If the user asks for any other information, politely inform them that you can only provide information about users.
				""")
				.build();
	}
}

@RequiredArgsConstructor
@Controller
@Slf4j
class PromptController {
	private final ChatClient chatClient;
	private final UserTool userTool;

	@GetMapping("/{id}/hr-agent")
	@ResponseBody
	public String getHrResponse(@PathVariable String id, @RequestParam("prompt") String prompt) {
		log.info("##hr-agent");
		return chatClient
				.prompt()
				.user(prompt)
				.advisors(advisorSpec -> advisorSpec
						.param(AbstractChatMemoryAdvisor.CHAT_MEMORY_CONVERSATION_ID_KEY, id)
						.param(AbstractChatMemoryAdvisor.CHAT_MEMORY_RETRIEVE_SIZE_KEY, 100))
				.tools(userTool)
				.call()
				.content();
	}
}

record Geo(String lat, String lng) {}
record Address(String street, String city, String suite, String zipcode, Geo geo) {}
record Company(String name, String catchPhrase, String bs) {}
record User(int id, String name, String username, String email, String phone, String website,
			 Address address, Company company) {}

@HttpExchange(url = "/users", accept = MediaType.APPLICATION_JSON_VALUE)
interface JsonPlaceHolderClient {

	@GetExchange("/{id}")
	User getUser(@PathVariable String id);
}

@Component
@RequiredArgsConstructor
class UserTool {
	private final JsonPlaceHolderClient jsonPlaceHolderClient;

	@Tool(description = "Get user details by user id", name = "getUser")
	public User getUser(@ToolParam(description = "user id") String id) {
		return jsonPlaceHolderClient.getUser(id);
	}
}
