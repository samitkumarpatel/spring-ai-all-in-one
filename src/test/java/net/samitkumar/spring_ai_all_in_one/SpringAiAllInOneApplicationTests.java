package net.samitkumar.spring_ai_all_in_one;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

@Import(TestcontainersConfiguration.class)
@SpringBootTest
class SpringAiAllInOneApplicationTests {

	@Test
	void contextLoads() {
	}

}
