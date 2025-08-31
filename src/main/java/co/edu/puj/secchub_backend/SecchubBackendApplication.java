package co.edu.puj.secchub_backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;

/**
 * App entrypoint. Security disabled for dev/tests.
 */
@SpringBootApplication(exclude = { SecurityAutoConfiguration.class })
public class SecchubBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(SecchubBackendApplication.class, args);
    }
}
