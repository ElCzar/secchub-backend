package co.edu.puj.secchub_backend.admin.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
import co.edu.puj.secchub_backend.admin.dto.TeacherResponseDTO;
import co.edu.puj.secchub_backend.admin.dto.TeacherUpdateRequestDTO;
import co.edu.puj.secchub_backend.security.jwt.JwtTokenProvider;
import io.r2dbc.spi.ConnectionFactory;

@SpringBootTest
@AutoConfigureWebTestClient
@Testcontainers
@DisplayName("Teacher Controller Integration Tests")
class TeacherControllerIntegrationTest extends DatabaseContainerIntegration {
    
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
                "/test-teachers.sql"
        );
    }

    // ==========================================
    // Test Data Providers
    // ==========================================
    
    /**
     * Provides admin and user roles for viewing teachers
     */
    private static Stream<Arguments> adminAndUserRolesProvider() {
        return Stream.of(
            Arguments.of("testAdmin@example.com", "ROLE_ADMIN"),
            Arguments.of("testUser@example.com", "ROLE_USER")
        );
    }

    /**
     * Provides roles with teacher access (admin, user, teacher)
     */
    private static Stream<Arguments> teacherAccessRolesProvider() {
        return Stream.of(
            Arguments.of("testAdmin@example.com", "ROLE_ADMIN"),
            Arguments.of("testUser@example.com", "ROLE_USER"),
            Arguments.of("testTeacher@example.com", "ROLE_TEACHER")
        );
    }

    /**
     * Provides admin role only
     */
    private static Stream<Arguments> adminRoleProvider() {
        return Stream.of(
            Arguments.of("testAdmin@example.com", "ROLE_ADMIN")
        );
    }

    /**
     * Provides non-admin roles for update forbidden tests
     */
    private static Stream<Arguments> nonAdminRolesProvider() {
        return Stream.of(
            Arguments.of("testUser@example.com", "ROLE_USER"),
            Arguments.of("testStudent@example.com", "ROLE_STUDENT"),
            Arguments.of("testTeacher@example.com", "ROLE_TEACHER"),
            Arguments.of("testProgram@example.com", "ROLE_PROGRAM")
        );
    }

    /**
     * Provides roles without teacher view access
     */
    private static Stream<Arguments> noAccessRolesProvider() {
        return Stream.of(
            Arguments.of("testStudent@example.com", "ROLE_STUDENT"),
            Arguments.of("testProgram@example.com", "ROLE_PROGRAM")
        );
    }

    // ==========================================
    // GET All Teachers Tests
    // ==========================================
    
    @ParameterizedTest(name = "GET /teachers as {1} should return all teachers")
    @MethodSource("adminAndUserRolesProvider")
    @DisplayName("GET /teachers as admin/user should return list")
    void getAllTeachers_asAdminOrUser_returnsList(String email, String role) {
        String token = jwtTokenProvider.generateToken(email, role);

        List<TeacherResponseDTO> teachers = webTestClient.get()
                .uri("/teachers")
                .header("Authorization", "Bearer " + token)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(TeacherResponseDTO.class)
                .returnResult()
                .getResponseBody();

        assertNotNull(teachers, "Teachers list should not be null");
        assertFalse(teachers.isEmpty(), "Teachers list should not be empty");
        
        // Verify at least one teacher from test data
        assertTrue(teachers.stream()
                .anyMatch(t -> t.getUserId().equals(4L)),
                "Should contain teacher with user ID 4");
    }

    @ParameterizedTest(name = "GET /teachers as {1} should return 403")
    @MethodSource("noAccessRolesProvider")
    @DisplayName("GET /teachers as unauthorized role should return 403")
    void getAllTeachers_asUnauthorized_returns403(String email, String role) {
        String token = jwtTokenProvider.generateToken(email, role);

        webTestClient.get()
                .uri("/teachers")
                .header("Authorization", "Bearer " + token)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isForbidden();
    }

    @Test
    @DisplayName("GET /teachers unauthenticated should return 401")
    void getAllTeachers_unauthenticated_returns401() {
        webTestClient.get()
                .uri("/teachers")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isUnauthorized();
    }

    // ==========================================
    // GET Teacher by ID Tests
    // ==========================================
    
    @ParameterizedTest(name = "GET /teachers/:id as {1} should return teacher")
    @MethodSource("adminAndUserRolesProvider")
    @DisplayName("GET /teachers/:id as admin/user should return teacher")
    void getTeacherById_asAdminOrUser_returnsTeacher(String email, String role) {
        String token = jwtTokenProvider.generateToken(email, role);

        TeacherResponseDTO teacher = webTestClient.get()
                .uri("/teachers/1")
                .header("Authorization", "Bearer " + token)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody(TeacherResponseDTO.class)
                .returnResult()
                .getResponseBody();

        assertNotNull(teacher, "Teacher should not be null");
        assertEquals(1L, teacher.getId(), "Teacher ID should be 1");
        assertEquals(4L, teacher.getUserId(), "User ID should be 4");
        assertEquals(1L, teacher.getEmploymentTypeId(), "Employment type should be 1");
        assertEquals(40, teacher.getMaxHours(), "Max hours should be 40");
    }

    @ParameterizedTest(name = "GET /teachers/:id as {1} should return 403")
    @MethodSource("noAccessRolesProvider")
    @DisplayName("GET /teachers/:id as unauthorized role should return 403")
    void getTeacherById_asUnauthorized_returns403(String email, String role) {
        String token = jwtTokenProvider.generateToken(email, role);

        webTestClient.get()
                .uri("/teachers/1")
                .header("Authorization", "Bearer " + token)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isForbidden();
    }

    @Test
    @DisplayName("GET /teachers/:id with non-existent ID should return 404")
    void getTeacherById_nonExistent_returns404() {
        String token = jwtTokenProvider.generateToken("testAdmin@example.com", "ROLE_ADMIN");

        webTestClient.get()
                .uri("/teachers/9999")
                .header("Authorization", "Bearer " + token)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    @DisplayName("GET /teachers/:id unauthenticated should return 401")
    void getTeacherById_unauthenticated_returns401() {
        webTestClient.get()
                .uri("/teachers/1")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isUnauthorized();
    }

    // ==========================================
    // PUT Update Teacher Tests
    // ==========================================
    
    @ParameterizedTest(name = "PUT /teachers/:id as admin should update teacher")
    @MethodSource("adminRoleProvider")
    @DisplayName("PUT /teachers/:id as admin should update employment type and hours")
    void updateTeacher_asAdmin_updatesSuccessfully(String email, String role) {
        String token = jwtTokenProvider.generateToken(email, role);

        TeacherUpdateRequestDTO updateDTO = TeacherUpdateRequestDTO.builder()
                .employmentTypeId(2L)  // Change to adjunct
                .maxHours(20)           // Reduce hours
                .build();

        TeacherResponseDTO updatedTeacher = webTestClient.put()
                .uri("/teachers/1")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(updateDTO)
                .exchange()
                .expectStatus().isOk()
                .expectBody(TeacherResponseDTO.class)
                .returnResult()
                .getResponseBody();

        assertNotNull(updatedTeacher, "Updated teacher should not be null");
        assertEquals(1L, updatedTeacher.getId(), "Teacher ID should remain 1");
        assertEquals(2L, updatedTeacher.getEmploymentTypeId(), "Employment type should be updated to 2");
        assertEquals(20, updatedTeacher.getMaxHours(), "Max hours should be updated to 20");

        // Verify in database
        Integer dbMaxHours = databaseClient.sql("SELECT max_hours FROM teacher WHERE id = :id")
                .bind("id", 1L)
                .map(row -> row.get("max_hours", Integer.class))
                .one()
                .block();
        assertEquals(20, dbMaxHours, "Max hours should be persisted in database");
    }

    @ParameterizedTest(name = "PUT /teachers/:id as {1} should return 403")
    @MethodSource("nonAdminRolesProvider")
    @DisplayName("PUT /teachers/:id as non-admin should return 403")
    void updateTeacher_asNonAdmin_returns403(String email, String role) {
        String token = jwtTokenProvider.generateToken(email, role);

        TeacherUpdateRequestDTO updateDTO = TeacherUpdateRequestDTO.builder()
                .employmentTypeId(2L)
                .maxHours(20)
                .build();

        webTestClient.put()
                .uri("/teachers/1")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(updateDTO)
                .exchange()
                .expectStatus().isForbidden();
    }

    @Test
    @DisplayName("PUT /teachers/:id with non-existent ID should return 404")
    void updateTeacher_nonExistent_returns404() {
        String token = jwtTokenProvider.generateToken("testAdmin@example.com", "ROLE_ADMIN");

        TeacherUpdateRequestDTO updateDTO = TeacherUpdateRequestDTO.builder()
                .employmentTypeId(2L)
                .maxHours(20)
                .build();

        webTestClient.put()
                .uri("/teachers/9999")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(updateDTO)
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    @DisplayName("PUT /teachers/:id unauthenticated should return 401")
    void updateTeacher_unauthenticated_returns401() {
        TeacherUpdateRequestDTO updateDTO = TeacherUpdateRequestDTO.builder()
                .employmentTypeId(2L)
                .maxHours(20)
                .build();

        webTestClient.put()
                .uri("/teachers/1")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(updateDTO)
                .exchange()
                .expectStatus().isUnauthorized();
    }

    // ==========================================
    // GET Teacher by User ID Tests
    // ==========================================
    
    @ParameterizedTest(name = "GET /teachers/user/:userId as {1} should return teacher")
    @MethodSource("teacherAccessRolesProvider")
    @DisplayName("GET /teachers/user/:userId with authorized role should return teacher")
    void getTeacherByUserId_asAuthorized_returnsTeacher(String email, String role) {
        String token = jwtTokenProvider.generateToken(email, role);

        TeacherResponseDTO teacher = webTestClient.get()
                .uri("/teachers/user/4")  // User ID 4 is testTeacher
                .header("Authorization", "Bearer " + token)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody(TeacherResponseDTO.class)
                .returnResult()
                .getResponseBody();

        assertNotNull(teacher, "Teacher should not be null");
        assertEquals(4L, teacher.getUserId(), "User ID should be 4");
        assertEquals(1L, teacher.getId(), "Teacher ID should be 1");
    }

    @Test
    @DisplayName("GET /teachers/user/:userId with non-existent user should return 404")
    void getTeacherByUserId_nonExistent_returns404() {
        String token = jwtTokenProvider.generateToken("testAdmin@example.com", "ROLE_ADMIN");

        webTestClient.get()
                .uri("/teachers/user/9999")
                .header("Authorization", "Bearer " + token)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    @DisplayName("GET /teachers/user/:userId unauthenticated should return 401")
    void getTeacherByUserId_unauthenticated_returns401() {
        webTestClient.get()
                .uri("/teachers/user/4")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isUnauthorized();
    }

    // ==========================================
    // GET Teachers by Employment Type Tests
    // ==========================================
    
    @ParameterizedTest(name = "GET /teachers/employment-type/:id as {1} should return teachers")
    @MethodSource("adminAndUserRolesProvider")
    @DisplayName("GET /teachers/employment-type/:id should return filtered teachers")
    void getTeachersByEmploymentType_asAdminOrUser_returnsFilteredList(String email, String role) {
        String token = jwtTokenProvider.generateToken(email, role);

        List<TeacherResponseDTO> teachers = webTestClient.get()
                .uri("/teachers/employment-type/1")  // Full-time
                .header("Authorization", "Bearer " + token)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(TeacherResponseDTO.class)
                .returnResult()
                .getResponseBody();

        assertNotNull(teachers, "Teachers list should not be null");
        assertFalse(teachers.isEmpty(), "Should have at least one full-time teacher");
        
        // Verify all returned teachers have correct employment type
        assertTrue(teachers.stream()
                .allMatch(t -> t.getEmploymentTypeId().equals(1L)),
                "All teachers should have employment type 1");
    }

    @ParameterizedTest(name = "GET /teachers/employment-type/:id as {1} should return 403")
    @MethodSource("noAccessRolesProvider")
    @DisplayName("GET /teachers/employment-type/:id as unauthorized role should return 403")
    void getTeachersByEmploymentType_asUnauthorized_returns403(String email, String role) {
        String token = jwtTokenProvider.generateToken(email, role);

        webTestClient.get()
                .uri("/teachers/employment-type/1")
                .header("Authorization", "Bearer " + token)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isForbidden();
    }

    @Test
    @DisplayName("GET /teachers/employment-type/:id unauthenticated should return 401")
    void getTeachersByEmploymentType_unauthenticated_returns401() {
        webTestClient.get()
                .uri("/teachers/employment-type/1")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isUnauthorized();
    }

    // ==========================================
    // GET Teachers with Minimum Hours Tests
    // ==========================================
    
    @ParameterizedTest(name = "GET /teachers/min-hours/:hours as {1} should return teachers")
    @MethodSource("adminAndUserRolesProvider")
    @DisplayName("GET /teachers/min-hours/:hours should return teachers meeting criteria")
    void getTeachersWithMinHours_asAdminOrUser_returnsFilteredList(String email, String role) {
        String token = jwtTokenProvider.generateToken(email, role);

        List<TeacherResponseDTO> teachers = webTestClient.get()
                .uri("/teachers/min-hours/30")  // At least 30 hours
                .header("Authorization", "Bearer " + token)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(TeacherResponseDTO.class)
                .returnResult()
                .getResponseBody();

        assertNotNull(teachers, "Teachers list should not be null");
        assertFalse(teachers.isEmpty(), "Should have at least one teacher with 30+ hours");
        
        // Verify all returned teachers meet minimum hours requirement
        assertTrue(teachers.stream()
                .allMatch(t -> t.getMaxHours() >= 30),
                "All teachers should have at least 30 max hours");
    }

    @Test
    @DisplayName("GET /teachers/min-hours/:hours with high threshold should return empty list")
    void getTeachersWithMinHours_highThreshold_returnsEmptyList() {
        String token = jwtTokenProvider.generateToken("testAdmin@example.com", "ROLE_ADMIN");

        List<TeacherResponseDTO> teachers = webTestClient.get()
                .uri("/teachers/min-hours/100")  // Very high threshold
                .header("Authorization", "Bearer " + token)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(TeacherResponseDTO.class)
                .returnResult()
                .getResponseBody();

        assertNotNull(teachers, "Teachers list should not be null");
        assertTrue(teachers.isEmpty(), "Should have no teachers with 100+ hours");
    }

    @ParameterizedTest(name = "GET /teachers/min-hours/:hours as {1} should return 403")
    @MethodSource("noAccessRolesProvider")
    @DisplayName("GET /teachers/min-hours/:hours as unauthorized role should return 403")
    void getTeachersWithMinHours_asUnauthorized_returns403(String email, String role) {
        String token = jwtTokenProvider.generateToken(email, role);

        webTestClient.get()
                .uri("/teachers/min-hours/30")
                .header("Authorization", "Bearer " + token)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isForbidden();
    }

    @Test
    @DisplayName("GET /teachers/min-hours/:hours unauthenticated should return 401")
    void getTeachersWithMinHours_unauthenticated_returns401() {
        webTestClient.get()
                .uri("/teachers/min-hours/30")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isUnauthorized();
    }

    // ==========================================
    // Business Logic Tests
    // ==========================================
    
    @Test
    @DisplayName("Updating teacher with partial data should preserve other fields")
    void updateTeacher_partialUpdate_preservesOtherFields() {
        String token = jwtTokenProvider.generateToken("testAdmin@example.com", "ROLE_ADMIN");

        // Update only max hours
        TeacherUpdateRequestDTO updateDTO = TeacherUpdateRequestDTO.builder()
                .maxHours(35)
                .build();

        TeacherResponseDTO updatedTeacher = webTestClient.put()
                .uri("/teachers/1")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(updateDTO)
                .exchange()
                .expectStatus().isOk()
                .expectBody(TeacherResponseDTO.class)
                .returnResult()
                .getResponseBody();

        assertNotNull(updatedTeacher, "Updated teacher should not be null");
        assertEquals(35, updatedTeacher.getMaxHours(), "Max hours should be updated");
        assertEquals(1L, updatedTeacher.getEmploymentTypeId(), "Employment type should be preserved");
        assertEquals(4L, updatedTeacher.getUserId(), "User ID should be preserved");
    }

    @Test
    @DisplayName("Filtering by employment type should only return matching teachers")
    void getTeachersByEmploymentType_shouldFilterCorrectly() {
        String token = jwtTokenProvider.generateToken("testAdmin@example.com", "ROLE_ADMIN");

        // Get full-time teachers
        List<TeacherResponseDTO> fullTimeTeachers = webTestClient.get()
                .uri("/teachers/employment-type/1")
                .header("Authorization", "Bearer " + token)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(TeacherResponseDTO.class)
                .returnResult()
                .getResponseBody();

        // Get adjunct teachers
        List<TeacherResponseDTO> adjunctTeachers = webTestClient.get()
                .uri("/teachers/employment-type/2")
                .header("Authorization", "Bearer " + token)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(TeacherResponseDTO.class)
                .returnResult()
                .getResponseBody();

        assertNotNull(fullTimeTeachers, "Full-time teachers list should not be null");
        assertNotNull(adjunctTeachers, "Adjunct teachers list should not be null");
        
        // Verify no overlap
        assertTrue(fullTimeTeachers.stream()
                .noneMatch(ft -> adjunctTeachers.stream()
                        .anyMatch(at -> at.getId().equals(ft.getId()))),
                "Full-time and adjunct lists should not overlap");
    }
}
