package co.edu.puj.secchub_backend.security.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.testcontainers.junit.jupiter.Testcontainers;

import co.edu.puj.secchub_backend.DatabaseContainerIntegration;
import co.edu.puj.secchub_backend.SqlScriptExecutor;
import co.edu.puj.secchub_backend.security.contract.UserInformationResponseDTO;
import co.edu.puj.secchub_backend.security.jwt.JwtTokenProvider;
import co.edu.puj.secchub_backend.security.model.User;
import co.edu.puj.secchub_backend.security.repository.UserRepository;
import reactor.core.publisher.Mono;

@SpringBootTest
@AutoConfigureWebTestClient
@Testcontainers
@DisplayName("User Controller Integration Tests")
class UserControllerIntegrationTest extends DatabaseContainerIntegration {
/**
    @Autowired
    private WebTestClient webTestClient;

    @Autowired
    private DatabaseClient databaseClient;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    private SqlScriptExecutor sqlScriptExecutor;

    @BeforeEach
    void setUp() {
        sqlScriptExecutor = new SqlScriptExecutor(databaseClient);
        sqlScriptExecutor.executeSqlScript("/test-cleanup.sql");
        sqlScriptExecutor.executeSqlScript("/test-users.sql");
    }

    @AfterEach
    void tearDown() {
        sqlScriptExecutor.executeSqlScript("/test-cleanup.sql");
    }

    // ==========================================
    // Logged-in User Tests
    // ==========================================
    @ParameterizedTest(name = "GET /user authenticated as {0} returns own information")
    @MethodSource("userAuthenticationProvider")
    @DisplayName("GET /user authenticated user should receive own information")
    void getUserInformation_authenticatedUser_returnsUser(String email, String role) {
        String token = jwtTokenProvider.generateToken(email, role);

        UserInformationResponseDTO dto = webTestClient.get()
                .uri("/user")
                .header("Authorization", "Bearer " + token)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody(UserInformationResponseDTO.class)
                .returnResult()
                .getResponseBody();

        assertNotNull(dto, "Response DTO should not be null");

        // Gets all user info from DB to compare
        Mono<User> user = userRepository.findByEmail(email)
                .switchIfEmpty(Mono.error(new RuntimeException("User not found")));
        assertNotNull(user, "User should exist in the database");

        user.subscribe(u -> {
            assertEquals(u.getId(), dto.getId(), "User ID should match");
            assertEquals(u.getUsername(), dto.getUsername(), "Username should match");
            assertEquals(u.getFaculty(), dto.getFaculty(), "User faculty should match");
            assertEquals(u.getName(), dto.getName(), "User name should match");
            assertEquals(u.getLastName(), dto.getLastName(), "User last name should match");
            assertEquals(u.getEmail(), dto.getEmail(), "User email should match");
            assertEquals(u.getStatusId(), dto.getStatusId(), "User status ID should match");
            assertEquals(u.getRoleId(), dto.getRoleId(), "User role ID should match");
            assertEquals(u.getDocumentTypeId(), dto.getDocumentTypeId(), "User document type ID should match");
            assertEquals(u.getDocumentNumber(), dto.getDocumentNumber(), "User document number should match");
        });
    }

    private static Stream<Arguments> userAuthenticationProvider() {
        return Stream.of(
                Arguments.of("testAdmin@example.com", "ROLE_ADMIN"),
                Arguments.of("testUser@example.com", "ROLE_USER"),
                Arguments.of("testTeacher@example.com", "ROLE_TEACHER"),
                Arguments.of("testProgram@example.com", "ROLE_PROGRAM"),
                Arguments.of("testStudent@example.com", "ROLE_STUDENT")
        );
    }

    @Test
    @DisplayName("GET /user authenticated with invalid token should return 401 Unauthorized")
    void getUserInformation_invalidToken_returns401() {
        String invalidToken = "invalid.token.value";

        webTestClient.get()
                .uri("/user")
                .header("Authorization", "Bearer " + invalidToken)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isUnauthorized();
    }

    @Test
    @DisplayName("GET /user authenticated user deleted from database should return 401 Unauthorized")
    void getUserInformation_deletedUser_returns401() {
        String email = "testUser@example.com";
        String role = "ROLE_USER";
        String token = jwtTokenProvider.generateToken(email, role);

        // Simulate user deletion
        databaseClient.sql("DELETE FROM users WHERE email = :email")
                .bind("email", email)
                .fetch()
                .rowsUpdated()
                .block();

        // Print stack trace for debugging
        webTestClient.get()
                .uri("/user")
                .header("Authorization", "Bearer " + token)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isUnauthorized();
    }

    // ==========================================
    // Obtain All Users Tests
    // ==========================================
    @Test
    @DisplayName("GET /user/all admin should receive all users list")
    void getAllUsersInformation_asAdmin_returnsList() {
        String adminToken = jwtTokenProvider.generateToken("testAdmin@example.com", "ROLE_ADMIN");

        Long userCount = databaseClient.sql("SELECT COUNT(*) FROM users")
                .map(row -> row.get(0, Long.class))
                .one()
                .block();
        assertNotNull(userCount, "User count from DB should not be null");

        List<UserInformationResponseDTO> list = webTestClient.get()
                .uri("/user/all")
                .header("Authorization", "Bearer " + adminToken)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(UserInformationResponseDTO.class)
                .returnResult()
                .getResponseBody();

        assertNotNull(list, "Response list should not be null");
        assertEquals(userCount.intValue(), list.size(), "Returned user list size should match DB count");
    }

    @ParameterizedTest(name = "GET /user/all non-admin as {0} should return 403 Forbidden")
    @MethodSource("nonAdminRolesProvider")
    @DisplayName("GET /user/all non-admin should return 403 Forbidden")
    void getAllUsersInformation_asNonAdmin_returns403(String email, String role) {
        String token = jwtTokenProvider.generateToken(email, role);

        webTestClient.get()
                .uri("/user/all")
                .header("Authorization", "Bearer " + token)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isForbidden();
    }

    private static Stream<Arguments> nonAdminRolesProvider() {
        return Stream.of(
                Arguments.of("testUser@example.com", "ROLE_USER"),
                Arguments.of("testTeacher@example.com", "ROLE_TEACHER"),
                Arguments.of("testProgram@example.com", "ROLE_PROGRAM"),
                Arguments.of("testStudent@example.com", "ROLE_STUDENT")
        );
    }

    @Test
    @DisplayName("GET /user/all with no token should return 401 Unauthorized")
    void getAllUsersInformation_noToken_returns401() {
        webTestClient.get()
                .uri("/user/all")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isUnauthorized();
    }

    // ==========================================
    // Obtain User by Email and ID Tests
    // ==========================================

    @ParameterizedTest(name = "GET /user/email authorized as {0} should fetch user by email")
    @MethodSource("userOrAdminRolesProvider")
    @DisplayName("GET /user/email authorized should fetch user by email")
    void getUserInformationByEmail_asAdmin_returnsUser(String email, String role) {
        String token = jwtTokenProvider.generateToken(email, role);

        UserInformationResponseDTO dto = webTestClient.get()
                .uri(uriBuilder -> uriBuilder.path("/user/email").queryParam("email", email).build())
                .header("Authorization", "Bearer " + token)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody(UserInformationResponseDTO.class)
                .returnResult()
                .getResponseBody();

        assertNotNull(dto);
        assertEquals(email, dto.getEmail());
    }

    @ParameterizedTest(name = "GET /user/id/user:id admin should fetch user by id")
    @MethodSource("userOrAdminRolesProvider")
    @DisplayName("GET /user/id/user:id authorized should fetch user by id")
    void getUserInformationById_asAdmin_returnsUser(String email, String role) {
        // Query DB for a user id for the given email
        Long id = databaseClient.sql("SELECT id FROM users WHERE email = :email LIMIT 1")
                .bind("email", email)
                .map(row -> row.get("id", Long.class))
                .one()
                .block();
        assertNotNull(id, "User id should exist in test data");

        String adminToken = jwtTokenProvider.generateToken(email, role);

        UserInformationResponseDTO dto = webTestClient.get()
                .uri("/user/id/{id}", id)
                .header("Authorization", "Bearer " + adminToken)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody(UserInformationResponseDTO.class)
                .returnResult()
                .getResponseBody();

        assertNotNull(dto);
        assertEquals(id, dto.getId());
    }

    private static Stream<Arguments> userOrAdminRolesProvider() {
        return Stream.of(
                Arguments.of("testUser@example.com", "ROLE_USER"),
                Arguments.of("testAdmin@example.com", "ROLE_ADMIN")
        );
    }

    @Test
    @DisplayName("GET /user/id/user:id with non-existing id should return 404 Not Found")
    void getUserInformationById_nonExistingId_returns404() {
        String adminToken = jwtTokenProvider.generateToken("testAdmin@example.com", "ROLE_ADMIN");

        webTestClient.get()
                .uri("/user/id/{id}", Long.MAX_VALUE)
                .header("Authorization", "Bearer " + adminToken)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    @DisplayName("GET /user/email with non-existing email should return 404 Not Found")
    void getUserInformationByEmail_nonExistingEmail_returns404() {
        String adminToken = jwtTokenProvider.generateToken("testAdmin@example.com", "ROLE_ADMIN");

        webTestClient.get()
                .uri(uriBuilder -> uriBuilder.path("/user/email").queryParam("email", "nonexistent@example.com").build())
                .header("Authorization", "Bearer " + adminToken)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isNotFound();
    }

    @ParameterizedTest(name = "GET /user/email without token as {0} should return 403 Forbidden")
    @MethodSource("nonUserOrAdminRolesProvider")
    @DisplayName("GET /user/email without required role should return 403 Forbidden")
    void unauthorized_withoutToken_returns403(String email, String role) {
        String token = jwtTokenProvider.generateToken(email, role);

        webTestClient.get()
                .uri(uriBuilder -> uriBuilder.path("/user/email").queryParam("email", email).build())
                .header("Authorization", "Bearer " + token)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isForbidden();
    }

    @ParameterizedTest(name = "GET /user/id/user:id without token as {0} should return 403 Forbidden")
    @MethodSource("nonUserOrAdminRolesProvider")
    @DisplayName("GET /user/id/user:id without required role should return 403 Forbidden")
    void getUserInformationById_unauthorized_withoutToken_returns403(String email, String role) {
        // Query DB for a user id for the given email
        Long id = databaseClient.sql("SELECT id FROM users WHERE email = :email LIMIT 1")
                .bind("email", email)
                .map(row -> row.get("id", Long.class))
                .one()
                .block();
        assertNotNull(id, "User id should exist in test data");

        String token = jwtTokenProvider.generateToken(email, role);

        webTestClient.get()
                .uri("/user/id/{id}", id)
                .header("Authorization", "Bearer " + token)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isForbidden();
    }

    private static Stream<Arguments> nonUserOrAdminRolesProvider() {
        return Stream.of(
                Arguments.of("testStudent@example.com", "ROLE_STUDENT"),
                Arguments.of("testProgram@example.com", "ROLE_PROGRAM"),
                Arguments.of("testTeacher@example.com", "ROLE_TEACHER")
        );
    }

    @Test
    @DisplayName("GET /user/id/user:id with no token should return 401 Unauthorized")
    void getUserInformationById_noToken_returns401() {
        // Using any valid user id from test data
        Long id = 1L;

        webTestClient.get()
                .uri("/user/id/{id}", id)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isUnauthorized();
    }

    @Test
    @DisplayName("GET /user/email with no token should return 401 Unauthorized")
    void getUserInformationByEmail_noToken_returns401() {
        String email = "testUser@example.com";

        webTestClient.get()
                .uri(uriBuilder -> uriBuilder.path("/user/email").queryParam("email", email).build())
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isUnauthorized();
    }
*/
}
