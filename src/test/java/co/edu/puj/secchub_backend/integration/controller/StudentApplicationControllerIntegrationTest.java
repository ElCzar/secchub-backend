package co.edu.puj.secchub_backend.integration.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
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
import co.edu.puj.secchub_backend.integration.dto.StudentApplicationRequestDTO;
import co.edu.puj.secchub_backend.integration.dto.StudentApplicationResponseDTO;
import co.edu.puj.secchub_backend.integration.dto.StudentApplicationScheduleRequestDTO;
import co.edu.puj.secchub_backend.security.jwt.JwtTokenProvider;
import io.r2dbc.spi.ConnectionFactory;

@SpringBootTest
@AutoConfigureWebTestClient
@Testcontainers
@DisplayName("Student Application Controller Integration Tests")
class StudentApplicationControllerIntegrationTest extends DatabaseContainerIntegration {
    
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
                "/test-student-applications.sql"
        );
    }

    // ==========================================
    // Test Data Providers
    // ==========================================
    
    /**
     * Provides student role for creating applications
     */
    private static Stream<Arguments> studentRoleProvider() {
        return Stream.of(
            Arguments.of("testStudent@example.com", "ROLE_STUDENT")
        );
    }

    /**
     * Provides admin and user roles for viewing/managing applications
     */
    private static Stream<Arguments> adminAndUserRolesProvider() {
        return Stream.of(
            Arguments.of("testAdmin@example.com", "ROLE_ADMIN"),
            Arguments.of("testUser@example.com", "ROLE_USER")
        );
    }

    /**
     * Provides non-student roles that cannot create applications
     */
    private static Stream<Arguments> nonStudentRolesProvider() {
        return Stream.of(
            Arguments.of("testAdmin@example.com", "ROLE_ADMIN"),
            Arguments.of("testUser@example.com", "ROLE_USER"),
            Arguments.of("testTeacher@example.com", "ROLE_TEACHER"),
            Arguments.of("testProgram@example.com", "ROLE_PROGRAM")
        );
    }

    // ==========================================
    // POST Create Student Application Tests
    // ==========================================
    
    @ParameterizedTest(name = "POST /student-applications as {1} should create application")
    @MethodSource("studentRoleProvider")
    @DisplayName("POST /student-applications as student should create application")
    void createStudentApplication_asStudent_createsSuccessfully(String email, String role) {
        String token = jwtTokenProvider.generateToken(email, role);

        // Create schedules
        StudentApplicationScheduleRequestDTO schedule1 = StudentApplicationScheduleRequestDTO.builder()
                .day("Monday")
                .startTime("14:00:00")
                .endTime("18:00:00")
                .build();

        StudentApplicationScheduleRequestDTO schedule2 = StudentApplicationScheduleRequestDTO.builder()
                .day("Wednesday")
                .startTime("14:00:00")
                .endTime("18:00:00")
                .build();

        // Create application request
        StudentApplicationRequestDTO requestDTO = StudentApplicationRequestDTO.builder()
                .courseId(5L)  // Software Engineering
                .sectionId(1L)
                .program("Computer Science")
                .studentSemester(6)
                .academicAverage(4.3)
                .phoneNumber("555-0199")
                .alternatePhoneNumber("555-0299")
                .address("456 Test Ave")
                .personalEmail("newteststu@personal.com")
                .wasTeachingAssistant(false)
                .courseAverage(4.6)
                .courseTeacher("Dr. Test")
                .schedules(Arrays.asList(schedule1, schedule2))
                .build();

        StudentApplicationResponseDTO response = webTestClient.post()
                .uri("/student-applications")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(requestDTO)
                .exchange()
                .expectStatus().isCreated()
                .expectBody(StudentApplicationResponseDTO.class)
                .returnResult()
                .getResponseBody();

        assertNotNull(response, "Response should not be null");
        assertNotNull(response.getId(), "Application ID should not be null");
        assertEquals(5L, response.getCourseId(), "Course ID should match");
        assertEquals(1L, response.getSectionId(), "Section ID should match");
        assertEquals("Computer Science", response.getProgram(), "Program should match");
        assertEquals(6, response.getStudentSemester(), "Student semester should match");
        assertNotNull(response.getSchedules(), "Schedules should not be null");
        assertEquals(2, response.getSchedules().size(), "Should have 2 schedules");

        // Verify in database
        Long count = databaseClient.sql("SELECT COUNT(*) FROM student_application WHERE id = :id")
                .bind("id", response.getId())
                .map(row -> row.get(0, Long.class))
                .one()
                .block();

        assertEquals(1, count, "Application should exist in database");

        Long countSchedules = databaseClient.sql("SELECT COUNT(*) FROM student_application_schedule WHERE student_application_id = :appId")
                .bind("appId", response.getId())
                .map(row -> row.get(0, Long.class))
                .one()
                .block();
            
        assertEquals(2, countSchedules, "Schedules should exist in database");
    }

    @ParameterizedTest(name = "POST /student-applications as {1} should return 403")
    @MethodSource("nonStudentRolesProvider")
    @DisplayName("POST /student-applications as non-student should return 403")
    void createStudentApplication_asNonStudent_returns403(String email, String role) {
        String token = jwtTokenProvider.generateToken(email, role);

        StudentApplicationRequestDTO requestDTO = StudentApplicationRequestDTO.builder()
                .courseId(5L)
                .sectionId(1L)
                .program("Computer Science")
                .studentSemester(6)
                .academicAverage(4.3)
                .build();

        webTestClient.post()
                .uri("/student-applications")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(requestDTO)
                .exchange()
                .expectStatus().isForbidden();
    }

    @Test
    @DisplayName("POST /student-applications unauthenticated should return 401")
    void createStudentApplication_unauthenticated_returns401() {
        StudentApplicationRequestDTO requestDTO = StudentApplicationRequestDTO.builder()
                .courseId(5L)
                .sectionId(1L)
                .program("Computer Science")
                .studentSemester(6)
                .academicAverage(4.3)
                .build();

        webTestClient.post()
                .uri("/student-applications")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(requestDTO)
                .exchange()
                .expectStatus().isUnauthorized();
    }

    // ==========================================
    // GET All Student Applications Tests
    // ==========================================
    
    @ParameterizedTest(name = "GET /student-applications as {1} should return list")
    @MethodSource("adminAndUserRolesProvider")
    @DisplayName("GET /student-applications as admin/user should return all applications")
    void getAllStudentApplications_asAdminOrUser_returnsList(String email, String role) {
        String token = jwtTokenProvider.generateToken(email, role);

        List<StudentApplicationResponseDTO> applications = webTestClient.get()
                .uri("/student-applications")
                .header("Authorization", "Bearer " + token)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(StudentApplicationResponseDTO.class)
                .returnResult()
                .getResponseBody();

        assertNotNull(applications, "Applications list should not be null");
        assertFalse(applications.isEmpty(), "Should have student applications");
        
        // Verify at least one application from test data
        assertTrue(applications.stream()
                .anyMatch(a -> a.getCourseId().equals(1L)),
                "Should contain Data Structures application");
    }

    @Test
    @DisplayName("GET /student-applications unauthenticated should return 401")
    void getAllStudentApplications_unauthenticated_returns401() {
        webTestClient.get()
                .uri("/student-applications")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isUnauthorized();
    }

    // ==========================================
    // GET Student Application by ID Tests
    // ==========================================
    
    @ParameterizedTest(name = "GET /student-applications/:id as {1} should return application")
    @MethodSource("adminAndUserRolesProvider")
    @DisplayName("GET /student-applications/:id should return specific application")
    void getStudentApplicationById_asAdminOrUser_returnsApplication(String email, String role) {
        String token = jwtTokenProvider.generateToken(email, role);

        StudentApplicationResponseDTO application = webTestClient.get()
                .uri("/student-applications/1")
                .header("Authorization", "Bearer " + token)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody(StudentApplicationResponseDTO.class)
                .returnResult()
                .getResponseBody();

        assertNotNull(application, "Application should not be null");
        assertEquals(1L, application.getId(), "Application ID should be 1");
        assertEquals(1L, application.getCourseId(), "Course ID should be 1 (Data Structures)");
        assertEquals(1L, application.getSectionId(), "Section ID should be 1");
        assertEquals(2L, application.getSemesterId(), "Semester ID should be 2 (current)");
        assertEquals("Computer Science", application.getProgram(), "Program should match");
        assertEquals(4, application.getStatusId(), "Status should be Pending (4)");
    }

    @Test
    @DisplayName("GET /student-applications/:id with non-existent ID should return 404")
    void getStudentApplicationById_nonExistent_returns404() {
        String token = jwtTokenProvider.generateToken("testAdmin@example.com", "ROLE_ADMIN");

        webTestClient.get()
                .uri("/student-applications/9999")
                .header("Authorization", "Bearer " + token)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isNotFound();
    }

    // ==========================================
    // GET Current Semester Applications Tests
    // ==========================================
    
    @ParameterizedTest(name = "GET /student-applications/current-semester as {1} should return current applications")
    @MethodSource("adminAndUserRolesProvider")
    @DisplayName("GET /student-applications/current-semester should return only current semester applications")
    void getCurrentSemesterStudentApplications_asAdminOrUser_returnsCurrentSemesterApplications(String email, String role) {
        String token = jwtTokenProvider.generateToken(email, role);

        List<StudentApplicationResponseDTO> applications = webTestClient.get()
                .uri("/student-applications/current-semester")
                .header("Authorization", "Bearer " + token)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(StudentApplicationResponseDTO.class)
                .returnResult()
                .getResponseBody();

        assertNotNull(applications, "Applications list should not be null");
        assertFalse(applications.isEmpty(), "Should have current semester applications");
        
        // Verify all applications are for current semester (id = 2)
        assertTrue(applications.stream()
                .allMatch(a -> a.getSemesterId().equals(2L)),
                "All applications should be for current semester");
    }

    // ==========================================
    // GET Applications by Status Tests
    // ==========================================
    
    @ParameterizedTest(name = "GET /student-applications/status/:statusId as {1} should return filtered applications")
    @MethodSource("adminAndUserRolesProvider")
    @DisplayName("GET /student-applications/status/:statusId should filter by status")
    void getStudentApplicationByStatus_asAdminOrUser_returnsFilteredApplications(String email, String role) {
        String token = jwtTokenProvider.generateToken(email, role);

        // Get pending applications (status_id = 4)
        List<StudentApplicationResponseDTO> pendingApplications = webTestClient.get()
                .uri("/student-applications/status/4")
                .header("Authorization", "Bearer " + token)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(StudentApplicationResponseDTO.class)
                .returnResult()
                .getResponseBody();

        assertNotNull(pendingApplications, "Pending applications list should not be null");
        assertFalse(pendingApplications.isEmpty(), "Should have pending applications");
        
        // Verify all returned applications have status 4
        assertTrue(pendingApplications.stream()
                .allMatch(a -> a.getStatusId().equals(4L)),
                "All applications should have status Pending (4)");
    }

    @ParameterizedTest(name = "GET /student-applications/status/:statusId with confirmed status as {1} should return confirmed applications")
    @MethodSource("adminAndUserRolesProvider")
    @DisplayName("GET /student-applications/status/:statusId should return confirmed applications")
    void getStudentApplicationByStatus_confirmed_returnsConfirmedApplications(String email, String role) {
        String token = jwtTokenProvider.generateToken(email, role);

        // Get confirmed applications (status_id = 8)
        List<StudentApplicationResponseDTO> confirmedApplications = webTestClient.get()
                .uri("/student-applications/status/8")
                .header("Authorization", "Bearer " + token)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(StudentApplicationResponseDTO.class)
                .returnResult()
                .getResponseBody();

        assertNotNull(confirmedApplications, "Confirmed applications list should not be null");
        
        // Verify all returned applications have status 8
        assertTrue(confirmedApplications.stream()
                .allMatch(a -> a.getStatusId().equals(8L)),
                "All applications should have status Confirmed (8)");
    }

    // ==========================================
    // GET Applications by Section Tests
    // ==========================================
    
    @ParameterizedTest(name = "GET /student-applications/section/:sectionId as {1} should return section applications")
    @MethodSource("adminAndUserRolesProvider")
    @DisplayName("GET /student-applications/section/:sectionId should filter by section")
    void getStudentApplicationBySection_asAdminOrUser_returnsSectionApplications(String email, String role) {
        String token = jwtTokenProvider.generateToken(email, role);

        List<StudentApplicationResponseDTO> applications = webTestClient.get()
                .uri("/student-applications/section/1")
                .header("Authorization", "Bearer " + token)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(StudentApplicationResponseDTO.class)
                .returnResult()
                .getResponseBody();

        assertNotNull(applications, "Applications list should not be null");
        assertFalse(applications.isEmpty(), "Should have applications for section 1");
        
        // Verify all applications are for section 1
        assertTrue(applications.stream()
                .allMatch(a -> a.getSectionId().equals(1L)),
                "All applications should be for section 1");
    }

    // ==========================================
    // PUT Approve/Reject Application Tests
    // ==========================================
    
    @ParameterizedTest(name = "PUT /student-applications/:id/approve as {1} should approve application")
    @MethodSource("adminAndUserRolesProvider")
    @DisplayName("PUT /student-applications/:id/approve should approve application")
    void approveStudentApplication_asAdminOrUser_approvesSuccessfully(String email, String role) {
        String token = jwtTokenProvider.generateToken(email, role);

        webTestClient.put()
                .uri("/student-applications/1/approve")
                .header("Authorization", "Bearer " + token)
                .exchange()
                .expectStatus().isOk();

        // Verify in database that status changed to Confirmed (8)
        Long statusId = databaseClient.sql("SELECT status_id FROM student_application WHERE id = :id")
                .bind("id", 1L)
                .map(row -> row.get("status_id", Long.class))
                .one()
                .block();
        assertEquals(8L, statusId, "Application should be approved (status 8)");
    }

    @ParameterizedTest(name = "PUT /student-applications/:id/reject as {1} should reject application")
    @MethodSource("adminAndUserRolesProvider")
    @DisplayName("PUT /student-applications/:id/reject should reject application")
    void rejectStudentApplication_asAdminOrUser_rejectsSuccessfully(String email, String role) {
        String token = jwtTokenProvider.generateToken(email, role);

        webTestClient.put()
                .uri("/student-applications/4/reject")
                .header("Authorization", "Bearer " + token)
                .exchange()
                .expectStatus().isOk();

        // Verify in database that status changed to Rejected (9)
        Long statusId = databaseClient.sql("SELECT status_id FROM student_application WHERE id = :id")
                .bind("id", 4L)
                .map(row -> row.get("status_id", Long.class))
                .one()
                .block();
        assertEquals(9L, statusId, "Application should be rejected (status 9)");
    }

    @Test
    @DisplayName("PUT /student-applications/:id/approve with non-existent ID should return 404")
    void approveStudentApplication_nonExistent_returns404() {
        String token = jwtTokenProvider.generateToken("testAdmin@example.com", "ROLE_ADMIN");

        webTestClient.put()
                .uri("/student-applications/9999/approve")
                .header("Authorization", "Bearer " + token)
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    @DisplayName("PUT /student-applications/:id/reject with non-existent ID should return 404")
    void rejectStudentApplication_nonExistent_returns404() {
        String token = jwtTokenProvider.generateToken("testAdmin@example.com", "ROLE_ADMIN");

        webTestClient.put()
                .uri("/student-applications/9999/reject")
                .header("Authorization", "Bearer " + token)
                .exchange()
                .expectStatus().isNotFound();
    }

    // ==========================================
    // Business Logic Tests
    // ==========================================
    
    @Test
    @DisplayName("Creating application with schedules should create both application and schedules")
    void createApplication_withSchedules_createsBoth() {
        String token = jwtTokenProvider.generateToken("testStudent@example.com", "ROLE_STUDENT");

        StudentApplicationScheduleRequestDTO schedule = StudentApplicationScheduleRequestDTO.builder()
                .day("Thursday")
                .startTime("10:00:00")
                .endTime("14:00:00")
                .build();

        StudentApplicationRequestDTO requestDTO = StudentApplicationRequestDTO.builder()
                .courseId(7L)  // Machine Learning
                .sectionId(1L)
                .program("Computer Science")
                .studentSemester(7)
                .academicAverage(4.5)
                .phoneNumber("555-0777")
                .personalEmail("teststu2@personal.com")
                .wasTeachingAssistant(true)
                .courseAverage(4.8)
                .courseTeacher("Dr. ML Expert")
                .schedules(Arrays.asList(schedule))
                .build();

        StudentApplicationResponseDTO response = webTestClient.post()
                .uri("/student-applications")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(requestDTO)
                .exchange()
                .expectStatus().isCreated()
                .expectBody(StudentApplicationResponseDTO.class)
                .returnResult()
                .getResponseBody();

        assertNotNull(response, "Response should not be null");
        assertNotNull(response.getSchedules(), "Schedules should be created");
        assertFalse(response.getSchedules().isEmpty(), "Should have at least one schedule");
    }

    @Test
    @DisplayName("Filtering by status should correctly separate pending, confirmed, and rejected applications")
    void getApplicationsByStatus_shouldSeparateStatuses() {
        String token = jwtTokenProvider.generateToken("testAdmin@example.com", "ROLE_ADMIN");

        // Get pending
        List<StudentApplicationResponseDTO> pending = webTestClient.get()
                .uri("/student-applications/status/4")
                .header("Authorization", "Bearer " + token)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectBodyList(StudentApplicationResponseDTO.class)
                .returnResult()
                .getResponseBody();

        // Get confirmed
        List<StudentApplicationResponseDTO> confirmed = webTestClient.get()
                .uri("/student-applications/status/8")
                .header("Authorization", "Bearer " + token)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectBodyList(StudentApplicationResponseDTO.class)
                .returnResult()
                .getResponseBody();

        // Get rejected
        List<StudentApplicationResponseDTO> rejected = webTestClient.get()
                .uri("/student-applications/status/9")
                .header("Authorization", "Bearer " + token)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectBodyList(StudentApplicationResponseDTO.class)
                .returnResult()
                .getResponseBody();

        assertNotNull(pending, "Pending list should not be null");
        assertNotNull(confirmed, "Confirmed list should not be null");
        assertNotNull(rejected, "Rejected list should not be null");

        // Verify no overlap between statuses
        assertTrue(pending.stream()
                .noneMatch(p -> confirmed.stream()
                        .anyMatch(c -> c.getId().equals(p.getId()))),
                "Pending and confirmed should not overlap");
        
        assertTrue(pending.stream()
                .noneMatch(p -> rejected.stream()
                        .anyMatch(r -> r.getId().equals(p.getId()))),
                "Pending and rejected should not overlap");
    }
    
    @Test
    @DisplayName("Approving already approved application should not cause errors")
    void approveApplication_alreadyApproved_shouldSucceed() {
        String token = jwtTokenProvider.generateToken("testAdmin@example.com", "ROLE_ADMIN");

        // Application 2 is already approved
        webTestClient.put()
                .uri("/student-applications/2/approve")
                .header("Authorization", "Bearer " + token)
                .exchange()
                .expectStatus().isOk();

        // Verify status is still confirmed
        Long statusId = databaseClient.sql("SELECT status_id FROM student_application WHERE id = :id")
                .bind("id", 2L)
                .map(row -> row.get("status_id", Long.class))
                .one()
                .block();
        assertEquals(8L, statusId, "Application should remain confirmed");
    }
}
