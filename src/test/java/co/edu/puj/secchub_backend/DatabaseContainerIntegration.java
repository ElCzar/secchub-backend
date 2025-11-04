package co.edu.puj.secchub_backend;

import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.mysql.MySQLContainer;

@SuppressWarnings("resource")
public abstract class DatabaseContainerIntegration {

    private static final MySQLContainer MYSQL_CONTAINER;

    static {
        MYSQL_CONTAINER = new MySQLContainer("mysql:8.4.6")
                .withInitScripts("schema.sql", "init-parameters.sql")
                .withReuse(true);
        MYSQL_CONTAINER.start();
    }

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.r2dbc.url", () -> MYSQL_CONTAINER.getJdbcUrl().replace("jdbc:", "r2dbc:"));
        registry.add("spring.r2dbc.username", MYSQL_CONTAINER::getUsername);
        registry.add("spring.r2dbc.password", MYSQL_CONTAINER::getPassword);
    }
}
