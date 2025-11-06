package co.edu.puj.secchub_backend.admin.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
import co.edu.puj.secchub_backend.admin.dto.CourseRequestDTO;
import co.edu.puj.secchub_backend.admin.dto.CourseResponseDTO;
import co.edu.puj.secchub_backend.admin.model.Course;
import co.edu.puj.secchub_backend.admin.repository.CourseRepository;
import co.edu.puj.secchub_backend.security.jwt.JwtTokenProvider;
import io.r2dbc.spi.ConnectionFactory;

@SpringBootTest
@AutoConfigureWebTestClient
@Testcontainers
@DisplayName("Course Controller Integration Tests")
class CourseControllerIntegrationTest extends DatabaseContainerIntegration {
    
    @Autowired
    private WebTestClient webTestClient;

    @Autowired
    private DatabaseClient databaseClient;

    @Autowired
    private CourseRepository courseRepository;

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
                "/test-courses.sql"
        );
    }

    // ==========================================
    // Test Data Providers
    // ==========================================
    
    /**
     * Provides authorized roles that can view courses (all authenticated users)
     */
    private static Stream<Arguments> authorizedRolesProvider() {
        return Stream.of(
            Arguments.of("testAdmin@example.com", "ROLE_ADMIN"),
            Arguments.of("testUser@example.com", "ROLE_USER"),
            Arguments.of("testStudent@example.com", "ROLE_STUDENT"),
            Arguments.of("testTeacher@example.com", "ROLE_TEACHER"),
            Arguments.of("testProgram@example.com", "ROLE_PROGRAM")
        );
    }

    /**
     * Provides admin-only roles for create/update/delete operations
     */
    private static Stream<Arguments> adminRoleProvider() {
        return Stream.of(
            Arguments.of("testAdmin@example.com", "ROLE_ADMIN")
        );
    }

    /**
     * Provides non-admin roles that should be forbidden from admin operations
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
    // GET All Courses Tests
    // ==========================================
    
    @ParameterizedTest(name = "GET /courses authorized as {1} should return all courses")
    @MethodSource("authorizedRolesProvider")
    @DisplayName("GET /courses authenticated user should receive all courses list")
    void getAllCourses_asAuthenticated_returnsList(String email, String role) {
        String token = jwtTokenProvider.generateToken(email, role);

        Long courseCount = databaseClient.sql("SELECT COUNT(*) FROM course")
                .map(row -> row.get(0, Long.class))
                .one()
                .block();
        assertNotNull(courseCount, "Course count from DB should not be null");

        List<CourseResponseDTO> list = webTestClient.get()
                .uri("/courses")
                .header("Authorization", "Bearer " + token)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(CourseResponseDTO.class)
                .returnResult()
                .getResponseBody();

        assertNotNull(list, "Response list should not be null");
        assertEquals(courseCount.intValue(), list.size(), "Returned course list size should match DB count");
    }

    @Test
    @DisplayName("GET /courses unauthenticated should return 401 Unauthorized")
    void getAllCourses_unauthenticated_returns401() {
        webTestClient.get()
                .uri("/courses")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isUnauthorized();
    }

    // ==========================================
    // GET Course by ID Tests
    // ==========================================
    
    @ParameterizedTest(name = "GET /courses/:id authorized as {1} should return course")
    @MethodSource("authorizedRolesProvider")
    @DisplayName("GET /courses/:id authenticated user should receive specific course")
    void getCourseById_asAuthenticated_returnsCourse(String email, String role) {
        String token = jwtTokenProvider.generateToken(email, role);

        // Get first course ID from DB
        Long courseId = databaseClient.sql("SELECT id FROM course ORDER BY id ASC LIMIT 1")
                .map(row -> row.get(0, Long.class))
                .one()
                .block();
        assertNotNull(courseId, "Course ID from DB should not be null");

        CourseResponseDTO dto = webTestClient.get()
                .uri("/courses/" + courseId)
                .header("Authorization", "Bearer " + token)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody(CourseResponseDTO.class)
                .returnResult()
                .getResponseBody();

        assertNotNull(dto, "Response DTO should not be null");
        assertEquals(courseId, dto.getId(), "Course ID should match");
        assertNotNull(dto.getName(), "Course name should not be null");
        assertNotNull(dto.getCredits(), "Course credits should not be null");
    }

    @ParameterizedTest(name = "GET /courses/:id with non-existent ID as {1} should return 404")
    @MethodSource("authorizedRolesProvider")
    @DisplayName("GET /courses/:id with non-existent ID should return 404 Not Found")
    void getCourseById_nonExistent_returns404(String email, String role) {
        String token = jwtTokenProvider.generateToken(email, role);
        Long nonExistentId = Long.MAX_VALUE;

        webTestClient.get()
                .uri("/courses/" + nonExistentId)
                .header("Authorization", "Bearer " + token)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    @DisplayName("GET /courses/:id unauthenticated should return 401 Unauthorized")
    void getCourseById_unauthenticated_returns401() {
        Long courseId = databaseClient.sql("SELECT id FROM course LIMIT 1")
                .map(row -> row.get(0, Long.class))
                .one()
                .block();
        assertNotNull(courseId, "Course ID from DB should not be null");

        webTestClient.get()
                .uri("/courses/" + courseId)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isUnauthorized();
    }

    // ==========================================
    // POST Create Course Tests
    // ==========================================
    
    @ParameterizedTest(name = "POST /courses as admin {1} should create course")
    @MethodSource("adminRoleProvider")
    @DisplayName("POST /courses as admin should create new course")
    void createCourse_asAdmin_createsSuccessfully(String email, String role) {
        String token = jwtTokenProvider.generateToken(email, role);

        CourseRequestDTO requestDTO = CourseRequestDTO.builder()
                .sectionId(1L)
                .name("Integration Testing Course")
                .credits(3)
                .description("A course created during integration testing")
                .isValid(true)
                .recommendation("No prerequisites")
                .statusId(1L)
                .build();

        CourseResponseDTO response = webTestClient.post()
                .uri("/courses")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(requestDTO)
                .exchange()
                .expectStatus().isCreated()
                .expectBody(CourseResponseDTO.class)
                .returnResult()
                .getResponseBody();

        assertNotNull(response, "Response DTO should not be null");

        // Verify in database
        Long count = databaseClient.sql("SELECT COUNT(*) FROM course WHERE name = :name AND credits = :credits")
                .bind("name", "Integration Testing Course")
                .bind("credits", 3)
                .map(row -> row.get(0, Long.class))
                .one()
                .block();
        assertEquals(1L, count, "Course should exist in database");
    }

    @ParameterizedTest(name = "POST /courses as non-admin {1} should return 403")
    @MethodSource("nonAdminRolesProvider")
    @DisplayName("POST /courses as non-admin should return 403 Forbidden")
    void createCourse_asNonAdmin_returns403(String email, String role) {
        String token = jwtTokenProvider.generateToken(email, role);

        CourseRequestDTO requestDTO = CourseRequestDTO.builder()
                .sectionId(1L)
                .name("Unauthorized Course")
                .credits(3)
                .description("Should not be created")
                .isValid(true)
                .statusId(1L)
                .build();

        webTestClient.post()
                .uri("/courses")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(requestDTO)
                .exchange()
                .expectStatus().isForbidden();
    }

    @Test
    @DisplayName("POST /courses unauthenticated should return 401 Unauthorized")
    void createCourse_unauthenticated_returns401() {
        CourseRequestDTO requestDTO = CourseRequestDTO.builder()
                .sectionId(1L)
                .name("Unauthorized Course")
                .credits(3)
                .description("Should not be created")
                .isValid(true)
                .statusId(1L)
                .build();

        webTestClient.post()
                .uri("/courses")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(requestDTO)
                .exchange()
                .expectStatus().isUnauthorized();
    }

    // ==========================================
    // PUT Update Course Tests
    // ==========================================
    
    @ParameterizedTest(name = "PUT /courses/:id as admin {1} should update course")
    @MethodSource("adminRoleProvider")
    @DisplayName("PUT /courses/:id as admin should update existing course")
    void updateCourse_asAdmin_updatesSuccessfully(String email, String role) {
        String token = jwtTokenProvider.generateToken(email, role);

        // Get first course from DB
        Long courseId = databaseClient.sql("SELECT id FROM course LIMIT 1")
                .map(row -> row.get(0, Long.class))
                .one()
                .block();
        assertNotNull(courseId, "Course ID from DB should not be null");

        CourseRequestDTO requestDTO = CourseRequestDTO.builder()
                .sectionId(1L)
                .name("Updated Course Name")
                .credits(4)
                .description("Updated course description")
                .isValid(false)
                .recommendation("Updated prerequisites")
                .statusId(1L)
                .build();

        webTestClient.put()
                .uri("/courses/" + courseId)
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(requestDTO)
                .exchange()
                .expectStatus().isOk()
                .expectBody(CourseResponseDTO.class)
                .returnResult()
                .getResponseBody();

        // Verify in database
        Long count = databaseClient.sql("SELECT COUNT(*) FROM course WHERE id = :id AND name = :name AND credits = :credits AND description = :description AND is_valid = :isValid AND recommendation = :recommendation")
                .bind("id", courseId)
                .bind("name", "Updated Course Name")
                .bind("credits", 4)
                .bind("description", "Updated course description")
                .bind("isValid", false)
                .bind("recommendation", "Updated prerequisites")
                .map(row -> row.get(0, Long.class))
                .one()
                .block();
        assertEquals(1L, count, "Course should be updated in database");
    }

    @ParameterizedTest(name = "PUT /courses/:id as non-admin {1} should return 403")
    @MethodSource("nonAdminRolesProvider")
    @DisplayName("PUT /courses/:id as non-admin should return 403 Forbidden")
    void updateCourse_asNonAdmin_returns403(String email, String role) {
        String token = jwtTokenProvider.generateToken(email, role);

        Long courseId = databaseClient.sql("SELECT id FROM course LIMIT 1")
                .map(row -> row.get(0, Long.class))
                .one()
                .block();
        assertNotNull(courseId, "Course ID from DB should not be null");

        CourseRequestDTO requestDTO = CourseRequestDTO.builder()
                .sectionId(1L)
                .name("Unauthorized Update")
                .credits(3)
                .description("Should not be updated")
                .isValid(true)
                .statusId(1L)
                .build();

        webTestClient.put()
                .uri("/courses/" + courseId)
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(requestDTO)
                .exchange()
                .expectStatus().isForbidden();
    }

    @ParameterizedTest(name = "PUT /courses/:id with non-existent ID as admin should return 404")
    @MethodSource("adminRoleProvider")
    @DisplayName("PUT /courses/:id with non-existent ID should return 404 Not Found")
    void updateCourse_nonExistent_returns404(String email, String role) {
        String token = jwtTokenProvider.generateToken(email, role);
        Long nonExistentId = Long.MAX_VALUE;

        CourseRequestDTO requestDTO = CourseRequestDTO.builder()
                .sectionId(1L)
                .name("Non-existent Course")
                .credits(3)
                .description("Should not be found")
                .isValid(true)
                .statusId(1L)
                .build();

        webTestClient.put()
                .uri("/courses/" + nonExistentId)
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(requestDTO)
                .exchange()
                .expectStatus().isNotFound();
    }

    // ==========================================
    // PATCH Partial Update Course Tests
    // ==========================================
    
    @ParameterizedTest(name = "PATCH /courses/:id as admin {1} should partially update course")
    @MethodSource("adminRoleProvider")
    @DisplayName("PATCH /courses/:id as admin should partially update course fields")
    void patchCourse_asAdmin_updatesPartially(String email, String role) {
        String token = jwtTokenProvider.generateToken(email, role);

        // Get first course from DB
        Long courseId = databaseClient.sql("SELECT id FROM course LIMIT 1")
                .map(row -> row.get(0, Long.class))
                .one()
                .block();
        assertNotNull(courseId, "Course ID from DB should not be null");

        // Get original course data
        CourseResponseDTO originalCourse = courseRepository.findById(courseId)
                .map(course -> CourseResponseDTO.builder()
                        .id(course.getId())
                        .sectionId(course.getSectionId())
                        .name(course.getName())
                        .credits(course.getCredits())
                        .description(course.getDescription())
                        .isValid(course.getIsValid())
                        .recommendation(course.getRecommendation())
                        .statusId(course.getStatusId())
                        .build())
                .block();
        assertNotNull(originalCourse, "Original course should exist");

        // Patch only name and credits
        Map<String, Object> updates = new HashMap<>();
        updates.put("name", "Patched Course Name");
        updates.put("credits", 5);

        webTestClient.patch()
                .uri("/courses/" + courseId)
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(updates)
                .exchange()
                .expectStatus().isOk()
                .expectBody(CourseResponseDTO.class)
                .returnResult()
                .getResponseBody();

        // Verify in database
        CourseResponseDTO dto = courseRepository.findById(courseId)
                .map(course -> CourseResponseDTO.builder()
                        .id(course.getId())
                        .sectionId(course.getSectionId())
                        .name(course.getName())
                        .credits(course.getCredits())
                        .description(course.getDescription())
                        .isValid(course.getIsValid())
                        .recommendation(course.getRecommendation())
                        .statusId(course.getStatusId())
                        .build())
                .block();
        
        assertNotNull(dto, "Updated course should exist");
        // Verify other fields remain unchanged
        assertEquals(originalCourse.getDescription(), dto.getDescription(), "Description should remain unchanged");
        assertEquals(originalCourse.getIsValid(), dto.getIsValid(), "IsValid should remain unchanged");
        assertEquals(originalCourse.getSectionId(), dto.getSectionId(), "SectionId should remain unchanged");
    }

    @ParameterizedTest(name = "PATCH /courses/:id as non-admin {1} should return 403")
    @MethodSource("nonAdminRolesProvider")
    @DisplayName("PATCH /courses/:id as non-admin should return 403 Forbidden")
    void patchCourse_asNonAdmin_returns403(String email, String role) {
        String token = jwtTokenProvider.generateToken(email, role);

        Long courseId = databaseClient.sql("SELECT id FROM course LIMIT 1")
                .map(row -> row.get(0, Long.class))
                .one()
                .block();
        assertNotNull(courseId, "Course ID from DB should not be null");

        Map<String, Object> updates = new HashMap<>();
        updates.put("name", "Unauthorized Patch");

        webTestClient.patch()
                .uri("/courses/" + courseId)
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(updates)
                .exchange()
                .expectStatus().isForbidden();
    }

    @ParameterizedTest(name = "PATCH /courses/:id with non-existent ID as admin should return 404")
    @MethodSource("adminRoleProvider")
    @DisplayName("PATCH /courses/:id with non-existent ID should return 404 Not Found")
    void patchCourse_nonExistent_returns404(String email, String role) {
        String token = jwtTokenProvider.generateToken(email, role);
        Long nonExistentId = Long.MAX_VALUE;

        Map<String, Object> updates = new HashMap<>();
        updates.put("name", "Non-existent Course");

        webTestClient.patch()
                .uri("/courses/" + nonExistentId)
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(updates)
                .exchange()
                .expectStatus().isNotFound();
    }

    // ==========================================
    // DELETE Course Tests
    // ==========================================
    
    @ParameterizedTest(name = "DELETE /courses/:id as admin {1} should delete course")
    @MethodSource("adminRoleProvider")
    @DisplayName("DELETE /courses/:id as admin should delete course successfully")
    void deleteCourse_asAdmin_deletesSuccessfully(String email, String role) {
        String token = jwtTokenProvider.generateToken(email, role);

        // Create a dedicated course for deletion to avoid affecting other tests
        CourseRequestDTO requestDTO = CourseRequestDTO.builder()
                .sectionId(1L)
                .name("Course to Delete - Admin Test")
                .credits(3)
                .description("This course will be deleted by admin")
                .isValid(true)
                .statusId(1L)
                .build();

        CourseResponseDTO response = webTestClient.post()
                .uri("/courses")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(requestDTO)
                .exchange()
                .expectStatus().isCreated()
                .expectBody(CourseResponseDTO.class)
                .returnResult()
                .getResponseBody();

        assertNotNull(response, "Response DTO should not be null");
        
        // Verify course was created in database
        Long courseId = databaseClient.sql("SELECT id FROM course WHERE name = :name AND credits = :credits")
                .bind("name", "Course to Delete - Admin Test")
                .bind("credits", 3)
                .map(row -> row.get(0, Long.class))
                .one()
                .block();
        assertNotNull(courseId, "Course to be deleted should exist in database");

        webTestClient.delete()
                .uri("/courses/" + courseId)
                .header("Authorization", "Bearer " + token)
                .exchange()
                .expectStatus().isNoContent();

        // Verify course is deleted from database
        Long count = databaseClient.sql("SELECT COUNT(*) FROM course WHERE id = :id")
                .bind("id", courseId)
                .map(row -> row.get(0, Long.class))
                .one()
                .block();
        assertEquals(0L, count, "Course should be deleted from database");
    }

    @ParameterizedTest(name = "DELETE /courses/:id as non-admin {1} should return 403")
    @MethodSource("nonAdminRolesProvider")
    @DisplayName("DELETE /courses/:id as non-admin should return 403 Forbidden")
    void deleteCourse_asNonAdmin_returns403(String email, String role) {
        String token = jwtTokenProvider.generateToken(email, role);

        // Get any existing course from test data (these won't be deleted)
        Long courseId = databaseClient.sql("SELECT id FROM course ORDER BY id ASC LIMIT 1")
                .map(row -> row.get(0, Long.class))
                .one()
                .block();
        assertNotNull(courseId, "Course ID from DB should not be null");

        webTestClient.delete()
                .uri("/courses/" + courseId)
                .header("Authorization", "Bearer " + token)
                .exchange()
                .expectStatus().isForbidden();

        // Verify course still exists
        Long count = databaseClient.sql("SELECT COUNT(*) FROM course WHERE id = :id")
                .bind("id", courseId)
                .map(row -> row.get(0, Long.class))
                .one()
                .block();
        assertEquals(1L, count, "Course should still exist in database");
    }

    @ParameterizedTest(name = "DELETE /courses/:id with non-existent ID as admin should return 404")
    @MethodSource("adminRoleProvider")
    @DisplayName("DELETE /courses/:id with non-existent ID should return 404 Not Found")
    void deleteCourse_nonExistent_returns404(String email, String role) {
        String token = jwtTokenProvider.generateToken(email, role);
        Long nonExistentId = Long.MAX_VALUE;

        webTestClient.delete()
                .uri("/courses/" + nonExistentId)
                .header("Authorization", "Bearer " + token)
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    @DisplayName("DELETE /courses/:id unauthenticated should return 401 Unauthorized")
    void deleteCourse_unauthenticated_returns401() {
        Long courseId = databaseClient.sql("SELECT id FROM course ORDER BY id ASC LIMIT 1")
                .map(row -> row.get(0, Long.class))
                .one()
                .block();
        assertNotNull(courseId, "Course ID from DB should not be null");

        webTestClient.delete()
                .uri("/courses/" + courseId)
                .exchange()
                .expectStatus().isUnauthorized();
    }

    // ==========================================
    // Business Logic Tests
    // ==========================================
    
    @Test
    @DisplayName("Course with varying credit values should be handled correctly")
    void coursesWithDifferentCredits_shouldBeHandledCorrectly() {
        String token = jwtTokenProvider.generateToken("testAdmin@example.com", "ROLE_ADMIN");

        // Create courses with different credit values
        for (int credits = 2; credits <= 5; credits++) {
            CourseRequestDTO requestDTO = CourseRequestDTO.builder()
                    .sectionId(1L)
                    .name("Course with " + credits + " credits")
                    .credits(credits)
                    .description("Testing credit value " + credits)
                    .isValid(true)
                    .statusId(1L)
                    .build();

            CourseResponseDTO response = webTestClient.post()
                    .uri("/courses")
                    .header("Authorization", "Bearer " + token)
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(requestDTO)
                    .exchange()
                    .expectStatus().isCreated()
                    .expectBody(CourseResponseDTO.class)
                    .returnResult()
                    .getResponseBody();

            assertNotNull(response, "Response DTO should not be null for " + credits + " credits");

            Course saved = databaseClient.sql("SELECT * FROM course WHERE name = :name AND credits = :credits")
                    .bind("name", "Course with " + credits + " credits")
                    .bind("credits", credits)
                    .map((row, metadata) -> {
                        Course course = new Course();
                        course.setId(row.get("id", Long.class));
                        course.setSectionId(row.get("section_id", Long.class));
                        course.setName(row.get("name", String.class));
                        course.setCredits(row.get("credits", Integer.class));
                        course.setDescription(row.get("description", String.class));
                        course.setIsValid(row.get("is_valid", Boolean.class));
                        course.setRecommendation(row.get("recommendation", String.class));
                        course.setStatusId(row.get("status_id", Long.class));
                        return course;
                    })
                    .one()
                    .block();
            assertNotNull(saved);
            assertEquals(credits, saved.getCredits(), "Credits should match");
        }
    }

    @Test
    @DisplayName("Course isValid flag should toggle correctly")
    void courseIsValidFlag_shouldToggleCorrectly() {
        String token = jwtTokenProvider.generateToken("testAdmin@example.com", "ROLE_ADMIN");

        // Get a course that is currently valid
        Long courseId = databaseClient.sql("SELECT id FROM course WHERE is_valid = TRUE LIMIT 1")
                .map(row -> row.get(0, Long.class))
                .one()
                .block();
        assertNotNull(courseId, "Valid course should exist");

        // Toggle isValid to false using PATCH
        Map<String, Object> updates = new HashMap<>();
        updates.put("isValid", false);

        CourseResponseDTO dto = webTestClient.patch()
                .uri("/courses/" + courseId)
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(updates)
                .exchange()
                .expectStatus().isOk()
                .expectBody(CourseResponseDTO.class)
                .returnResult()
                .getResponseBody();

        assertNotNull(dto);
        assertFalse(dto.getIsValid(), "isValid should be false after patch");

        // Toggle back to true
        updates.put("isValid", true);
        dto = webTestClient.patch()
                .uri("/courses/" + courseId)
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(updates)
                .exchange()
                .expectStatus().isOk()
                .expectBody(CourseResponseDTO.class)
                .returnResult()
                .getResponseBody();

        assertNotNull(dto);
        assertTrue(dto.getIsValid(), "isValid should be true after second patch");
    }
}
