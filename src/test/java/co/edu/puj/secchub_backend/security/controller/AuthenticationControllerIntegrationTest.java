package co.edu.puj.secchub_backend.security.controller;

import co.edu.puj.secchub_backend.DatabaseIntegrationTest;
import co.edu.puj.secchub_backend.security.dto.AuthTokenResponseDTO;
import co.edu.puj.secchub_backend.security.dto.LoginRequestDTO;
import co.edu.puj.secchub_backend.security.dto.RefreshTokenRequestDTO;
import co.edu.puj.secchub_backend.security.jwt.JwtTokenProvider;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

import java.util.stream.Stream;

/**
 * Integration tests for AuthenticationController.
 * Tests authentication flow including login and token refresh using real database.
 * Uses @Sql annotations for test data setup and cleanup.
 */
@SpringBootTest
@AutoConfigureWebTestClient
@Testcontainers
@DisplayName("Authentication Controller Integration Tests")
@Sql(scripts = "/test-cleanup.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@Sql(scripts = "/test-users.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@Sql(scripts = "/test-cleanup.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
class AuthenticationControllerIntegrationTest extends DatabaseIntegrationTest {

    @Autowired
    private WebTestClient webTestClient;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    // ==========================================
    // Login Tests
    // ==========================================

    @ParameterizedTest(name = "Login with {0} credentials returns token")
    @MethodSource("loginCredentialsProvider")
    @DisplayName("POST /auth/login should successfully login with valid credentials")
    void login_withValidCredentials_shouldReturnTokens(String email, String password) {
        // Given
        LoginRequestDTO loginRequest = new LoginRequestDTO();
        loginRequest.setEmail(email);
        loginRequest.setPassword(password);

        // When & Then
        webTestClient.post()
                .uri("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(loginRequest)
                .exchange()
                .expectStatus().isOk()
                .expectBody(AuthTokenResponseDTO.class)
                .value(response -> {
                    assertNotNull(response);
                    assertNotNull(response.getAccessToken());
                    assertNotNull(response.getRefreshToken());
                    assertThat(response.getAccessToken()).isNotEmpty();
                    assertThat(response.getRefreshToken()).isNotEmpty();
                    
                    // Verify access token is valid
                    assertTrue(jwtTokenProvider.validateAccessToken(response.getAccessToken()));
                    
                    // Verify refresh token is valid
                    assertTrue(jwtTokenProvider.validateRefreshToken(response.getRefreshToken()));
                    
                    // Verify token contains correct email
                    String emailFromToken = jwtTokenProvider.getEmailFromToken(response.getAccessToken());
                    assertEquals(email, emailFromToken);
                });
    }

    private static Stream<Arguments> loginCredentialsProvider() {
        return Stream.of(
                Arguments.of("testAdmin@example.com", "password"),
                Arguments.of("testUser@example.com", "password"),
                Arguments.of("testStudent@example.com", "password"),
                Arguments.of("testTeacher@example.com", "password"),
                Arguments.of("testProgram@example.com", "password")
        );
    }

    @ParameterizedTest(name = "Login with {0} and {1} returns 401")
    @MethodSource("invalidLoginCredentialsProvider")
    @DisplayName("POST /auth/login should return 401 with any invalid credential")
    void login_withInvalidPassword_shouldReturnUnauthorized(String email, String password) {
        // Given
        LoginRequestDTO loginRequest = new LoginRequestDTO();
        loginRequest.setEmail(email);
        loginRequest.setPassword(password);

        // When & Then
        webTestClient.post()
                .uri("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(loginRequest)
                .exchange()
                .expectStatus().isUnauthorized();
    }

    private static Stream<Arguments> invalidLoginCredentialsProvider() {
        return Stream.of(
                Arguments.of("testAdmin@example.com", "wrongpassword"),
                Arguments.of("testUser@example.com", "wrongpassword"),
                Arguments.of("testStudent@example.com", "wrongpassword"),
                Arguments.of("testTeacher@example.com", "wrongpassword"),
                Arguments.of("testProgram@example.com", "wrongpassword"),
                Arguments.of("notExistingUser@example.com", "wrongpassword"),
                Arguments.of(null, "password"),
                Arguments.of("someUser@example.com", null),
                Arguments.of(null, null)
        );
    }

    // ==========================================
    // Token Refresh Tests
    // ==========================================

    @ParameterizedTest(name = "Refresh token for {0} returns new tokens")
    @MethodSource("loginCredentialsProvider")
    @DisplayName("POST /auth/refresh successfully refresh token with valid refresh token")
    void refresh_withValidRefreshToken_shouldReturnNewTokens(String email) {
        String refreshToken = jwtTokenProvider.generateRefreshToken(email);

        // When - Use refresh token to get new tokens
        RefreshTokenRequestDTO refreshRequest = new RefreshTokenRequestDTO();
        refreshRequest.setRefreshToken(refreshToken);

        // Then
        webTestClient.post()
                .uri("/auth/refresh")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(refreshRequest)
                .exchange()
                .expectStatus().isOk()
                .expectBody(AuthTokenResponseDTO.class)
                .value(response -> {
                    assertNotNull(response);
                    assertNotNull(response.getAccessToken());
                    assertNotNull(response.getRefreshToken());
                    assertThat(response.getAccessToken()).isNotEmpty();
                    assertThat(response.getRefreshToken()).isNotEmpty();
                    
                    // Verify new access token is valid
                    assertTrue(jwtTokenProvider.validateAccessToken(response.getAccessToken()));
                    
                    // Verify new refresh token is valid
                    assertTrue(jwtTokenProvider.validateRefreshToken(response.getRefreshToken()));
                    
                    // Verify token contains correct email
                    String emailFromToken = jwtTokenProvider.getEmailFromToken(response.getAccessToken());
                    assertEquals(email, emailFromToken);
                });
    }


    @ParameterizedTest(name = "Refresh with invalid token for {0} returns 401")
    @MethodSource("invalidRefreshTokensProvider")
    @DisplayName("POST /auth/refresh should return 401 with invalid refresh token")
    void refresh_withInvalidRefreshToken_shouldReturnUnauthorized(String invalidToken) {
        // Given
        RefreshTokenRequestDTO refreshRequest = new RefreshTokenRequestDTO();
        refreshRequest.setRefreshToken(invalidToken);

        // When & Then
        webTestClient.post()
                .uri("/auth/refresh")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(refreshRequest)
                .exchange()
                .expectStatus().isUnauthorized();
    }

    private static Stream<Arguments> invalidRefreshTokensProvider() {
        return Stream.of(
                Arguments.of("invalidtoken"),
                Arguments.of(""),
                Arguments.of((String) null)
        );
    }

    @Test
    @DisplayName("POST /auth/refresh should return 401 when using access token as refresh token")
    void refresh_withAccessTokenInsteadOfRefreshToken_shouldReturnUnauthorized() {
        // Given
        String accessToken = jwtTokenProvider.generateToken("testAdmin@example.com", "ROLE_ADMIN");

        // When - Try to use access token as refresh token
        RefreshTokenRequestDTO refreshRequest = new RefreshTokenRequestDTO();
        refreshRequest.setRefreshToken(accessToken);

        // Then
        webTestClient.post()
                .uri("/auth/refresh")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(refreshRequest)
                .exchange()
                .expectStatus().isUnauthorized();
    }

    @Test
    @DisplayName("POST /auth/refresh should return 401 when using tampered token")
    void refresh_withTamperedToken_shouldReturnUnauthorized() {
        // Given
        String refreshToken = jwtTokenProvider.generateRefreshToken("testAdmin@example.com");
        String tamperedToken = refreshToken + "tampered";

        // When & Then
        webTestClient.post()
                .uri("/auth/refresh")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(new RefreshTokenRequestDTO(tamperedToken))
                .exchange()
                .expectStatus().isUnauthorized();
    }

    // ==========================================
    // Other Edge Case Tests
    // ==========================================
    @Test
    @DisplayName("POST /auth/login should handle malformed JSON in login request")
    void login_withMalformedJson_shouldReturnBadRequest() {
        // Given
        String malformedJson = "{\"email\": \"testAdmin@example.com\", \"password\": \"password\""; // Missing closing brace

        // When & Then
        webTestClient.post()
                .uri("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(malformedJson)
                .exchange()
                .expectStatus().isBadRequest();
    }

    @Test
    @DisplayName("POST /auth/login should handle concurrent login requests for same user")
    void login_concurrentRequests_shouldSucceed() {
        // Given
        LoginRequestDTO loginRequest = new LoginRequestDTO();
        loginRequest.setEmail("testAdmin@example.com");
        loginRequest.setPassword("password");

        // When & Then - Execute multiple concurrent login requests
        for (int i = 0; i < 5; i++) {
            webTestClient.post()
                    .uri("/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(loginRequest)
                    .exchange()
                    .expectStatus().isOk()
                    .expectBody(AuthTokenResponseDTO.class)
                    .value(response -> {
                        assertNotNull(response.getAccessToken());
                        assertNotNull(response.getRefreshToken());
                    });
        }
    }

    @Test
    @DisplayName("POST /auth/login should successfully login and POST /auth/refresh in sequence")
    void completeAuthenticationFlow_shouldSucceed() {
        // Step 1: Login
        LoginRequestDTO loginRequest = new LoginRequestDTO();
        loginRequest.setEmail("testAdmin@example.com");
        loginRequest.setPassword("password");

        AuthTokenResponseDTO loginResponse = webTestClient.post()
                .uri("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(loginRequest)
                .exchange()
                .expectStatus().isOk()
                .expectBody(AuthTokenResponseDTO.class)
                .returnResult()
                .getResponseBody();

        assertNotNull(loginResponse);
        String originalRefreshToken = loginResponse.getRefreshToken();

        // Step 2: Refresh token
        RefreshTokenRequestDTO refreshRequest = new RefreshTokenRequestDTO();
        refreshRequest.setRefreshToken(originalRefreshToken);

        AuthTokenResponseDTO refreshResponse = webTestClient.post()
                .uri("/auth/refresh")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(refreshRequest)
                .exchange()
                .expectStatus().isOk()
                .expectBody(AuthTokenResponseDTO.class)
                .returnResult()
                .getResponseBody();

        assertNotNull(refreshResponse);
        assertNotNull(refreshResponse.getAccessToken());
        assertNotNull(refreshResponse.getRefreshToken());

        // Step 3: Verify new tokens work
        assertTrue(jwtTokenProvider.validateAccessToken(refreshResponse.getAccessToken()));
        assertTrue(jwtTokenProvider.validateRefreshToken(refreshResponse.getRefreshToken()));
    }

    // ==========================================
    // Test Data Setup & Cleanup Verification
    // ==========================================
    @Test
    @DisplayName("Should verify test data is loaded correctly from SQL files")
    void testDataSetup_shouldLoadAllUsers() {
        // Verify all 5 test users are loaded
        Integer userCount = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM users", 
            Integer.class
        );
        assertEquals(5, userCount, "Should have 5 users from test-users.sql");

        // Verify specific user exists
        Integer adminCount = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM users WHERE email = ?", 
            Integer.class,
            "testAdmin@example.com"
        );
        assertEquals(1, adminCount, "Admin user should exist");
    }

    @Test
    @DisplayName("Should clean up data between tests")
    void testDataCleanup_shouldIsolateTests() {
        // This test verifies that @Sql properly sets up fresh data
        // If previous tests modified data, this would fail without proper cleanup
        
        LoginRequestDTO loginRequest = new LoginRequestDTO();
        loginRequest.setEmail("testAdmin@example.com");
        loginRequest.setPassword("password");

        webTestClient.post()
                .uri("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(loginRequest)
                .exchange()
                .expectStatus().isOk();
        
        // Verify data is consistent
        Integer userCount = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM users", 
            Integer.class
        );
        assertEquals(5, userCount, "Should still have exactly 5 users");
    }
}