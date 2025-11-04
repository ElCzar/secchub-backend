package co.edu.puj.secchub_backend.planning.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.fail;

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
import co.edu.puj.secchub_backend.planning.dto.ClassroomRequestDTO;
import co.edu.puj.secchub_backend.planning.dto.ClassroomResponseDTO;
import co.edu.puj.secchub_backend.planning.model.Classroom;
import co.edu.puj.secchub_backend.planning.repository.ClassroomRepository;
import co.edu.puj.secchub_backend.security.jwt.JwtTokenProvider;

@SpringBootTest
@AutoConfigureWebTestClient
@Testcontainers
@DisplayName("Classroom Controller Integration Tests")
class ClassroomControllerIntegrationTest extends DatabaseContainerIntegration {
/**
    @Autowired
    private WebTestClient webTestClient;

    @Autowired
    private DatabaseClient databaseClient;

    @Autowired
    private ClassroomRepository classroomRepository;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    private SqlScriptExecutor sqlScriptExecutor;

    @BeforeEach
    void setUp() {
        sqlScriptExecutor = new SqlScriptExecutor(databaseClient);
        // Clean up any existing test data (preserves parametric data)
        sqlScriptExecutor.executeSqlScript("/test-cleanup.sql");
        // Load test data
        sqlScriptExecutor.executeSqlScript("/test-users.sql");
        sqlScriptExecutor.executeSqlScript("/test-classrooms.sql");
    }

    @AfterEach
    void tearDown() {
        // Clean up after each test
        sqlScriptExecutor.executeSqlScript("/test-cleanup.sql");
    }

    // ==========================================
    // GET All Classrooms Tests
    // ==========================================
    @ParameterizedTest(name = "GET /classrooms authorized as {0} should return all classrooms")
    @MethodSource("authorizedRolesProvider")
    @DisplayName("GET /classrooms authorized user should receive all classrooms list")
    void getAllClassrooms_asAuthorized_returnsList(String email, String role) {
        String token = jwtTokenProvider.generateToken(email, role);

        Long classroomCount = databaseClient.sql("SELECT COUNT(*) FROM classroom")
                .map(row -> row.get(0, Long.class))
                .one()
                .block();
        assertNotNull(classroomCount, "Classroom count from DB should not be null");

        List<ClassroomResponseDTO> list = webTestClient.get()
                .uri("/classrooms")
                .header("Authorization", "Bearer " + token)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(ClassroomResponseDTO.class)
                .returnResult()
                .getResponseBody();

        assertNotNull(list, "Response list should not be null");
        assertEquals(classroomCount.intValue(), list.size(), "Returned classroom list size should match DB count");
    }

    @ParameterizedTest(name = "GET /classrooms unauthorized as {0} should return 403 Forbidden")
    @MethodSource("unauthorizedRolesProvider")
    @DisplayName("GET /classrooms unauthorized should return 403 Forbidden")
    void getAllClassrooms_asUnauthorized_returns403(String email, String role) {
        String token = jwtTokenProvider.generateToken(email, role);

        webTestClient.get()
                .uri("/classrooms")
                .header("Authorization", "Bearer " + token)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isForbidden();
    }

    @Test
    @DisplayName("GET /classrooms with no token should return 401 Unauthorized")
    void getAllClassrooms_noToken_returns401() {
        webTestClient.get()
                .uri("/classrooms")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isUnauthorized();
    }

    // ==========================================
    // GET Classroom by ID Tests
    // ==========================================
    @ParameterizedTest(name = "GET /classrooms/:id authorized as {0} should return classroom by id")
    @MethodSource("authorizedRolesProvider")
    @DisplayName("GET /classrooms/:id authorized should fetch classroom by id")
    void getClassroomById_asAuthorized_returnsClassroom(String email, String role) {
        Long id = databaseClient.sql("SELECT id FROM classroom LIMIT 1")
                .map(row -> row.get("id", Long.class))
                .one()
                .block();
        assertNotNull(id, "Classroom id should exist in test data");

        String token = jwtTokenProvider.generateToken(email, role);

        ClassroomResponseDTO dto = webTestClient.get()
                .uri("/classrooms/{id}", id)
                .header("Authorization", "Bearer " + token)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody(ClassroomResponseDTO.class)
                .returnResult()
                .getResponseBody();

        assertNotNull(dto);
        assertEquals(id, dto.getId());

        // Verify classroom data matches database
        Classroom classroom;
        try {
            classroom = classroomRepository.findById(id).block();
            assertNotNull(classroom, "Classroom should exist in the database");
        } catch (Exception e) {
            fail("Classroom with id " + id + " should exist in the database");
            return;
        }

        assertEquals(classroom.getClassroomTypeId(), dto.getClassroomTypeId(), "Classroom type ID should match");
        assertEquals(classroom.getCampus(), dto.getCampus(), "Campus should match");
        assertEquals(classroom.getLocation(), dto.getLocation(), "Location should match");
        assertEquals(classroom.getRoom(), dto.getRoom(), "Room should match");
        assertEquals(classroom.getCapacity(), dto.getCapacity(), "Capacity should match");
    }

    @Test
    @DisplayName("GET /classrooms/:id with non-existing id should return 404 Not Found")
    void getClassroomById_nonExistingId_returns404() {
        String adminToken = jwtTokenProvider.generateToken("testAdmin@example.com", "ROLE_ADMIN");

        webTestClient.get()
                .uri("/classrooms/{id}", Long.MAX_VALUE)
                .header("Authorization", "Bearer " + adminToken)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isNotFound();
    }

    @ParameterizedTest(name = "GET /classrooms/:id unauthorized as {0} should return 403 Forbidden")
    @MethodSource("unauthorizedRolesProvider")
    @DisplayName("GET /classrooms/:id unauthorized should return 403 Forbidden")
    void getClassroomById_asUnauthorized_returns403(String email, String role) {
        Long id = databaseClient.sql("SELECT id FROM classroom LIMIT 1")
                .map(row -> row.get("id", Long.class))
                .one()
                .block();
        assertNotNull(id, "Classroom id should exist in test data");

        String token = jwtTokenProvider.generateToken(email, role);

        webTestClient.get()
                .uri("/classrooms/{id}", id)
                .header("Authorization", "Bearer " + token)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isForbidden();
    }

    @Test
    @DisplayName("GET /classrooms/:id with no token should return 401 Unauthorized")
    void getClassroomById_noToken_returns401() {
        Long id = 1L;

        webTestClient.get()
                .uri("/classrooms/{id}", id)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isUnauthorized();
    }

    // ==========================================
    // POST Create Classroom Tests
    // ==========================================
    @Test
    @DisplayName("POST /classrooms as admin should create new classroom")
    void createClassroom_asAdmin_createsClassroom() {
        String adminToken = jwtTokenProvider.generateToken("testAdmin@example.com", "ROLE_ADMIN");

        ClassroomRequestDTO request = ClassroomRequestDTO.builder()
                .classroomTypeId(1L)
                .campus("New Campus")
                .location("New Building")
                .room("N-100")
                .capacity(50)
                .build();

        ClassroomResponseDTO dto = webTestClient.post()
                .uri("/classrooms")
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isCreated()
                .expectBody(ClassroomResponseDTO.class)
                .returnResult()
                .getResponseBody();

        assertNotNull(dto);
        assertNotNull(dto.getId());
        assertEquals(request.getClassroomTypeId(), dto.getClassroomTypeId());
        assertEquals(request.getCampus(), dto.getCampus());
        assertEquals(request.getLocation(), dto.getLocation());
        assertEquals(request.getRoom(), dto.getRoom());
        assertEquals(request.getCapacity(), dto.getCapacity());

        // Verify in database
        Classroom saved = classroomRepository.findById(dto.getId()).block();
        assertNotNull(saved, "Classroom should be saved in database");
        assertEquals(request.getRoom(), saved.getRoom());
    }

    @ParameterizedTest(name = "POST /classrooms as {0} non-admin should return 403 Forbidden")
    @MethodSource("nonAdminRolesProvider")
    @DisplayName("POST /classrooms non-admin should return 403 Forbidden")
    void createClassroom_asNonAdmin_returns403(String email, String role) {
        String token = jwtTokenProvider.generateToken(email, role);

        ClassroomRequestDTO request = ClassroomRequestDTO.builder()
                .classroomTypeId(1L)
                .campus("Campus")
                .location("Location")
                .room("R-001")
                .capacity(30)
                .build();

        webTestClient.post()
                .uri("/classrooms")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isForbidden();
    }

    @Test
    @DisplayName("POST /classrooms with no token should return 401 Unauthorized")
    void createClassroom_noToken_returns401() {
        ClassroomRequestDTO request = ClassroomRequestDTO.builder()
                .classroomTypeId(1L)
                .campus("Campus")
                .location("Location")
                .room("R-001")
                .capacity(30)
                .build();

        webTestClient.post()
                .uri("/classrooms")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isUnauthorized();
    }

    // ==========================================
    // PUT Update Classroom Tests
    // ==========================================
    @Test
    @DisplayName("PUT /classrooms/:id as admin should update classroom")
    void updateClassroom_asAdmin_updatesClassroom() {
        Long id = databaseClient.sql("SELECT id FROM classroom LIMIT 1")
                .map(row -> row.get("id", Long.class))
                .one()
                .block();
        assertNotNull(id, "Classroom id should exist in test data");

        String adminToken = jwtTokenProvider.generateToken("testAdmin@example.com", "ROLE_ADMIN");

        ClassroomRequestDTO request = ClassroomRequestDTO.builder()
                .classroomTypeId(2L)
                .campus("Updated Campus")
                .location("Updated Building")
                .room("U-999")
                .capacity(75)
                .build();

        ClassroomResponseDTO dto = webTestClient.put()
                .uri("/classrooms/{id}", id)
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isOk()
                .expectBody(ClassroomResponseDTO.class)
                .returnResult()
                .getResponseBody();

        assertNotNull(dto);
        assertEquals(id, dto.getId());
        assertEquals(request.getClassroomTypeId(), dto.getClassroomTypeId());
        assertEquals(request.getCampus(), dto.getCampus());
        assertEquals(request.getLocation(), dto.getLocation());
        assertEquals(request.getRoom(), dto.getRoom());
        assertEquals(request.getCapacity(), dto.getCapacity());

        // Verify in database
        Classroom updated = classroomRepository.findById(id).block();
        assertNotNull(updated, "Classroom should exist in database");
        assertEquals(request.getRoom(), updated.getRoom());
        assertEquals(request.getCapacity(), updated.getCapacity());
    }

    @Test
    @DisplayName("PUT /classrooms/:id with non-existing id should return 404 Not Found")
    void updateClassroom_nonExistingId_returns404() {
        String adminToken = jwtTokenProvider.generateToken("testAdmin@example.com", "ROLE_ADMIN");

        ClassroomRequestDTO request = ClassroomRequestDTO.builder()
                .classroomTypeId(1L)
                .campus("Campus")
                .location("Location")
                .room("R-001")
                .capacity(30)
                .build();

        webTestClient.put()
                .uri("/classrooms/{id}", Long.MAX_VALUE)
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isNotFound();
    }

    @ParameterizedTest(name = "PUT /classrooms/:id as {0} non-admin should return 403 Forbidden")
    @MethodSource("nonAdminRolesProvider")
    @DisplayName("PUT /classrooms/:id non-admin should return 403 Forbidden")
    void updateClassroom_asNonAdmin_returns403(String email, String role) {
        Long id = databaseClient.sql("SELECT id FROM classroom LIMIT 1")
                .map(row -> row.get("id", Long.class))
                .one()
                .block();
        assertNotNull(id, "Classroom id should exist in test data");

        String token = jwtTokenProvider.generateToken(email, role);

        ClassroomRequestDTO request = ClassroomRequestDTO.builder()
                .classroomTypeId(1L)
                .campus("Campus")
                .location("Location")
                .room("R-001")
                .capacity(30)
                .build();

        webTestClient.put()
                .uri("/classrooms/{id}", id)
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isForbidden();
    }

    @Test
    @DisplayName("PUT /classrooms/:id with no token should return 401 Unauthorized")
    void updateClassroom_noToken_returns401() {
        Long id = 1L;

        ClassroomRequestDTO request = ClassroomRequestDTO.builder()
                .classroomTypeId(1L)
                .campus("Campus")
                .location("Location")
                .room("R-001")
                .capacity(30)
                .build();

        webTestClient.put()
                .uri("/classrooms/{id}", id)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isUnauthorized();
    }

    // ==========================================
    // DELETE Classroom Tests
    // ==========================================
    @Test
    @DisplayName("DELETE /classrooms/:id as admin should delete classroom")
    void deleteClassroom_asAdmin_deletesClassroom() {
        Long id = databaseClient.sql("SELECT id FROM classroom LIMIT 1")
                .map(row -> row.get("id", Long.class))
                .one()
                .block();
        assertNotNull(id, "Classroom id should exist in test data");

        String adminToken = jwtTokenProvider.generateToken("testAdmin@example.com", "ROLE_ADMIN");

        webTestClient.delete()
                .uri("/classrooms/{id}", id)
                .header("Authorization", "Bearer " + adminToken)
                .exchange()
                .expectStatus().isNoContent();

        // Verify deletion in database
        boolean exists = classroomRepository.existsById(id).block();
        assertEquals(false, exists, "Classroom should be deleted from database");
    }

    @Test
    @DisplayName("DELETE /classrooms/:id with non-existing id should return 404 Not Found")
    void deleteClassroom_nonExistingId_returns404() {
        String adminToken = jwtTokenProvider.generateToken("testAdmin@example.com", "ROLE_ADMIN");

        webTestClient.delete()
                .uri("/classrooms/{id}", Long.MAX_VALUE)
                .header("Authorization", "Bearer " + adminToken)
                .exchange()
                .expectStatus().isNotFound();
    }

    @ParameterizedTest(name = "DELETE /classrooms/:id as {0} non-admin should return 403 Forbidden")
    @MethodSource("nonAdminRolesProvider")
    @DisplayName("DELETE /classrooms/:id non-admin should return 403 Forbidden")
    void deleteClassroom_asNonAdmin_returns403(String email, String role) {
        Long id = databaseClient.sql("SELECT id FROM classroom LIMIT 1")
                .map(row -> row.get("id", Long.class))
                .one()
                .block();
        assertNotNull(id, "Classroom id should exist in test data");

        String token = jwtTokenProvider.generateToken(email, role);

        webTestClient.delete()
                .uri("/classrooms/{id}", id)
                .header("Authorization", "Bearer " + token)
                .exchange()
                .expectStatus().isForbidden();
    }

    @Test
    @DisplayName("DELETE /classrooms/:id with no token should return 401 Unauthorized")
    void deleteClassroom_noToken_returns401() {
        Long id = 1L;

        webTestClient.delete()
                .uri("/classrooms/{id}", id)
                .exchange()
                .expectStatus().isUnauthorized();
    }

    // ==========================================
    // GET Classrooms by Type Tests
    // ==========================================
    @ParameterizedTest(name = "GET /classrooms/type/:typeId authorized as {0} should return classrooms by type")
    @MethodSource("authorizedRolesProvider")
    @DisplayName("GET /classrooms/type/:typeId authorized should fetch classrooms by type")
    void getClassroomsByType_asAuthorized_returnsClassrooms(String email, String role) {
        Long typeId = databaseClient.sql("SELECT classroom_type_id FROM classroom LIMIT 1")
                .map(row -> row.get("classroom_type_id", Long.class))
                .one()
                .block();
        assertNotNull(typeId, "Classroom type id should exist in test data");

        String token = jwtTokenProvider.generateToken(email, role);

        Long expectedCount = databaseClient.sql("SELECT COUNT(*) FROM classroom WHERE classroom_type_id = :typeId")
                .bind("typeId", typeId)
                .map(row -> row.get(0, Long.class))
                .one()
                .block();
        assertNotNull(expectedCount, "Classroom count for type should not be null");

        List<ClassroomResponseDTO> list = webTestClient.get()
                .uri("/classrooms/type/{typeId}", typeId)
                .header("Authorization", "Bearer " + token)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(ClassroomResponseDTO.class)
                .returnResult()
                .getResponseBody();

        assertNotNull(list, "Response list should not be null");
        assertEquals(expectedCount.intValue(), list.size(), "Returned classroom count should match DB count");

        // Verify all classrooms have the correct type
        list.forEach(classroom -> 
                assertEquals(typeId, classroom.getClassroomTypeId(), "All classrooms should have the requested type"));
    }

    @ParameterizedTest(name = "GET /classrooms/type/:typeId unauthorized as {0} should return 403 Forbidden")
    @MethodSource("unauthorizedRolesProvider")
    @DisplayName("GET /classrooms/type/:typeId unauthorized should return 403 Forbidden")
    void getClassroomsByType_asUnauthorized_returns403(String email, String role) {
        Long typeId = 1L;
        String token = jwtTokenProvider.generateToken(email, role);

        webTestClient.get()
                .uri("/classrooms/type/{typeId}", typeId)
                .header("Authorization", "Bearer " + token)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isForbidden();
    }

    @Test
    @DisplayName("GET /classrooms/type/:typeId with no token should return 401 Unauthorized")
    void getClassroomsByType_noToken_returns401() {
        Long typeId = 1L;

        webTestClient.get()
                .uri("/classrooms/type/{typeId}", typeId)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isUnauthorized();
    }

    // ==========================================
    // Method Sources for Parameterized Tests
    // ==========================================
    private static Stream<Arguments> authorizedRolesProvider() {
        return Stream.of(
                Arguments.of("testAdmin@example.com", "ROLE_ADMIN"),
                Arguments.of("testUser@example.com", "ROLE_USER")
        );
    }

    private static Stream<Arguments> unauthorizedRolesProvider() {
        return Stream.of(
                Arguments.of("testTeacher@example.com", "ROLE_TEACHER"),
                Arguments.of("testProgram@example.com", "ROLE_PROGRAM"),
                Arguments.of("testStudent@example.com", "ROLE_STUDENT")
        );
    }

    private static Stream<Arguments> nonAdminRolesProvider() {
        return Stream.of(
                Arguments.of("testUser@example.com", "ROLE_USER"),
                Arguments.of("testTeacher@example.com", "ROLE_TEACHER"),
                Arguments.of("testProgram@example.com", "ROLE_PROGRAM"),
                Arguments.of("testStudent@example.com", "ROLE_STUDENT")
        );
    }
*/
}