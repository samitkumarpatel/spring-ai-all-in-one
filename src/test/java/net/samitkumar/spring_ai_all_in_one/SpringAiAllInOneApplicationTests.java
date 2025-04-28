package net.samitkumar.spring_ai_all_in_one;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

@Import(TestcontainersConfiguration.class)
@SpringBootTest
class SpringAiAllInOneApplicationTests {

	@Test
	void contextLoads() {
	}

	@Test
	void jsonPlaceHolderClientTest(@Autowired JsonPlaceHolderClient jsonPlaceHolderClient) {
		System.out.println(jsonPlaceHolderClient.getUser("1"));
	}

}
