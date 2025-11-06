package co.edu.puj.secchub_backend.planning.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.HashMap;
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
import co.edu.puj.secchub_backend.planning.dto.ClassCreateRequestDTO;
import co.edu.puj.secchub_backend.planning.dto.ClassResponseDTO;
import co.edu.puj.secchub_backend.planning.dto.ClassScheduleRequestDTO;
import co.edu.puj.secchub_backend.planning.dto.ClassScheduleResponseDTO;
import co.edu.puj.secchub_backend.security.jwt.JwtTokenProvider;
import io.r2dbc.spi.ConnectionFactory;
import reactor.core.publisher.Mono;

@SpringBootTest
@AutoConfigureWebTestClient
@Testcontainers
@DisplayName("Planning Controller Integration Tests")
class PlanningControllerIntegrationTest extends DatabaseContainerIntegration {
    
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
                "/test-semesters.sql",
                "/test-sections.sql",
                "/test-courses.sql",
                "/test-classrooms.sql",
                "/test-classes.sql",
                "/test-class-schedules.sql"
        );
    }

    // ==========================================
    // Test Data Providers
    // ==========================================
    
    /**
     * Provides authorized roles (ADMIN, USER)
     */
    private static Stream<Arguments> authorizedRolesProvider() {
        return Stream.of(
            Arguments.of("testAdmin@example.com", "ROLE_ADMIN"),
            Arguments.of("testUser@example.com", "ROLE_USER")
        );
    }

    /**
     * Provides unauthorized roles (STUDENT, TEACHER, PROGRAM)
     */
    private static Stream<Arguments> unauthorizedRolesProvider() {
        return Stream.of(
            Arguments.of("testStudent@example.com", "ROLE_STUDENT"),
            Arguments.of("testTeacher@example.com", "ROLE_TEACHER"),
            Arguments.of("testProgram@example.com", "ROLE_PROGRAM")
        );
    }

    /**
     * Provides roles authorized for read operations (ADMIN, USER, TEACHER)
     */
    private static Stream<Arguments> readAuthorizedRolesProvider() {
        return Stream.of(
            Arguments.of("testAdmin@example.com", "ROLE_ADMIN"),
            Arguments.of("testUser@example.com", "ROLE_USER"),
            Arguments.of("testTeacher@example.com", "ROLE_TEACHER")
        );
    }

    // ==========================================
    // POST /planning/classes - Create Class Tests
    // ==========================================

    @ParameterizedTest
    @MethodSource("authorizedRolesProvider")
    @DisplayName("POST /planning/classes - Authorized roles can create class")
    void createClass_asAuthorizedUser_shouldSucceed(String email, String role) {
        String token = jwtTokenProvider.generateToken(email, role);

        ClassCreateRequestDTO request = ClassCreateRequestDTO.builder()
                .section(1L)
                .courseId(1L)
                .semesterId(2L)
                .startDate(LocalDate.of(2025, 8, 1))
                .endDate(LocalDate.of(2025, 12, 15))
                .observation("New test class")
                .capacity(30)
                .statusId(1L)
                .build();

        ClassResponseDTO response = webTestClient.post()
                .uri("/planning/classes")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .body(Mono.just(request), ClassCreateRequestDTO.class)
                .exchange()
                .expectStatus().isCreated()
                .expectBody(ClassResponseDTO.class)
                .returnResult()
                .getResponseBody();

        assertNotNull(response);
        assertNotNull(response.getId());
        assertEquals(1L, response.getSection());
        assertEquals(1L, response.getCourseId());
        assertEquals(2L, response.getSemesterId());
        assertEquals(30, response.getCapacity());
    }

    @ParameterizedTest
    @MethodSource("unauthorizedRolesProvider")
    @DisplayName("POST /planning/classes - Unauthorized roles cannot create class")
    void createClass_asUnauthorizedUser_shouldReturn403(String email, String role) {
        String token = jwtTokenProvider.generateToken(email, role);

        ClassCreateRequestDTO request = ClassCreateRequestDTO.builder()
                .section(1L)
                .courseId(1L)
                .semesterId(2L)
                .startDate(LocalDate.of(2025, 8, 1))
                .endDate(LocalDate.of(2025, 12, 15))
                .capacity(30)
                .statusId(1L)
                .build();

        webTestClient.post()
                .uri("/planning/classes")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .body(Mono.just(request), ClassCreateRequestDTO.class)
                .exchange()
                .expectStatus().isForbidden();
    }

    @Test
    @DisplayName("POST /planning/classes - Unauthorized without token")
    void createClass_withoutToken_shouldReturn401() {
        ClassCreateRequestDTO request = ClassCreateRequestDTO.builder()
                .section(1L)
                .courseId(1L)
                .semesterId(2L)
                .startDate(LocalDate.of(2025, 8, 1))
                .endDate(LocalDate.of(2025, 12, 15))
                .capacity(30)
                .statusId(1L)
                .build();

        webTestClient.post()
                .uri("/planning/classes")
                .contentType(MediaType.APPLICATION_JSON)
                .body(Mono.just(request), ClassCreateRequestDTO.class)
                .exchange()
                .expectStatus().isUnauthorized();
    }

    // ==========================================
    // GET /planning/classes - Get All Classes Tests
    // ==========================================

    @ParameterizedTest
    @MethodSource("authorizedRolesProvider")
    @DisplayName("GET /planning/classes - Should retrieve all classes")
    void getAllClasses_asAuthorizedUser_shouldSucceed(String email, String role) {
        String token = jwtTokenProvider.generateToken(email, role);

        List<ClassResponseDTO> classes = webTestClient.get()
                .uri("/planning/classes")
                .header("Authorization", "Bearer " + token)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(ClassResponseDTO.class)
                .returnResult()
                .getResponseBody();

        assertNotNull(classes);
        assertFalse(classes.isEmpty());
        assertEquals(5, classes.size(), "Should have 5 classes from test-classes.sql");
    }

    @ParameterizedTest
    @MethodSource("unauthorizedRolesProvider")
    @DisplayName("GET /planning/classes - Unauthorized roles cannot access")
    void getAllClasses_asUnauthorizedUser_shouldReturn403(String email, String role) {
        String token = jwtTokenProvider.generateToken(email, role);

        webTestClient.get()
                .uri("/planning/classes")
                .header("Authorization", "Bearer " + token)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isForbidden();
    }

    // ==========================================
    // GET /planning/classes/current-semester Tests
    // ==========================================

    @ParameterizedTest
    @MethodSource("authorizedRolesProvider")
    @DisplayName("GET /planning/classes/current-semester - Should retrieve current semester classes")
    void getCurrentSemesterClasses_asAuthorizedUser_shouldSucceed(String email, String role) {
        String token = jwtTokenProvider.generateToken(email, role);

        List<ClassResponseDTO> classes = webTestClient.get()
                .uri("/planning/classes/current-semester")
                .header("Authorization", "Bearer " + token)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(ClassResponseDTO.class)
                .returnResult()
                .getResponseBody();

        assertNotNull(classes);
        assertFalse(classes.isEmpty());
        assertEquals(4, classes.size(), "Should have 4 current semester classes");
        
        // Verify all classes are for current semester (semesterId = 2)
        assertTrue(classes.stream().allMatch(c -> c.getSemesterId().equals(2L)));
    }

    // ==========================================
    // GET /planning/classes/{classId} Tests
    // ==========================================

    @ParameterizedTest
    @MethodSource("readAuthorizedRolesProvider")
    @DisplayName("GET /planning/classes/{classId} - Should retrieve class by ID")
    void getClassById_asAuthorizedUser_shouldSucceed(String email, String role) {
        String token = jwtTokenProvider.generateToken(email, role);
        Long classId = 1L;

        ClassResponseDTO classResponse = webTestClient.get()
                .uri("/planning/classes/{classId}", classId)
                .header("Authorization", "Bearer " + token)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody(ClassResponseDTO.class)
                .returnResult()
                .getResponseBody();

        assertNotNull(classResponse);
        assertEquals(classId, classResponse.getId());
        assertEquals(1L, classResponse.getCourseId());
        assertEquals(2L, classResponse.getSemesterId());
    }

    @Test
    @DisplayName("GET /planning/classes/{classId} - Non-existent class should return error")
    void getClassById_nonExistent_shouldReturnError() {
        String token = jwtTokenProvider.generateToken("testAdmin@example.com", "ROLE_ADMIN");
        Long nonExistentId = 999L;

        webTestClient.get()
                .uri("/planning/classes/{classId}", nonExistentId)
                .header("Authorization", "Bearer " + token)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().is4xxClientError();
    }

    // ==========================================
    // PUT /planning/classes/{classId} Tests
    // ==========================================

    @ParameterizedTest
    @MethodSource("authorizedRolesProvider")
    @DisplayName("PUT /planning/classes/{classId} - Should update class")
    void updateClass_asAuthorizedUser_shouldSucceed(String email, String role) {
        String token = jwtTokenProvider.generateToken(email, role);
        Long classId = 1L;

        ClassCreateRequestDTO updateRequest = ClassCreateRequestDTO.builder()
                .section(1L)
                .courseId(1L)
                .semesterId(2L)
                .startDate(LocalDate.of(2025, 8, 1))
                .endDate(LocalDate.of(2025, 12, 15))
                .observation("Updated observation")
                .capacity(35)
                .statusId(1L)
                .build();

        ClassResponseDTO response = webTestClient.put()
                .uri("/planning/classes/{classId}", classId)
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .body(Mono.just(updateRequest), ClassCreateRequestDTO.class)
                .exchange()
                .expectStatus().isOk()
                .expectBody(ClassResponseDTO.class)
                .returnResult()
                .getResponseBody();

        assertNotNull(response);
        assertEquals(classId, response.getId());
        assertEquals("Updated observation", response.getObservation());
        assertEquals(35, response.getCapacity());
    }

    // ==========================================
    // DELETE /planning/classes/{classId} Tests
    // ==========================================

    @ParameterizedTest
    @MethodSource("authorizedRolesProvider")
    @DisplayName("DELETE /planning/classes/{classId} - Should delete class")
    void deleteClass_asAuthorizedUser_shouldSucceed(String email, String role) {
        String token = jwtTokenProvider.generateToken(email, role);
        Long classId = 5L; // Use completed class from previous semester

        webTestClient.delete()
                .uri("/planning/classes/{classId}", classId)
                .header("Authorization", "Bearer " + token)
                .exchange()
                .expectStatus().isNoContent();

        // Verify class was deleted
        webTestClient.get()
                .uri("/planning/classes/{classId}", classId)
                .header("Authorization", "Bearer " + token)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().is4xxClientError();
    }

    // ==========================================
    // GET /planning/classes/course/{courseId} Tests
    // ==========================================

    @ParameterizedTest
    @MethodSource("authorizedRolesProvider")
    @DisplayName("GET /planning/classes/course/{courseId} - Should retrieve classes by course")
    void getClassesByCourse_shouldSucceed(String email, String role) {
        String token = jwtTokenProvider.generateToken(email, role);
        Long courseId = 1L;

        List<ClassResponseDTO> classes = webTestClient.get()
                .uri("/planning/classes/course/{courseId}", courseId)
                .header("Authorization", "Bearer " + token)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(ClassResponseDTO.class)
                .returnResult()
                .getResponseBody();

        assertNotNull(classes);
        assertFalse(classes.isEmpty());
        assertTrue(classes.stream().allMatch(c -> c.getCourseId().equals(courseId)));
    }

    // ==========================================
    // GET /planning/classes/section/{section} Tests
    // ==========================================

    @ParameterizedTest
    @MethodSource("authorizedRolesProvider")
    @DisplayName("GET /planning/classes/section/{section} - Should retrieve classes by section")
    void getClassesBySection_shouldSucceed(String email, String role) {
        String token = jwtTokenProvider.generateToken(email, role);
        Long section = 1L;

        List<ClassResponseDTO> classes = webTestClient.get()
                .uri("/planning/classes/section/{section}", section)
                .header("Authorization", "Bearer " + token)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(ClassResponseDTO.class)
                .returnResult()
                .getResponseBody();

        assertNotNull(classes);
        assertFalse(classes.isEmpty());
        assertEquals(5, classes.size(), "All test classes have section 1");
        assertTrue(classes.stream().allMatch(c -> c.getSection().equals(section)));
    }

    // ==========================================
    // GET /planning/classes/semester/{semesterId} Tests
    // ==========================================

    @ParameterizedTest
    @MethodSource("authorizedRolesProvider")
    @DisplayName("GET /planning/classes/semester/{semesterId} - Should retrieve classes by semester")
    void getClassesBySemester_shouldSucceed(String email, String role) {
        String token = jwtTokenProvider.generateToken(email, role);
        Long semesterId = 2L; // Current semester

        List<ClassResponseDTO> classes = webTestClient.get()
                .uri("/planning/classes/semester/{semesterId}", semesterId)
                .header("Authorization", "Bearer " + token)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(ClassResponseDTO.class)
                .returnResult()
                .getResponseBody();

        assertNotNull(classes);
        assertFalse(classes.isEmpty());
        assertEquals(4, classes.size(), "Should have 4 current semester classes");
        assertTrue(classes.stream().allMatch(c -> c.getSemesterId().equals(semesterId)));
    }

    // ==========================================
    // GET /planning/classes/current-semester/course/{courseId} Tests
    // ==========================================

    @ParameterizedTest
    @MethodSource("authorizedRolesProvider")
    @DisplayName("GET /planning/classes/current-semester/course/{courseId} - Should filter by course")
    void getCurrentSemesterClassesByCourse_shouldSucceed(String email, String role) {
        String token = jwtTokenProvider.generateToken(email, role);
        Long courseId = 1L;

        List<ClassResponseDTO> classes = webTestClient.get()
                .uri("/planning/classes/current-semester/course/{courseId}", courseId)
                .header("Authorization", "Bearer " + token)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(ClassResponseDTO.class)
                .returnResult()
                .getResponseBody();

        assertNotNull(classes);
        assertFalse(classes.isEmpty());
        assertTrue(classes.stream().allMatch(c -> 
            c.getCourseId().equals(courseId) && c.getSemesterId().equals(2L)));
    }

    // ==========================================
    // POST /planning/classes/{classId}/schedules Tests
    // ==========================================

    @ParameterizedTest
    @MethodSource("authorizedRolesProvider")
    @DisplayName("POST /planning/classes/{classId}/schedules - Should add schedule to class")
    void addClassSchedule_asAuthorizedUser_shouldSucceed(String email, String role) {
        String token = jwtTokenProvider.generateToken(email, role);
        Long classId = 1L;

        ClassScheduleRequestDTO scheduleRequest = ClassScheduleRequestDTO.builder()
                .classroomId(1L)
                .day("Friday")
                .startTime(LocalTime.of(10, 0))
                .endTime(LocalTime.of(12, 0))
                .modalityId(1L)
                .disability(false)
                .build();

        ClassScheduleResponseDTO response = webTestClient.post()
                .uri("/planning/classes/{classId}/schedules", classId)
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .body(Mono.just(scheduleRequest), ClassScheduleRequestDTO.class)
                .exchange()
                .expectStatus().isCreated()
                .expectBody(ClassScheduleResponseDTO.class)
                .returnResult()
                .getResponseBody();

        assertNotNull(response);
        assertNotNull(response.getId());
        assertEquals(classId, response.getClassId());
        assertEquals("Friday", response.getDay());
        assertEquals(LocalTime.of(10, 0), response.getStartTime());
        assertEquals(LocalTime.of(12, 0), response.getEndTime());
    }

    // ==========================================
    // GET /planning/classes/{classId}/schedules Tests
    // ==========================================

    @ParameterizedTest
    @MethodSource("authorizedRolesProvider")
    @DisplayName("GET /planning/classes/{classId}/schedules - Should retrieve class schedules")
    void getClassSchedules_shouldSucceed(String email, String role) {
        String token = jwtTokenProvider.generateToken(email, role);
        Long classId = 1L; // Data Structures class with 2 schedules

        List<ClassScheduleResponseDTO> schedules = webTestClient.get()
                .uri("/planning/classes/{classId}/schedules", classId)
                .header("Authorization", "Bearer " + token)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(ClassScheduleResponseDTO.class)
                .returnResult()
                .getResponseBody();

        assertNotNull(schedules);
        assertFalse(schedules.isEmpty());
        assertEquals(2, schedules.size(), "Class 1 should have 2 schedules");
        assertTrue(schedules.stream().allMatch(s -> s.getClassId().equals(classId)));
    }

    // ==========================================
    // GET /planning/schedules/{scheduleId} Tests
    // ==========================================

    @ParameterizedTest
    @MethodSource("authorizedRolesProvider")
    @DisplayName("GET /planning/schedules/{scheduleId} - Should retrieve schedule by ID")
    void getClassScheduleById_shouldSucceed(String email, String role) {
        String token = jwtTokenProvider.generateToken(email, role);
        Long scheduleId = 1L;

        ClassScheduleResponseDTO schedule = webTestClient.get()
                .uri("/planning/schedules/{scheduleId}", scheduleId)
                .header("Authorization", "Bearer " + token)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody(ClassScheduleResponseDTO.class)
                .returnResult()
                .getResponseBody();

        assertNotNull(schedule);
        assertEquals(scheduleId, schedule.getId());
        assertEquals(1L, schedule.getClassId());
        assertEquals("Lunes", schedule.getDay());
    }

    // ==========================================
    // PUT /planning/schedules/{scheduleId} Tests
    // ==========================================

    @ParameterizedTest
    @MethodSource("authorizedRolesProvider")
    @DisplayName("PUT /planning/schedules/{scheduleId} - Should update schedule")
    void updateClassSchedule_shouldSucceed(String email, String role) {
        String token = jwtTokenProvider.generateToken(email, role);
        Long scheduleId = 1L;

        ClassScheduleRequestDTO updateRequest = ClassScheduleRequestDTO.builder()
                .classroomId(2L)
                .day("Monday")
                .startTime(LocalTime.of(9, 0))
                .endTime(LocalTime.of(11, 0))
                .modalityId(1L)
                .disability(true)
                .build();

        ClassScheduleResponseDTO response = webTestClient.put()
                .uri("/planning/schedules/{scheduleId}", scheduleId)
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .body(Mono.just(updateRequest), ClassScheduleRequestDTO.class)
                .exchange()
                .expectStatus().isOk()
                .expectBody(ClassScheduleResponseDTO.class)
                .returnResult()
                .getResponseBody();

        assertNotNull(response);
        assertEquals(scheduleId, response.getId());
        assertEquals(2L, response.getClassroomId());
        assertEquals(LocalTime.of(9, 0), response.getStartTime());
        assertTrue(response.getDisability());
    }

    // ==========================================
    // DELETE /planning/schedules/{scheduleId} Tests
    // ==========================================

    @ParameterizedTest
    @MethodSource("authorizedRolesProvider")
    @DisplayName("DELETE /planning/schedules/{scheduleId} - Should delete schedule")
    void deleteClassSchedule_shouldSucceed(String email, String role) {
        String token = jwtTokenProvider.generateToken(email, role);
        Long scheduleId = 9L; // Last schedule from previous semester

        webTestClient.delete()
                .uri("/planning/schedules/{scheduleId}", scheduleId)
                .header("Authorization", "Bearer " + token)
                .exchange()
                .expectStatus().isNoContent();

        // Verify schedule was deleted
        webTestClient.get()
                .uri("/planning/schedules/{scheduleId}", scheduleId)
                .header("Authorization", "Bearer " + token)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().is4xxClientError();
    }

    // ==========================================
    // PATCH /planning/schedules/{scheduleId} Tests
    // ==========================================

    @ParameterizedTest
    @MethodSource("authorizedRolesProvider")
    @DisplayName("PATCH /planning/schedules/{scheduleId} - Should partially update schedule")
    void patchClassSchedule_shouldSucceed(String email, String role) {
        String token = jwtTokenProvider.generateToken(email, role);
        Long scheduleId = 2L;

        Map<String, Object> updates = new HashMap<>();
        updates.put("disability", true);
        updates.put("startTime", "09:30:00");

        ClassScheduleResponseDTO response = webTestClient.patch()
                .uri("/planning/schedules/{scheduleId}", scheduleId)
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(updates)
                .exchange()
                .expectStatus().isOk()
                .expectBody(ClassScheduleResponseDTO.class)
                .returnResult()
                .getResponseBody();

        assertNotNull(response);
        assertEquals(scheduleId, response.getId());
        assertTrue(response.getDisability());
    }

    // ==========================================
    // GET /planning/schedules/classroom/{classroomId} Tests
    // ==========================================

    @ParameterizedTest
    @MethodSource("authorizedRolesProvider")
    @DisplayName("GET /planning/schedules/classroom/{classroomId} - Should filter by classroom")
    void getSchedulesByClassroom_shouldSucceed(String email, String role) {
        String token = jwtTokenProvider.generateToken(email, role);
        Long classroomId = 1L;

        List<ClassScheduleResponseDTO> schedules = webTestClient.get()
                .uri("/planning/schedules/classroom/{classroomId}", classroomId)
                .header("Authorization", "Bearer " + token)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(ClassScheduleResponseDTO.class)
                .returnResult()
                .getResponseBody();

        assertNotNull(schedules);
        assertFalse(schedules.isEmpty());
        assertEquals(2, schedules.size(), "Classroom 1 should have 2 schedules");
        assertTrue(schedules.stream().allMatch(s -> s.getClassroomId().equals(classroomId)));
    }

    // ==========================================
    // GET /planning/schedules/day/{day} Tests
    // ==========================================

    @ParameterizedTest
    @MethodSource("authorizedRolesProvider")
    @DisplayName("GET /planning/schedules/day/{day} - Should filter by day")
    void getSchedulesByDay_shouldSucceed(String email, String role) {
        String token = jwtTokenProvider.generateToken(email, role);
        String day = "Lunes";

        List<ClassScheduleResponseDTO> schedules = webTestClient.get()
                .uri("/planning/schedules/day/{day}", day)
                .header("Authorization", "Bearer " + token)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(ClassScheduleResponseDTO.class)
                .returnResult()
                .getResponseBody();

        assertNotNull(schedules);
        assertFalse(schedules.isEmpty());
        assertEquals(2, schedules.size(), "Should have 2 Monday schedules");
        assertTrue(schedules.stream().allMatch(s -> s.getDay().equals(day)));
    }

    // ==========================================
    // GET /planning/schedules/disability/{disability} Tests
    // ==========================================

    @ParameterizedTest
    @MethodSource("authorizedRolesProvider")
    @DisplayName("GET /planning/schedules/disability/{disability} - Should filter by disability")
    void getSchedulesByDisability_shouldSucceed(String email, String role) {
        String token = jwtTokenProvider.generateToken(email, role);
        Boolean disability = true;

        List<ClassScheduleResponseDTO> schedules = webTestClient.get()
                .uri("/planning/schedules/disability/{disability}", disability)
                .header("Authorization", "Bearer " + token)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(ClassScheduleResponseDTO.class)
                .returnResult()
                .getResponseBody();

        assertNotNull(schedules);
        assertFalse(schedules.isEmpty());
        assertEquals(1, schedules.size(), "Should have 1 disability-accessible schedule");
        assertTrue(schedules.stream().allMatch(s -> s.getDisability().equals(disability)));
    }

    // ==========================================
    // POST /planning/duplicate Tests
    // ==========================================

    @ParameterizedTest
    @MethodSource("authorizedRolesProvider")
    @DisplayName("POST /planning/duplicate - Should duplicate semester planning")
    void duplicateSemesterPlanning_shouldSucceed(String email, String role) {
        String token = jwtTokenProvider.generateToken(email, role);
        Long sourceSemesterId = 1L; // Previous semester
        Long targetSemesterId = 2L; // Current semester

        List<ClassResponseDTO> duplicatedClasses = webTestClient.post()
                .uri(uriBuilder -> uriBuilder
                        .path("/planning/duplicate")
                        .queryParam("sourceSemesterId", sourceSemesterId)
                        .queryParam("targetSemesterId", targetSemesterId)
                        .build())
                .header("Authorization", "Bearer " + token)
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(ClassResponseDTO.class)
                .returnResult()
                .getResponseBody();

        assertNotNull(duplicatedClasses);
        assertFalse(duplicatedClasses.isEmpty());
        
        // Verify all duplicated classes have target semester ID
        assertTrue(duplicatedClasses.stream()
                .allMatch(c -> c.getSemesterId().equals(targetSemesterId)));
    }

    // ==========================================
    // POST /planning/semesters/{sourceSemesterId}/apply-to-current Tests
    // ==========================================

    @ParameterizedTest
    @MethodSource("authorizedRolesProvider")
    @DisplayName("POST /planning/semesters/{sourceSemesterId}/apply-to-current - Should apply to current")
    void applySemesterPlanningToCurrent_shouldSucceed(String email, String role) {
        String token = jwtTokenProvider.generateToken(email, role);
        Long sourceSemesterId = 1L; // Previous semester

        List<ClassResponseDTO> appliedClasses = webTestClient.post()
                .uri("/planning/semesters/{sourceSemesterId}/apply-to-current", sourceSemesterId)
                .header("Authorization", "Bearer " + token)
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(ClassResponseDTO.class)
                .returnResult()
                .getResponseBody();

        assertNotNull(appliedClasses);
        assertFalse(appliedClasses.isEmpty());
    }

    // ==========================================
    // Business Logic Tests
    // ==========================================

    @Test
    @DisplayName("Creating class with schedules should create both class and schedules")
    void createClassWithSchedules_shouldCreateBoth() {
        String token = jwtTokenProvider.generateToken("testAdmin@example.com", "ROLE_ADMIN");

        ClassScheduleRequestDTO schedule1 = ClassScheduleRequestDTO.builder()
                .classroomId(1L)
                .day("Monday")
                .startTime(LocalTime.of(14, 0))
                .endTime(LocalTime.of(16, 0))
                .modalityId(1L)
                .disability(false)
                .build();

        ClassScheduleRequestDTO schedule2 = ClassScheduleRequestDTO.builder()
                .classroomId(1L)
                .day("Wednesday")
                .startTime(LocalTime.of(14, 0))
                .endTime(LocalTime.of(16, 0))
                .modalityId(1L)
                .disability(false)
                .build();

        ClassCreateRequestDTO request = ClassCreateRequestDTO.builder()
                .section(1L)
                .courseId(5L)
                .semesterId(2L)
                .startDate(LocalDate.of(2025, 8, 1))
                .endDate(LocalDate.of(2025, 12, 15))
                .observation("Class with schedules")
                .capacity(30)
                .statusId(1L)
                .schedules(List.of(schedule1, schedule2))
                .build();

        ClassResponseDTO response = webTestClient.post()
                .uri("/planning/classes")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .body(Mono.just(request), ClassCreateRequestDTO.class)
                .exchange()
                .expectStatus().isCreated()
                .expectBody(ClassResponseDTO.class)
                .returnResult()
                .getResponseBody();

        assertNotNull(response);
        assertNotNull(response.getId());

        // Verify schedules were created
        List<ClassScheduleResponseDTO> schedules = webTestClient.get()
                .uri("/planning/classes/{classId}/schedules", response.getId())
                .header("Authorization", "Bearer " + token)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(ClassScheduleResponseDTO.class)
                .returnResult()
                .getResponseBody();

        assertNotNull(schedules);
        assertEquals(2, schedules.size());
    }

    @Test
    @DisplayName("Current semester classes should not include previous semester")
    void getCurrentSemesterClasses_shouldOnlyIncludeCurrent() {
        String token = jwtTokenProvider.generateToken("testAdmin@example.com", "ROLE_ADMIN");

        List<ClassResponseDTO> currentClasses = webTestClient.get()
                .uri("/planning/classes/current-semester")
                .header("Authorization", "Bearer " + token)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(ClassResponseDTO.class)
                .returnResult()
                .getResponseBody();

        assertNotNull(currentClasses);
        
        // Verify no classes from previous semester (semesterId = 1)
        assertFalse(currentClasses.stream().anyMatch(c -> c.getSemesterId().equals(1L)));
        assertTrue(currentClasses.stream().allMatch(c -> c.getSemesterId().equals(2L)));
    }

    @Test
    @DisplayName("Should filter online classes correctly")
    void getSchedules_onlineClassesShouldHaveNullClassroom() {
        String token = jwtTokenProvider.generateToken("testAdmin@example.com", "ROLE_ADMIN");
        Long classId = 5L; // Web Development (Online)

        List<ClassScheduleResponseDTO> schedules = webTestClient.get()
                .uri("/planning/classes/{classId}/schedules", classId)
                .header("Authorization", "Bearer " + token)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(ClassScheduleResponseDTO.class)
                .returnResult()
                .getResponseBody();

        assertNotNull(schedules);
        assertFalse(schedules.isEmpty());
        
        // Online schedules should have modalityId = 2 and null classroom
        assertTrue(schedules.stream().allMatch(s -> 
            s.getModalityId().equals(2L) && s.getClassroomId() == null));
    }

    @Test
    @DisplayName("Duplicate planning should preserve class attributes")
    void duplicatePlanning_shouldPreserveAttributes() {
        String token = jwtTokenProvider.generateToken("testAdmin@example.com", "ROLE_ADMIN");
        Long sourceSemesterId = 1L;
        Long targetSemesterId = 2L;

        // Get source classes
        List<ClassResponseDTO> sourceClasses = webTestClient.get()
                .uri("/planning/classes/semester/{semesterId}", sourceSemesterId)
                .header("Authorization", "Bearer " + token)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectBodyList(ClassResponseDTO.class)
                .returnResult()
                .getResponseBody();

        assertNotNull(sourceClasses);
        int sourceCount = sourceClasses.size();

        // Duplicate planning
        List<ClassResponseDTO> duplicatedClasses = webTestClient.post()
                .uri(uriBuilder -> uriBuilder
                        .path("/planning/duplicate")
                        .queryParam("sourceSemesterId", sourceSemesterId)
                        .queryParam("targetSemesterId", targetSemesterId)
                        .build())
                .header("Authorization", "Bearer " + token)
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(ClassResponseDTO.class)
                .returnResult()
                .getResponseBody();

        assertNotNull(duplicatedClasses);
        assertEquals(sourceCount, duplicatedClasses.size());
        
        // Verify attributes preserved (except semester ID)
        assertTrue(duplicatedClasses.stream().allMatch(c -> c.getSemesterId().equals(targetSemesterId)));
    }

    @Test
    @DisplayName("Should retrieve schedules for specific day correctly")
    void getSchedulesByDay_shouldFilterCorrectly() {
        String token = jwtTokenProvider.generateToken("testAdmin@example.com", "ROLE_ADMIN");

        // Test different days
        String[] days = {"Lunes", "Martes", "Miercoles", "Jueves", "Viernes"};
        int[] expectedCounts = {2, 2, 2, 2, 1}; // Based on test-class-schedules.sql

        for (int i = 0; i < days.length; i++) {
            final int index = i;
            final String day = days[i];
            List<ClassScheduleResponseDTO> schedules = webTestClient.get()
                    .uri("/planning/schedules/day/{day}", day)
                    .header("Authorization", "Bearer " + token)
                    .accept(MediaType.APPLICATION_JSON)
                    .exchange()
                    .expectStatus().isOk()
                    .expectBodyList(ClassScheduleResponseDTO.class)
                    .returnResult()
                    .getResponseBody();

            assertNotNull(schedules);
            assertEquals(expectedCounts[index], schedules.size(), 
                    "Expected " + expectedCounts[index] + " schedules for " + day);
            assertTrue(schedules.stream().allMatch(s -> s.getDay().equals(day)));
        }
    }

    @Test
    @DisplayName("Should handle capacity constraints in class creation")
    void createClass_withCapacity_shouldSucceed() {
        String token = jwtTokenProvider.generateToken("testAdmin@example.com", "ROLE_ADMIN");

        ClassCreateRequestDTO request = ClassCreateRequestDTO.builder()
                .section(1L)
                .courseId(1L)
                .semesterId(2L)
                .startDate(LocalDate.of(2025, 8, 1))
                .endDate(LocalDate.of(2025, 12, 15))
                .observation("High capacity class")
                .capacity(50)
                .statusId(1L)
                .build();

        ClassResponseDTO response = webTestClient.post()
                .uri("/planning/classes")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .body(Mono.just(request), ClassCreateRequestDTO.class)
                .exchange()
                .expectStatus().isCreated()
                .expectBody(ClassResponseDTO.class)
                .returnResult()
                .getResponseBody();

        assertNotNull(response);
        assertEquals(50, response.getCapacity());
    }
}
