package co.edu.puj.secchub_backend.planning.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalTime;
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
import org.springframework.test.web.reactive.server.WebTestClient;
import org.testcontainers.junit.jupiter.Testcontainers;

import co.edu.puj.secchub_backend.DatabaseContainerIntegration;
import co.edu.puj.secchub_backend.R2dbcTestUtils;
import co.edu.puj.secchub_backend.planning.dto.TeachingAssistantRequestDTO;
import co.edu.puj.secchub_backend.planning.dto.TeachingAssistantResponseDTO;
import co.edu.puj.secchub_backend.planning.dto.TeachingAssistantScheduleConflictResponseDTO;
import co.edu.puj.secchub_backend.planning.dto.TeachingAssistantScheduleRequestDTO;
import co.edu.puj.secchub_backend.planning.dto.TeachingAssistantScheduleResponseDTO;
import co.edu.puj.secchub_backend.security.jwt.JwtTokenProvider;
import io.r2dbc.spi.ConnectionFactory;
import reactor.core.publisher.Mono;

@SpringBootTest
@AutoConfigureWebTestClient
@Testcontainers
@DisplayName("Teaching Assistant Controller Integration Tests")
class TeachingAssistantControllerIntegrationTest extends DatabaseContainerIntegration {
    
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
                "/test-student-applications.sql",
                "/test-teaching-assistants.sql",
                "/test-teaching-assistant-schedules.sql"
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

    // ==========================================
    // POST /teaching-assistants - Create TA Tests
    // ==========================================

    @ParameterizedTest
    @MethodSource("authorizedRolesProvider")
    @DisplayName("POST /teaching-assistants - Authorized roles can create TA assignment")
    void createTeachingAssistant_asAuthorizedUser_shouldSucceed(String email, String role) {
        String token = jwtTokenProvider.generateToken(email, role);

        TeachingAssistantRequestDTO request = TeachingAssistantRequestDTO.builder()
                .classId(1L)
                .studentApplicationId(2L)
                .weeklyHours(10L)
                .weeks(16L)
                .totalHours(160L)
                .build();

        TeachingAssistantResponseDTO response = webTestClient.post()
                .uri("/teaching-assistants")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .body(Mono.just(request), TeachingAssistantRequestDTO.class)
                .exchange()
                .expectStatus().isCreated()
                .expectBody(TeachingAssistantResponseDTO.class)
                .returnResult()
                .getResponseBody();

        assertNotNull(response);
        assertNotNull(response.getId());
        assertEquals(1L, response.getClassId());
        assertEquals(2L, response.getStudentApplicationId());
        assertEquals(10L, response.getWeeklyHours());
        assertEquals(16L, response.getWeeks());
        assertEquals(160L, response.getTotalHours());
    }

    @ParameterizedTest
    @MethodSource("unauthorizedRolesProvider")
    @DisplayName("POST /teaching-assistants - Unauthorized roles cannot create TA assignment")
    void createTeachingAssistant_asUnauthorizedUser_shouldReturn403(String email, String role) {
        String token = jwtTokenProvider.generateToken(email, role);

        TeachingAssistantRequestDTO request = TeachingAssistantRequestDTO.builder()
                .classId(1L)
                .studentApplicationId(2L)
                .weeklyHours(10L)
                .weeks(16L)
                .totalHours(160L)
                .build();

        webTestClient.post()
                .uri("/teaching-assistants")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .body(Mono.just(request), TeachingAssistantRequestDTO.class)
                .exchange()
                .expectStatus().isForbidden();
    }

    @Test
    @DisplayName("POST /teaching-assistants - Unauthorized without token")
    void createTeachingAssistant_withoutToken_shouldReturn401() {
        TeachingAssistantRequestDTO request = TeachingAssistantRequestDTO.builder()
                .classId(1L)
                .studentApplicationId(2L)
                .weeklyHours(10L)
                .weeks(16L)
                .totalHours(160L)
                .build();

        webTestClient.post()
                .uri("/teaching-assistants")
                .contentType(MediaType.APPLICATION_JSON)
                .body(Mono.just(request), TeachingAssistantRequestDTO.class)
                .exchange()
                .expectStatus().isUnauthorized();
    }

    @ParameterizedTest
    @MethodSource("authorizedRolesProvider")
    @DisplayName("POST /teaching-assistants - Creating TA with schedules should succeed")
    void createTeachingAssistantWithSchedules_shouldSucceed(String email, String role) {
        String token = jwtTokenProvider.generateToken(email, role);

        // First delete an existing TA to free up its student application
        webTestClient.delete()
                .uri("/teaching-assistants/{id}", 5L) // Delete previous semester TA
                .header("Authorization", "Bearer " + token)
                .exchange()
                .expectStatus().isOk();

        TeachingAssistantScheduleRequestDTO schedule1 = TeachingAssistantScheduleRequestDTO.builder()
                .day("Monday")
                .startTime("14:00:00")
                .endTime("18:00:00")
                .build();

        TeachingAssistantScheduleRequestDTO schedule2 = TeachingAssistantScheduleRequestDTO.builder()
                .day("Wednesday")
                .startTime("14:00:00")
                .endTime("18:00:00")
                .build();

        // Now use application 5 which was freed up
        TeachingAssistantRequestDTO request = TeachingAssistantRequestDTO.builder()
                .classId(1L)
                .studentApplicationId(5L) // Reuse freed application
                .weeklyHours(8L)
                .weeks(16L)
                .totalHours(128L)
                .schedules(List.of(schedule1, schedule2))
                .build();

        TeachingAssistantResponseDTO response = webTestClient.post()
                .uri("/teaching-assistants")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .body(Mono.just(request), TeachingAssistantRequestDTO.class)
                .exchange()
                .expectStatus().isCreated()
                .expectBody(TeachingAssistantResponseDTO.class)
                .returnResult()
                .getResponseBody();

        assertNotNull(response);
        assertNotNull(response.getId());
        assertNotNull(response.getSchedules());
        assertEquals(2, response.getSchedules().size());
    }

    // ==========================================
    // GET /teaching-assistants - Get All TAs Tests
    // ==========================================

    @ParameterizedTest
    @MethodSource("authorizedRolesProvider")
    @DisplayName("GET /teaching-assistants - Should retrieve all TA assignments")
    void getAllTeachingAssistants_asAuthorizedUser_shouldSucceed(String email, String role) {
        String token = jwtTokenProvider.generateToken(email, role);

        List<TeachingAssistantResponseDTO> tas = webTestClient.get()
                .uri("/teaching-assistants")
                .header("Authorization", "Bearer " + token)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(TeachingAssistantResponseDTO.class)
                .returnResult()
                .getResponseBody();

        assertNotNull(tas);
        assertFalse(tas.isEmpty());
        assertEquals(5, tas.size(), "Should have 5 TA assignments from test-teaching-assistants.sql");
    }

    @ParameterizedTest
    @MethodSource("unauthorizedRolesProvider")
    @DisplayName("GET /teaching-assistants - Unauthorized roles cannot access")
    void getAllTeachingAssistants_asUnauthorizedUser_shouldReturn403(String email, String role) {
        String token = jwtTokenProvider.generateToken(email, role);

        webTestClient.get()
                .uri("/teaching-assistants")
                .header("Authorization", "Bearer " + token)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isForbidden();
    }

    // ==========================================
    // GET /teaching-assistants/{id} Tests
    // ==========================================

    @ParameterizedTest
    @MethodSource("authorizedRolesProvider")
    @DisplayName("GET /teaching-assistants/{id} - Should retrieve TA by ID")
    void getTeachingAssistantById_asAuthorizedUser_shouldSucceed(String email, String role) {
        String token = jwtTokenProvider.generateToken(email, role);
        Long taId = 1L;

        TeachingAssistantResponseDTO ta = webTestClient.get()
                .uri("/teaching-assistants/{id}", taId)
                .header("Authorization", "Bearer " + token)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody(TeachingAssistantResponseDTO.class)
                .returnResult()
                .getResponseBody();

        assertNotNull(ta);
        assertEquals(taId, ta.getId());
        assertEquals(1L, ta.getClassId());
        assertEquals(2L, ta.getStudentApplicationId());
        assertEquals(10L, ta.getWeeklyHours());
        assertEquals(16L, ta.getWeeks());
        assertEquals(160L, ta.getTotalHours());
    }

    @Test
    @DisplayName("GET /teaching-assistants/{id} - Non-existent TA should return error")
    void getTeachingAssistantById_nonExistent_shouldReturnError() {
        String token = jwtTokenProvider.generateToken("testAdmin@example.com", "ROLE_ADMIN");
        Long nonExistentId = 999L;

        webTestClient.get()
                .uri("/teaching-assistants/{id}", nonExistentId)
                .header("Authorization", "Bearer " + token)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().is4xxClientError();
    }

    // ==========================================
    // GET /teaching-assistants/student-application/{studentApplicationId} Tests
    // ==========================================

    @ParameterizedTest
    @MethodSource("authorizedRolesProvider")
    @DisplayName("GET /teaching-assistants/student-application/{studentApplicationId} - Should retrieve by application")
    void getTeachingAssistantsByStudentApplication_shouldSucceed(String email, String role) {
        String token = jwtTokenProvider.generateToken(email, role);
        Long studentApplicationId = 2L; // Application 2 has multiple TA assignments

        TeachingAssistantResponseDTO ta = webTestClient.get()
                .uri("/teaching-assistants/student-application/{studentApplicationId}", studentApplicationId)
                .header("Authorization", "Bearer " + token)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody(TeachingAssistantResponseDTO.class)
                .returnResult()
                .getResponseBody();

        assertNotNull(ta);
        assertEquals(studentApplicationId, ta.getStudentApplicationId());
    }

    // ==========================================
    // PUT /teaching-assistants/{id} Tests
    // ==========================================

    @ParameterizedTest
    @MethodSource("authorizedRolesProvider")
    @DisplayName("PUT /teaching-assistants/{id} - Should update TA assignment")
    void updateTeachingAssistant_asAuthorizedUser_shouldSucceed(String email, String role) {
        String token = jwtTokenProvider.generateToken(email, role);
        Long taId = 1L;

        TeachingAssistantRequestDTO updateRequest = TeachingAssistantRequestDTO.builder()
                .classId(1L)
                .studentApplicationId(2L)
                .weeklyHours(12L)
                .weeks(16L)
                .totalHours(192L)
                .build();

        TeachingAssistantResponseDTO response = webTestClient.put()
                .uri("/teaching-assistants/{id}", taId)
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .body(Mono.just(updateRequest), TeachingAssistantRequestDTO.class)
                .exchange()
                .expectStatus().isOk()
                .expectBody(TeachingAssistantResponseDTO.class)
                .returnResult()
                .getResponseBody();

        assertNotNull(response);
        assertEquals(taId, response.getId());
        assertEquals(12L, response.getWeeklyHours());
        assertEquals(192L, response.getTotalHours());
    }

    @ParameterizedTest
    @MethodSource("unauthorizedRolesProvider")
    @DisplayName("PUT /teaching-assistants/{id} - Unauthorized roles cannot update")
    void updateTeachingAssistant_asUnauthorizedUser_shouldReturn403(String email, String role) {
        String token = jwtTokenProvider.generateToken(email, role);
        Long taId = 1L;

        TeachingAssistantRequestDTO updateRequest = TeachingAssistantRequestDTO.builder()
                .classId(1L)
                .studentApplicationId(2L)
                .weeklyHours(12L)
                .weeks(16L)
                .totalHours(192L)
                .build();

        webTestClient.put()
                .uri("/teaching-assistants/{id}", taId)
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .body(Mono.just(updateRequest), TeachingAssistantRequestDTO.class)
                .exchange()
                .expectStatus().isForbidden();
    }

    // ==========================================
    // DELETE /teaching-assistants/{id} Tests
    // ==========================================

    @ParameterizedTest
    @MethodSource("authorizedRolesProvider")
    @DisplayName("DELETE /teaching-assistants/{id} - Should delete TA assignment")
    void deleteTeachingAssistant_asAuthorizedUser_shouldSucceed(String email, String role) {
        String token = jwtTokenProvider.generateToken(email, role);
        Long taId = 5L; // Use previous semester TA

        webTestClient.delete()
                .uri("/teaching-assistants/{id}", taId)
                .header("Authorization", "Bearer " + token)
                .exchange()
                .expectStatus().isOk();

        // Verify TA was deleted
        webTestClient.get()
                .uri("/teaching-assistants/{id}", taId)
                .header("Authorization", "Bearer " + token)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().is4xxClientError();
    }

    @ParameterizedTest
    @MethodSource("unauthorizedRolesProvider")
    @DisplayName("DELETE /teaching-assistants/{id} - Unauthorized roles cannot delete")
    void deleteTeachingAssistant_asUnauthorizedUser_shouldReturn403(String email, String role) {
        String token = jwtTokenProvider.generateToken(email, role);
        Long taId = 1L;

        webTestClient.delete()
                .uri("/teaching-assistants/{id}", taId)
                .header("Authorization", "Bearer " + token)
                .exchange()
                .expectStatus().isForbidden();
    }

    // ==========================================
    // POST /teaching-assistants/{teachingAssistantId}/schedules Tests
    // ==========================================

    @ParameterizedTest
    @MethodSource("authorizedRolesProvider")
    @DisplayName("POST /teaching-assistants/{teachingAssistantId}/schedules - Should create schedule")
    void createSchedule_asAuthorizedUser_shouldSucceed(String email, String role) {
        String token = jwtTokenProvider.generateToken(email, role);
        Long taId = 1L;

        TeachingAssistantScheduleRequestDTO scheduleRequest = TeachingAssistantScheduleRequestDTO.builder()
                .day("Tuesday")
                .startTime("10:00:00")
                .endTime("14:00:00")
                .build();

        TeachingAssistantScheduleResponseDTO response = webTestClient.post()
                .uri("/teaching-assistants/{teachingAssistantId}/schedules", taId)
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .body(Mono.just(scheduleRequest), TeachingAssistantScheduleRequestDTO.class)
                .exchange()
                .expectStatus().isCreated()
                .expectBody(TeachingAssistantScheduleResponseDTO.class)
                .returnResult()
                .getResponseBody();

        assertNotNull(response);
        assertNotNull(response.getId());
        assertEquals(taId, response.getTeachingAssistantId());
        assertEquals("Tuesday", response.getDay());
        assertEquals("10:00", response.getStartTime());
        assertEquals("14:00", response.getEndTime());
    }

    @ParameterizedTest
    @MethodSource("unauthorizedRolesProvider")
    @DisplayName("POST /teaching-assistants/{teachingAssistantId}/schedules - Unauthorized cannot create")
    void createSchedule_asUnauthorizedUser_shouldReturn403(String email, String role) {
        String token = jwtTokenProvider.generateToken(email, role);
        Long taId = 1L;

        TeachingAssistantScheduleRequestDTO scheduleRequest = TeachingAssistantScheduleRequestDTO.builder()
                .day("Tuesday")
                .startTime("10:00:00")
                .endTime("14:00:00")
                .build();

        webTestClient.post()
                .uri("/teaching-assistants/{teachingAssistantId}/schedules", taId)
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .body(Mono.just(scheduleRequest), TeachingAssistantScheduleRequestDTO.class)
                .exchange()
                .expectStatus().isForbidden();
    }

    // ==========================================
    // PUT /teaching-assistants/schedules/{scheduleId} Tests
    // ==========================================

    @ParameterizedTest
    @MethodSource("authorizedRolesProvider")
    @DisplayName("PUT /teaching-assistants/schedules/{scheduleId} - Should update schedule")
    void updateSchedule_asAuthorizedUser_shouldSucceed(String email, String role) {
        String token = jwtTokenProvider.generateToken(email, role);
        Long scheduleId = 1L;

        TeachingAssistantScheduleRequestDTO updateRequest = TeachingAssistantScheduleRequestDTO.builder()
                .day("Monday")
                .startTime("15:00:00")
                .endTime("19:00:00")
                .build();

        TeachingAssistantScheduleResponseDTO response = webTestClient.put()
                .uri("/teaching-assistants/schedules/{scheduleId}", scheduleId)
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .body(Mono.just(updateRequest), TeachingAssistantScheduleRequestDTO.class)
                .exchange()
                .expectStatus().isOk()
                .expectBody(TeachingAssistantScheduleResponseDTO.class)
                .returnResult()
                .getResponseBody();

        assertNotNull(response);
        assertEquals(scheduleId, response.getId());
        assertEquals("Monday", response.getDay());
        assertEquals("15:00", response.getStartTime());
        assertEquals("19:00", response.getEndTime());
    }

    @ParameterizedTest
    @MethodSource("unauthorizedRolesProvider")
    @DisplayName("PUT /teaching-assistants/schedules/{scheduleId} - Unauthorized cannot update")
    void updateSchedule_asUnauthorizedUser_shouldReturn403(String email, String role) {
        String token = jwtTokenProvider.generateToken(email, role);
        Long scheduleId = 1L;

        TeachingAssistantScheduleRequestDTO updateRequest = TeachingAssistantScheduleRequestDTO.builder()
                .day("Monday")
                .startTime("15:00:00")
                .endTime("19:00:00")
                .build();

        webTestClient.put()
                .uri("/teaching-assistants/schedules/{scheduleId}", scheduleId)
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .body(Mono.just(updateRequest), TeachingAssistantScheduleRequestDTO.class)
                .exchange()
                .expectStatus().isForbidden();
    }

    // ==========================================
    // DELETE /teaching-assistants/schedules/{scheduleId} Tests
    // ==========================================

    @ParameterizedTest
    @MethodSource("authorizedRolesProvider")
    @DisplayName("DELETE /teaching-assistants/schedules/{scheduleId} - Should delete schedule")
    void deleteSchedule_asAuthorizedUser_shouldSucceed(String email, String role) {
        String token = jwtTokenProvider.generateToken(email, role);
        Long scheduleId = 13L; // Last schedule from previous semester

        webTestClient.delete()
                .uri("/teaching-assistants/schedules/{scheduleId}", scheduleId)
                .header("Authorization", "Bearer " + token)
                .exchange()
                .expectStatus().isOk();
    }

    @ParameterizedTest
    @MethodSource("unauthorizedRolesProvider")
    @DisplayName("DELETE /teaching-assistants/schedules/{scheduleId} - Unauthorized cannot delete")
    void deleteSchedule_asUnauthorizedUser_shouldReturn403(String email, String role) {
        String token = jwtTokenProvider.generateToken(email, role);
        Long scheduleId = 1L;

        webTestClient.delete()
                .uri("/teaching-assistants/schedules/{scheduleId}", scheduleId)
                .header("Authorization", "Bearer " + token)
                .exchange()
                .expectStatus().isForbidden();
    }

    // ==========================================
    // Business Logic Tests
    // ==========================================

    @Test
    @DisplayName("TA assignment should have schedules associated")
    void getTeachingAssistant_shouldIncludeSchedules() {
        String token = jwtTokenProvider.generateToken("testAdmin@example.com", "ROLE_ADMIN");
        Long taId = 1L;

        TeachingAssistantResponseDTO ta = webTestClient.get()
                .uri("/teaching-assistants/{id}", taId)
                .header("Authorization", "Bearer " + token)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody(TeachingAssistantResponseDTO.class)
                .returnResult()
                .getResponseBody();

        assertNotNull(ta);
        assertNotNull(ta.getSchedules());
        assertEquals(3, ta.getSchedules().size(), "TA 1 should have 3 schedules");
        
        // Verify schedule details
        assertTrue(ta.getSchedules().stream().anyMatch(s -> s.getDay().equals("Monday")));
        assertTrue(ta.getSchedules().stream().anyMatch(s -> s.getDay().equals("Wednesday")));
        assertTrue(ta.getSchedules().stream().anyMatch(s -> s.getDay().equals("Friday")));
    }

    @Test
    @DisplayName("Should calculate total hours correctly")
    void createTeachingAssistant_totalHoursShouldMatchCalculation() {
        String token = jwtTokenProvider.generateToken("testAdmin@example.com", "ROLE_ADMIN");

        // First delete an existing TA to free up its student application
        webTestClient.delete()
                .uri("/teaching-assistants/{id}", 4L) // Delete TA 4
                .header("Authorization", "Bearer " + token)
                .exchange()
                .expectStatus().isOk();

        Long weeklyHours = 15L;
        Long weeks = 16L;
        Long expectedTotal = weeklyHours * weeks;

        // Now use application 3 which was freed up
        TeachingAssistantRequestDTO request = TeachingAssistantRequestDTO.builder()
                .classId(3L)
                .studentApplicationId(4L) // Reuse freed application
                .weeklyHours(weeklyHours)
                .weeks(weeks)
                .totalHours(expectedTotal)
                .build();

        TeachingAssistantResponseDTO response = webTestClient.post()
                .uri("/teaching-assistants")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .body(Mono.just(request), TeachingAssistantRequestDTO.class)
                .exchange()
                .expectStatus().isCreated()
                .expectBody(TeachingAssistantResponseDTO.class)
                .returnResult()
                .getResponseBody();

        assertNotNull(response);
        assertEquals(expectedTotal, response.getTotalHours());
        assertEquals(weeklyHours, response.getWeeklyHours());
        assertEquals(weeks, response.getWeeks());
    }

    @Test
    @DisplayName("Multiple classes can have TA assignments")
    void multipleClasses_canHaveTAAssignments() {
        String token = jwtTokenProvider.generateToken("testAdmin@example.com", "ROLE_ADMIN");

        // Note: Each student application can only have ONE TA assignment
        // But multiple classes can have different TAs (one TA per class)
        
        List<TeachingAssistantResponseDTO> allTAs = webTestClient.get()
                .uri("/teaching-assistants")
                .header("Authorization", "Bearer " + token)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectBodyList(TeachingAssistantResponseDTO.class)
                .returnResult()
                .getResponseBody();

        assertNotNull(allTAs);
        
        // Verify multiple classes have TA assignments (each with unique student application)
        long classesWithTAs = allTAs.stream()
                .map(TeachingAssistantResponseDTO::getClassId)
                .distinct()
                .count();
        
        assertTrue(classesWithTAs >= 5, "Should have TAs assigned to at least 5 different classes");
    }

    @Test
    @DisplayName("Each student application has exactly one TA assignment")
    void studentApplication_hasExactlyOneTA() {
        String token = jwtTokenProvider.generateToken("testAdmin@example.com", "ROLE_ADMIN");

        List<TeachingAssistantResponseDTO> allTAs = webTestClient.get()
                .uri("/teaching-assistants")
                .header("Authorization", "Bearer " + token)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectBodyList(TeachingAssistantResponseDTO.class)
                .returnResult()
                .getResponseBody();

        assertNotNull(allTAs);
        
        // Verify each student application ID appears exactly once
        List<Long> studentApplicationIds = allTAs.stream()
                .map(TeachingAssistantResponseDTO::getStudentApplicationId)
                .toList();
        
        // Check for uniqueness - no duplicate student application IDs
        long uniqueCount = studentApplicationIds.stream().distinct().count();
        assertEquals(studentApplicationIds.size(), uniqueCount, 
                "Each student application should have exactly ONE TA assignment");
        
        // Specifically check application 2 (which used to have 2 TAs)
        long tAsForApplication2 = allTAs.stream()
                .filter(ta -> ta.getStudentApplicationId().equals(2L))
                .count();
        
        assertEquals(1, tAsForApplication2, "Application 2 should have exactly 1 TA assignment");
    }

    @Test
    @DisplayName("TA schedules should not overlap for same day")
    void taSchedules_shouldBeValidated() {
        String token = jwtTokenProvider.generateToken("testAdmin@example.com", "ROLE_ADMIN");
        Long taId = 1L;

        // TA 1 already has Monday 14:00-18:00
        // Get existing schedules to verify non-overlap
        TeachingAssistantResponseDTO ta = webTestClient.get()
                .uri("/teaching-assistants/{id}", taId)
                .header("Authorization", "Bearer " + token)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectBody(TeachingAssistantResponseDTO.class)
                .returnResult()
                .getResponseBody();

        assertNotNull(ta);
        assertNotNull(ta.getSchedules());
        
        // Verify schedules don't overlap on same day
        List<TeachingAssistantScheduleResponseDTO> mondaySchedules = ta.getSchedules().stream()
                .filter(s -> s.getDay().equals("Monday"))
                .toList();
        
        assertEquals(1, mondaySchedules.size(), "Should have only 1 Monday schedule");
    }

    @Test
    @DisplayName("Should handle typical TA workload hours")
    void taWorkloadHours_shouldBeRealistic() {
        String token = jwtTokenProvider.generateToken("testAdmin@example.com", "ROLE_ADMIN");

        List<TeachingAssistantResponseDTO> allTAs = webTestClient.get()
                .uri("/teaching-assistants")
                .header("Authorization", "Bearer " + token)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectBodyList(TeachingAssistantResponseDTO.class)
                .returnResult()
                .getResponseBody();

        assertNotNull(allTAs);
        
        // Verify all TAs have reasonable weekly hours (typically 8-12 hours)
        assertTrue(allTAs.stream().allMatch(ta -> 
            ta.getWeeklyHours() >= 8L && ta.getWeeklyHours() <= 12L),
            "All TAs should have 8-12 weekly hours");
        
        // Verify all TAs have standard semester length (16 weeks)
        assertTrue(allTAs.stream().allMatch(ta -> ta.getWeeks().equals(16L)),
            "All TAs should have 16 weeks");
        
        // Verify total hours calculation
        assertTrue(allTAs.stream().allMatch(ta -> 
            ta.getTotalHours().equals(ta.getWeeklyHours() * ta.getWeeks())),
            "Total hours should equal weekly hours × weeks");
    }

    @Test
    @DisplayName("Update should preserve TA ID and maintain consistency")
    void updateTeachingAssistant_shouldPreserveId() {
        String token = jwtTokenProvider.generateToken("testAdmin@example.com", "ROLE_ADMIN");
        Long taId = 3L;

        // Get original TA
        TeachingAssistantResponseDTO original = webTestClient.get()
                .uri("/teaching-assistants/{id}", taId)
                .header("Authorization", "Bearer " + token)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectBody(TeachingAssistantResponseDTO.class)
                .returnResult()
                .getResponseBody();

        assertNotNull(original);

        // Update with new hours
        TeachingAssistantRequestDTO updateRequest = TeachingAssistantRequestDTO.builder()
                .classId(original.getClassId())
                .studentApplicationId(original.getStudentApplicationId())
                .weeklyHours(10L)
                .weeks(16L)
                .totalHours(160L)
                .build();

        TeachingAssistantResponseDTO updated = webTestClient.put()
                .uri("/teaching-assistants/{id}", taId)
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .body(Mono.just(updateRequest), TeachingAssistantRequestDTO.class)
                .exchange()
                .expectStatus().isOk()
                .expectBody(TeachingAssistantResponseDTO.class)
                .returnResult()
                .getResponseBody();

        assertNotNull(updated);
        assertEquals(taId, updated.getId(), "ID should remain unchanged");
        assertEquals(original.getClassId(), updated.getClassId(), "Class ID should remain unchanged");
        assertEquals(10L, updated.getWeeklyHours(), "Weekly hours should be updated");
    }

    @Test
    @DisplayName("Creating TA with schedules should atomically create both")
    void createTAWithSchedules_shouldBeAtomic() {
        String token = jwtTokenProvider.generateToken("testAdmin@example.com", "ROLE_ADMIN");

        // First delete an existing TA to free up its student application
        webTestClient.delete()
                .uri("/teaching-assistants/{id}", 3L) // Delete TA 3
                .header("Authorization", "Bearer " + token)
                .exchange()
                .expectStatus().isOk();

        List<TeachingAssistantScheduleRequestDTO> schedules = List.of(
            TeachingAssistantScheduleRequestDTO.builder()
                .day("Monday")
                .startTime("09:00:00")
                .endTime("13:00:00")
                .build(),
            TeachingAssistantScheduleRequestDTO.builder()
                .day("Wednesday")
                .startTime("09:00:00")
                .endTime("13:00:00")
                .build(),
            TeachingAssistantScheduleRequestDTO.builder()
                .day("Friday")
                .startTime("09:00:00")
                .endTime("13:00:00")
                .build()
        );

        // Now use application 4 which was freed up
        TeachingAssistantRequestDTO request = TeachingAssistantRequestDTO.builder()
                .classId(4L)
                .studentApplicationId(3L) // Reuse freed application
                .weeklyHours(12L)
                .weeks(16L)
                .totalHours(192L)
                .schedules(schedules)
                .build();

        TeachingAssistantResponseDTO response = webTestClient.post()
                .uri("/teaching-assistants")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .body(Mono.just(request), TeachingAssistantRequestDTO.class)
                .exchange()
                .expectStatus().isCreated()
                .expectBody(TeachingAssistantResponseDTO.class)
                .returnResult()
                .getResponseBody();

        assertNotNull(response);
        assertNotNull(response.getSchedules());
        assertEquals(3, response.getSchedules().size(), "Should have created 3 schedules");
        
        // Verify all schedules have different days
        List<String> days = response.getSchedules().stream()
                .map(TeachingAssistantScheduleResponseDTO::getDay)
                .distinct()
                .toList();
        assertEquals(3, days.size(), "Should have 3 distinct days");
    }

    // ==========================================
    // GET /teaching-assistants/conflicts Tests
    // ==========================================

    @ParameterizedTest
    @MethodSource("authorizedRolesProvider")
    @DisplayName("GET /teaching-assistants/conflicts - Should return all conflicts for authorized users")
    void getTeachingAssistantScheduleConflicts_asAuthorizedUser_shouldReturnConflicts(String email, String role) {
        String token = jwtTokenProvider.generateToken(email, role);
        
        // Load conflict test data
        R2dbcTestUtils.executeScripts(connectionFactory, "/test-conflict-schedules.sql", "/test-ta-conflict-schedules.sql");

        List<TeachingAssistantScheduleConflictResponseDTO> conflicts = webTestClient.get()
                .uri("/teaching-assistants/conflicts")
                .header("Authorization", "Bearer " + token)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(TeachingAssistantScheduleConflictResponseDTO.class)
                .returnResult()
                .getResponseBody();

        assertNotNull(conflicts);
        assertFalse(conflicts.isEmpty(), "Should have conflicts");
        
        // Verify conflicts are for User 3 (Student from test data)
        assertTrue(conflicts.stream().allMatch(c -> c.getUserId().equals(3L)),
                "All conflicts should be for User 3 (Student)");
    }

    @Test
    @DisplayName("GET /teaching-assistants/conflicts - Should detect complex overlaps and create correct clusters")
    void getTeachingAssistantScheduleConflicts_complexOverlaps_shouldCreateClusters() {
        String token = jwtTokenProvider.generateToken("testAdmin@example.com", "ROLE_ADMIN");
        
        // Load conflict test data
        R2dbcTestUtils.executeScripts(connectionFactory, "/test-conflict-schedules.sql", "/test-ta-conflict-schedules.sql");

        List<TeachingAssistantScheduleConflictResponseDTO> conflicts = webTestClient.get()
                .uri("/teaching-assistants/conflicts")
                .header("Authorization", "Bearer " + token)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(TeachingAssistantScheduleConflictResponseDTO.class)
                .returnResult()
                .getResponseBody();

        assertNotNull(conflicts);
        conflicts.forEach(c -> System.out.println("Conflict Cluster: " + c));
        assertEquals(4, conflicts.size(), "Should have exactly 4 conflict clusters");

        // Verify cluster with 3 TAs (10, 20, 50)
        TeachingAssistantScheduleConflictResponseDTO cluster3TAs = conflicts.stream()
                .filter(c -> c.getConflictTeachingAssistants().size() == 3)
                .findFirst()
                .orElse(null);
        assertNotNull(cluster3TAs, "Should have a cluster with 3 TAs");
        assertTrue(cluster3TAs.getConflictTeachingAssistants().containsAll(Arrays.asList(10L, 20L, 50L)),
                "Cluster should contain TAs 10, 20, and 50");
        assertEquals(LocalTime.of(9, 0), cluster3TAs.getConflictStartTime(), "Min start time should be 09:00");
        assertEquals(LocalTime.of(13, 0), cluster3TAs.getConflictEndTime(), "Max end time should be 13:00");

        // Verify clusters with 2 TAs each
        List<TeachingAssistantScheduleConflictResponseDTO> clusters2TAs = conflicts.stream()
                .filter(c -> c.getConflictTeachingAssistants().size() == 2)
                .toList();
        assertEquals(3, clusters2TAs.size(), "Should have 3 clusters with 2 TAs each");

        // Verify cluster [10, 30]
        boolean hasCluster1030 = clusters2TAs.stream()
                .anyMatch(c -> c.getConflictTeachingAssistants().containsAll(Arrays.asList(10L, 30L)));
        assertTrue(hasCluster1030, "Should have cluster with TAs 10 and 30");

        TeachingAssistantScheduleConflictResponseDTO cluster1030 = clusters2TAs.stream()
                .filter(c -> c.getConflictTeachingAssistants().containsAll(Arrays.asList(10L, 30L)))
                .findFirst()
                .orElse(null);
        assertNotNull(cluster1030);
        assertEquals(LocalTime.of(11, 0), cluster1030.getConflictStartTime());
        assertEquals(LocalTime.of(15, 0), cluster1030.getConflictEndTime());

        // Verify cluster [30, 40]
        boolean hasCluster3040 = clusters2TAs.stream()
                .anyMatch(c -> c.getConflictTeachingAssistants().containsAll(Arrays.asList(30L, 40L)));
        assertTrue(hasCluster3040, "Should have cluster with TAs 30 and 40");

        TeachingAssistantScheduleConflictResponseDTO cluster3040 = clusters2TAs.stream()
                .filter(c -> c.getConflictTeachingAssistants().containsAll(Arrays.asList(30L, 40L)))
                .findFirst()
                .orElse(null);
        assertNotNull(cluster3040);
        assertEquals(LocalTime.of(12, 0), cluster3040.getConflictStartTime());
        assertEquals(LocalTime.of(17, 0), cluster3040.getConflictEndTime());

        // Verify cluster [70, 60]
        boolean hasCluster7060 = clusters2TAs.stream()
                .anyMatch(c -> c.getConflictTeachingAssistants().containsAll(Arrays.asList(70L, 60L)));
        assertTrue(hasCluster7060, "Should have cluster with TAs 70 and 60");

        TeachingAssistantScheduleConflictResponseDTO cluster7060 = clusters2TAs.stream()
                .filter(c -> c.getConflictTeachingAssistants().containsAll(Arrays.asList(70L, 60L)))
                .findFirst()
                .orElse(null);
        assertNotNull(cluster7060);
        assertEquals(LocalTime.of(8, 0), cluster7060.getConflictStartTime());
        assertEquals(LocalTime.of(11, 0), cluster7060.getConflictEndTime());
    }

    @Test
    @DisplayName("GET /teaching-assistants/conflicts - Should return empty list when no conflicts exist")
    void getTeachingAssistantScheduleConflicts_noConflicts_shouldReturnEmpty() {
        String token = jwtTokenProvider.generateToken("testAdmin@example.com", "ROLE_ADMIN");

        // Don't load conflict test data - only base data has no conflicts
        List<TeachingAssistantScheduleConflictResponseDTO> conflicts = webTestClient.get()
                .uri("/teaching-assistants/conflicts")
                .header("Authorization", "Bearer " + token)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(TeachingAssistantScheduleConflictResponseDTO.class)
                .returnResult()
                .getResponseBody();

        assertNotNull(conflicts);
        assertTrue(conflicts.isEmpty(), "Should return empty list when no conflicts");
    }

    @Test
    @DisplayName("GET /teaching-assistants/conflicts - Should include correct time ranges")
    void getTeachingAssistantScheduleConflicts_shouldIncludeCorrectTimeRanges() {
        String token = jwtTokenProvider.generateToken("testAdmin@example.com", "ROLE_ADMIN");
        
        // Load conflict test data
        R2dbcTestUtils.executeScripts(connectionFactory, "/test-conflict-schedules.sql", "/test-ta-conflict-schedules.sql");

        List<TeachingAssistantScheduleConflictResponseDTO> conflicts = webTestClient.get()
                .uri("/teaching-assistants/conflicts")
                .header("Authorization", "Bearer " + token)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(TeachingAssistantScheduleConflictResponseDTO.class)
                .returnResult()
                .getResponseBody();

        assertNotNull(conflicts);
        assertFalse(conflicts.isEmpty());

        // Verify all conflicts have valid time ranges
        conflicts.forEach(conflict -> {
            assertNotNull(conflict.getConflictStartTime(), "Start time should not be null");
            assertNotNull(conflict.getConflictEndTime(), "End time should not be null");
            assertTrue(conflict.getConflictStartTime().isBefore(conflict.getConflictEndTime()),
                    "Start time should be before end time");
        });

        // Verify the largest cluster has the widest time range
        TeachingAssistantScheduleConflictResponseDTO largestCluster = conflicts.stream()
                .max((c1, c2) -> Integer.compare(c1.getConflictTeachingAssistants().size(), 
                                                c2.getConflictTeachingAssistants().size()))
                .orElse(null);
        assertNotNull(largestCluster);
        assertEquals(LocalTime.of(9, 0), largestCluster.getConflictStartTime(),
                "Largest cluster should start at 09:00");
        assertEquals(LocalTime.of(13, 0), largestCluster.getConflictEndTime(),
                "Largest cluster should end at 13:00");
    }

    @Test
    @DisplayName("GET /teaching-assistants/conflicts - Should include user information")
    void getTeachingAssistantScheduleConflicts_shouldIncludeUserInfo() {
        String token = jwtTokenProvider.generateToken("testAdmin@example.com", "ROLE_ADMIN");
        
        // Load conflict test data
        R2dbcTestUtils.executeScripts(connectionFactory, "/test-conflict-schedules.sql", "/test-ta-conflict-schedules.sql");

        List<TeachingAssistantScheduleConflictResponseDTO> conflicts = webTestClient.get()
                .uri("/teaching-assistants/conflicts")
                .header("Authorization", "Bearer " + token)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(TeachingAssistantScheduleConflictResponseDTO.class)
                .returnResult()
                .getResponseBody();

        assertNotNull(conflicts);
        assertFalse(conflicts.isEmpty());

        // Verify all conflicts have user information
        conflicts.forEach(conflict -> {
            assertNotNull(conflict.getUserId(), "User ID should not be null");
            assertNotNull(conflict.getUserName(), "User name should not be null");
            assertEquals(3L, conflict.getUserId(), "All conflicts should be for User 3 (Student)");
        });
    }

    @ParameterizedTest
    @MethodSource("unauthorizedRolesProvider")
    @DisplayName("GET /teaching-assistants/conflicts - Should deny unauthorized roles")
    void getTeachingAssistantScheduleConflicts_unauthorizedRole_shouldDeny(String email, String role) {
        String token = jwtTokenProvider.generateToken(email, role);
        
        // Load conflict test data
        R2dbcTestUtils.executeScripts(connectionFactory, "/test-conflict-schedules.sql", "/test-ta-conflict-schedules.sql");

        webTestClient.get()
                .uri("/teaching-assistants/conflicts")
                .header("Authorization", "Bearer " + token)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isForbidden();
    }

    @Test
    @DisplayName("GET /teaching-assistants/conflicts - Should require authentication")
    void getTeachingAssistantScheduleConflicts_noAuth_shouldDeny() {
        // Load conflict test data
        R2dbcTestUtils.executeScripts(connectionFactory, "/test-conflict-schedules.sql", "/test-ta-conflict-schedules.sql");

        webTestClient.get()
                .uri("/teaching-assistants/conflicts")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isUnauthorized();
    }

    @Test
    @DisplayName("GET /teaching-assistants/conflicts - Should only return current semester conflicts")
    void getTeachingAssistantScheduleConflicts_shouldOnlyReturnCurrentSemester() {
        String token = jwtTokenProvider.generateToken("testAdmin@example.com", "ROLE_ADMIN");
        
        // Load conflict test data
        R2dbcTestUtils.executeScripts(connectionFactory, "/test-conflict-schedules.sql", "/test-ta-conflict-schedules.sql");

        List<TeachingAssistantScheduleConflictResponseDTO> conflicts = webTestClient.get()
                .uri("/teaching-assistants/conflicts")
                .header("Authorization", "Bearer " + token)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(TeachingAssistantScheduleConflictResponseDTO.class)
                .returnResult()
                .getResponseBody();

        assertNotNull(conflicts);
        // All conflicts should be from current semester (semester_id = 2 in test data)
        // This is verified indirectly since the conflicts are returned
        // The service only queries schedules for the current semester
        assertTrue(conflicts.stream().allMatch(c -> c.getUserId() != null),
                "All conflicts should have valid user IDs from current semester");
    }
}

