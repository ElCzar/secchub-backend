package co.edu.puj.secchub_backend.integration.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.util.Arrays;
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
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.testcontainers.junit.jupiter.Testcontainers;

import co.edu.puj.secchub_backend.DatabaseContainerIntegration;
import co.edu.puj.secchub_backend.R2dbcTestUtils;
import co.edu.puj.secchub_backend.integration.dto.AcademicRequestBatchRequestDTO;
import co.edu.puj.secchub_backend.integration.dto.AcademicRequestRequestDTO;
import co.edu.puj.secchub_backend.integration.dto.AcademicRequestResponseDTO;
import co.edu.puj.secchub_backend.integration.dto.RequestScheduleRequestDTO;
import co.edu.puj.secchub_backend.integration.dto.RequestScheduleResponseDTO;
import co.edu.puj.secchub_backend.security.jwt.JwtTokenProvider;
import io.r2dbc.spi.ConnectionFactory;

@SpringBootTest
@AutoConfigureWebTestClient
@Testcontainers
@DisplayName("Academic Request Controller Integration Tests")
class AcademicRequestControllerIntegrationTest extends DatabaseContainerIntegration {
    
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
                "/test-academic-requests.sql"
        );
    }

    // ==========================================
    // Test Data Providers
    // ==========================================
    
    /**
     * Provides program role for creating academic requests
     */
    private static Stream<Arguments> programRoleProvider() {
        return Stream.of(
            Arguments.of("testProgram@example.com", "ROLE_PROGRAM")
        );
    }

    /**
     * Provides admin and user roles for viewing/managing requests
     */
    private static Stream<Arguments> adminAndUserRolesProvider() {
        return Stream.of(
            Arguments.of("testAdmin@example.com", "ROLE_ADMIN"),
            Arguments.of("testUser@example.com", "ROLE_USER")
        );
    }

    /**
     * Provides roles with access to view requests
     */
    private static Stream<Arguments> viewAccessRolesProvider() {
        return Stream.of(
            Arguments.of("testAdmin@example.com", "ROLE_ADMIN"),
            Arguments.of("testUser@example.com", "ROLE_USER"),
            Arguments.of("testProgram@example.com", "ROLE_PROGRAM")
        );
    }

    /**
     * Provides non-program roles that cannot create requests
     */
    private static Stream<Arguments> nonProgramRolesProvider() {
        return Stream.of(
            Arguments.of("testAdmin@example.com", "ROLE_ADMIN"),
            Arguments.of("testUser@example.com", "ROLE_USER"),
            Arguments.of("testStudent@example.com", "ROLE_STUDENT"),
            Arguments.of("testTeacher@example.com", "ROLE_TEACHER")
        );
    }

    // ==========================================
    // POST Create Academic Request Batch Tests
    // ==========================================
    
    @ParameterizedTest(name = "POST /academic-requests as {1} should create batch")
    @MethodSource("programRoleProvider")
    @DisplayName("POST /academic-requests as program should create batch of requests")
    void createAcademicRequestBatch_asProgram_createsSuccessfully(String email, String role) {
        String token = jwtTokenProvider.generateToken(email, role);

        // Create schedules for the request
        RequestScheduleRequestDTO schedule1 = RequestScheduleRequestDTO.builder()
                .classRoomTypeId(1L)
                .startTime("08:00:00")
                .endTime("10:00:00")
                .day("Monday")
                .modalityId(1L)
                .disability(false)
                .build();

        RequestScheduleRequestDTO schedule2 = RequestScheduleRequestDTO.builder()
                .classRoomTypeId(1L)
                .startTime("08:00:00")
                .endTime("10:00:00")
                .day("Wednesday")
                .modalityId(1L)
                .disability(false)
                .build();

        // Create academic request
        AcademicRequestRequestDTO request = AcademicRequestRequestDTO.builder()
                .courseId(5L)  // Software Engineering
                .startDate(LocalDate.of(2025, 8, 1))
                .endDate(LocalDate.of(2025, 12, 15))
                .capacity(30)
                .observation("Test batch request")
                .schedules(Arrays.asList(schedule1, schedule2))
                .build();

        // Create batch
        AcademicRequestBatchRequestDTO batchRequest = AcademicRequestBatchRequestDTO.builder()
                .requests(Arrays.asList(request))
                .build();

        List<AcademicRequestResponseDTO> responses = webTestClient.post()
                .uri("/academic-requests")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(batchRequest)
                .exchange()
                .expectStatus().isCreated()
                .expectBodyList(AcademicRequestResponseDTO.class)
                .returnResult()
                .getResponseBody();

        assertNotNull(responses, "Response list should not be null");
        assertFalse(responses.isEmpty(), "Should have created at least one request");
        
        AcademicRequestResponseDTO response = responses.get(0);
        assertNotNull(response.getId(), "Request ID should not be null");
        assertEquals(5L, response.getCourseId(), "Course ID should match");
        assertEquals(30, response.getCapacity(), "Capacity should match");
        assertFalse(response.getAccepted(), "Should not be accepted initially");
        assertFalse(response.getCombined(), "Should not be combined initially");
        assertNotNull(response.getSchedules(), "Schedules should not be null");
        assertEquals(2, response.getSchedules().size(), "Should have 2 schedules");

        // Verify in database
        Long count = databaseClient.sql("SELECT COUNT(*) FROM academic_request WHERE course_id = :courseId")
                .bind("courseId", 5L)
                .map(row -> row.get(0, Long.class))
                .one()
                .block();
        assertTrue(count >= 1, "Request should exist in database");
    }

    @ParameterizedTest(name = "POST /academic-requests as {1} should return 403")
    @MethodSource("nonProgramRolesProvider")
    @DisplayName("POST /academic-requests as non-program should return 403")
    void createAcademicRequestBatch_asNonProgram_returns403(String email, String role) {
        String token = jwtTokenProvider.generateToken(email, role);

        AcademicRequestRequestDTO request = AcademicRequestRequestDTO.builder()
                .courseId(5L)
                .startDate(LocalDate.of(2025, 8, 1))
                .endDate(LocalDate.of(2025, 12, 15))
                .capacity(30)
                .build();

        AcademicRequestBatchRequestDTO batchRequest = AcademicRequestBatchRequestDTO.builder()
                .requests(Arrays.asList(request))
                .build();

        webTestClient.post()
                .uri("/academic-requests")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(batchRequest)
                .exchange()
                .expectStatus().isForbidden();
    }

    @Test
    @DisplayName("POST /academic-requests unauthenticated should return 401")
    void createAcademicRequestBatch_unauthenticated_returns401() {
        AcademicRequestRequestDTO request = AcademicRequestRequestDTO.builder()
                .courseId(5L)
                .startDate(LocalDate.of(2025, 8, 1))
                .endDate(LocalDate.of(2025, 12, 15))
                .capacity(30)
                .build();

        AcademicRequestBatchRequestDTO batchRequest = AcademicRequestBatchRequestDTO.builder()
                .requests(Arrays.asList(request))
                .build();

        webTestClient.post()
                .uri("/academic-requests")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(batchRequest)
                .exchange()
                .expectStatus().isUnauthorized();
    }

    // ==========================================
    // GET All Academic Requests Tests
    // ==========================================
    
    @ParameterizedTest(name = "GET /academic-requests as {1} should return list")
    @MethodSource("adminAndUserRolesProvider")
    @DisplayName("GET /academic-requests as admin/user should return all requests")
    void getAllAcademicRequests_asAdminOrUser_returnsList(String email, String role) {
        String token = jwtTokenProvider.generateToken(email, role);

        List<AcademicRequestResponseDTO> requests = webTestClient.get()
                .uri("/academic-requests")
                .header("Authorization", "Bearer " + token)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(AcademicRequestResponseDTO.class)
                .returnResult()
                .getResponseBody();

        assertNotNull(requests, "Requests list should not be null");
        assertFalse(requests.isEmpty(), "Should have academic requests");
        
        // Verify at least one request from test data
        assertTrue(requests.stream()
                .anyMatch(r -> r.getCourseId().equals(1L)),
                "Should contain Data Structures request");
    }

    @Test
    @DisplayName("GET /academic-requests unauthenticated should return 401")
    void getAllAcademicRequests_unauthenticated_returns401() {
        webTestClient.get()
                .uri("/academic-requests")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isUnauthorized();
    }

    // ==========================================
    // GET Academic Request by ID Tests
    // ==========================================
    
    @ParameterizedTest(name = "GET /academic-requests/:id as {1} should return request")
    @MethodSource("adminAndUserRolesProvider")
    @DisplayName("GET /academic-requests/:id should return specific request")
    void getAcademicRequestById_asAdminOrUser_returnsRequest(String email, String role) {
        String token = jwtTokenProvider.generateToken(email, role);

        AcademicRequestResponseDTO request = webTestClient.get()
                .uri("/academic-requests/1")
                .header("Authorization", "Bearer " + token)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody(AcademicRequestResponseDTO.class)
                .returnResult()
                .getResponseBody();

        assertNotNull(request, "Request should not be null");
        assertEquals(1L, request.getId(), "Request ID should be 1");
        assertEquals(1L, request.getCourseId(), "Course ID should be 1 (Data Structures)");
        assertEquals(2L, request.getSemesterId(), "Semester ID should be 2 (current)");
        assertEquals(35, request.getCapacity(), "Capacity should be 35");
        assertFalse(request.getAccepted(), "Should not be accepted");
    }

    @Test
    @DisplayName("GET /academic-requests/:id with non-existent ID should return 404")
    void getAcademicRequestById_nonExistent_returns404() {
        String token = jwtTokenProvider.generateToken("testAdmin@example.com", "ROLE_ADMIN");

        webTestClient.get()
                .uri("/academic-requests/9999")
                .header("Authorization", "Bearer " + token)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isNotFound();
    }

    // ==========================================
    // GET Current Semester Requests Tests
    // ==========================================
    
    @ParameterizedTest(name = "GET /academic-requests/current-semester as {1} should return current requests")
    @MethodSource("adminAndUserRolesProvider")
    @DisplayName("GET /academic-requests/current-semester should return only current semester requests")
    void getCurrentSemesterAcademicRequests_asAdminOrUser_returnsCurrentSemesterRequests(String email, String role) {
        String token = jwtTokenProvider.generateToken(email, role);

        List<AcademicRequestResponseDTO> requests = webTestClient.get()
                .uri("/academic-requests/current-semester")
                .header("Authorization", "Bearer " + token)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(AcademicRequestResponseDTO.class)
                .returnResult()
                .getResponseBody();

        assertNotNull(requests, "Requests list should not be null");
        assertFalse(requests.isEmpty(), "Should have current semester requests");
        
        // Verify all requests are for current semester (id = 2)
        assertTrue(requests.stream()
                .allMatch(r -> r.getSemesterId().equals(2L)),
                "All requests should be for current semester");
    }

    // ==========================================
    // GET Requests by Semester Tests
    // ==========================================
    
    @ParameterizedTest(name = "GET /academic-requests/by-semester as {1} should return filtered requests")
    @MethodSource("viewAccessRolesProvider")
    @DisplayName("GET /academic-requests/by-semester should filter by semester")
    void getAcademicRequestsBySemester_withSemesterId_returnsFilteredRequests(String email, String role) {
        String token = jwtTokenProvider.generateToken(email, role);

        List<AcademicRequestResponseDTO> requests = webTestClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/academic-requests/by-semester")
                        .queryParam("semesterId", 1)
                        .build())
                .header("Authorization", "Bearer " + token)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(AcademicRequestResponseDTO.class)
                .returnResult()
                .getResponseBody();

        assertNotNull(requests, "Requests list should not be null");
        
        // Verify all requests are for specified semester
        assertTrue(requests.stream()
                .allMatch(r -> r.getSemesterId().equals(1L)),
                "All requests should be for semester 1");
    }

    // ==========================================
    // PUT Update Academic Request Tests
    // ==========================================
    
    @ParameterizedTest(name = "PUT /academic-requests/:id as {1} should update request")
    @MethodSource("adminAndUserRolesProvider")
    @DisplayName("PUT /academic-requests/:id should update request details")
    void updateAcademicRequest_asAdminOrUser_updatesSuccessfully(String email, String role) {
        String token = jwtTokenProvider.generateToken(email, role);

        AcademicRequestRequestDTO updateDTO = AcademicRequestRequestDTO.builder()
                .courseId(1L)
                .startDate(LocalDate.of(2025, 8, 1))
                .endDate(LocalDate.of(2025, 12, 15))
                .capacity(40)  // Changed from 35 to 40
                .observation("Updated observation")
                .build();

        AcademicRequestResponseDTO updated = webTestClient.put()
                .uri("/academic-requests/1")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(updateDTO)
                .exchange()
                .expectStatus().isOk()
                .expectBody(AcademicRequestResponseDTO.class)
                .returnResult()
                .getResponseBody();

        assertNotNull(updated, "Updated request should not be null");
        assertEquals(1L, updated.getId(), "Request ID should remain 1");
        assertEquals(40, updated.getCapacity(), "Capacity should be updated to 40");
        assertEquals("Updated observation", updated.getObservation(), "Observation should be updated");

        // Verify in database
        Integer dbCapacity = databaseClient.sql("SELECT capacity FROM academic_request WHERE id = :id")
                .bind("id", 1L)
                .map(row -> row.get("capacity", Integer.class))
                .one()
                .block();
        assertEquals(40, dbCapacity, "Capacity should be persisted in database");
    }

    @Test
    @DisplayName("PUT /academic-requests/:id with non-existent ID should return 404")
    void updateAcademicRequest_nonExistent_returns404() {
        String token = jwtTokenProvider.generateToken("testAdmin@example.com", "ROLE_ADMIN");

        AcademicRequestRequestDTO updateDTO = AcademicRequestRequestDTO.builder()
                .courseId(1L)
                .startDate(LocalDate.of(2025, 8, 1))
                .endDate(LocalDate.of(2025, 12, 15))
                .capacity(40)
                .build();

        webTestClient.put()
                .uri("/academic-requests/9999")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(updateDTO)
                .exchange()
                .expectStatus().isNotFound();
    }

    // ==========================================
    // DELETE Academic Request Tests
    // ==========================================
    
    @ParameterizedTest(name = "DELETE /academic-requests/:id as {1} should delete request")
    @MethodSource("adminAndUserRolesProvider")
    @DisplayName("DELETE /academic-requests/:id should delete request")
    void deleteAcademicRequest_asAdminOrUser_deletesSuccessfully(String email, String role) {
        String token = jwtTokenProvider.generateToken(email, role);

        webTestClient.delete()
                .uri("/academic-requests/4")  // Delete Operating Systems request
                .header("Authorization", "Bearer " + token)
                .exchange()
                .expectStatus().isNoContent();

        // Verify deletion in database
        Long count = databaseClient.sql("SELECT COUNT(*) FROM academic_request WHERE id = :id")
                .bind("id", 4L)
                .map(row -> row.get(0, Long.class))
                .one()
                .block();
        assertEquals(0L, count, "Request should be deleted from database");
    }

    @Test
    @DisplayName("DELETE /academic-requests/:id with non-existent ID should return 404")
    void deleteAcademicRequest_nonExistent_returns404() {
        String token = jwtTokenProvider.generateToken("testAdmin@example.com", "ROLE_ADMIN");

        webTestClient.delete()
                .uri("/academic-requests/9999")
                .header("Authorization", "Bearer " + token)
                .exchange()
                .expectStatus().isNotFound();
    }

    // ==========================================
    // Request Schedules Tests
    // ==========================================
    
    @ParameterizedTest(name = "POST /academic-requests/:id/schedules as {1} should add schedule")
    @MethodSource("adminAndUserRolesProvider")
    @DisplayName("POST /academic-requests/:id/schedules should add new schedule")
    void addRequestSchedule_asAdminOrUser_createsSuccessfully(String email, String role) {
        String token = jwtTokenProvider.generateToken(email, role);

        RequestScheduleRequestDTO scheduleDTO = RequestScheduleRequestDTO.builder()
                .classRoomTypeId(1L)
                .startTime("14:00:00")
                .endTime("16:00:00")
                .day("Friday")
                .modalityId(1L)
                .disability(false)
                .build();

        RequestScheduleResponseDTO response = webTestClient.post()
                .uri("/academic-requests/1/schedules")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(scheduleDTO)
                .exchange()
                .expectStatus().isCreated()
                .expectBody(RequestScheduleResponseDTO.class)
                .returnResult()
                .getResponseBody();

        assertNotNull(response, "Response should not be null");
        assertNotNull(response.getId(), "Schedule ID should not be null");
        assertEquals("Friday", response.getDay(), "Day should match");
        assertEquals("14:00", response.getStartTime(), "Start time should match");
        assertEquals("16:00", response.getEndTime(), "End time should match");
        assertEquals(1L, response.getClassRoomTypeId(), "Classroom type ID should match");
        assertEquals(1L, response.getModalityId(), "Modality ID should match");
        assertFalse(response.getDisability(), "Disability should be false");
    }

    @ParameterizedTest(name = "GET /academic-requests/:id/schedules as {1} should return schedules")
    @MethodSource("adminAndUserRolesProvider")
    @DisplayName("GET /academic-requests/:id/schedules should return request schedules")
    void getRequestSchedules_asAdminOrUser_returnsSchedules(String email, String role) {
        String token = jwtTokenProvider.generateToken(email, role);

        List<RequestScheduleResponseDTO> schedules = webTestClient.get()
                .uri("/academic-requests/1/schedules")
                .header("Authorization", "Bearer " + token)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(RequestScheduleResponseDTO.class)
                .returnResult()
                .getResponseBody();

        assertNotNull(schedules, "Schedules list should not be null");
        assertFalse(schedules.isEmpty(), "Should have schedules");
        assertEquals(2, schedules.size(), "Request 1 should have 2 schedules");
    }

    @ParameterizedTest(name = "PUT /academic-requests/:id/schedules/:scheduleId as {1} should update schedule")
    @MethodSource("adminAndUserRolesProvider")
    @DisplayName("PUT /academic-requests/:id/schedules/:scheduleId should update schedule")
    void updateRequestSchedule_asAdminOrUser_updatesSuccessfully(String email, String role) {
        String token = jwtTokenProvider.generateToken(email, role);

        RequestScheduleRequestDTO updateDTO = RequestScheduleRequestDTO.builder()
                .classRoomTypeId(2L)  // Change to lab
                .startTime("09:00:00")  // Change time
                .endTime("11:00:00")
                .day("Monday")
                .modalityId(2L)  // Change to virtual
                .disability(true)  // Add disability accommodation
                .build();

        RequestScheduleResponseDTO updated = webTestClient.put()
                .uri("/academic-requests/1/schedules/1")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(updateDTO)
                .exchange()
                .expectStatus().isOk()
                .expectBody(RequestScheduleResponseDTO.class)
                .returnResult()
                .getResponseBody();

        assertNotNull(updated, "Updated schedule should not be null");
        assertEquals(2L, updated.getClassRoomTypeId(), "Classroom type should be updated");
        assertEquals("09:00", updated.getStartTime(), "Start time should be updated");
        assertTrue(updated.getDisability(), "Disability should be true");
    }

    @ParameterizedTest(name = "PATCH /academic-requests/:id/schedules/:scheduleId as {1} should partially update")
    @MethodSource("adminAndUserRolesProvider")
    @DisplayName("PATCH /academic-requests/:id/schedules/:scheduleId should partially update schedule")
    void patchRequestSchedule_asAdminOrUser_updatesPartially(String email, String role) {
        String token = jwtTokenProvider.generateToken(email, role);

        Map<String, Object> updates = new HashMap<>();
        updates.put("startTime", "10:00:00");
        updates.put("endTime", "12:00:00");

        RequestScheduleResponseDTO patched = webTestClient.patch()
                .uri("/academic-requests/1/schedules/1")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(updates)
                .exchange()
                .expectStatus().isOk()
                .expectBody(RequestScheduleResponseDTO.class)
                .returnResult()
                .getResponseBody();

        assertNotNull(patched, "Patched schedule should not be null");
        assertEquals("10:00", patched.getStartTime(), "Start time should be updated");
        assertEquals("12:00", patched.getEndTime(), "End time should be updated");
        assertEquals("Monday", patched.getDay(), "Day should be preserved");
    }

    @ParameterizedTest(name = "DELETE /academic-requests/:id/schedules/:scheduleId as {1} should delete schedule")
    @MethodSource("adminAndUserRolesProvider")
    @DisplayName("DELETE /academic-requests/:id/schedules/:scheduleId should delete schedule")
    void deleteRequestSchedule_asAdminOrUser_deletesSuccessfully(String email, String role) {
        String token = jwtTokenProvider.generateToken(email, role);

        webTestClient.delete()
                .uri("/academic-requests/1/schedules/2")
                .header("Authorization", "Bearer " + token)
                .exchange()
                .expectStatus().isNoContent();

        // Verify deletion
        Long count = databaseClient.sql("SELECT COUNT(*) FROM request_schedule WHERE id = :id")
                .bind("id", 2L)
                .map(row -> row.get(0, Long.class))
                .one()
                .block();
        assertEquals(0L, count, "Schedule should be deleted from database");
    }

    // ==========================================
    // Mark as Accepted/Combined Tests
    // ==========================================
    
    @ParameterizedTest(name = "PATCH /academic-requests/:id/accept as {1} should mark as accepted")
    @MethodSource("adminAndUserRolesProvider")
    @DisplayName("PATCH /academic-requests/:id/accept should mark request as accepted")
    void markAsAccepted_asAdminOrUser_marksSuccessfully(String email, String role) {
        String token = jwtTokenProvider.generateToken(email, role);

        webTestClient.patch()
                .uri("/academic-requests/1/accept")
                .header("Authorization", "Bearer " + token)
                .exchange()
                .expectStatus().isOk();

        // Verify in database
        Boolean accepted = databaseClient.sql("SELECT accepted FROM academic_request WHERE id = :id")
                .bind("id", 1L)
                .map(row -> row.get("accepted", Boolean.class))
                .one()
                .block();
        assertTrue(accepted, "Request should be marked as accepted");
    }

    @ParameterizedTest(name = "PATCH /academic-requests/:id/combine as {1} should mark as combined")
    @MethodSource("adminAndUserRolesProvider")
    @DisplayName("PATCH /academic-requests/:id/combine should mark request as combined")
    void markAsCombined_asAdminOrUser_marksSuccessfully(String email, String role) {
        String token = jwtTokenProvider.generateToken(email, role);

        webTestClient.patch()
                .uri("/academic-requests/1/combine")
                .header("Authorization", "Bearer " + token)
                .exchange()
                .expectStatus().isOk();

        // Verify in database
        Boolean combined = databaseClient.sql("SELECT combined FROM academic_request WHERE id = :id")
                .bind("id", 1L)
                .map(row -> row.get("combined", Boolean.class))
                .one()
                .block();
        assertTrue(combined, "Request should be marked as combined");
    }

    @ParameterizedTest(name = "PATCH /academic-requests/accept-multiple as {1} should mark multiple as accepted")
    @MethodSource("adminAndUserRolesProvider")
    @DisplayName("PATCH /academic-requests/accept-multiple should mark multiple requests as accepted")
    void markMultipleAsAccepted_asAdminOrUser_marksSuccessfully(String email, String role) {
        String token = jwtTokenProvider.generateToken(email, role);

        Map<String, List<Long>> requestBody = new HashMap<>();
        requestBody.put("requestIds", Arrays.asList(1L, 4L));

        webTestClient.patch()
                .uri("/academic-requests/accept-multiple")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(requestBody)
                .exchange()
                .expectStatus().isOk();

        // Verify both marked as accepted
        Long acceptedCount = databaseClient.sql("SELECT COUNT(*) FROM academic_request WHERE id IN (1, 4) AND accepted = true")
                .map(row -> row.get(0, Long.class))
                .one()
                .block();
        assertEquals(2L, acceptedCount, "Both requests should be marked as accepted");
    }

    @ParameterizedTest(name = "PATCH /academic-requests/combine-multiple as {1} should mark multiple as combined")
    @MethodSource("adminAndUserRolesProvider")
    @DisplayName("PATCH /academic-requests/combine-multiple should mark multiple requests as combined")
    void markMultipleAsCombined_asAdminOrUser_marksSuccessfully(String email, String role) {
        String token = jwtTokenProvider.generateToken(email, role);

        Map<String, List<Long>> requestBody = new HashMap<>();
        requestBody.put("requestIds", Arrays.asList(1L, 4L));

        webTestClient.patch()
                .uri("/academic-requests/combine-multiple")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(requestBody)
                .exchange()
                .expectStatus().isOk();

        // Verify both marked as combined
        Long combinedCount = databaseClient.sql("SELECT COUNT(*) FROM academic_request WHERE id IN (1, 4) AND combined = true")
                .map(row -> row.get(0, Long.class))
                .one()
                .block();
        assertEquals(2L, combinedCount, "Both requests should be marked as combined");
    }

    // ==========================================
    // Business Logic Tests
    // ==========================================
    
    @Test
    @DisplayName("Creating request with schedules should create both request and schedules")
    void createRequest_withSchedules_createsBoth() {
        String token = jwtTokenProvider.generateToken("testProgram@example.com", "ROLE_PROGRAM");

        RequestScheduleRequestDTO schedule = RequestScheduleRequestDTO.builder()
                .classRoomTypeId(1L)
                .startTime("08:00:00")
                .endTime("10:00:00")
                .day("Tuesday")
                .modalityId(1L)
                .disability(false)
                .build();

        AcademicRequestRequestDTO request = AcademicRequestRequestDTO.builder()
                .courseId(7L)  // Machine Learning
                .startDate(LocalDate.of(2025, 8, 1))
                .endDate(LocalDate.of(2025, 12, 15))
                .capacity(25)
                .schedules(Arrays.asList(schedule))
                .build();

        AcademicRequestBatchRequestDTO batchRequest = AcademicRequestBatchRequestDTO.builder()
                .requests(Arrays.asList(request))
                .build();

        List<AcademicRequestResponseDTO> responses = webTestClient.post()
                .uri("/academic-requests")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(batchRequest)
                .exchange()
                .expectStatus().isCreated()
                .expectBodyList(AcademicRequestResponseDTO.class)
                .returnResult()
                .getResponseBody();

        assertNotNull(responses, "Responses should not be null");
        AcademicRequestResponseDTO response = responses.get(0);
        assertNotNull(response.getSchedules(), "Schedules should be created");
        assertFalse(response.getSchedules().isEmpty(), "Should have at least one schedule");
    }

    @Test
    @DisplayName("Filtering by current semester should only return current semester requests")
    void getCurrentSemesterRequests_shouldOnlyReturnCurrentSemester() {
        String token = jwtTokenProvider.generateToken("testAdmin@example.com", "ROLE_ADMIN");

        List<AcademicRequestResponseDTO> currentRequests = webTestClient.get()
                .uri("/academic-requests/current-semester")
                .header("Authorization", "Bearer " + token)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(AcademicRequestResponseDTO.class)
                .returnResult()
                .getResponseBody();

        assertNotNull(currentRequests, "Current requests should not be null");
        
        // Verify none are from previous semester
        assertFalse(currentRequests.stream()
                .anyMatch(r -> r.getSemesterId().equals(1L)),
                "Should not contain previous semester requests");
        
        // Verify all are from current semester
        assertTrue(currentRequests.stream()
                .allMatch(r -> r.getSemesterId().equals(2L)),
                "All should be from current semester");
    }

    @Test
    @DisplayName("Accepted and combined flags should be mutually exclusive in workflow")
    void acceptedAndCombined_shouldBeWorkflowExclusive() {
        String token = jwtTokenProvider.generateToken("testAdmin@example.com", "ROLE_ADMIN");

        // Request 2 is already accepted
        AcademicRequestResponseDTO request2 = webTestClient.get()
                .uri("/academic-requests/2")
                .header("Authorization", "Bearer " + token)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectBody(AcademicRequestResponseDTO.class)
                .returnResult()
                .getResponseBody();

        assertNotNull(request2, "Request 2 should not be null");
        assertTrue(request2.getAccepted(), "Request 2 should be accepted");
        assertFalse(request2.getCombined(), "Request 2 should not be combined");

        // Request 3 is combined
        AcademicRequestResponseDTO request3 = webTestClient.get()
                .uri("/academic-requests/3")
                .header("Authorization", "Bearer " + token)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectBody(AcademicRequestResponseDTO.class)
                .returnResult()
                .getResponseBody();

        assertNotNull(request3, "Request 3 should not be null");
        assertFalse(request3.getAccepted(), "Request 3 should not be accepted");
        assertTrue(request3.getCombined(), "Request 3 should be combined");
    }
}
