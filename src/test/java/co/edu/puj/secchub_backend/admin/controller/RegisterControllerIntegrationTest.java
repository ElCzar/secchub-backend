package co.edu.puj.secchub_backend.admin.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

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
import co.edu.puj.secchub_backend.admin.contract.TeacherResponseDTO;
import co.edu.puj.secchub_backend.admin.dto.SectionRegisterRequestDTO;
import co.edu.puj.secchub_backend.admin.dto.SectionResponseDTO;
import co.edu.puj.secchub_backend.admin.dto.TeacherRegisterRequestDTO;
import co.edu.puj.secchub_backend.admin.dto.UserRegisterRequestDTO;
import co.edu.puj.secchub_backend.security.jwt.JwtTokenProvider;
import io.r2dbc.spi.ConnectionFactory;

@SpringBootTest
@AutoConfigureWebTestClient
@Testcontainers
@DisplayName("Register Controller Integration Tests")
class RegisterControllerIntegrationTest extends DatabaseContainerIntegration {
    
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
                "/test-users.sql"
        );
    }

    // ==========================================
    // Test Data Providers
    // ==========================================
    
    /**
     * Provides admin-only roles for registration operations
     */
    private static Stream<Arguments> adminRoleProvider() {
        return Stream.of(
            Arguments.of("testAdmin@example.com", "ROLE_ADMIN")
        );
    }

    /**
     * Provides non-admin roles that should be forbidden from registration
     */
    private static Stream<Arguments> nonAdminRolesProvider() {
        return Stream.of(
            Arguments.of("testUser@example.com", "ROLE_USER"),
            Arguments.of("testStudent@example.com", "ROLE_STUDENT"),
            Arguments.of("testTeacher@example.com", "ROLE_TEACHER"),
            Arguments.of("testProgram@example.com", "ROLE_PROGRAM")
        );
    }

    // ==========================================
    // POST Register Student Tests
    // ==========================================
    
    @ParameterizedTest(name = "POST /admin/register/student as admin {1} should register student")
    @MethodSource("adminRoleProvider")
    @DisplayName("POST /admin/register/student as admin should create new student")
    void registerStudent_asAdmin_createsSuccessfully(String email, String role) {
        String token = jwtTokenProvider.generateToken(email, role);

        UserRegisterRequestDTO requestDTO = UserRegisterRequestDTO.builder()
                .username("newstudent")
                .password("password123")
                .faculty("Engineering")
                .name("New")
                .lastName("Student")
                .email("newstudent@example.com")
                .documentTypeId("1")
                .documentNumber("1234567890")
                .build();

        Long userId = webTestClient.post()
                .uri("/admin/register/student")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(requestDTO)
                .exchange()
                .expectStatus().isCreated()
                .expectBody(Long.class)
                .returnResult()
                .getResponseBody();

        assertNotNull(userId, "User ID should not be null");

        // Verify user exists in database with correct role
        Long count = databaseClient.sql("SELECT COUNT(*) FROM users WHERE email = :email AND role_id = 3")
                .bind("email", "newstudent@example.com")
                .map(row -> row.get(0, Long.class))
                .one()
                .block();
        assertEquals(1L, count, "Student should exist in database with role_id=3 (STUDENT)");
    }

    @ParameterizedTest(name = "POST /admin/register/student as non-admin {1} should return 403")
    @MethodSource("nonAdminRolesProvider")
    @DisplayName("POST /admin/register/student as non-admin should return 403 Forbidden")
    void registerStudent_asNonAdmin_returns403(String email, String role) {
        String token = jwtTokenProvider.generateToken(email, role);

        UserRegisterRequestDTO requestDTO = UserRegisterRequestDTO.builder()
                .username("unauthorizedstudent")
                .password("password123")
                .faculty("Engineering")
                .name("Unauthorized")
                .lastName("Student")
                .email("unauthorized@example.com")
                .documentTypeId("1")
                .documentNumber("9999999999")
                .build();

        webTestClient.post()
                .uri("/admin/register/student")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(requestDTO)
                .exchange()
                .expectStatus().isForbidden();

        // Verify user was not created
        Long count = databaseClient.sql("SELECT COUNT(*) FROM users WHERE email = :email")
                .bind("email", "unauthorized@example.com")
                .map(row -> row.get(0, Long.class))
                .one()
                .block();
        assertEquals(0L, count, "Unauthorized user should not be created");
    }

    @Test
    @DisplayName("POST /admin/register/student unauthenticated should return 401 Unauthorized")
    void registerStudent_unauthenticated_returns401() {
        UserRegisterRequestDTO requestDTO = UserRegisterRequestDTO.builder()
                .username("unauthstudent")
                .password("password123")
                .faculty("Engineering")
                .name("Unauth")
                .lastName("Student")
                .email("unauth@example.com")
                .documentTypeId("1")
                .documentNumber("0000000000")
                .build();

        webTestClient.post()
                .uri("/admin/register/student")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(requestDTO)
                .exchange()
                .expectStatus().isUnauthorized();
    }

    // ==========================================
    // POST Register Admin Tests
    // ==========================================
    
    @ParameterizedTest(name = "POST /admin/register/admin as admin {1} should register admin")
    @MethodSource("adminRoleProvider")
    @DisplayName("POST /admin/register/admin as admin should create new admin")
    void registerAdmin_asAdmin_createsSuccessfully(String email, String role) {
        String token = jwtTokenProvider.generateToken(email, role);

        UserRegisterRequestDTO requestDTO = UserRegisterRequestDTO.builder()
                .username("newadmin")
                .password("password123")
                .faculty("Administration")
                .name("New")
                .lastName("Admin")
                .email("newadmin@example.com")
                .documentTypeId("1")
                .documentNumber("1111111111")
                .build();

        Long userId = webTestClient.post()
                .uri("/admin/register/admin")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(requestDTO)
                .exchange()
                .expectStatus().isCreated()
                .expectBody(Long.class)
                .returnResult()
                .getResponseBody();

        assertNotNull(userId, "User ID should not be null");

        // Verify admin exists in database with correct role
        Long count = databaseClient.sql("SELECT COUNT(*) FROM users WHERE email = :email AND role_id = 1")
                .bind("email", "newadmin@example.com")
                .map(row -> row.get(0, Long.class))
                .one()
                .block();
        assertEquals(1L, count, "Admin should exist in database with role_id=1 (ADMIN)");
    }

    @ParameterizedTest(name = "POST /admin/register/admin as non-admin {1} should return 403")
    @MethodSource("nonAdminRolesProvider")
    @DisplayName("POST /admin/register/admin as non-admin should return 403 Forbidden")
    void registerAdmin_asNonAdmin_returns403(String email, String role) {
        String token = jwtTokenProvider.generateToken(email, role);

        UserRegisterRequestDTO requestDTO = UserRegisterRequestDTO.builder()
                .username("unauthorizedadmin")
                .password("password123")
                .faculty("Administration")
                .name("Unauthorized")
                .lastName("Admin")
                .email("unadmin@example.com")
                .documentTypeId("1")
                .documentNumber("2222222222")
                .build();

        webTestClient.post()
                .uri("/admin/register/admin")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(requestDTO)
                .exchange()
                .expectStatus().isForbidden();
    }

    // ==========================================
    // POST Register Program Tests
    // ==========================================
    
    @ParameterizedTest(name = "POST /admin/register/program as admin {1} should register program")
    @MethodSource("adminRoleProvider")
    @DisplayName("POST /admin/register/program as admin should create new program coordinator")
    void registerProgram_asAdmin_createsSuccessfully(String email, String role) {
        String token = jwtTokenProvider.generateToken(email, role);

        UserRegisterRequestDTO requestDTO = UserRegisterRequestDTO.builder()
                .username("newprogram")
                .password("password123")
                .faculty("Arts")
                .name("New")
                .lastName("Program")
                .email("newprogram@example.com")
                .documentTypeId("1")
                .documentNumber("3333333333")
                .build();

        Long userId = webTestClient.post()
                .uri("/admin/register/program")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(requestDTO)
                .exchange()
                .expectStatus().isCreated()
                .expectBody(Long.class)
                .returnResult()
                .getResponseBody();

        assertNotNull(userId, "User ID should not be null");

        // Verify program coordinator exists in database with correct role
        Long count = databaseClient.sql("SELECT COUNT(*) FROM users WHERE email = :email AND role_id = 5")
                .bind("email", "newprogram@example.com")
                .map(row -> row.get(0, Long.class))
                .one()
                .block();
        assertEquals(1L, count, "Program coordinator should exist in database with role_id=5 (PROGRAM)");
    }

    @ParameterizedTest(name = "POST /admin/register/program as non-admin {1} should return 403")
    @MethodSource("nonAdminRolesProvider")
    @DisplayName("POST /admin/register/program as non-admin should return 403 Forbidden")
    void registerProgram_asNonAdmin_returns403(String email, String role) {
        String token = jwtTokenProvider.generateToken(email, role);

        UserRegisterRequestDTO requestDTO = UserRegisterRequestDTO.builder()
                .username("unauthorizedprogram")
                .password("password123")
                .faculty("Arts")
                .name("Unauthorized")
                .lastName("Program")
                .email("unprogram@example.com")
                .documentTypeId("1")
                .documentNumber("4444444444")
                .build();

        webTestClient.post()
                .uri("/admin/register/program")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(requestDTO)
                .exchange()
                .expectStatus().isForbidden();
    }

    // ==========================================
    // POST Register Teacher Tests
    // ==========================================
    
    @ParameterizedTest(name = "POST /admin/register/teacher as admin {1} should register teacher")
    @MethodSource("adminRoleProvider")
    @DisplayName("POST /admin/register/teacher as admin should create new teacher with profile")
    void registerTeacher_asAdmin_createsSuccessfully(String email, String role) {
        String token = jwtTokenProvider.generateToken(email, role);

        UserRegisterRequestDTO userDTO = UserRegisterRequestDTO.builder()
                .username("newteacher")
                .password("password123")
                .faculty("Engineering")
                .name("New")
                .lastName("Teacher")
                .email("newteacher@example.com")
                .documentTypeId("1")
                .documentNumber("5555555555")
                .build();

        TeacherRegisterRequestDTO requestDTO = TeacherRegisterRequestDTO.builder()
                .employmentTypeId(1L)
                .maxHours(20)
                .user(userDTO)
                .build();

        TeacherResponseDTO responseDTO = webTestClient.post()
                .uri("/admin/register/teacher")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(requestDTO)
                .exchange()
                .expectStatus().isCreated()
                .expectBody(TeacherResponseDTO.class)
                .returnResult()
                .getResponseBody();

        assertNotNull(responseDTO, "Response DTO should not be null");

        // Verify teacher information

        // Verify user exists with correct role
        Long userId = databaseClient.sql("SELECT id FROM users WHERE email = :email AND role_id = 4")
                .bind("email", "newteacher@example.com")
                .map(row -> row.get(0, Long.class))
                .one()
                .block();
        assertNotNull(userId, "Teacher user should exist with role_id=4 (TEACHER)");

        // Verify teacher profile exists
        Long teacherCount = databaseClient.sql("SELECT COUNT(*) FROM teacher")
                .map(row -> row.get(0, Long.class))
                .one()
                .block();
        assertEquals(1L, teacherCount, "Teacher profile should exist in database");
    }

    @ParameterizedTest(name = "POST /admin/register/teacher as non-admin {1} should return 403")
    @MethodSource("nonAdminRolesProvider")
    @DisplayName("POST /admin/register/teacher as non-admin should return 403 Forbidden")
    void registerTeacher_asNonAdmin_returns403(String email, String role) {
        String token = jwtTokenProvider.generateToken(email, role);

        UserRegisterRequestDTO userDTO = UserRegisterRequestDTO.builder()
                .username("unauthorizedteacher")
                .password("password123")
                .faculty("Engineering")
                .name("Unauthorized")
                .lastName("Teacher")
                .email("unteacher@example.com")
                .documentTypeId("1")
                .documentNumber("6666666666")
                .build();

        TeacherRegisterRequestDTO requestDTO = TeacherRegisterRequestDTO.builder()
                .employmentTypeId(1L)
                .maxHours(20)
                .user(userDTO)
                .build();

        webTestClient.post()
                .uri("/admin/register/teacher")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(requestDTO)
                .exchange()
                .expectStatus().isForbidden();
    }

    @Test
    @DisplayName("POST /admin/register/teacher unauthenticated should return 401 Unauthorized")
    void registerTeacher_unauthenticated_returns401() {
        UserRegisterRequestDTO userDTO = UserRegisterRequestDTO.builder()
                .username("unauthteacher")
                .password("password123")
                .faculty("Engineering")
                .name("Unauth")
                .lastName("Teacher")
                .email("unauthteacher@example.com")
                .documentTypeId("1")
                .documentNumber("7777777777")
                .build();

        TeacherRegisterRequestDTO requestDTO = TeacherRegisterRequestDTO.builder()
                .employmentTypeId(1L)
                .maxHours(20)
                .user(userDTO)
                .build();

        webTestClient.post()
                .uri("/admin/register/teacher")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(requestDTO)
                .exchange()
                .expectStatus().isUnauthorized();
    }

    // ==========================================
    // POST Register Section Tests
    // ==========================================
    
    @ParameterizedTest(name = "POST /admin/register/section as admin {1} should register section")
    @MethodSource("adminRoleProvider")
    @DisplayName("POST /admin/register/section as admin should create new section with coordinator")
    void registerSection_asAdmin_createsSuccessfully(String email, String role) {
        String token = jwtTokenProvider.generateToken(email, role);

        UserRegisterRequestDTO userDTO = UserRegisterRequestDTO.builder()
                .username("sectioncoordinator")
                .password("password123")
                .faculty("Business")
                .name("Section")
                .lastName("Coordinator")
                .email("coordinator@example.com")
                .documentTypeId("1")
                .documentNumber("8888888888")
                .build();

        SectionRegisterRequestDTO requestDTO = SectionRegisterRequestDTO.builder()
                .name("New Test Section")
                .user(userDTO)
                .build();

        webTestClient.post()
                .uri("/admin/register/section")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(requestDTO)
                .exchange()
                .expectStatus().isCreated()
                .expectBody(SectionResponseDTO.class)
                .returnResult()
                .getResponseBody();

        // Verify user exists with correct role (USER role for section coordinator)
        Long userCount = databaseClient.sql("SELECT COUNT(*) FROM users WHERE email = :email AND role_id = 2")
                .bind("email", "coordinator@example.com")
                .map(row -> row.get(0, Long.class))
                .one()
                .block();
        assertEquals(1L, userCount, "Section coordinator should exist with role_id=2 (USER)");

        // Verify section exists
        Long sectionCount = databaseClient.sql("SELECT COUNT(*) FROM section")
                .map(row -> row.get(0, Long.class))
                .one()
                .block();
        assertEquals(1L, sectionCount, "Section should exist in database");
    }

    @ParameterizedTest(name = "POST /admin/register/section as non-admin {1} should return 403")
    @MethodSource("nonAdminRolesProvider")
    @DisplayName("POST /admin/register/section as non-admin should return 403 Forbidden")
    void registerSection_asNonAdmin_returns403(String email, String role) {
        String token = jwtTokenProvider.generateToken(email, role);

        UserRegisterRequestDTO userDTO = UserRegisterRequestDTO.builder()
                .username("unauthorizedsection")
                .password("password123")
                .faculty("Business")
                .name("Unauthorized")
                .lastName("Section")
                .email("unsection@example.com")
                .documentTypeId("1")
                .documentNumber("9999999999")
                .build();

        SectionRegisterRequestDTO requestDTO = SectionRegisterRequestDTO.builder()
                .name("Unauthorized Section")
                .user(userDTO)
                .build();

        webTestClient.post()
                .uri("/admin/register/section")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(requestDTO)
                .exchange()
                .expectStatus().isForbidden();
    }

    @Test
    @DisplayName("POST /admin/register/section unauthenticated should return 401 Unauthorized")
    void registerSection_unauthenticated_returns401() {
        UserRegisterRequestDTO userDTO = UserRegisterRequestDTO.builder()
                .username("unauthsection")
                .password("password123")
                .faculty("Business")
                .name("Unauth")
                .lastName("Section")
                .email("unauthsection@example.com")
                .documentTypeId("1")
                .documentNumber("0000000001")
                .build();

        SectionRegisterRequestDTO requestDTO = SectionRegisterRequestDTO.builder()
                .name("Unauth Section")
                .user(userDTO)
                .build();

        webTestClient.post()
                .uri("/admin/register/section")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(requestDTO)
                .exchange()
                .expectStatus().isUnauthorized();
    }

    // ==========================================
    // Business Logic Tests
    // ==========================================
    
    @Test
    @DisplayName("Teacher with different employment types should be registered correctly")
    void registerTeacher_withDifferentEmploymentTypes_shouldSucceed() {
        String token = jwtTokenProvider.generateToken("testAdmin@example.com", "ROLE_ADMIN");

        // Test employment type 1
        UserRegisterRequestDTO userDTO1 = UserRegisterRequestDTO.builder()
                .username("teacher1")
                .password("password123")
                .faculty("Engineering")
                .name("Teacher")
                .lastName("One")
                .email("teacher1@example.com")
                .documentTypeId("1")
                .documentNumber("1000000001")
                .build();

        TeacherRegisterRequestDTO requestDTO1 = TeacherRegisterRequestDTO.builder()
                .employmentTypeId(1L)
                .maxHours(10)
                .user(userDTO1)
                .build();

        TeacherResponseDTO response1 = webTestClient.post()
                .uri("/admin/register/teacher")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(requestDTO1)
                .exchange()
                .expectStatus().isCreated()
                .expectBody(TeacherResponseDTO.class)
                .returnResult()
                .getResponseBody();

        assertNotNull(response1);
        assertEquals(1L, response1.getEmploymentTypeId());
        assertEquals(10, response1.getMaxHours());

        // Test employment type 2 with different max hours
        UserRegisterRequestDTO userDTO2 = UserRegisterRequestDTO.builder()
                .username("teacher2")
                .password("password123")
                .faculty("Science")
                .name("Teacher")
                .lastName("Two")
                .email("teacher2@example.com")
                .documentTypeId("1")
                .documentNumber("1000000002")
                .build();

        TeacherRegisterRequestDTO requestDTO2 = TeacherRegisterRequestDTO.builder()
                .employmentTypeId(2L)
                .maxHours(40)
                .user(userDTO2)
                .build();

        TeacherResponseDTO response2 = webTestClient.post()
                .uri("/admin/register/teacher")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(requestDTO2)
                .exchange()
                .expectStatus().isCreated()
                .expectBody(TeacherResponseDTO.class)
                .returnResult()
                .getResponseBody();

        assertNotNull(response2);
        assertEquals(2L, response2.getEmploymentTypeId());
        assertEquals(40, response2.getMaxHours());
    }

    @Test
    @DisplayName("Multiple sections with different coordinators should be created independently")
    void registerMultipleSections_withDifferentCoordinators_shouldSucceed() {
        String token = jwtTokenProvider.generateToken("testAdmin@example.com", "ROLE_ADMIN");

        // Create first section
        UserRegisterRequestDTO userDTO1 = UserRegisterRequestDTO.builder()
                .username("sectioncoord1")
                .password("password123")
                .faculty("Engineering")
                .name("Coordinator")
                .lastName("One")
                .email("coord1@example.com")
                .documentTypeId("1")
                .documentNumber("2000000001")
                .build();

        SectionRegisterRequestDTO requestDTO1 = SectionRegisterRequestDTO.builder()
                .name("Section Alpha")
                .user(userDTO1)
                .build();

        SectionResponseDTO response1 = webTestClient.post()
                .uri("/admin/register/section")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(requestDTO1)
                .exchange()
                .expectStatus().isCreated()
                .expectBody(SectionResponseDTO.class)
                .returnResult()
                .getResponseBody();

        assertNotNull(response1);
        assertEquals("Section Alpha", response1.getName());

        // Create second section
        UserRegisterRequestDTO userDTO2 = UserRegisterRequestDTO.builder()
                .username("sectioncoord2")
                .password("password123")
                .faculty("Arts")
                .name("Coordinator")
                .lastName("Two")
                .email("coord2@example.com")
                .documentTypeId("1")
                .documentNumber("2000000002")
                .build();

        SectionRegisterRequestDTO requestDTO2 = SectionRegisterRequestDTO.builder()
                .name("Section Beta")
                .user(userDTO2)
                .build();

        SectionResponseDTO response2 = webTestClient.post()
                .uri("/admin/register/section")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(requestDTO2)
                .exchange()
                .expectStatus().isCreated()
                .expectBody(SectionResponseDTO.class)
                .returnResult()
                .getResponseBody();

        assertNotNull(response2);
        assertEquals("Section Beta", response2.getName());

        // Verify both sections exist
        Long sectionCount = databaseClient.sql("SELECT COUNT(*) FROM section WHERE name IN ('Section Alpha', 'Section Beta')")
                .map(row -> row.get(0, Long.class))
                .one()
                .block();
        assertEquals(2L, sectionCount, "Both sections should exist in database");
    }

    @Test
    @DisplayName("All user types should be created with unique emails")
    void registerAllUserTypes_withUniqueEmails_shouldSucceed() {
        String token = jwtTokenProvider.generateToken("testAdmin@example.com", "ROLE_ADMIN");

        // Register a student
        UserRegisterRequestDTO studentDTO = UserRegisterRequestDTO.builder()
                .username("uniquestudent")
                .password("password123")
                .faculty("Engineering")
                .name("Unique")
                .lastName("Student")
                .email("unique.student@example.com")
                .documentTypeId("1")
                .documentNumber("3000000001")
                .build();

        webTestClient.post()
                .uri("/admin/register/student")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(studentDTO)
                .exchange()
                .expectStatus().isCreated()
                .expectBody(Long.class)
                .returnResult()
                .getResponseBody();
        
        Long studentCount = databaseClient.sql("SELECT COUNT(*) FROM users WHERE email = :email")
                .bind("email", "unique.student@example.com")
                .map(row -> row.get(0, Long.class))
                .one()
                .block();

        assertEquals(1L, studentCount, "Student count should be 1");

        // Register an admin
        UserRegisterRequestDTO adminDTO = UserRegisterRequestDTO.builder()
                .username("uniqueadmin")
                .password("password123")
                .faculty("Administration")
                .name("Unique")
                .lastName("Admin")
                .email("unique.admin@example.com")
                .documentTypeId("1")
                .documentNumber("3000000002")
                .build();

        webTestClient.post()
                .uri("/admin/register/admin")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(adminDTO)
                .exchange()
                .expectStatus().isCreated()
                .expectBody(Long.class)
                .returnResult()
                .getResponseBody();

        Long adminCount = databaseClient.sql("SELECT COUNT(*) FROM users WHERE email = :email")
                .bind("email", "unique.admin@example.com")
                .map(row -> row.get(0, Long.class))
                .one()
                .block();
        
        assertEquals(1L, adminCount, "Admin count should be 1");

        // Register a program coordinator
        UserRegisterRequestDTO programDTO = UserRegisterRequestDTO.builder()
                .username("uniqueprogram")
                .password("password123")
                .faculty("Arts")
                .name("Unique")
                .lastName("Program")
                .email("unique.program@example.com")
                .documentTypeId("1")
                .documentNumber("3000000003")
                .build();

        webTestClient.post()
                .uri("/admin/register/program")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(programDTO)
                .exchange()
                .expectStatus().isCreated()
                .expectBody(Long.class)
                .returnResult()
                .getResponseBody();
        
        Long programCount = databaseClient.sql("SELECT COUNT(*) FROM users WHERE email = :email")
                .bind("email", "unique.program@example.com")
                .map(row -> row.get(0, Long.class))
                .one()
                .block();
        assertEquals(1L, programCount, "Program coordinator count should be 1");
    }
}
