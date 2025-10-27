package co.edu.puj.secchub_backend;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.modulith.core.ApplicationModules;
import org.springframework.modulith.docs.Documenter;
import org.testcontainers.junit.jupiter.Testcontainers;


@SpringBootTest
@Testcontainers
class SecchubBackendApplicationTests extends DatabaseContainerIntegration {
	ApplicationModules modules = ApplicationModules.of(SecchubBackendApplication.class);

	@Test
	@DisplayName("Context loads successfully")
	void contextLoads() {
		assertTrue(true);
	}

	@Test
    void writeModulithDocumentation(ApplicationContext context) {
        new Documenter(modules)
            .writeModulesAsPlantUml()           // Generates PlantUML diagrams
            .writeIndividualModulesAsPlantUml() // Generates individual module diagrams
            .writeModuleCanvases();              // Generates module canvases
        
        assertTrue(true);
    }
}
