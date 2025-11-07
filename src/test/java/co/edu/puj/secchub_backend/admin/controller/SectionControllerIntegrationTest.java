package co.edu.puj.secchub_backend.admin.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

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
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.testcontainers.junit.jupiter.Testcontainers;

import co.edu.puj.secchub_backend.DatabaseContainerIntegration;
import co.edu.puj.secchub_backend.R2dbcTestUtils;
import co.edu.puj.secchub_backend.admin.dto.SectionResponseDTO;
import co.edu.puj.secchub_backend.security.jwt.JwtTokenProvider;
import io.r2dbc.spi.ConnectionFactory;

@SpringBootTest
@AutoConfigureWebTestClient
@Testcontainers
@DisplayName("Section Controller Integration Tests")
class SectionControllerIntegrationTest extends DatabaseContainerIntegration {
    
    @Autowired
    private WebTestClient webTestClient;

    @Autowired
    private DatabaseClient databaseClient;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private ConnectionFactory connectionFactory;

    @BeforeEach
    void setUp() {
        R2dbcTestUtils.executeScripts(connectionFactory,
                "/test-cleanup.sql",
                "/test-users.sql",
                "/test-sections.sql",
                "/test-courses.sql",
                "/test-semesters.sql",
                "/test-teachers.sql",
                "/test-classrooms.sql",
                "/test-classes.sql",
                "/test-class-schedules.sql",
                "/test-teacher-classes.sql"
        );
    }

    // ==========================================
    // Test Data Providers
    // ==========================================
    
    /**
     * Provides all authenticated roles for viewing sections
     */
    private static Stream<Arguments> authenticatedRolesProvider() {
        return Stream.of(
            Arguments.of("testAdmin@example.com", "ROLE_ADMIN"),
            Arguments.of("testUser@example.com", "ROLE_USER"),
            Arguments.of("testStudent@example.com", "ROLE_STUDENT"),
            Arguments.of("testTeacher@example.com", "ROLE_TEACHER"),
            Arguments.of("testProgram@example.com", "ROLE_PROGRAM")
        );
    }

    /**
     * Provides admin and user roles for section management
     */
    private static Stream<Arguments> adminAndUserRolesProvider() {
        return Stream.of(
            Arguments.of("testAdmin@example.com", "ROLE_ADMIN"),
            Arguments.of("testUser@example.com", "ROLE_USER")
        );
    }

    /**
     * Provides user role for section-specific operations
     */
    private static Stream<Arguments> userRoleProvider() {
        return Stream.of(
            Arguments.of("testUser@example.com", "ROLE_USER")
        );
    }

    // ==========================================
    // GET All Sections Tests
    // ==========================================
    
    @ParameterizedTest(name = "GET /sections as {1} should return all sections")
    @MethodSource("authenticatedRolesProvider")
    @DisplayName("GET /sections authenticated user should receive all sections list")
    void getAllSections_asAuthenticated_returnsList(String email, String role) {
        String token = jwtTokenProvider.generateToken(email, role);

        Long sectionCount = databaseClient.sql("SELECT COUNT(*) FROM section")
                .map(row -> row.get(0, Long.class))
                .one()
                .block();
        assertNotNull(sectionCount, "Section count from DB should not be null");

        List<SectionResponseDTO> list = webTestClient.get()
                .uri("/sections")
                .header("Authorization", "Bearer " + token)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(SectionResponseDTO.class)
                .returnResult()
                .getResponseBody();

        assertNotNull(list, "Response list should not be null");
        assertEquals(sectionCount.intValue(), list.size(), "Returned section list size should match DB count");
    }

    @Test
    @DisplayName("GET /sections unauthenticated should return 401 Unauthorized")
    void getAllSections_unauthenticated_returns401() {
        webTestClient.get()
                .uri("/sections")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isUnauthorized();
    }

    // ==========================================
    // GET Section by ID Tests
    // ==========================================
    
    @ParameterizedTest(name = "GET /sections/:id as {1} should return section")
    @MethodSource("authenticatedRolesProvider")
    @DisplayName("GET /sections/:id authenticated user should receive specific section")
    void getSectionById_asAuthenticated_returnsSection(String email, String role) {
        String token = jwtTokenProvider.generateToken(email, role);

        // Get first section ID from DB
        Long sectionId = databaseClient.sql("SELECT id FROM section ORDER BY id ASC LIMIT 1")
                .map(row -> row.get(0, Long.class))
                .one()
                .block();
        assertNotNull(sectionId, "Section ID from DB should not be null");

        SectionResponseDTO dto = webTestClient.get()
                .uri("/sections/" + sectionId)
                .header("Authorization", "Bearer " + token)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody(SectionResponseDTO.class)
                .returnResult()
                .getResponseBody();

        assertNotNull(dto, "Response DTO should not be null");
        assertEquals(sectionId, dto.getId(), "Section ID should match");
        assertNotNull(dto.getName(), "Section name should not be null");
        assertNotNull(dto.getUserId(), "Section user ID should not be null");
    }

    @ParameterizedTest(name = "GET /sections/:id with non-existent ID as {1} should return 404")
    @MethodSource("authenticatedRolesProvider")
    @DisplayName("GET /sections/:id with non-existent ID should return 404 Not Found")
    void getSectionById_nonExistent_returns404(String email, String role) {
        String token = jwtTokenProvider.generateToken(email, role);
        Long nonExistentId = Long.MAX_VALUE;

        webTestClient.get()
                .uri("/sections/" + nonExistentId)
                .header("Authorization", "Bearer " + token)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    @DisplayName("GET /sections/:id unauthenticated should return 401 Unauthorized")
    void getSectionById_unauthenticated_returns401() {
        Long sectionId = databaseClient.sql("SELECT id FROM section LIMIT 1")
                .map(row -> row.get(0, Long.class))
                .one()
                .block();
        assertNotNull(sectionId, "Section ID from DB should not be null");

        webTestClient.get()
                .uri("/sections/" + sectionId)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isUnauthorized();
    }

    // ==========================================
    // GET Section by User ID Tests
    // ==========================================
    
    @ParameterizedTest(name = "GET /sections/user/:userId as {1} should return section")
    @MethodSource("adminAndUserRolesProvider")
    @DisplayName("GET /sections/user/:userId should return section for user")
    void getSectionByUserId_asAuthorized_returnsSection(String email, String role) {
        String token = jwtTokenProvider.generateToken(email, role);

        // Get a user ID that has a section
        Long userId = databaseClient.sql("SELECT user_id FROM section ORDER BY id ASC LIMIT 1")
                .map(row -> row.get(0, Long.class))
                .one()
                .block();
        assertNotNull(userId, "User ID from DB should not be null");

        SectionResponseDTO dto = webTestClient.get()
                .uri("/sections/user/" + userId)
                .header("Authorization", "Bearer " + token)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody(SectionResponseDTO.class)
                .returnResult()
                .getResponseBody();

        assertNotNull(dto, "Response DTO should not be null");
        assertEquals(userId, dto.getUserId(), "User ID should match");
    }

    @Test
    @DisplayName("GET /sections/user/:userId with non-existent user should return 404")
    void getSectionByUserId_nonExistent_returns404() {
        String token = jwtTokenProvider.generateToken("testAdmin@example.com", "ROLE_ADMIN");
        Long nonExistentUserId = 999999L;

        webTestClient.get()
                .uri("/sections/user/" + nonExistentUserId)
                .header("Authorization", "Bearer " + token)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isNotFound();
    }

    // ==========================================
    // POST Close Planning Tests
    // ==========================================
    
    @ParameterizedTest(name = "POST /sections/close-planning as {1} should close planning")
    @MethodSource("userRoleProvider")
    @DisplayName("POST /sections/close-planning as USER should close planning for their section")
    void closePlanning_asUser_closesSuccessfully(String email, String role) {
        String token = jwtTokenProvider.generateToken(email, role);

        // Verify section exists and planning is initially open
        Long userId = databaseClient.sql("SELECT id FROM users WHERE email = :email")
                .bind("email", email)
                .map(row -> row.get(0, Long.class))
                .one()
                .block();
        assertNotNull(userId, "User ID should not be null");

        Boolean initialPlanningStatus = databaseClient
            .sql("SELECT planning_closed FROM section WHERE user_id = :userId")
            .bind("userId", userId)
            .map(row -> row.get("planning_closed", Boolean.class))
            .one()
            .block();
        assertNotNull(initialPlanningStatus, "Initial planning status should not be null");

        SectionResponseDTO dto = webTestClient.post()
                .uri("/sections/close-planning")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody(SectionResponseDTO.class)
                .returnResult()
                .getResponseBody();

        assertNotNull(dto, "Response DTO should not be null");
        assertEquals(true, dto.isPlanningClosed(), "Planning should be closed");

        // Verify in database
        Boolean closedStatus = databaseClient.sql("SELECT planning_closed FROM section WHERE user_id = :userId")
                .bind("userId", userId)
                .map(row -> row.get(0, Boolean.class))
                .one()
                .block();
        assertEquals(true, closedStatus, "Planning should be closed in database");
    }

    @Test
    @DisplayName("POST /sections/close-planning as non-USER should return 403")
    void closePlanning_asNonUser_returns403() {
        String token = jwtTokenProvider.generateToken("testAdmin@example.com", "ROLE_ADMIN");

        webTestClient.post()
                .uri("/sections/close-planning")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isForbidden();
    }

    // ==========================================
    // GET Is Planning Closed Tests
    // ==========================================
    
    @ParameterizedTest(name = "GET /sections/is-planning-closed as {1} should return planning status")
    @MethodSource("userRoleProvider")
    @DisplayName("GET /sections/is-planning-closed as USER should return their section's planning status")
    void isPlanningClosed_asUser_returnsStatus(String email, String role) {
        String token = jwtTokenProvider.generateToken(email, role);

        Boolean planningClosed = webTestClient.get()
                .uri("/sections/is-planning-closed")
                .header("Authorization", "Bearer " + token)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody(Boolean.class)
                .returnResult()
                .getResponseBody();

        assertNotNull(planningClosed, "Planning closed status should not be null");
        
        // Verify against database
        Long userId = databaseClient.sql("SELECT id FROM users WHERE email = :email")
                .bind("email", email)
                .map(row -> row.get(0, Long.class))
                .one()
                .block();
        
        Boolean dbStatus = databaseClient.sql("SELECT planning_closed FROM section WHERE user_id = :userId")
                .bind("userId", userId)
                .map(row -> row.get(0, Boolean.class))
                .one()
                .block();
        
        assertEquals(dbStatus, planningClosed, "Planning status should match database");
    }

    @Test
    @DisplayName("GET /sections/is-planning-closed as non-USER should return 403")
    void isPlanningClosed_asNonUser_returns403() {
        String token = jwtTokenProvider.generateToken("testAdmin@example.com", "ROLE_ADMIN");

        webTestClient.get()
                .uri("/sections/is-planning-closed")
                .header("Authorization", "Bearer " + token)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isForbidden();
    }

    // ==========================================
    // Business Logic Tests
    // ==========================================
    
    @Test
    @DisplayName("Multiple sections should have different coordinators")
    void multipleSections_shouldHaveDifferentCoordinators() {
        String token = jwtTokenProvider.generateToken("testAdmin@example.com", "ROLE_ADMIN");

        List<SectionResponseDTO> list = webTestClient.get()
                .uri("/sections")
                .header("Authorization", "Bearer " + token)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(SectionResponseDTO.class)
                .returnResult()
                .getResponseBody();

        assertNotNull(list, "Section list should not be null");
        assertFalse(list.isEmpty(), "Section list should not be empty");

        // Verify each section has a unique user_id (coordinator)
        long uniqueUserIds = list.stream()
                .map(SectionResponseDTO::getUserId)
                .distinct()
                .count();
        
        // At least some sections should have different coordinators
        // (In test data, we expect at least 2 different coordinators)
        assertEquals(list.size(), uniqueUserIds, "Each section should have a unique coordinator");
    }

    @Test
    @DisplayName("Section planning status should toggle correctly")
    void sectionPlanningStatus_shouldToggle() {
        String token = jwtTokenProvider.generateToken("testUser@example.com", "ROLE_USER");

        // Get initial status
        Boolean initialStatus = webTestClient.get()
                .uri("/sections/is-planning-closed")
                .header("Authorization", "Bearer " + token)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody(Boolean.class)
                .returnResult()
                .getResponseBody();

        assertNotNull(initialStatus, "Initial status should not be null");

        // Close planning
        webTestClient.post()
                .uri("/sections/close-planning")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk();

        // Verify status changed
        Boolean newStatus = webTestClient.get()
                .uri("/sections/is-planning-closed")
                .header("Authorization", "Bearer " + token)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody(Boolean.class)
                .returnResult()
                .getResponseBody();

        assertNotNull(newStatus, "New status should not be null");
        assertEquals(true, newStatus, "Planning should be closed after closing");
    }

    // ==========================================
    // GET Planning Status Stats Tests
    // ==========================================
    
    @Test
    @DisplayName("GET /sections/planning-status-stats as ADMIN should return correct statistics")
    void getPlanningStatusStats_asAdmin_returnsStats() {
        String token = jwtTokenProvider.generateToken("testAdmin@example.com", "ROLE_ADMIN");

        // Get counts from database
        Long totalCount = databaseClient.sql("SELECT COUNT(*) FROM section")
                .map(row -> row.get(0, Long.class))
                .one()
                .block();
        
        Long closedCount = databaseClient.sql("SELECT COUNT(*) FROM section WHERE planning_closed = TRUE")
                .map(row -> row.get(0, Long.class))
                .one()
                .block();
        
        Long openCount = totalCount - closedCount;

        webTestClient.get()
                .uri("/sections/planning-status-stats")
                .header("Authorization", "Bearer " + token)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.openCount").isEqualTo(openCount.intValue())
                .jsonPath("$.closedCount").isEqualTo(closedCount.intValue())
                .jsonPath("$.totalCount").isEqualTo(totalCount.intValue());
    }

    @Test
    @DisplayName("GET /sections/planning-status-stats as non-ADMIN should return 403")
    void getPlanningStatusStats_asNonAdmin_returns403() {
        String token = jwtTokenProvider.generateToken("testUser@example.com", "ROLE_USER");

        webTestClient.get()
                .uri("/sections/planning-status-stats")
                .header("Authorization", "Bearer " + token)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isForbidden();
    }

    @Test
    @DisplayName("GET /sections/planning-status-stats unauthenticated should return 401")
    void getPlanningStatusStats_unauthenticated_returns401() {
        webTestClient.get()
                .uri("/sections/planning-status-stats")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isUnauthorized();
    }

    // ==========================================
    // GET Sections Summary Tests
    // ==========================================
    
    @Test
    @DisplayName("GET /sections/summary as ADMIN should return summaries with class counts")
    void getSectionsSummary_asAdmin_returnsSummaries() {
        String token = jwtTokenProvider.generateToken("testAdmin@example.com", "ROLE_ADMIN");

        // Get section count from database
        Long sectionCount = databaseClient.sql("SELECT COUNT(*) FROM section")
                .map(row -> row.get(0, Long.class))
                .one()
                .block();
        assertNotNull(sectionCount, "Section count should not be null");

        webTestClient.get()
                .uri("/sections/summary")
                .header("Authorization", "Bearer " + token)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.length()").isEqualTo(sectionCount.intValue())
                .jsonPath("$[0].name").exists()
                .jsonPath("$[0].planningClosed").exists()
                .jsonPath("$[0].assignedClasses").exists()
                .jsonPath("$[0].unconfirmedTeachers").exists();
    }

    @Test
    @DisplayName("GET /sections/summary should return correct class counts from database")
    void getSectionsSummary_shouldReturnCorrectClassCounts() {
        String token = jwtTokenProvider.generateToken("testAdmin@example.com", "ROLE_ADMIN");

        // Get first section and calculate expected values
        Long sectionId = databaseClient.sql("SELECT id FROM section ORDER BY id ASC LIMIT 1")
                .map(row -> row.get(0, Long.class))
                .one()
                .block();
        assertNotNull(sectionId, "Section ID should not be null");

        // Count classes in current semester for this section
        Long expectedClassCount = databaseClient.sql(
                """
                SELECT COUNT(c.id)
                FROM class c
                INNER JOIN course co ON c.course_id = co.id
                INNER JOIN semester s ON c.semester_id = s.id
                WHERE co.section_id = :sectionId
                AND s.is_current = TRUE
                """)
                .bind("sectionId", sectionId)
                .map(row -> row.get(0, Long.class))
                .one()
                .block();

        // Count classes without teachers in current semester for this section
        Long expectedClassesWithoutTeachers = databaseClient.sql(
                """
                SELECT COUNT(c.id)
                FROM class c
                INNER JOIN course co ON c.course_id = co.id
                INNER JOIN semester s ON c.semester_id = s.id
                LEFT JOIN teacher_class tc ON c.id = tc.class_id
                WHERE co.section_id = :sectionId
                AND s.is_current = TRUE
                AND tc.id IS NULL
                """)
                .bind("sectionId", sectionId)
                .map(row -> row.get(0, Long.class))
                .one()
                .block();

        webTestClient.get()
                .uri("/sections/summary")
                .header("Authorization", "Bearer " + token)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$[0].assignedClasses").isEqualTo(expectedClassCount != null ? expectedClassCount.intValue() : 0)
                .jsonPath("$[0].unconfirmedTeachers").isEqualTo(expectedClassesWithoutTeachers != null ? expectedClassesWithoutTeachers.intValue() : 0);
    }

    @Test
    @DisplayName("GET /sections/summary should reflect planning status from database")
    void getSectionsSummary_shouldReflectPlanningStatus() {
        String token = jwtTokenProvider.generateToken("testAdmin@example.com", "ROLE_ADMIN");

        // Get a section's planning status from database
        Boolean expectedPlanningClosed = databaseClient.sql(
                "SELECT planning_closed FROM section ORDER BY id ASC LIMIT 1")
                .map(row -> row.get(0, Boolean.class))
                .one()
                .block();
        assertNotNull(expectedPlanningClosed, "Planning closed status should not be null");

        webTestClient.get()
                .uri("/sections/summary")
                .header("Authorization", "Bearer " + token)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$[0].planningClosed").isEqualTo(expectedPlanningClosed);
    }

    @Test
    @DisplayName("GET /sections/summary as non-ADMIN should return 403")
    void getSectionsSummary_asNonAdmin_returns403() {
        String token = jwtTokenProvider.generateToken("testUser@example.com", "ROLE_USER");

        webTestClient.get()
                .uri("/sections/summary")
                .header("Authorization", "Bearer " + token)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isForbidden();
    }

    @Test
    @DisplayName("GET /sections/summary unauthenticated should return 401")
    void getSectionsSummary_unauthenticated_returns401() {
        webTestClient.get()
                .uri("/sections/summary")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isUnauthorized();
    }

    @Test
    @DisplayName("GET /sections/summary should handle sections with no classes gracefully")
    void getSectionsSummary_withNoClasses_shouldReturnZeroCounts() {
        String token = jwtTokenProvider.generateToken("testAdmin@example.com", "ROLE_ADMIN");

        // Find a section with no classes (if any)
        webTestClient.get()
                .uri("/sections/summary")
                .header("Authorization", "Bearer " + token)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$").isArray()
                .jsonPath("$[*].assignedClasses").exists()
                .jsonPath("$[*].unconfirmedTeachers").exists();
    }
}
