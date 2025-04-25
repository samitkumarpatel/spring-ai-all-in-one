package net.samitkumar.spring_ai_all_in_one;

import org.springframework.boot.SpringApplication;

public class TestSpringAiAllInOneApplication {

	public static void main(String[] args) {
		SpringApplication.from(SpringAiAllInOneApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}
