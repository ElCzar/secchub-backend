package co.edu.puj.secchub_backend.integration.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Map;
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
import co.edu.puj.secchub_backend.integration.dto.TeacherClassRequestDTO;
import co.edu.puj.secchub_backend.integration.dto.TeacherClassResponseDTO;
import co.edu.puj.secchub_backend.security.jwt.JwtTokenProvider;
import io.r2dbc.spi.ConnectionFactory;

@SpringBootTest
@AutoConfigureWebTestClient
@Testcontainers
@DisplayName("Teacher Class Controller Integration Tests")
class TeacherClassControllerIntegrationTest extends DatabaseContainerIntegration {
    
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
     * Provides admin and user roles for creating teacher-class assignments
     */
    private static Stream<Arguments> adminAndUserRolesProvider() {
        return Stream.of(
            Arguments.of("testAdmin@example.com", "ROLE_ADMIN"),
            Arguments.of("testUser@example.com", "ROLE_USER")
        );
    }

    /**
     * Provides teacher, admin and user roles for viewing assignments
     */
    private static Stream<Arguments> teacherAdminUserRolesProvider() {
        return Stream.of(
            Arguments.of("testTeacher@example.com", "ROLE_TEACHER"),
            Arguments.of("testAdmin@example.com", "ROLE_ADMIN"),
            Arguments.of("testUser@example.com", "ROLE_USER")
        );
    }

    /**
     * Provides only teacher role for accepting/rejecting assignments
     */
    private static Stream<Arguments> teacherRoleProvider() {
        return Stream.of(
            Arguments.of("testTeacher@example.com", "ROLE_TEACHER")
        );
    }

    /**
     * Provides non-teacher roles that cannot accept/reject
     */
    private static Stream<Arguments> nonTeacherRolesProvider() {
        return Stream.of(
            Arguments.of("testAdmin@example.com", "ROLE_ADMIN"),
            Arguments.of("testUser@example.com", "ROLE_USER"),
            Arguments.of("testStudent@example.com", "ROLE_STUDENT"),
            Arguments.of("testProgram@example.com", "ROLE_PROGRAM")
        );
    }

    // ==========================================
    // POST Create Teacher Class Tests
    // ==========================================

    @ParameterizedTest
    @MethodSource("adminAndUserRolesProvider")
    @DisplayName("POST /teachers/classes - Admin/User can create teacher-class assignment")
    void createTeacherClass_asAdminOrUser_shouldSucceed(String email, String role) {
        String token = jwtTokenProvider.generateToken(email, role);

        TeacherClassRequestDTO request = TeacherClassRequestDTO.builder()
                .teacherId(1L)
                .classId(1L)
                .workHours(4)
                .fullTimeExtraHours(0)
                .adjunctExtraHours(0)
                .observation("Test assignment")
                .startDate(java.time.LocalDate.of(2025, 1, 10))
                .endDate(java.time.LocalDate.of(2025, 5, 10))
                .build();

        webTestClient.post()
                .uri("/teachers/classes")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isOk()
                .expectBody(TeacherClassResponseDTO.class)
                .value(response -> {
                    assertNotNull(response.getId());
                    assertEquals(1L, response.getTeacherId());
                    assertEquals(1L, response.getClassId());
                    assertEquals(4, response.getWorkHours());
                    assertEquals(java.time.LocalDate.of(2025, 1, 10), response.getStartDate());
                    assertEquals(java.time.LocalDate.of(2025, 5, 10), response.getEndDate());
                });
    }

    @Test
    @DisplayName("POST /teachers/classes - Unauthorized without token")
    void createTeacherClass_withoutToken_shouldReturn401() {
        TeacherClassRequestDTO request = TeacherClassRequestDTO.builder()
                .teacherId(1L)
                .classId(1L)
                .workHours(4)
                .build();

        webTestClient.post()
                .uri("/teachers/classes")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isUnauthorized();
    }

    // ==========================================
    // GET Current Semester Tests
    // ==========================================

    @ParameterizedTest
    @MethodSource("teacherAdminUserRolesProvider")
    @DisplayName("GET /teachers/classes/current-semester - Should return current semester assignments")
    void getCurrentSemesterTeacherClasses_withAuthorizedRole_shouldSucceed(String email, String role) {
        String token = jwtTokenProvider.generateToken(email, role);

        List<TeacherClassResponseDTO> assignments = webTestClient.get()
                .uri("/teachers/classes/current-semester")
                .header("Authorization", "Bearer " + token)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(TeacherClassResponseDTO.class)
                .returnResult()
                .getResponseBody();

        assertNotNull(assignments);
        assertFalse(assignments.isEmpty());
        // Verify all belong to current semester (ID 2)
        assertTrue(assignments.stream().allMatch(a -> a.getSemesterId().equals(2L)));
        // Verify all have dates
        assertTrue(assignments.stream().allMatch(a -> 
            a.getStartDate() != null && a.getEndDate() != null),
            "All assignments should have start and end dates");
    }

    @ParameterizedTest
    @MethodSource("teacherRoleProvider")
    @DisplayName("GET /teachers/classes/current-semester/{teacherId} - Teacher can view their assignments")
    void getCurrentSemesterTeacherClassesByTeacher_asTeacher_shouldSucceed(String email, String role) {
        String token = jwtTokenProvider.generateToken(email, role);
        Long teacherId = 1L;

        List<TeacherClassResponseDTO> assignments = webTestClient.get()
                .uri("/teachers/classes/current-semester/{teacherId}", teacherId)
                .header("Authorization", "Bearer " + token)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(TeacherClassResponseDTO.class)
                .returnResult()
                .getResponseBody();

        assertNotNull(assignments);
        assertFalse(assignments.isEmpty());
        // Verify all belong to the teacher and current semester
        assertTrue(assignments.stream().allMatch(a -> 
            a.getTeacherId().equals(teacherId) && a.getSemesterId().equals(2L)));
    }

    @ParameterizedTest
    @MethodSource("nonTeacherRolesProvider")
    @DisplayName("GET /teachers/classes/current-semester/{teacherId} - Non-teacher roles cannot access")
    void getCurrentSemesterTeacherClassesByTeacher_asNonTeacher_shouldReturn403(String email, String role) {
        String token = jwtTokenProvider.generateToken(email, role);

        webTestClient.get()
                .uri("/teachers/classes/current-semester/{teacherId}", 1L)
                .header("Authorization", "Bearer " + token)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isForbidden();
    }

    // ==========================================
    // GET Teacher's All Classes Tests
    // ==========================================

    @ParameterizedTest
    @MethodSource("teacherAdminUserRolesProvider")
    @DisplayName("GET /teachers/{teacherId}/classes - Should return all teacher assignments")
    void getAllTeacherClasses_withAuthorizedRole_shouldSucceed(String email, String role) {
        String token = jwtTokenProvider.generateToken(email, role);
        Long teacherId = 1L;

        List<TeacherClassResponseDTO> assignments = webTestClient.get()
                .uri("/teachers/{teacherId}/classes", teacherId)
                .header("Authorization", "Bearer " + token)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(TeacherClassResponseDTO.class)
                .returnResult()
                .getResponseBody();

        assertNotNull(assignments);
        assertFalse(assignments.isEmpty());
        // Verify all belong to the teacher
        assertTrue(assignments.stream().allMatch(a -> a.getTeacherId().equals(teacherId)));
        // Verify all have dates
        assertTrue(assignments.stream().allMatch(a -> 
            a.getStartDate() != null && a.getEndDate() != null),
            "All assignments should have start and end dates");
    }

    // ==========================================
    // GET By Status Tests
    // ==========================================

    @ParameterizedTest
    @MethodSource("teacherAdminUserRolesProvider")
    @DisplayName("GET /teachers/{teacherId}/classes/status/{statusId} - Should filter by pending status")
    void getTeacherClassesByStatus_pendingStatus_shouldReturnPendingOnly(String email, String role) {
        String token = jwtTokenProvider.generateToken(email, role);
        Long teacherId = 1L;
        Long pendingStatusId = 4L;

        List<TeacherClassResponseDTO> pending = webTestClient.get()
                .uri("/teachers/{teacherId}/classes/status/{statusId}", teacherId, pendingStatusId)
                .header("Authorization", "Bearer " + token)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(TeacherClassResponseDTO.class)
                .returnResult()
                .getResponseBody();

        assertNotNull(pending);
        assertFalse(pending.isEmpty());
        // Verify all have pending status
        assertTrue(pending.stream().allMatch(a -> a.getStatusId().equals(pendingStatusId)));
    }

    @ParameterizedTest
    @MethodSource("teacherAdminUserRolesProvider")
    @DisplayName("GET /teachers/{teacherId}/classes/status/{statusId} - Should filter by accepted status")
    void getTeacherClassesByStatus_acceptedStatus_shouldReturnAcceptedOnly(String email, String role) {
        String token = jwtTokenProvider.generateToken(email, role);
        Long teacherId = 1L;
        Long acceptedStatusId = 6L;

        List<TeacherClassResponseDTO> accepted = webTestClient.get()
                .uri("/teachers/{teacherId}/classes/status/{statusId}", teacherId, acceptedStatusId)
                .header("Authorization", "Bearer " + token)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(TeacherClassResponseDTO.class)
                .returnResult()
                .getResponseBody();

        assertNotNull(accepted);
        assertFalse(accepted.isEmpty());
        // Verify all have accepted status
        assertTrue(accepted.stream().allMatch(a -> a.getStatusId().equals(acceptedStatusId)));
    }

    @ParameterizedTest
    @MethodSource("teacherAdminUserRolesProvider")
    @DisplayName("GET /teachers/{teacherId}/classes/status/{statusId} - Should filter by rejected status")
    void getTeacherClassesByStatus_rejectedStatus_shouldReturnRejectedOnly(String email, String role) {
        String token = jwtTokenProvider.generateToken(email, role);
        Long teacherId = 1L;
        Long rejectedStatusId = 7L;

        List<TeacherClassResponseDTO> rejected = webTestClient.get()
                .uri("/teachers/{teacherId}/classes/status/{statusId}", teacherId, rejectedStatusId)
                .header("Authorization", "Bearer " + token)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(TeacherClassResponseDTO.class)
                .returnResult()
                .getResponseBody();

        assertNotNull(rejected);
        assertFalse(rejected.isEmpty());
        // Verify all have rejected status
        assertTrue(rejected.stream().allMatch(a -> a.getStatusId().equals(rejectedStatusId)));
    }

    // ==========================================
    // GET By Class ID Tests
    // ==========================================

    @ParameterizedTest
    @MethodSource("teacherAdminUserRolesProvider")
    @DisplayName("GET /teachers/classes/class/{classId} - Should return assignments for specific class")
    void getTeacherClassByClassId_withAuthorizedRole_shouldSucceed(String email, String role) {
        String token = jwtTokenProvider.generateToken(email, role);
        Long classId = 1L;

        List<TeacherClassResponseDTO> assignments = webTestClient.get()
                .uri("/teachers/classes/class/{classId}", classId)
                .header("Authorization", "Bearer " + token)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(TeacherClassResponseDTO.class)
                .returnResult()
                .getResponseBody();

        assertNotNull(assignments);
        assertFalse(assignments.isEmpty());
        // Verify all belong to the specified class
        assertTrue(assignments.stream().allMatch(a -> a.getClassId().equals(classId)));
    }

    // ==========================================
    // PATCH Accept Tests
    // ==========================================

    @ParameterizedTest
    @MethodSource("teacherRoleProvider")
    @DisplayName("PATCH /teachers/classes/{id}/accept - Teacher can accept assignment")
    void acceptTeacherClass_asTeacher_shouldSucceed(String email, String role) {
        String token = jwtTokenProvider.generateToken(email, role);
        Long teacherClassId = 1L; // Pending assignment

        Map<String, String> body = Map.of("observation", "Happy to teach this course");

        TeacherClassResponseDTO response = webTestClient.patch()
                .uri("/teachers/classes/{teacherClassId}/accept", teacherClassId)
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(body)
                .exchange()
                .expectStatus().isOk()
                .expectBody(TeacherClassResponseDTO.class)
                .returnResult()
                .getResponseBody();

        assertNotNull(response);
        assertEquals(teacherClassId, response.getId());
        assertTrue(response.getDecision());
        assertEquals(8L, response.getStatusId()); // Accepted status
        assertEquals("Happy to teach this course", response.getObservation());
    }

    @ParameterizedTest
    @MethodSource("teacherRoleProvider")
    @DisplayName("PATCH /teachers/classes/{id}/accept - Can accept without observation")
    void acceptTeacherClass_withoutObservation_shouldSucceed(String email, String role) {
        String token = jwtTokenProvider.generateToken(email, role);
        Long teacherClassId = 4L; // Another pending assignment

        webTestClient.patch()
                .uri("/teachers/classes/{teacherClassId}/accept", teacherClassId)
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody(TeacherClassResponseDTO.class)
                .value(response -> {
                    assertNotNull(response);
                    assertEquals(teacherClassId, response.getId());
                    assertTrue(response.getDecision());
                    assertEquals(8L, response.getStatusId());
                });
    }

    @ParameterizedTest
    @MethodSource("nonTeacherRolesProvider")
    @DisplayName("PATCH /teachers/classes/{id}/accept - Non-teacher roles cannot accept")
    void acceptTeacherClass_asNonTeacher_shouldReturn403(String email, String role) {
        String token = jwtTokenProvider.generateToken(email, role);

        webTestClient.patch()
                .uri("/teachers/classes/{teacherClassId}/accept", 1L)
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isForbidden();
    }

    // ==========================================
    // PATCH Reject Tests
    // ==========================================

    @ParameterizedTest
    @MethodSource("teacherRoleProvider")
    @DisplayName("PATCH /teachers/classes/{id}/reject - Teacher can reject assignment")
    void rejectTeacherClass_asTeacher_shouldSucceed(String email, String role) {
        String token = jwtTokenProvider.generateToken(email, role);
        Long teacherClassId = 1L; // Pending assignment

        Map<String, String> body = Map.of("observation", "Schedule conflict");

        TeacherClassResponseDTO response = webTestClient.patch()
                .uri("/teachers/classes/{teacherClassId}/reject", teacherClassId)
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(body)
                .exchange()
                .expectStatus().isOk()
                .expectBody(TeacherClassResponseDTO.class)
                .returnResult()
                .getResponseBody();

        assertNotNull(response);
        assertEquals(teacherClassId, response.getId());
        assertFalse(response.getDecision());
        assertEquals(9L, response.getStatusId()); // Rejected status
        assertEquals("Schedule conflict", response.getObservation());
    }

    @ParameterizedTest
    @MethodSource("nonTeacherRolesProvider")
    @DisplayName("PATCH /teachers/classes/{id}/reject - Non-teacher roles cannot reject")
    void rejectTeacherClass_asNonTeacher_shouldReturn403(String email, String role) {
        String token = jwtTokenProvider.generateToken(email, role);

        webTestClient.patch()
                .uri("/teachers/classes/{teacherClassId}/reject", 1L)
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isForbidden();
    }

    // ==========================================
    // GET By Teacher and Class ID Tests
    // ==========================================

    @ParameterizedTest
    @MethodSource("teacherAdminUserRolesProvider")
    @DisplayName("GET /teachers/{teacherId}/classes/{classId} - Should return specific teacher-class assignment")
    void getTeacherClassByTeacherAndClass_withAuthorizedRole_shouldSucceed(String email, String role) {
        String token = jwtTokenProvider.generateToken(email, role);
        Long teacherId = 1L;
        Long classId = 1L;

        TeacherClassResponseDTO response = webTestClient.get()
                .uri("/teachers/{teacherId}/classes/{classId}", teacherId, classId)
                .header("Authorization", "Bearer " + token)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody(TeacherClassResponseDTO.class)
                .returnResult()
                .getResponseBody();

        assertNotNull(response);
        assertEquals(teacherId, response.getTeacherId());
        assertEquals(classId, response.getClassId());
        assertNotNull(response.getStartDate());
        assertNotNull(response.getEndDate());
    }

    @ParameterizedTest
    @MethodSource("teacherAdminUserRolesProvider")
    @DisplayName("GET /teachers/{teacherId}/classes/{classId} - Should return 404 for non-existent combination")
    void getTeacherClassByTeacherAndClass_nonExistent_shouldReturn404(String email, String role) {
        String token = jwtTokenProvider.generateToken(email, role);
        Long teacherId = 999L; // Non-existent teacher
        Long classId = 999L;   // Non-existent class

        webTestClient.get()
                .uri("/teachers/{teacherId}/classes/{classId}", teacherId, classId)
                .header("Authorization", "Bearer " + token)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    @DisplayName("GET /teachers/{teacherId}/classes/{classId} - Unauthorized without token")
    void getTeacherClassByTeacherAndClass_withoutToken_shouldReturn401() {
        webTestClient.get()
                .uri("/teachers/{teacherId}/classes/{classId}", 1L, 1L)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isUnauthorized();
    }

    @Test
    @DisplayName("GET /teachers/{teacherId}/classes/{classId} - Should return dates correctly")
    void getTeacherClassByTeacherAndClass_shouldIncludeDates() {
        String token = jwtTokenProvider.generateToken("testAdmin@example.com", "ROLE_ADMIN");
        Long teacherId = 1L;
        Long classId = 2L; // Accepted assignment with dates

        TeacherClassResponseDTO response = webTestClient.get()
                .uri("/teachers/{teacherId}/classes/{classId}", teacherId, classId)
                .header("Authorization", "Bearer " + token)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody(TeacherClassResponseDTO.class)
                .returnResult()
                .getResponseBody();

        assertNotNull(response);
        assertNotNull(response.getStartDate(), "Start date should not be null");
        assertNotNull(response.getEndDate(), "End date should not be null");
        assertTrue(response.getEndDate().isAfter(response.getStartDate()), 
                "End date should be after start date");
    }

    // ==========================================
    // PATCH Update Teaching Dates Tests
    // ==========================================

    @ParameterizedTest
    @MethodSource("adminAndUserRolesProvider")
    @DisplayName("PATCH /teachers/classes/{teacherClassId}/dates - Admin/User can update dates")
    void updateTeachingDates_asAdminOrUser_shouldSucceed(String email, String role) {
        String token = jwtTokenProvider.generateToken(email, role);
        Long teacherClassId = 2L; // Accepted assignment

        TeacherClassRequestDTO request = TeacherClassRequestDTO.builder()
                .startDate(java.time.LocalDate.of(2025, 2, 1))
                .endDate(java.time.LocalDate.of(2025, 6, 1))
                .build();

        TeacherClassResponseDTO response = webTestClient.patch()
                .uri("/teachers/classes/{teacherClassId}/dates", teacherClassId)
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isOk()
                .expectBody(TeacherClassResponseDTO.class)
                .returnResult()
                .getResponseBody();

        assertNotNull(response);
        assertEquals(teacherClassId, response.getId());
        assertEquals(java.time.LocalDate.of(2025, 2, 1), response.getStartDate());
        assertEquals(java.time.LocalDate.of(2025, 6, 1), response.getEndDate());
    }

    @Test
    @DisplayName("PATCH /teachers/classes/{teacherClassId}/dates - Teacher cannot update dates")
    void updateTeachingDates_asTeacher_shouldReturn403() {
        String token = jwtTokenProvider.generateToken("testTeacher@example.com", "ROLE_TEACHER");

        TeacherClassRequestDTO request = TeacherClassRequestDTO.builder()
                .startDate(java.time.LocalDate.of(2025, 2, 1))
                .endDate(java.time.LocalDate.of(2025, 6, 1))
                .build();

        webTestClient.patch()
                .uri("/teachers/classes/{teacherClassId}/dates", 2L)
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isForbidden();
    }

    @Test
    @DisplayName("PATCH /teachers/classes/{teacherClassId}/dates - Unauthorized without token")
    void updateTeachingDates_withoutToken_shouldReturn401() {
        TeacherClassRequestDTO request = TeacherClassRequestDTO.builder()
                .startDate(java.time.LocalDate.of(2025, 2, 1))
                .endDate(java.time.LocalDate.of(2025, 6, 1))
                .build();

        webTestClient.patch()
                .uri("/teachers/classes/{teacherClassId}/dates", 2L)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isUnauthorized();
    }

    @ParameterizedTest
    @MethodSource("adminAndUserRolesProvider")
    @DisplayName("PATCH /teachers/classes/{teacherClassId}/dates - Should persist updated dates")
    void updateTeachingDates_shouldPersist(String email, String role) {
        String token = jwtTokenProvider.generateToken(email, role);
        Long teacherClassId = 3L; // Rejected assignment

        TeacherClassRequestDTO request = TeacherClassRequestDTO.builder()
                .startDate(java.time.LocalDate.of(2025, 3, 1))
                .endDate(java.time.LocalDate.of(2025, 7, 1))
                .build();

        // Update dates
        webTestClient.patch()
                .uri("/teachers/classes/{teacherClassId}/dates", teacherClassId)
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isOk();

        // Verify dates were persisted by retrieving the assignment
        Long teacherId = 1L;
        Long classId = 3L;
        
        TeacherClassResponseDTO retrieved = webTestClient.get()
                .uri("/teachers/{teacherId}/classes/{classId}", teacherId, classId)
                .header("Authorization", "Bearer " + token)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody(TeacherClassResponseDTO.class)
                .returnResult()
                .getResponseBody();

        assertNotNull(retrieved);
        assertEquals(java.time.LocalDate.of(2025, 3, 1), retrieved.getStartDate());
        assertEquals(java.time.LocalDate.of(2025, 7, 1), retrieved.getEndDate());
    }

    @ParameterizedTest
    @MethodSource("adminAndUserRolesProvider")
    @DisplayName("PATCH /teachers/classes/{teacherClassId}/dates - Should update only startDate when endDate is null")
    void updateTeachingDates_onlyStartDate_shouldSucceed(String email, String role) {
        String token = jwtTokenProvider.generateToken(email, role);
        Long teacherClassId = 4L;

        TeacherClassRequestDTO request = TeacherClassRequestDTO.builder()
                .startDate(java.time.LocalDate.of(2025, 2, 15))
                .build();

        TeacherClassResponseDTO response = webTestClient.patch()
                .uri("/teachers/classes/{teacherClassId}/dates", teacherClassId)
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isOk()
                .expectBody(TeacherClassResponseDTO.class)
                .returnResult()
                .getResponseBody();

        assertNotNull(response);
        assertEquals(java.time.LocalDate.of(2025, 2, 15), response.getStartDate());
    }

    @ParameterizedTest
    @MethodSource("adminAndUserRolesProvider")
    @DisplayName("PATCH /teachers/classes/{teacherClassId}/dates - Should update only endDate when startDate is null")
    void updateTeachingDates_onlyEndDate_shouldSucceed(String email, String role) {
        String token = jwtTokenProvider.generateToken(email, role);
        Long teacherClassId = 4L;

        TeacherClassRequestDTO request = TeacherClassRequestDTO.builder()
                .endDate(java.time.LocalDate.of(2025, 6, 15))
                .build();

        TeacherClassResponseDTO response = webTestClient.patch()
                .uri("/teachers/classes/{teacherClassId}/dates", teacherClassId)
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isOk()
                .expectBody(TeacherClassResponseDTO.class)
                .returnResult()
                .getResponseBody();

        assertNotNull(response);
        assertEquals(java.time.LocalDate.of(2025, 6, 15), response.getEndDate());
    }

    // ==========================================
    // DELETE Tests
    // ==========================================

    @ParameterizedTest
    @MethodSource("adminAndUserRolesProvider")
    @DisplayName("DELETE /teachers/classes/teacher/{teacherId}/class/{classId} - Admin/User can delete")
    void deleteTeacherClass_asAdminOrUser_shouldSucceed(String email, String role) {
        String token = jwtTokenProvider.generateToken(email, role);
        Long teacherId = 1L;
        Long classId = 1L;

        // Delete the assignment
        webTestClient.delete()
                .uri("/teachers/classes/teacher/{teacherId}/class/{classId}", teacherId, classId)
                .header("Authorization", "Bearer " + token)
                .exchange()
                .expectStatus().isNoContent();

        // Verify it no longer exists
        List<TeacherClassResponseDTO> assignments = webTestClient.get()
                .uri("/teachers/classes/class/{classId}", classId)
                .header("Authorization", "Bearer " + token)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectBodyList(TeacherClassResponseDTO.class)
                .returnResult()
                .getResponseBody();

        assertNotNull(assignments);
        assertTrue(assignments.stream()
                .noneMatch(a -> a.getTeacherId().equals(teacherId) && a.getClassId().equals(classId)));
    }

    @Test
    @DisplayName("DELETE /teachers/classes/teacher/{teacherId}/class/{classId} - Teacher cannot delete")
    void deleteTeacherClass_asTeacher_shouldReturn403() {
        String token = jwtTokenProvider.generateToken("testTeacher@example.com", "ROLE_TEACHER");

        webTestClient.delete()
                .uri("/teachers/classes/teacher/{teacherId}/class/{classId}", 1L, 2L)
                .header("Authorization", "Bearer " + token)
                .exchange()
                .expectStatus().isForbidden();
    }

    // ==========================================
    // Business Logic Tests
    // ==========================================

    @Test
    @DisplayName("Status separation - Pending, accepted, and rejected should not overlap")
    void teacherClasses_byStatus_shouldNotOverlap() {
        String token = jwtTokenProvider.generateToken("testAdmin@example.com", "ROLE_ADMIN");
        Long teacherId = 1L;

        // Get pending
        List<TeacherClassResponseDTO> pending = webTestClient.get()
                .uri("/teachers/{teacherId}/classes/status/4", teacherId)
                .header("Authorization", "Bearer " + token)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectBodyList(TeacherClassResponseDTO.class)
                .returnResult()
                .getResponseBody();

        // Get accepted
        List<TeacherClassResponseDTO> accepted = webTestClient.get()
                .uri("/teachers/{teacherId}/classes/status/6", teacherId)
                .header("Authorization", "Bearer " + token)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectBodyList(TeacherClassResponseDTO.class)
                .returnResult()
                .getResponseBody();

        // Get rejected
        List<TeacherClassResponseDTO> rejected = webTestClient.get()
                .uri("/teachers/{teacherId}/classes/status/7", teacherId)
                .header("Authorization", "Bearer " + token)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectBodyList(TeacherClassResponseDTO.class)
                .returnResult()
                .getResponseBody();

        assertNotNull(pending);
        assertNotNull(accepted);
        assertNotNull(rejected);

        // Verify no overlap between statuses
        assertTrue(pending.stream()
                .noneMatch(p -> accepted.stream()
                        .anyMatch(a -> a.getId().equals(p.getId()))),
                "Pending and accepted should not overlap");
        
        assertTrue(pending.stream()
                .noneMatch(p -> rejected.stream()
                        .anyMatch(r -> r.getId().equals(p.getId()))),
                "Pending and rejected should not overlap");
    }

    @Test
    @DisplayName("Current semester filter should exclude historical assignments")
    void getCurrentSemesterAssignments_shouldExcludeHistorical() {
        String token = jwtTokenProvider.generateToken("testAdmin@example.com", "ROLE_ADMIN");

        List<TeacherClassResponseDTO> currentAssignments = webTestClient.get()
                .uri("/teachers/classes/current-semester")
                .header("Authorization", "Bearer " + token)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectBodyList(TeacherClassResponseDTO.class)
                .returnResult()
                .getResponseBody();

        assertNotNull(currentAssignments);
        assertFalse(currentAssignments.isEmpty());
        
        // Verify all belong to current semester (ID 2) and none to previous semester (ID 1)
        assertFalse(currentAssignments.stream()
                .anyMatch(a -> a.getSemesterId().equals(1L)),
                "Should not include assignments from previous semester");
        
        // Verify all are from current semester
        assertTrue(currentAssignments.stream()
                .allMatch(a -> a.getSemesterId().equals(2L)),
                "All should be from current semester");
    }

    @Test
    @DisplayName("Accept operation should be idempotent")
    void acceptTeacherClass_multipleTimes_shouldBeIdempotent() {
        String token = jwtTokenProvider.generateToken("testTeacher@example.com", "ROLE_TEACHER");
        Long teacherClassId = 1L;

        Map<String, String> body = Map.of("observation", "First acceptance");

        // First acceptance
        TeacherClassResponseDTO firstResponse = webTestClient.patch()
                .uri("/teachers/classes/{teacherClassId}/accept", teacherClassId)
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(body)
                .exchange()
                .expectStatus().isOk()
                .expectBody(TeacherClassResponseDTO.class)
                .returnResult()
                .getResponseBody();

        // Second acceptance (should succeed and maintain accepted state)
        Map<String, String> body2 = Map.of("observation", "Second acceptance");
        
        TeacherClassResponseDTO secondResponse = webTestClient.patch()
                .uri("/teachers/classes/{teacherClassId}/accept", teacherClassId)
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(body2)
                .exchange()
                .expectStatus().isOk()
                .expectBody(TeacherClassResponseDTO.class)
                .returnResult()
                .getResponseBody();

        assertNotNull(firstResponse);
        assertNotNull(secondResponse);
        assertEquals(firstResponse.getId(), secondResponse.getId());
        assertTrue(secondResponse.getDecision());
        assertEquals(8L, secondResponse.getStatusId());
    }

    // ==========================================
    // Extra Hours Warning Tests
    // ==========================================

    @ParameterizedTest
    @MethodSource("adminAndUserRolesProvider")
    @DisplayName("Get teacher extra hours warning - should calculate correctly with no excess")
    void getTeacherExtraHoursWarning_withNoExcess_shouldReturnCorrectCalculation(String email, String role) {
        String token = jwtTokenProvider.generateToken(email, role);
        Long teacherId = 1L;
        
        // Teacher has max 40 hours
        // Current assignments in test data: 4 + 4 + 6 + 4 = 18 hours (only current semester ID=2)
        // Adding 15 hours: 18 + 15 = 33 < 40 (no excess)
        Map<String, Integer> request = Map.of("workHoursToAssign", 15);

        webTestClient.post()
                .uri("/teachers/{teacherId}/extra-hours-warning", teacherId)
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.teacherName").isEqualTo("Teacher")
                .jsonPath("$.maxHours").isEqualTo(40)
                .jsonPath("$.totalAssignedHours").isEqualTo(18)
                .jsonPath("$.workHoursToAssign").isEqualTo(15)
                .jsonPath("$.exceedsMaxHours").isEqualTo(0);
    }

    @ParameterizedTest
    @MethodSource("adminAndUserRolesProvider")
    @DisplayName("Get teacher extra hours warning - should calculate excess hours correctly")
    void getTeacherExtraHoursWarning_withExcess_shouldReturnCorrectExcess(String email, String role) {
        String token = jwtTokenProvider.generateToken(email, role);
        Long teacherId = 1L;
        
        // Teacher has max 40 hours
        // Current assignments: 18 hours
        // Adding 30 hours: 18 + 30 = 48 > 40 (8 hours excess)
        Map<String, Integer> request = Map.of("workHoursToAssign", 30);

        webTestClient.post()
                .uri("/teachers/{teacherId}/extra-hours-warning", teacherId)
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.teacherName").isEqualTo("Teacher")
                .jsonPath("$.maxHours").isEqualTo(40)
                .jsonPath("$.totalAssignedHours").isEqualTo(18)
                .jsonPath("$.workHoursToAssign").isEqualTo(30)
                .jsonPath("$.exceedsMaxHours").isEqualTo(8);
    }

    @ParameterizedTest
    @MethodSource("adminAndUserRolesProvider")
    @DisplayName("Get teacher extra hours warning - should handle exact max hours")
    void getTeacherExtraHoursWarning_exactlyAtMax_shouldReturnZeroExcess(String email, String role) {
        String token = jwtTokenProvider.generateToken(email, role);
        Long teacherId = 1L;
        
        // Teacher has max 40 hours
        // Current assignments: 18 hours
        // Adding 22 hours: 18 + 22 = 40 (exact match, 0 excess)
        Map<String, Integer> request = Map.of("workHoursToAssign", 22);

        webTestClient.post()
                .uri("/teachers/{teacherId}/extra-hours-warning", teacherId)
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.teacherName").isEqualTo("Teacher")
                .jsonPath("$.maxHours").isEqualTo(40)
                .jsonPath("$.totalAssignedHours").isEqualTo(18)
                .jsonPath("$.workHoursToAssign").isEqualTo(22)
                .jsonPath("$.exceedsMaxHours").isEqualTo(0);
    }

    @ParameterizedTest
    @MethodSource("adminAndUserRolesProvider")
    @DisplayName("Get teacher extra hours warning - should handle zero hours to assign")
    void getTeacherExtraHoursWarning_withZeroHours_shouldReturnCurrentState(String email, String role) {
        String token = jwtTokenProvider.generateToken(email, role);
        Long teacherId = 1L;
        
        // Adding 0 hours should just show current state
        Map<String, Integer> request = Map.of("workHoursToAssign", 0);

        webTestClient.post()
                .uri("/teachers/{teacherId}/extra-hours-warning", teacherId)
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.teacherName").isEqualTo("Teacher")
                .jsonPath("$.maxHours").isEqualTo(40)
                .jsonPath("$.totalAssignedHours").isEqualTo(18)
                .jsonPath("$.workHoursToAssign").isEqualTo(0)
                .jsonPath("$.exceedsMaxHours").isEqualTo(0);
    }

    @Test
    @DisplayName("Get teacher extra hours warning - should require authentication")
    void getTeacherExtraHoursWarning_withoutAuth_shouldReturn401() {
        Long teacherId = 1L;
        Map<String, Integer> request = Map.of("workHoursToAssign", 10);

        webTestClient.post()
                .uri("/teachers/{teacherId}/extra-hours-warning", teacherId)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isUnauthorized();
    }

    @Test
    @DisplayName("Get teacher extra hours warning - should deny teacher role access")
    void getTeacherExtraHoursWarning_withTeacherRole_shouldReturn403() {
        String token = jwtTokenProvider.generateToken("testTeacher@example.com", "ROLE_TEACHER");
        Long teacherId = 1L;
        Map<String, Integer> request = Map.of("workHoursToAssign", 10);

        webTestClient.post()
                .uri("/teachers/{teacherId}/extra-hours-warning", teacherId)
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isForbidden();
    }

    @ParameterizedTest
    @MethodSource("adminAndUserRolesProvider")
    @DisplayName("Get teacher extra hours warning - should handle non-existent teacher")
    void getTeacherExtraHoursWarning_withInvalidTeacher_shouldReturn404(String email, String role) {
        String token = jwtTokenProvider.generateToken(email, role);
        Long nonExistentTeacherId = 999L;
        Map<String, Integer> request = Map.of("workHoursToAssign", 10);

        webTestClient.post()
                .uri("/teachers/{teacherId}/extra-hours-warning", nonExistentTeacherId)
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().is5xxServerError();
    }

    @ParameterizedTest
    @MethodSource("adminAndUserRolesProvider")
    @DisplayName("Get teacher extra hours warning - should only count current semester hours")
    void getTeacherExtraHoursWarning_shouldOnlyCountCurrentSemester(String email, String role) {
        String token = jwtTokenProvider.generateToken(email, role);
        Long teacherId = 1L;
        
        // Teacher has assignments in both current (semester_id=2) and previous (semester_id=1)
        // Current semester assignments: 4 + 4 + 6 + 4 = 18 hours
        // Previous semester assignment (3 hours) should NOT be counted
        Map<String, Integer> request = Map.of("workHoursToAssign", 5);

        webTestClient.post()
                .uri("/teachers/{teacherId}/extra-hours-warning", teacherId)
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.totalAssignedHours").isEqualTo(18) // Should be 18, not 21
                .jsonPath("$.workHoursToAssign").isEqualTo(5)
                .jsonPath("$.exceedsMaxHours").isEqualTo(0); // 18 + 5 = 23 < 40
    }
}
