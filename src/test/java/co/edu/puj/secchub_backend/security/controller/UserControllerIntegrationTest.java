package co.edu.puj.secchub_backend.security.controller;

import co.edu.puj.secchub_backend.DatabaseIntegrationTest;
import co.edu.puj.secchub_backend.security.dto.UserInformationResponseDTO;
import co.edu.puj.secchub_backend.security.jwt.JwtTokenProvider;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

@SpringBootTest
@AutoConfigureWebTestClient
@Testcontainers
@DisplayName("User Controller Integration Tests")
@Sql(scripts = "/test-cleanup.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@Sql(scripts = "/test-users.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@Sql(scripts = "/test-cleanup.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
class UserControllerTest extends DatabaseIntegrationTest {

    @Autowired
    private WebTestClient webTestClient;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    private static final String ADMIN_EMAIL = "testAdmin@example.com";
    private static final String USER_EMAIL = "testUser@example.com";

    // ==========================================
    // Logged-in User Tests
    // ==========================================
    @Test
    @DisplayName("GET /user authenticated user should receive own information")
    void getUserInformation_authenticatedUser_returnsUser() {
        String token = jwtTokenProvider.generateToken(USER_EMAIL, "ROLE_USER");

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
        assertEquals(USER_EMAIL, dto.getEmail(), "Returned email should match the authenticated user's email");
        assertNotNull(dto.getUsername(), "Username should be present");
    }

    @Test
    @DisplayName("GET /user/all admin should receive all users list")
    void getAllUsersInformation_asAdmin_returnsList() {
        String adminToken = jwtTokenProvider.generateToken(ADMIN_EMAIL, "ROLE_ADMIN");

        Integer userCount = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM users", Integer.class);
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

    @Test
    @DisplayName("GET /user/email admin should fetch user by email")
    void getUserInformationByEmail_asAdmin_returnsUser() {
        String adminToken = jwtTokenProvider.generateToken(ADMIN_EMAIL, "ROLE_ADMIN");

        UserInformationResponseDTO dto = webTestClient.get()
                .uri(uriBuilder -> uriBuilder.path("/user/email").queryParam("email", USER_EMAIL).build())
                .header("Authorization", "Bearer " + adminToken)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody(UserInformationResponseDTO.class)
                .returnResult()
                .getResponseBody();

        assertNotNull(dto);
        assertEquals(USER_EMAIL, dto.getEmail());
    }

    @Test
    @DisplayName("GET /user/id/{id} admin should fetch user by id")
    void getUserInformationById_asAdmin_returnsUser() {
        // Query DB for a user id for USER_EMAIL
        Long id = jdbcTemplate.queryForObject("SELECT id FROM users WHERE email = ? LIMIT 1", Long.class, USER_EMAIL);
        assertNotNull(id, "User id should exist in test data");

        String adminToken = jwtTokenProvider.generateToken(ADMIN_EMAIL, "ROLE_ADMIN");

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

    @Test
    @DisplayName("GET /user without token should return 401 Unauthorized")
    void unauthorized_withoutToken_returns401() {
        webTestClient.get()
                .uri("/user")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isUnauthorized();
    }

    @Test
    @DisplayName("GET /user/all with non-admin should return 403 Forbidden")
    void forbidden_nonAdmin_access_all_returns403() {
        String userToken = jwtTokenProvider.generateToken(USER_EMAIL, "ROLE_USER");

        webTestClient.get()
                .uri("/user/all")
                .header("Authorization", "Bearer " + userToken)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isForbidden();
    }
}
