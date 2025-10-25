package co.edu.puj.secchub_backend;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.testcontainers.junit.jupiter.Testcontainers;


@SpringBootTest
@Testcontainers
class SecchubBackendApplicationTests extends DatabaseIntegrationTest {

	@Test
	@DisplayName("Context loads successfully")
	void contextLoads() {
		// Test will fail if the application context cannot start
	}
}
