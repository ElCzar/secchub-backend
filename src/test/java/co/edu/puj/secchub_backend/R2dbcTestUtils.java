package co.edu.puj.secchub_backend;

import io.r2dbc.spi.ConnectionFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.r2dbc.connection.init.ScriptUtils;
import reactor.core.publisher.Mono;

public class R2dbcTestUtils {

    public static void executeScript(ConnectionFactory connectionFactory, String scriptPath) {
        Mono.from(connectionFactory.create())
            .flatMap(connection -> 
                ScriptUtils.executeSqlScript(connection, new ClassPathResource(scriptPath))
                    .then(Mono.from(connection.close()))
            )
            .block();
    }

    public static void executeScripts(ConnectionFactory connectionFactory, String... scriptPaths) {
        for (String scriptPath : scriptPaths) {
            executeScript(connectionFactory, scriptPath);
        }
    }
}