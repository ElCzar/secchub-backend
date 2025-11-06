package co.edu.puj.secchub_backend.admin.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
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
import co.edu.puj.secchub_backend.admin.dto.SemesterRequestDTO;
import co.edu.puj.secchub_backend.admin.dto.SemesterResponseDTO;
import co.edu.puj.secchub_backend.security.jwt.JwtTokenProvider;
import io.r2dbc.spi.ConnectionFactory;

@SpringBootTest
@AutoConfigureWebTestClient
@Testcontainers
@DisplayName("Semester Controller Integration Tests")
class SemesterControllerIntegrationTest extends DatabaseContainerIntegration {
    
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
                "/test-semesters.sql"
        );
    }

    // ==========================================
    // Test Data Providers
    // ==========================================
    
    /**
     * Provides admin roles for semester creation
     */
    private static Stream<Arguments> adminRoleProvider() {
        return Stream.of(
            Arguments.of("testAdmin@example.com", "ROLE_ADMIN")
        );
    }

    /**
     * Provides non-admin roles that should be forbidden from creating semesters
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
     * Provides all authenticated roles for viewing semesters
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
     * Provides roles authorized to query by year and period (admin, user, program)
     */
    private static Stream<Arguments> queryAuthorizedRolesProvider() {
        return Stream.of(
            Arguments.of("testAdmin@example.com", "ROLE_ADMIN"),
            Arguments.of("testUser@example.com", "ROLE_USER"),
            Arguments.of("testProgram@example.com", "ROLE_PROGRAM")
        );
    }

    // ==========================================
    // POST Create Semester Tests
    // ==========================================
    
    @ParameterizedTest(name = "POST /semesters as admin {1} should create semester")
    @MethodSource("adminRoleProvider")
    @DisplayName("POST /semesters as admin should create new semester")
    void createSemester_asAdmin_createsSuccessfully(String email, String role) {
        String token = jwtTokenProvider.generateToken(email, role);

        SemesterRequestDTO requestDTO = SemesterRequestDTO.builder()
                .year(2027)
                .period(1)
                .startDate(LocalDate.of(2027, 1, 15))
                .endDate(LocalDate.of(2027, 6, 30))
                .startSpecialWeek(LocalDate.of(2027, 3, 15))
                .build();

        SemesterResponseDTO responseDTO = webTestClient.post()
                .uri("/semesters")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(requestDTO)
                .exchange()
                .expectStatus().isCreated()
                .expectBody(SemesterResponseDTO.class)
                .returnResult()
                .getResponseBody();

        assertNotNull(responseDTO, "Response DTO should not be null");
        assertNotNull(responseDTO.getId(), "Semester ID should not be null");
        assertEquals(2027, responseDTO.getYear(), "Year should match");
        assertEquals(1, responseDTO.getPeriod(), "Period should match");
        assertEquals(LocalDate.of(2027, 1, 15), responseDTO.getStartDate(), "Start date should match");
        assertEquals(LocalDate.of(2027, 6, 30), responseDTO.getEndDate(), "End date should match");
        assertEquals(LocalDate.of(2027, 3, 15), responseDTO.getStartSpecialWeek(), "Start special week should match");

        // Verify semester exists in database
        Long count = databaseClient.sql("SELECT COUNT(*) FROM semester WHERE year = :year AND period = :period")
                .bind("year", 2027)
                .bind("period", 1)
                .map(row -> row.get(0, Long.class))
                .one()
                .block();
        assertEquals(1L, count, "Semester should exist in database");
    }

    @ParameterizedTest(name = "POST /semesters as non-admin {1} should return 403")
    @MethodSource("nonAdminRolesProvider")
    @DisplayName("POST /semesters as non-admin should return 403 Forbidden")
    void createSemester_asNonAdmin_returns403(String email, String role) {
        String token = jwtTokenProvider.generateToken(email, role);

        SemesterRequestDTO requestDTO = SemesterRequestDTO.builder()
                .year(2027)
                .period(2)
                .startDate(LocalDate.of(2027, 7, 1))
                .endDate(LocalDate.of(2027, 12, 20))
                .build();

        webTestClient.post()
                .uri("/semesters")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(requestDTO)
                .exchange()
                .expectStatus().isForbidden();

        // Verify semester was not created
        Long count = databaseClient.sql("SELECT COUNT(*) FROM semester WHERE year = :year AND period = :period")
                .bind("year", 2027)
                .bind("period", 2)
                .map(row -> row.get(0, Long.class))
                .one()
                .block();
        assertEquals(0L, count, "Unauthorized semester should not be created");
    }

    @Test
    @DisplayName("POST /semesters unauthenticated should return 401 Unauthorized")
    void createSemester_unauthenticated_returns401() {
        SemesterRequestDTO requestDTO = SemesterRequestDTO.builder()
                .year(2026)
                .period(1)
                .startDate(LocalDate.of(2026, 1, 15))
                .endDate(LocalDate.of(2026, 6, 30))
                .build();

        webTestClient.post()
                .uri("/semesters")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(requestDTO)
                .exchange()
                .expectStatus().isUnauthorized();
    }

    // ==========================================
    // GET Current Semester Tests
    // ==========================================
    
    @ParameterizedTest(name = "GET /semesters/current as {1} should return current semester")
    @MethodSource("authenticatedRolesProvider")
    @DisplayName("GET /semesters/current authenticated user should receive current semester")
    void getCurrentSemester_asAuthenticated_returnsCurrentSemester(String email, String role) {
        String token = jwtTokenProvider.generateToken(email, role);

        SemesterResponseDTO dto = webTestClient.get()
                .uri("/semesters/current")
                .header("Authorization", "Bearer " + token)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody(SemesterResponseDTO.class)
                .returnResult()
                .getResponseBody();

        assertNotNull(dto, "Response DTO should not be null");
        assertNotNull(dto.getId(), "Semester ID should not be null");
        assertNotNull(dto.getYear(), "Semester year should not be null");
        assertNotNull(dto.getPeriod(), "Semester period should not be null");
        assertNotNull(dto.getStartDate(), "Start date should not be null");
        assertNotNull(dto.getEndDate(), "End date should not be null");
        
        // Verify it's actually the current semester (dates encompass today)
        LocalDate today = LocalDate.now();
        LocalDate startDate = dto.getStartDate();
        LocalDate endDate = dto.getEndDate();
        assertTrue(
            !startDate.isAfter(today) && !endDate.isBefore(today),
            "Current semester should encompass today's date"
        );
    }

    @Test
    @DisplayName("GET /semesters/current unauthenticated should return 401 Unauthorized")
    void getCurrentSemester_unauthenticated_returns401() {
        webTestClient.get()
                .uri("/semesters/current")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isUnauthorized();
    }

    // ==========================================
    // GET Semester by Year and Period Tests
    // ==========================================
    
    @ParameterizedTest(name = "GET /semesters?year&period as {1} should return semester")
    @MethodSource("queryAuthorizedRolesProvider")
    @DisplayName("GET /semesters?year&period authorized user should receive specific semester")
    void getSemesterByYearAndPeriod_asAuthorized_returnsSemester(String email, String role) {
        String token = jwtTokenProvider.generateToken(email, role);

        // Get first semester from DB
        Integer year = databaseClient.sql("SELECT year FROM semester ORDER BY year DESC, period DESC LIMIT 1")
                .map(row -> row.get(0, Integer.class))
                .one()
                .block();
        Integer period = databaseClient.sql("SELECT period FROM semester ORDER BY year DESC, period DESC LIMIT 1")
                .map(row -> row.get(0, Integer.class))
                .one()
                .block();
        assertNotNull(year, "Year from DB should not be null");
        assertNotNull(period, "Period from DB should not be null");

        SemesterResponseDTO dto = webTestClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/semesters")
                        .queryParam("year", year)
                        .queryParam("period", period)
                        .build())
                .header("Authorization", "Bearer " + token)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody(SemesterResponseDTO.class)
                .returnResult()
                .getResponseBody();

        assertNotNull(dto, "Response DTO should not be null");
        assertEquals(year, dto.getYear(), "Year should match");
        assertEquals(period, dto.getPeriod(), "Period should match");
    }

    @Test
    @DisplayName("GET /semesters?year&period with non-existent semester should return 404")
    void getSemesterByYearAndPeriod_nonExistent_returns404() {
        String token = jwtTokenProvider.generateToken("testAdmin@example.com", "ROLE_ADMIN");

        webTestClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/semesters")
                        .queryParam("year", 1900)
                        .queryParam("period", 1)
                        .build())
                .header("Authorization", "Bearer " + token)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    @DisplayName("GET /semesters?year&period unauthenticated should return 401 Unauthorized")
    void getSemesterByYearAndPeriod_unauthenticated_returns401() {
        webTestClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/semesters")
                        .queryParam("year", 2024)
                        .queryParam("period", 1)
                        .build())
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isUnauthorized();
    }

    // ==========================================
    // GET All Semesters Tests
    // ==========================================
    
    @ParameterizedTest(name = "GET /semesters/all as {1} should return all semesters")
    @MethodSource("authenticatedRolesProvider")
    @DisplayName("GET /semesters/all authenticated user should receive all semesters list")
    void getAllSemesters_asAuthenticated_returnsList(String email, String role) {
        String token = jwtTokenProvider.generateToken(email, role);

        Long semesterCount = databaseClient.sql("SELECT COUNT(*) FROM semester")
                .map(row -> row.get(0, Long.class))
                .one()
                .block();
        assertNotNull(semesterCount, "Semester count from DB should not be null");

        List<SemesterResponseDTO> list = webTestClient.get()
                .uri("/semesters/all")
                .header("Authorization", "Bearer " + token)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(SemesterResponseDTO.class)
                .returnResult()
                .getResponseBody();

        assertNotNull(list, "Response list should not be null");
        assertEquals(semesterCount.intValue(), list.size(), "Returned semester list size should match DB count");
    }

    @Test
    @DisplayName("GET /semesters/all unauthenticated should return 401 Unauthorized")
    void getAllSemesters_unauthenticated_returns401() {
        webTestClient.get()
                .uri("/semesters/all")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isUnauthorized();
    }

    // ==========================================
    // Business Logic Tests
    // ==========================================
    
    @Test
    @DisplayName("Semesters should have unique year-period combinations")
    void semesters_shouldHaveUniqueYearPeriodCombinations() {
        String token = jwtTokenProvider.generateToken("testAdmin@example.com", "ROLE_ADMIN");

        List<SemesterResponseDTO> list = webTestClient.get()
                .uri("/semesters/all")
                .header("Authorization", "Bearer " + token)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(SemesterResponseDTO.class)
                .returnResult()
                .getResponseBody();

        assertNotNull(list, "Semester list should not be null");

        // Verify each year-period combination is unique
        long uniqueCombinations = list.stream()
                .map(s -> s.getYear() + "-" + s.getPeriod())
                .distinct()
                .count();
        
        assertEquals(list.size(), uniqueCombinations, "Each year-period combination should be unique");
    }

    @Test
    @DisplayName("Semester end date should be after start date")
    void semesterEndDate_shouldBeAfterStartDate() {
        String token = jwtTokenProvider.generateToken("testAdmin@example.com", "ROLE_ADMIN");

        List<SemesterResponseDTO> list = webTestClient.get()
                .uri("/semesters/all")
                .header("Authorization", "Bearer " + token)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(SemesterResponseDTO.class)
                .returnResult()
                .getResponseBody();

        assertNotNull(list, "Semester list should not be null");

        // Verify all semesters have end date after start date
        for (SemesterResponseDTO semester : list) {
            LocalDate startDate = semester.getStartDate();
            LocalDate endDate = semester.getEndDate();
            assertTrue(
                endDate.isAfter(startDate),
                "Semester " + semester.getYear() + "-" + semester.getPeriod() + 
                " end date should be after start date"
            );
        }
    }

    @Test
    @DisplayName("Creating multiple semesters for different years should succeed")
    void createMultipleSemesters_forDifferentYears_shouldSucceed() {
        String token = jwtTokenProvider.generateToken("testAdmin@example.com", "ROLE_ADMIN");

        // Create semester for 2026-1
        SemesterRequestDTO requestFirstSemester = SemesterRequestDTO.builder()
                .year(2026)
                .period(1)
                .startDate(LocalDate.of(2026, 1, 15))
                .endDate(LocalDate.of(2026, 6, 30))
                .startSpecialWeek(LocalDate.of(2026, 3, 15))
                .build();

        SemesterResponseDTO responseFirstSemester = webTestClient.post()
                .uri("/semesters")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(requestFirstSemester)
                .exchange()
                .expectStatus().isCreated()
                .expectBody(SemesterResponseDTO.class)
                .returnResult()
                .getResponseBody();

        assertNotNull(responseFirstSemester, "First semester should be created");
        assertEquals(2026, responseFirstSemester.getYear(), "Year should be 2026");
        assertEquals(1, responseFirstSemester.getPeriod(), "Period should be 1");

        // Create semester for 2026-2
        SemesterRequestDTO requestSecondSemester = SemesterRequestDTO.builder()
                .year(2026)
                .period(2)
                .startDate(LocalDate.of(2026, 7, 1))
                .endDate(LocalDate.of(2026, 12, 20))
                .startSpecialWeek(LocalDate.of(2026, 10, 15))
                .build();

        SemesterResponseDTO responseSecondSemester = webTestClient.post()
                .uri("/semesters")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(requestSecondSemester)
                .exchange()
                .expectStatus().isCreated()
                .expectBody(SemesterResponseDTO.class)
                .returnResult()
                .getResponseBody();

        assertNotNull(responseSecondSemester, "Second semester should be created");
        assertEquals(2026, responseSecondSemester.getYear(), "Year should be 2026");
        assertEquals(2, responseSecondSemester.getPeriod(), "Period should be 2");

        // Verify both exist in database
        Long count = databaseClient.sql("SELECT COUNT(*) FROM semester WHERE year = 2026")
                .map(row -> row.get(0, Long.class))
                .one()
                .block();
        assertEquals(2L, count, "Both semesters for 2026 should exist");
    }
}
