package co.edu.puj.secchub_backend;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import org.springframework.r2dbc.core.DatabaseClient;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Utility class to execute SQL script files using R2DBC DatabaseClient.
 * Supports multiple statements separated by semicolons.
 */
public class SqlScriptExecutor {

    private final DatabaseClient databaseClient;

    public SqlScriptExecutor(DatabaseClient databaseClient) {
        this.databaseClient = databaseClient;
    }

    /**
     * Executes SQL statements from a classpath resource file.
     * 
     * @param scriptPath Path to the SQL script file (e.g., "/test-users.sql")
     */
    public void executeSqlScript(String scriptPath) {
        List<String> statements = readSqlStatements(scriptPath);
        
        Flux.fromIterable(statements)
            .filter(sql -> !sql.trim().isEmpty())
            .flatMap(sql -> databaseClient.sql(sql)
                .fetch()
                .rowsUpdated()
                .onErrorResume(e -> {
                    System.err.println("Error executing SQL: " + sql);
                    System.err.println("Error: " + e.getMessage());
                    return Mono.empty();
                }))
            .blockLast();
    }

    /**
     * Reads SQL statements from a classpath resource file.
     * Splits statements by semicolon and handles basic comment removal.
     */
    private List<String> readSqlStatements(String scriptPath) {
        List<String> statements = new ArrayList<>();
        StringBuilder currentStatement = new StringBuilder();

        try (InputStream is = getClass().getResourceAsStream(scriptPath);
            BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
            
            String line;
            while ((line = reader.readLine()) != null) {
                // Skip comment lines and empty lines
                String trimmedLine = line.trim();
                if (trimmedLine.isEmpty() || trimmedLine.startsWith("--") || trimmedLine.startsWith("#")) {
                    continue;
                }
                
                // Remove inline comments
                int commentIndex = trimmedLine.indexOf("--");
                if (commentIndex > 0) {
                    trimmedLine = trimmedLine.substring(0, commentIndex).trim();
                }
                
                currentStatement.append(trimmedLine).append(" ");
                
                // Check if statement is complete (ends with semicolon)
                if (trimmedLine.endsWith(";")) {
                    String statement = currentStatement.toString().trim();
                    // Remove the trailing semicolon as R2DBC doesn't need it
                    if (statement.endsWith(";")) {
                        statement = statement.substring(0, statement.length() - 1).trim();
                    }
                    statements.add(statement);
                    currentStatement.setLength(0);
                }
            }
            
            // Add any remaining statement
            if (!currentStatement.isEmpty()) {
                String statement = currentStatement.toString().trim();
                if (statement.endsWith(";")) {
                    statement = statement.substring(0, statement.length() - 1).trim();
                }
                if (!statement.isEmpty()) {
                    statements.add(statement);
                }
            }
            
        } catch (IOException e) {
            throw new RuntimeException("Failed to read SQL script: " + scriptPath, e);
        }

        return statements;
    }
}
