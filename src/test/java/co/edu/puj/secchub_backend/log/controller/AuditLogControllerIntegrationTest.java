package co.edu.puj.secchub_backend.log.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Stream;

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
import org.springframework.test.web.reactive.server.WebTestClient;
import org.testcontainers.junit.jupiter.Testcontainers;

import co.edu.puj.secchub_backend.DatabaseContainerIntegration;
import co.edu.puj.secchub_backend.R2dbcTestUtils;
import co.edu.puj.secchub_backend.log.dto.AuditLogResponseDTO;
import co.edu.puj.secchub_backend.security.jwt.JwtTokenProvider;
import io.r2dbc.spi.ConnectionFactory;

@SpringBootTest
@AutoConfigureWebTestClient
@Testcontainers
@DisplayName("Audit Log Controller Integration Tests")
class AuditLogControllerIntegrationTest extends DatabaseContainerIntegration {
    
    @Autowired
    private WebTestClient webTestClient;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private ConnectionFactory connectionFactory;

    @BeforeEach
    void setUp() {
        R2dbcTestUtils.executeScripts(connectionFactory,
                "/test-cleanup.sql",
                "/test-users.sql",
                "/test-audit-logs.sql"
        );
    }

    // ==========================================
    // Test Data Providers
    // ==========================================
    
    /**
     * Provides admin role (only role with access)
     */
    private static Stream<Arguments> adminRoleProvider() {
        return Stream.of(
            Arguments.of("testAdmin@example.com", "ROLE_ADMIN")
        );
    }

    /**
     * Provides non-admin roles that should be denied access
     */
    private static Stream<Arguments> nonAdminRolesProvider() {
        return Stream.of(
            Arguments.of("testUser@example.com", "ROLE_USER"),
            Arguments.of("testTeacher@example.com", "ROLE_TEACHER"),
            Arguments.of("testStudent@example.com", "ROLE_STUDENT"),
            Arguments.of("testProgram@example.com", "ROLE_PROGRAM")
        );
    }

    // ==========================================
    // GET All Audit Logs Tests
    // ==========================================

    @ParameterizedTest
    @MethodSource("adminRoleProvider")
    @DisplayName("GET /audit-logs - Admin can retrieve all audit logs")
    void getAllAuditLogs_asAdmin_shouldSucceed(String email, String role) {
        String token = jwtTokenProvider.generateToken(email, role);

        List<AuditLogResponseDTO> logs = webTestClient.get()
                .uri("/audit-logs")
                .header("Authorization", "Bearer " + token)
                .accept(MediaType.APPLICATION_NDJSON)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_NDJSON)
                .expectBodyList(AuditLogResponseDTO.class)
                .returnResult()
                .getResponseBody();

        assertNotNull(logs);
        assertFalse(logs.isEmpty());
        assertEquals(20, logs.size(), "Should have all 20 test audit logs");
    }

    @ParameterizedTest
    @MethodSource("nonAdminRolesProvider")
    @DisplayName("GET /audit-logs - Non-admin roles cannot access")
    void getAllAuditLogs_asNonAdmin_shouldReturn403(String email, String role) {
        String token = jwtTokenProvider.generateToken(email, role);

        webTestClient.get()
                .uri("/audit-logs")
                .header("Authorization", "Bearer " + token)
                .accept(MediaType.APPLICATION_NDJSON)
                .exchange()
                .expectStatus().isForbidden();
    }

    @Test
    @DisplayName("GET /audit-logs - Unauthorized without token")
    void getAllAuditLogs_withoutToken_shouldReturn401() {
        webTestClient.get()
                .uri("/audit-logs")
                .accept(MediaType.APPLICATION_NDJSON)
                .exchange()
                .expectStatus().isUnauthorized();
    }

    // ==========================================
    // GET By Email Tests
    // ==========================================

    @ParameterizedTest
    @MethodSource("adminRoleProvider")
    @DisplayName("GET /audit-logs/email/{email} - Should filter by admin email")
    void getAuditLogsByEmail_adminEmail_shouldReturnOnlyAdminLogs(String email, String role) {
        String token = jwtTokenProvider.generateToken(email, role);
        String targetEmail = "testAdmin@example.com";

        List<AuditLogResponseDTO> logs = webTestClient.get()
                .uri("/audit-logs/email/{email}", targetEmail)
                .header("Authorization", "Bearer " + token)
                .accept(MediaType.APPLICATION_NDJSON)
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(AuditLogResponseDTO.class)
                .returnResult()
                .getResponseBody();

        assertNotNull(logs);
        assertFalse(logs.isEmpty());
        assertEquals(9, logs.size(), "Should have 9 admin audit logs");
        assertTrue(logs.stream().allMatch(log -> log.getEmail().equals(targetEmail)),
                "All logs should be from admin email");
    }

    @ParameterizedTest
    @MethodSource("adminRoleProvider")
    @DisplayName("GET /audit-logs/email/{email} - Should filter by user email")
    void getAuditLogsByEmail_userEmail_shouldReturnOnlyUserLogs(String email, String role) {
        String token = jwtTokenProvider.generateToken(email, role);
        String targetEmail = "testUser@example.com";

        List<AuditLogResponseDTO> logs = webTestClient.get()
                .uri("/audit-logs/email/{email}", targetEmail)
                .header("Authorization", "Bearer " + token)
                .accept(MediaType.APPLICATION_NDJSON)
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(AuditLogResponseDTO.class)
                .returnResult()
                .getResponseBody();

        assertNotNull(logs);
        assertFalse(logs.isEmpty());
        assertEquals(5, logs.size(), "Should have 5 user audit logs");
        assertTrue(logs.stream().allMatch(log -> log.getEmail().equals(targetEmail)),
                "All logs should be from user email");
    }

    @ParameterizedTest
    @MethodSource("adminRoleProvider")
    @DisplayName("GET /audit-logs/email/{email} - Should filter by teacher email")
    void getAuditLogsByEmail_teacherEmail_shouldReturnOnlyTeacherLogs(String email, String role) {
        String token = jwtTokenProvider.generateToken(email, role);
        String targetEmail = "testTeacher@example.com";

        List<AuditLogResponseDTO> logs = webTestClient.get()
                .uri("/audit-logs/email/{email}", targetEmail)
                .header("Authorization", "Bearer " + token)
                .accept(MediaType.APPLICATION_NDJSON)
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(AuditLogResponseDTO.class)
                .returnResult()
                .getResponseBody();

        assertNotNull(logs);
        assertEquals(2, logs.size(), "Should have 2 teacher audit logs");
        assertTrue(logs.stream().allMatch(log -> log.getEmail().equals(targetEmail)));
    }

    @ParameterizedTest
    @MethodSource("nonAdminRolesProvider")
    @DisplayName("GET /audit-logs/email/{email} - Non-admin cannot access")
    void getAuditLogsByEmail_asNonAdmin_shouldReturn403(String email, String role) {
        String token = jwtTokenProvider.generateToken(email, role);

        webTestClient.get()
                .uri("/audit-logs/email/{email}", "testAdmin@example.com")
                .header("Authorization", "Bearer " + token)
                .accept(MediaType.APPLICATION_NDJSON)
                .exchange()
                .expectStatus().isForbidden();
    }

    // ==========================================
    // GET By Action Tests
    // ==========================================

    @ParameterizedTest
    @MethodSource("adminRoleProvider")
    @DisplayName("GET /audit-logs/action/{action} - Should filter by CREATE action")
    void getAuditLogsByAction_createAction_shouldReturnOnlyCreateLogs(String email, String role) {
        String token = jwtTokenProvider.generateToken(email, role);
        String action = "CREATE";

        List<AuditLogResponseDTO> logs = webTestClient.get()
                .uri("/audit-logs/action/{action}", action)
                .header("Authorization", "Bearer " + token)
                .accept(MediaType.APPLICATION_NDJSON)
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(AuditLogResponseDTO.class)
                .returnResult()
                .getResponseBody();

        assertNotNull(logs);
        assertFalse(logs.isEmpty());
        assertEquals(9, logs.size(), "Should have 9 CREATE audit logs");
        assertTrue(logs.stream().allMatch(log -> log.getAction().equals(action)),
                "All logs should have CREATE action");
    }

    @ParameterizedTest
    @MethodSource("adminRoleProvider")
    @DisplayName("GET /audit-logs/action/{action} - Should filter by UPDATE action")
    void getAuditLogsByAction_updateAction_shouldReturnOnlyUpdateLogs(String email, String role) {
        String token = jwtTokenProvider.generateToken(email, role);
        String action = "UPDATE";

        List<AuditLogResponseDTO> logs = webTestClient.get()
                .uri("/audit-logs/action/{action}", action)
                .header("Authorization", "Bearer " + token)
                .accept(MediaType.APPLICATION_NDJSON)
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(AuditLogResponseDTO.class)
                .returnResult()
                .getResponseBody();

        assertNotNull(logs);
        assertFalse(logs.isEmpty());
        assertEquals(8, logs.size(), "Should have 8 UPDATE audit logs");
        assertTrue(logs.stream().allMatch(log -> log.getAction().equals(action)),
                "All logs should have UPDATE action");
    }

    @ParameterizedTest
    @MethodSource("adminRoleProvider")
    @DisplayName("GET /audit-logs/action/{action} - Should filter by DELETE action")
    void getAuditLogsByAction_deleteAction_shouldReturnOnlyDeleteLogs(String email, String role) {
        String token = jwtTokenProvider.generateToken(email, role);
        String action = "DELETE";

        List<AuditLogResponseDTO> logs = webTestClient.get()
                .uri("/audit-logs/action/{action}", action)
                .header("Authorization", "Bearer " + token)
                .accept(MediaType.APPLICATION_NDJSON)
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(AuditLogResponseDTO.class)
                .returnResult()
                .getResponseBody();

        assertNotNull(logs);
        assertFalse(logs.isEmpty());
        assertEquals(3, logs.size(), "Should have 3 DELETE audit logs");
        assertTrue(logs.stream().allMatch(log -> log.getAction().equals(action)),
                "All logs should have DELETE action");
    }

    @ParameterizedTest
    @MethodSource("nonAdminRolesProvider")
    @DisplayName("GET /audit-logs/action/{action} - Non-admin cannot access")
    void getAuditLogsByAction_asNonAdmin_shouldReturn403(String email, String role) {
        String token = jwtTokenProvider.generateToken(email, role);

        webTestClient.get()
                .uri("/audit-logs/action/{action}", "CREATE")
                .header("Authorization", "Bearer " + token)
                .accept(MediaType.APPLICATION_NDJSON)
                .exchange()
                .expectStatus().isForbidden();
    }

    // ==========================================
    // GET By Date Range Tests
    // ==========================================

    @ParameterizedTest
    @MethodSource("adminRoleProvider")
    @DisplayName("GET /audit-logs/date-range - Should filter by early October date range")
    void getAuditLogsByDateRange_earlyOctober_shouldReturnEarlyOctoberLogs(String email, String role) {
        String token = jwtTokenProvider.generateToken(email, role);
        LocalDateTime start = LocalDateTime.of(2025, 10, 1, 0, 0);
        LocalDateTime end = LocalDateTime.of(2025, 10, 10, 23, 59);

        List<AuditLogResponseDTO> logs = webTestClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/audit-logs/date-range")
                        .queryParam("start", start.toString())
                        .queryParam("end", end.toString())
                        .build())
                .header("Authorization", "Bearer " + token)
                .accept(MediaType.APPLICATION_NDJSON)
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(AuditLogResponseDTO.class)
                .returnResult()
                .getResponseBody();

        assertNotNull(logs);
        assertFalse(logs.isEmpty());
        // Should include logs from Oct 1-10
        assertTrue(logs.stream().allMatch(log -> 
                !log.getTimestamp().isBefore(start) && !log.getTimestamp().isAfter(end)),
                "All logs should be within date range");
    }

    @ParameterizedTest
    @MethodSource("adminRoleProvider")
    @DisplayName("GET /audit-logs/date-range - Should filter by late October date range")
    void getAuditLogsByDateRange_lateOctober_shouldReturnLateOctoberLogs(String email, String role) {
        String token = jwtTokenProvider.generateToken(email, role);
        LocalDateTime start = LocalDateTime.of(2025, 10, 25, 0, 0);
        LocalDateTime end = LocalDateTime.of(2025, 10, 31, 23, 59);

        List<AuditLogResponseDTO> logs = webTestClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/audit-logs/date-range")
                        .queryParam("start", start.toString())
                        .queryParam("end", end.toString())
                        .build())
                .header("Authorization", "Bearer " + token)
                .accept(MediaType.APPLICATION_NDJSON)
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(AuditLogResponseDTO.class)
                .returnResult()
                .getResponseBody();

        assertNotNull(logs);
        assertFalse(logs.isEmpty());
        assertTrue(logs.stream().allMatch(log -> 
                !log.getTimestamp().isBefore(start) && !log.getTimestamp().isAfter(end)),
                "All logs should be within date range");
    }

    @ParameterizedTest
    @MethodSource("adminRoleProvider")
    @DisplayName("GET /audit-logs/date-range - Should filter by November date range")
    void getAuditLogsByDateRange_november_shouldReturnNovemberLogs(String email, String role) {
        String token = jwtTokenProvider.generateToken(email, role);
        LocalDateTime start = LocalDateTime.of(2025, 11, 1, 0, 0);
        LocalDateTime end = LocalDateTime.of(2025, 11, 30, 23, 59);

        List<AuditLogResponseDTO> logs = webTestClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/audit-logs/date-range")
                        .queryParam("start", start.toString())
                        .queryParam("end", end.toString())
                        .build())
                .header("Authorization", "Bearer " + token)
                .accept(MediaType.APPLICATION_NDJSON)
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(AuditLogResponseDTO.class)
                .returnResult()
                .getResponseBody();

        assertNotNull(logs);
        assertFalse(logs.isEmpty());
        // Should have 5 November logs
        assertTrue(logs.stream().allMatch(log -> 
                !log.getTimestamp().isBefore(start) && !log.getTimestamp().isAfter(end)),
                "All logs should be within November date range");
    }

    @ParameterizedTest
    @MethodSource("nonAdminRolesProvider")
    @DisplayName("GET /audit-logs/date-range - Non-admin cannot access")
    void getAuditLogsByDateRange_asNonAdmin_shouldReturn403(String email, String role) {
        String token = jwtTokenProvider.generateToken(email, role);
        LocalDateTime start = LocalDateTime.of(2025, 10, 1, 0, 0);
        LocalDateTime end = LocalDateTime.of(2025, 10, 31, 23, 59);

        webTestClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/audit-logs/date-range")
                        .queryParam("start", start.toString())
                        .queryParam("end", end.toString())
                        .build())
                .header("Authorization", "Bearer " + token)
                .accept(MediaType.APPLICATION_NDJSON)
                .exchange()
                .expectStatus().isForbidden();
    }

    // ==========================================
    // GET By Method Name Tests
    // ==========================================

    @ParameterizedTest
    @MethodSource("adminRoleProvider")
    @DisplayName("GET /audit-logs/method/{methodName} - Should filter by createSemester method")
    void getAuditLogsByMethodName_createSemester_shouldReturnOnlyCreateSemesterLogs(String email, String role) {
        String token = jwtTokenProvider.generateToken(email, role);
        String methodName = "createSemester";

        List<AuditLogResponseDTO> logs = webTestClient.get()
                .uri("/audit-logs/method/{methodName}", methodName)
                .header("Authorization", "Bearer " + token)
                .accept(MediaType.APPLICATION_NDJSON)
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(AuditLogResponseDTO.class)
                .returnResult()
                .getResponseBody();

        assertNotNull(logs);
        assertFalse(logs.isEmpty());
        assertTrue(logs.stream().allMatch(log -> log.getMethodName().equals(methodName)),
                "All logs should be from createSemester method");
    }

    @ParameterizedTest
    @MethodSource("adminRoleProvider")
    @DisplayName("GET /audit-logs/method/{methodName} - Should filter by acceptTeacherClass method")
    void getAuditLogsByMethodName_acceptTeacherClass_shouldReturnOnlyAcceptLogs(String email, String role) {
        String token = jwtTokenProvider.generateToken(email, role);
        String methodName = "acceptTeacherClass";

        List<AuditLogResponseDTO> logs = webTestClient.get()
                .uri("/audit-logs/method/{methodName}", methodName)
                .header("Authorization", "Bearer " + token)
                .accept(MediaType.APPLICATION_NDJSON)
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(AuditLogResponseDTO.class)
                .returnResult()
                .getResponseBody();

        assertNotNull(logs);
        assertFalse(logs.isEmpty());
        assertEquals(1, logs.size());
        assertTrue(logs.stream().allMatch(log -> log.getMethodName().equals(methodName)),
                "All logs should be from acceptTeacherClass method");
    }

    @ParameterizedTest
    @MethodSource("nonAdminRolesProvider")
    @DisplayName("GET /audit-logs/method/{methodName} - Non-admin cannot access")
    void getAuditLogsByMethodName_asNonAdmin_shouldReturn403(String email, String role) {
        String token = jwtTokenProvider.generateToken(email, role);

        webTestClient.get()
                .uri("/audit-logs/method/{methodName}", "createSemester")
                .header("Authorization", "Bearer " + token)
                .accept(MediaType.APPLICATION_NDJSON)
                .exchange()
                .expectStatus().isForbidden();
    }

    // ==========================================
    // GET By Email and Action Tests
    // ==========================================

    @ParameterizedTest
    @MethodSource("adminRoleProvider")
    @DisplayName("GET /audit-logs/email/{email}/action/{action} - Should filter by admin and CREATE")
    void getAuditLogsByEmailAndAction_adminCreate_shouldReturnAdminCreateLogs(String email, String role) {
        String token = jwtTokenProvider.generateToken(email, role);
        String targetEmail = "testAdmin@example.com";
        String action = "CREATE";

        List<AuditLogResponseDTO> logs = webTestClient.get()
                .uri("/audit-logs/email/{email}/action/{action}", targetEmail, action)
                .header("Authorization", "Bearer " + token)
                .accept(MediaType.APPLICATION_NDJSON)
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(AuditLogResponseDTO.class)
                .returnResult()
                .getResponseBody();

        assertNotNull(logs);
        assertFalse(logs.isEmpty());
        assertTrue(logs.stream().allMatch(log -> 
                log.getEmail().equals(targetEmail) && log.getAction().equals(action)),
                "All logs should be admin CREATE logs");
    }

    @ParameterizedTest
    @MethodSource("adminRoleProvider")
    @DisplayName("GET /audit-logs/email/{email}/action/{action} - Should filter by user and UPDATE")
    void getAuditLogsByEmailAndAction_userUpdate_shouldReturnUserUpdateLogs(String email, String role) {
        String token = jwtTokenProvider.generateToken(email, role);
        String targetEmail = "testUser@example.com";
        String action = "UPDATE";

        List<AuditLogResponseDTO> logs = webTestClient.get()
                .uri("/audit-logs/email/{email}/action/{action}", targetEmail, action)
                .header("Authorization", "Bearer " + token)
                .accept(MediaType.APPLICATION_NDJSON)
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(AuditLogResponseDTO.class)
                .returnResult()
                .getResponseBody();

        assertNotNull(logs);
        assertFalse(logs.isEmpty());
        assertTrue(logs.stream().allMatch(log -> 
                log.getEmail().equals(targetEmail) && log.getAction().equals(action)),
                "All logs should be user UPDATE logs");
    }

    @ParameterizedTest
    @MethodSource("adminRoleProvider")
    @DisplayName("GET /audit-logs/email/{email}/action/{action} - Should filter by teacher and UPDATE")
    void getAuditLogsByEmailAndAction_teacherUpdate_shouldReturnTeacherUpdateLogs(String email, String role) {
        String token = jwtTokenProvider.generateToken(email, role);
        String targetEmail = "testTeacher@example.com";
        String action = "UPDATE";

        List<AuditLogResponseDTO> logs = webTestClient.get()
                .uri("/audit-logs/email/{email}/action/{action}", targetEmail, action)
                .header("Authorization", "Bearer " + token)
                .accept(MediaType.APPLICATION_NDJSON)
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(AuditLogResponseDTO.class)
                .returnResult()
                .getResponseBody();

        assertNotNull(logs);
        assertFalse(logs.isEmpty());
        assertEquals(2, logs.size());
        assertTrue(logs.stream().allMatch(log -> 
                log.getEmail().equals(targetEmail) && log.getAction().equals(action)),
                "All logs should be teacher UPDATE logs");
    }

    @ParameterizedTest
    @MethodSource("nonAdminRolesProvider")
    @DisplayName("GET /audit-logs/email/{email}/action/{action} - Non-admin cannot access")
    void getAuditLogsByEmailAndAction_asNonAdmin_shouldReturn403(String email, String role) {
        String token = jwtTokenProvider.generateToken(email, role);

        webTestClient.get()
                .uri("/audit-logs/email/{email}/action/{action}", "testAdmin@example.com", "CREATE")
                .header("Authorization", "Bearer " + token)
                .accept(MediaType.APPLICATION_NDJSON)
                .exchange()
                .expectStatus().isForbidden();
    }

    // ==========================================
    // Business Logic Tests
    // ==========================================

    @Test
    @DisplayName("Action types should not overlap")
    void auditLogs_byAction_shouldNotOverlap() {
        String token = jwtTokenProvider.generateToken("testAdmin@example.com", "ROLE_ADMIN");

        // Get CREATE logs
        List<AuditLogResponseDTO> createLogs = webTestClient.get()
                .uri("/audit-logs/action/{action}", "CREATE")
                .header("Authorization", "Bearer " + token)
                .accept(MediaType.APPLICATION_NDJSON)
                .exchange()
                .expectBodyList(AuditLogResponseDTO.class)
                .returnResult()
                .getResponseBody();

        // Get UPDATE logs
        List<AuditLogResponseDTO> updateLogs = webTestClient.get()
                .uri("/audit-logs/action/{action}", "UPDATE")
                .header("Authorization", "Bearer " + token)
                .accept(MediaType.APPLICATION_NDJSON)
                .exchange()
                .expectBodyList(AuditLogResponseDTO.class)
                .returnResult()
                .getResponseBody();

        // Get DELETE logs
        List<AuditLogResponseDTO> deleteLogs = webTestClient.get()
                .uri("/audit-logs/action/{action}", "DELETE")
                .header("Authorization", "Bearer " + token)
                .accept(MediaType.APPLICATION_NDJSON)
                .exchange()
                .expectBodyList(AuditLogResponseDTO.class)
                .returnResult()
                .getResponseBody();

        assertNotNull(createLogs);
        assertNotNull(updateLogs);
        assertNotNull(deleteLogs);

        // Verify no overlap between action types
        assertTrue(createLogs.stream()
                .noneMatch(c -> updateLogs.stream()
                        .anyMatch(u -> u.getId().equals(c.getId()))),
                "CREATE and UPDATE should not overlap");
        
        assertTrue(createLogs.stream()
                .noneMatch(c -> deleteLogs.stream()
                        .anyMatch(d -> d.getId().equals(c.getId()))),
                "CREATE and DELETE should not overlap");
        
        assertTrue(updateLogs.stream()
                .noneMatch(u -> deleteLogs.stream()
                        .anyMatch(d -> d.getId().equals(u.getId()))),
                "UPDATE and DELETE should not overlap");
    }

    @Test
    @DisplayName("User-specific logs should be properly isolated")
    void auditLogs_byEmail_shouldBeIsolated() {
        String token = jwtTokenProvider.generateToken("testAdmin@example.com", "ROLE_ADMIN");

        // Get admin logs
        List<AuditLogResponseDTO> adminLogs = webTestClient.get()
                .uri("/audit-logs/email/{email}", "testAdmin@example.com")
                .header("Authorization", "Bearer " + token)
                .accept(MediaType.APPLICATION_NDJSON)
                .exchange()
                .expectBodyList(AuditLogResponseDTO.class)
                .returnResult()
                .getResponseBody();

        // Get user logs
        List<AuditLogResponseDTO> userLogs = webTestClient.get()
                .uri("/audit-logs/email/{email}", "testUser@example.com")
                .header("Authorization", "Bearer " + token)
                .accept(MediaType.APPLICATION_NDJSON)
                .exchange()
                .expectBodyList(AuditLogResponseDTO.class)
                .returnResult()
                .getResponseBody();

        assertNotNull(adminLogs);
        assertNotNull(userLogs);

        // Verify no overlap between users
        assertTrue(adminLogs.stream()
                .noneMatch(a -> userLogs.stream()
                        .anyMatch(u -> u.getId().equals(a.getId()))),
                "Admin and user logs should not overlap");
    }

    @Test
    @DisplayName("Date ranges should work correctly")
    void auditLogs_byDateRange_shouldFilterCorrectly() {
        String token = jwtTokenProvider.generateToken("testAdmin@example.com", "ROLE_ADMIN");

        // Get October logs
        LocalDateTime octoberStart = LocalDateTime.of(2025, 10, 1, 0, 0);
        LocalDateTime octoberEnd = LocalDateTime.of(2025, 10, 31, 23, 59);

        List<AuditLogResponseDTO> octoberLogs = webTestClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/audit-logs/date-range")
                        .queryParam("start", octoberStart.toString())
                        .queryParam("end", octoberEnd.toString())
                        .build())
                .header("Authorization", "Bearer " + token)
                .accept(MediaType.APPLICATION_NDJSON)
                .exchange()
                .expectBodyList(AuditLogResponseDTO.class)
                .returnResult()
                .getResponseBody();

        // Get November logs
        LocalDateTime novemberStart = LocalDateTime.of(2025, 11, 1, 0, 0);
        LocalDateTime novemberEnd = LocalDateTime.of(2025, 11, 30, 23, 59);

        List<AuditLogResponseDTO> novemberLogs = webTestClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/audit-logs/date-range")
                        .queryParam("start", novemberStart.toString())
                        .queryParam("end", novemberEnd.toString())
                        .build())
                .header("Authorization", "Bearer " + token)
                .accept(MediaType.APPLICATION_NDJSON)
                .exchange()
                .expectBodyList(AuditLogResponseDTO.class)
                .returnResult()
                .getResponseBody();

        assertNotNull(octoberLogs);
        assertNotNull(novemberLogs);

        // Verify date ranges don't overlap
        assertTrue(octoberLogs.stream()
                .noneMatch(o -> novemberLogs.stream()
                        .anyMatch(n -> n.getId().equals(o.getId()))),
                "October and November logs should not overlap");
        
        // Verify all October logs are in October
        assertTrue(octoberLogs.stream()
                .allMatch(log -> log.getTimestamp().getMonth().getValue() == 10),
                "All October logs should have October timestamp");
        
        // Verify all November logs are in November
        assertTrue(novemberLogs.stream()
                .allMatch(log -> log.getTimestamp().getMonth().getValue() == 11),
                "All November logs should have November timestamp");
    }

    @Test
    @DisplayName("Combined email and action filter should return correct subset")
    void auditLogs_byEmailAndAction_shouldReturnCorrectSubset() {
        String token = jwtTokenProvider.generateToken("testAdmin@example.com", "ROLE_ADMIN");
        String targetEmail = "testAdmin@example.com";
        String action = "CREATE";

        // Get all admin logs
        List<AuditLogResponseDTO> allAdminLogs = webTestClient.get()
                .uri("/audit-logs/email/{email}", targetEmail)
                .header("Authorization", "Bearer " + token)
                .accept(MediaType.APPLICATION_NDJSON)
                .exchange()
                .expectBodyList(AuditLogResponseDTO.class)
                .returnResult()
                .getResponseBody();

        // Get admin CREATE logs
        List<AuditLogResponseDTO> adminCreateLogs = webTestClient.get()
                .uri("/audit-logs/email/{email}/action/{action}", targetEmail, action)
                .header("Authorization", "Bearer " + token)
                .accept(MediaType.APPLICATION_NDJSON)
                .exchange()
                .expectBodyList(AuditLogResponseDTO.class)
                .returnResult()
                .getResponseBody();

        assertNotNull(allAdminLogs);
        assertNotNull(adminCreateLogs);

        // adminCreateLogs should be a subset of allAdminLogs
        assertTrue(adminCreateLogs.size() <= allAdminLogs.size(),
                "Combined filter should return subset or equal");
        
        // All adminCreateLogs should be in allAdminLogs
        assertTrue(adminCreateLogs.stream()
                .allMatch(ac -> allAdminLogs.stream()
                        .anyMatch(aa -> aa.getId().equals(ac.getId()))),
                "All admin CREATE logs should be in admin logs");
    }
}
