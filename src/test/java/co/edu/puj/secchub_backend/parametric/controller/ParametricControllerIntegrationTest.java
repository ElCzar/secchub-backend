package co.edu.puj.secchub_backend.parametric.controller;

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
import org.springframework.test.web.reactive.server.WebTestClient;
import org.testcontainers.junit.jupiter.Testcontainers;

import co.edu.puj.secchub_backend.DatabaseContainerIntegration;
import co.edu.puj.secchub_backend.R2dbcTestUtils;
import co.edu.puj.secchub_backend.parametric.contracts.ClassroomTypeDTO;
import co.edu.puj.secchub_backend.parametric.contracts.DocumentTypeDTO;
import co.edu.puj.secchub_backend.parametric.contracts.EmploymentTypeDTO;
import co.edu.puj.secchub_backend.parametric.contracts.ModalityDTO;
import co.edu.puj.secchub_backend.parametric.contracts.RoleDTO;
import co.edu.puj.secchub_backend.parametric.contracts.StatusDTO;
import co.edu.puj.secchub_backend.security.jwt.JwtTokenProvider;
import io.r2dbc.spi.ConnectionFactory;

@SpringBootTest
@AutoConfigureWebTestClient
@Testcontainers
@DisplayName("Parametric Controller Integration Tests")
class ParametricControllerIntegrationTest extends DatabaseContainerIntegration {
    
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
                "/test-users.sql"
        );
    }

    // ==========================================
    // Test Data Providers
    // ==========================================
    
    /**
     * Provides all authenticated roles (any role can access parametric data)
     */
    private static Stream<Arguments> authenticatedRolesProvider() {
        return Stream.of(
            Arguments.of("testAdmin@example.com", "ROLE_ADMIN"),
            Arguments.of("testUser@example.com", "ROLE_USER"),
            Arguments.of("testTeacher@example.com", "ROLE_TEACHER"),
            Arguments.of("testStudent@example.com", "ROLE_STUDENT"),
            Arguments.of("testProgram@example.com", "ROLE_PROGRAM")
        );
    }

    // ==========================================
    // GET All Statuses Tests
    // ==========================================

    @ParameterizedTest
    @MethodSource("authenticatedRolesProvider")
    @DisplayName("GET /parametric/statuses - Authenticated users can retrieve all statuses")
    void getAllStatuses_asAuthenticatedUser_shouldSucceed(String email, String role) {
        String token = jwtTokenProvider.generateToken(email, role);

        List<StatusDTO> statuses = webTestClient.get()
                .uri("/parametric/statuses")
                .header("Authorization", "Bearer " + token)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(StatusDTO.class)
                .returnResult()
                .getResponseBody();

        assertNotNull(statuses);
        assertFalse(statuses.isEmpty());
        assertEquals(14, statuses.size(), "Should have 14 statuses from init-parameters.sql");
        
        // Verify some expected statuses
        assertTrue(statuses.stream().anyMatch(s -> s.getName().equals("Active")));
        assertTrue(statuses.stream().anyMatch(s -> s.getName().equals("Pending")));
        assertTrue(statuses.stream().anyMatch(s -> s.getName().equals("Confirmed")));
        assertTrue(statuses.stream().anyMatch(s -> s.getName().equals("Rejected")));
    }

    @Test
    @DisplayName("GET /parametric/statuses - Unauthorized without token")
    void getAllStatuses_withoutToken_shouldReturn401() {
        webTestClient.get()
                .uri("/parametric/statuses")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isUnauthorized();
    }

    // ==========================================
    // GET Status by ID Tests
    // ==========================================

    @ParameterizedTest
    @MethodSource("authenticatedRolesProvider")
    @DisplayName("GET /parametric/statuses/{id} - Should retrieve Active status")
    void getStatusById_activeStatus_shouldSucceed(String email, String role) {
        String token = jwtTokenProvider.generateToken(email, role);
        Long statusId = 1L;

        String statusName = webTestClient.get()
                .uri("/parametric/statuses/{id}", statusId)
                .header("Authorization", "Bearer " + token)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class)
                .returnResult()
                .getResponseBody();

        assertNotNull(statusName);
        assertEquals("Active", statusName);
    }

    @ParameterizedTest
    @MethodSource("authenticatedRolesProvider")
    @DisplayName("GET /parametric/statuses/{id} - Should retrieve Pending status")
    void getStatusById_pendingStatus_shouldSucceed(String email, String role) {
        String token = jwtTokenProvider.generateToken(email, role);
        Long statusId = 4L;

        String statusName = webTestClient.get()
                .uri("/parametric/statuses/{id}", statusId)
                .header("Authorization", "Bearer " + token)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class)
                .returnResult()
                .getResponseBody();

        assertNotNull(statusName);
        assertEquals("Pending", statusName);
    }

    @ParameterizedTest
    @MethodSource("authenticatedRolesProvider")
    @DisplayName("GET /parametric/statuses/{id} - Should retrieve Confirmed status")
    void getStatusById_confirmedStatus_shouldSucceed(String email, String role) {
        String token = jwtTokenProvider.generateToken(email, role);
        Long statusId = 8L;

        String statusName = webTestClient.get()
                .uri("/parametric/statuses/{id}", statusId)
                .header("Authorization", "Bearer " + token)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class)
                .returnResult()
                .getResponseBody();

        assertNotNull(statusName);
        assertEquals("Confirmed", statusName);
    }

    // ==========================================
    // GET All Roles Tests
    // ==========================================

    @ParameterizedTest
    @MethodSource("authenticatedRolesProvider")
    @DisplayName("GET /parametric/roles - Authenticated users can retrieve all roles")
    void getAllRoles_asAuthenticatedUser_shouldSucceed(String email, String role) {
        String token = jwtTokenProvider.generateToken(email, role);

        List<RoleDTO> roles = webTestClient.get()
                .uri("/parametric/roles")
                .header("Authorization", "Bearer " + token)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(RoleDTO.class)
                .returnResult()
                .getResponseBody();

        assertNotNull(roles);
        assertFalse(roles.isEmpty());
        assertEquals(5, roles.size(), "Should have 5 roles from init-parameters.sql");
        
        // Verify all expected roles
        assertTrue(roles.stream().anyMatch(r -> r.getName().equals("ROLE_ADMIN")));
        assertTrue(roles.stream().anyMatch(r -> r.getName().equals("ROLE_USER")));
        assertTrue(roles.stream().anyMatch(r -> r.getName().equals("ROLE_STUDENT")));
        assertTrue(roles.stream().anyMatch(r -> r.getName().equals("ROLE_TEACHER")));
        assertTrue(roles.stream().anyMatch(r -> r.getName().equals("ROLE_PROGRAM")));
    }

    @Test
    @DisplayName("GET /parametric/roles - Unauthorized without token")
    void getAllRoles_withoutToken_shouldReturn401() {
        webTestClient.get()
                .uri("/parametric/roles")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isUnauthorized();
    }

    // ==========================================
    // GET Role by ID Tests
    // ==========================================

    @ParameterizedTest
    @MethodSource("authenticatedRolesProvider")
    @DisplayName("GET /parametric/roles/{id} - Should retrieve ROLE_ADMIN")
    void getRoleById_adminRole_shouldSucceed(String email, String role) {
        String token = jwtTokenProvider.generateToken(email, role);
        Long roleId = 1L;

        String roleName = webTestClient.get()
                .uri("/parametric/roles/{id}", roleId)
                .header("Authorization", "Bearer " + token)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class)
                .returnResult()
                .getResponseBody();

        assertNotNull(roleName);
        assertEquals("ROLE_ADMIN", roleName);
    }

    @ParameterizedTest
    @MethodSource("authenticatedRolesProvider")
    @DisplayName("GET /parametric/roles/{id} - Should retrieve ROLE_TEACHER")
    void getRoleById_teacherRole_shouldSucceed(String email, String role) {
        String token = jwtTokenProvider.generateToken(email, role);
        Long roleId = 4L;

        String roleName = webTestClient.get()
                .uri("/parametric/roles/{id}", roleId)
                .header("Authorization", "Bearer " + token)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class)
                .returnResult()
                .getResponseBody();

        assertNotNull(roleName);
        assertEquals("ROLE_TEACHER", roleName);
    }

    // ==========================================
    // GET All Document Types Tests
    // ==========================================

    @ParameterizedTest
    @MethodSource("authenticatedRolesProvider")
    @DisplayName("GET /parametric/document-types - Authenticated users can retrieve all document types")
    void getAllDocumentTypes_asAuthenticatedUser_shouldSucceed(String email, String role) {
        String token = jwtTokenProvider.generateToken(email, role);

        List<DocumentTypeDTO> documentTypes = webTestClient.get()
                .uri("/parametric/document-types")
                .header("Authorization", "Bearer " + token)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(DocumentTypeDTO.class)
                .returnResult()
                .getResponseBody();

        assertNotNull(documentTypes);
        assertFalse(documentTypes.isEmpty());
        assertEquals(6, documentTypes.size(), "Should have 6 document types from init-parameters.sql");
        
        // Verify Colombian document types
        assertTrue(documentTypes.stream().anyMatch(d -> d.getName().equals("CC")));
        assertTrue(documentTypes.stream().anyMatch(d -> d.getName().equals("TI")));
        assertTrue(documentTypes.stream().anyMatch(d -> d.getName().equals("CE")));
        assertTrue(documentTypes.stream().anyMatch(d -> d.getName().equals("Pasaporte")));
    }

    @Test
    @DisplayName("GET /parametric/document-types - Unauthorized without token")
    void getAllDocumentTypes_withoutToken_shouldReturn401() {
        webTestClient.get()
                .uri("/parametric/document-types")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isUnauthorized();
    }

    // ==========================================
    // GET Document Type by ID Tests
    // ==========================================

    @ParameterizedTest
    @MethodSource("authenticatedRolesProvider")
    @DisplayName("GET /parametric/document-types/{id} - Should retrieve CC document type")
    void getDocumentTypeById_cc_shouldSucceed(String email, String role) {
        String token = jwtTokenProvider.generateToken(email, role);
        Long documentTypeId = 1L;

        String documentTypeName = webTestClient.get()
                .uri("/parametric/document-types/{id}", documentTypeId)
                .header("Authorization", "Bearer " + token)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class)
                .returnResult()
                .getResponseBody();

        assertNotNull(documentTypeName);
        assertEquals("CC", documentTypeName);
    }

    // ==========================================
    // GET All Employment Types Tests
    // ==========================================

    @ParameterizedTest
    @MethodSource("authenticatedRolesProvider")
    @DisplayName("GET /parametric/employment-types - Authenticated users can retrieve all employment types")
    void getAllEmploymentTypes_asAuthenticatedUser_shouldSucceed(String email, String role) {
        String token = jwtTokenProvider.generateToken(email, role);

        List<EmploymentTypeDTO> employmentTypes = webTestClient.get()
                .uri("/parametric/employment-types")
                .header("Authorization", "Bearer " + token)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(EmploymentTypeDTO.class)
                .returnResult()
                .getResponseBody();

        assertNotNull(employmentTypes);
        assertFalse(employmentTypes.isEmpty());
        assertEquals(2, employmentTypes.size(), "Should have 2 employment types from init-parameters.sql");
        
        // Verify employment types
        assertTrue(employmentTypes.stream().anyMatch(e -> e.getName().equals("Tiempo Completo")));
        assertTrue(employmentTypes.stream().anyMatch(e -> e.getName().equals("Medio Tiempo")));
    }

    @Test
    @DisplayName("GET /parametric/employment-types - Unauthorized without token")
    void getAllEmploymentTypes_withoutToken_shouldReturn401() {
        webTestClient.get()
                .uri("/parametric/employment-types")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isUnauthorized();
    }

    // ==========================================
    // GET Employment Type by ID Tests
    // ==========================================

    @ParameterizedTest
    @MethodSource("authenticatedRolesProvider")
    @DisplayName("GET /parametric/employment-types/{id} - Should retrieve Tiempo Completo")
    void getEmploymentTypeById_fullTime_shouldSucceed(String email, String role) {
        String token = jwtTokenProvider.generateToken(email, role);
        Long employmentTypeId = 1L;

        String employmentTypeName = webTestClient.get()
                .uri("/parametric/employment-types/{id}", employmentTypeId)
                .header("Authorization", "Bearer " + token)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class)
                .returnResult()
                .getResponseBody();

        assertNotNull(employmentTypeName);
        assertEquals("Tiempo Completo", employmentTypeName);
    }

    @ParameterizedTest
    @MethodSource("authenticatedRolesProvider")
    @DisplayName("GET /parametric/employment-types/{id} - Should retrieve Medio Tiempo")
    void getEmploymentTypeById_partTime_shouldSucceed(String email, String role) {
        String token = jwtTokenProvider.generateToken(email, role);
        Long employmentTypeId = 2L;

        String employmentTypeName = webTestClient.get()
                .uri("/parametric/employment-types/{id}", employmentTypeId)
                .header("Authorization", "Bearer " + token)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class)
                .returnResult()
                .getResponseBody();

        assertNotNull(employmentTypeName);
        assertEquals("Medio Tiempo", employmentTypeName);
    }

    // ==========================================
    // GET All Modalities Tests
    // ==========================================

    @ParameterizedTest
    @MethodSource("authenticatedRolesProvider")
    @DisplayName("GET /parametric/modalities - Authenticated users can retrieve all modalities")
    void getAllModalities_asAuthenticatedUser_shouldSucceed(String email, String role) {
        String token = jwtTokenProvider.generateToken(email, role);

        List<ModalityDTO> modalities = webTestClient.get()
                .uri("/parametric/modalities")
                .header("Authorization", "Bearer " + token)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(ModalityDTO.class)
                .returnResult()
                .getResponseBody();

        assertNotNull(modalities);
        assertFalse(modalities.isEmpty());
        assertEquals(2, modalities.size(), "Should have 2 modalities from init-parameters.sql");
        
        // Verify modalities
        assertTrue(modalities.stream().anyMatch(m -> m.getName().equals("Presencial")));
        assertTrue(modalities.stream().anyMatch(m -> m.getName().equals("Online")));
    }

    @Test
    @DisplayName("GET /parametric/modalities - Unauthorized without token")
    void getAllModalities_withoutToken_shouldReturn401() {
        webTestClient.get()
                .uri("/parametric/modalities")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isUnauthorized();
    }

    // ==========================================
    // GET Modality by ID Tests
    // ==========================================

    @ParameterizedTest
    @MethodSource("authenticatedRolesProvider")
    @DisplayName("GET /parametric/modalities/{id} - Should retrieve Presencial modality")
    void getModalityById_presencial_shouldSucceed(String email, String role) {
        String token = jwtTokenProvider.generateToken(email, role);
        Long modalityId = 1L;

        String modalityName = webTestClient.get()
                .uri("/parametric/modalities/{id}", modalityId)
                .header("Authorization", "Bearer " + token)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class)
                .returnResult()
                .getResponseBody();

        assertNotNull(modalityName);
        assertEquals("Presencial", modalityName);
    }

    @ParameterizedTest
    @MethodSource("authenticatedRolesProvider")
    @DisplayName("GET /parametric/modalities/{id} - Should retrieve Online modality")
    void getModalityById_online_shouldSucceed(String email, String role) {
        String token = jwtTokenProvider.generateToken(email, role);
        Long modalityId = 2L;

        String modalityName = webTestClient.get()
                .uri("/parametric/modalities/{id}", modalityId)
                .header("Authorization", "Bearer " + token)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class)
                .returnResult()
                .getResponseBody();

        assertNotNull(modalityName);
        assertEquals("Online", modalityName);
    }

    // ==========================================
    // GET All Classroom Types Tests
    // ==========================================

    @ParameterizedTest
    @MethodSource("authenticatedRolesProvider")
    @DisplayName("GET /parametric/classroom-types - Authenticated users can retrieve all classroom types")
    void getAllClassroomTypes_asAuthenticatedUser_shouldSucceed(String email, String role) {
        String token = jwtTokenProvider.generateToken(email, role);

        List<ClassroomTypeDTO> classroomTypes = webTestClient.get()
                .uri("/parametric/classroom-types")
                .header("Authorization", "Bearer " + token)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(ClassroomTypeDTO.class)
                .returnResult()
                .getResponseBody();

        assertNotNull(classroomTypes);
        assertFalse(classroomTypes.isEmpty());
        assertEquals(4, classroomTypes.size(), "Should have 4 classroom types from init-parameters.sql");
        
        // Verify classroom types
        assertTrue(classroomTypes.stream().anyMatch(c -> c.getName().equals("Aula")));
        assertTrue(classroomTypes.stream().anyMatch(c -> c.getName().equals("Laboratorio")));
        assertTrue(classroomTypes.stream().anyMatch(c -> c.getName().equals("Aula Movil")));
        assertTrue(classroomTypes.stream().anyMatch(c -> c.getName().equals("Auditorio")));
    }

    @Test
    @DisplayName("GET /parametric/classroom-types - Unauthorized without token")
    void getAllClassroomTypes_withoutToken_shouldReturn401() {
        webTestClient.get()
                .uri("/parametric/classroom-types")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isUnauthorized();
    }

    // ==========================================
    // GET Classroom Type by ID Tests
    // ==========================================

    @ParameterizedTest
    @MethodSource("authenticatedRolesProvider")
    @DisplayName("GET /parametric/classroom-types/{id} - Should retrieve Aula classroom type")
    void getClassroomTypeById_aula_shouldSucceed(String email, String role) {
        String token = jwtTokenProvider.generateToken(email, role);
        Long classroomTypeId = 1L;

        String classroomTypeName = webTestClient.get()
                .uri("/parametric/classroom-types/{id}", classroomTypeId)
                .header("Authorization", "Bearer " + token)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class)
                .returnResult()
                .getResponseBody();

        assertNotNull(classroomTypeName);
        assertEquals("Aula", classroomTypeName);
    }

    @ParameterizedTest
    @MethodSource("authenticatedRolesProvider")
    @DisplayName("GET /parametric/classroom-types/{id} - Should retrieve Laboratorio classroom type")
    void getClassroomTypeById_laboratorio_shouldSucceed(String email, String role) {
        String token = jwtTokenProvider.generateToken(email, role);
        Long classroomTypeId = 2L;

        String classroomTypeName = webTestClient.get()
                .uri("/parametric/classroom-types/{id}", classroomTypeId)
                .header("Authorization", "Bearer " + token)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class)
                .returnResult()
                .getResponseBody();

        assertNotNull(classroomTypeName);
        assertEquals("Laboratorio", classroomTypeName);
    }

    // ==========================================
    // Business Logic Tests
    // ==========================================

    @Test
    @DisplayName("All parametric lists should have expected counts from init-parameters.sql")
    void allParametricLists_shouldHaveExpectedCounts() {
        String token = jwtTokenProvider.generateToken("testAdmin@example.com", "ROLE_ADMIN");

        // Statuses: 14
        List<StatusDTO> statuses = webTestClient.get()
                .uri("/parametric/statuses")
                .header("Authorization", "Bearer " + token)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectBodyList(StatusDTO.class)
                .returnResult()
                .getResponseBody();
        assertNotNull(statuses);
        assertEquals(14, statuses.size());

        // Roles: 5
        List<RoleDTO> roles = webTestClient.get()
                .uri("/parametric/roles")
                .header("Authorization", "Bearer " + token)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectBodyList(RoleDTO.class)
                .returnResult()
                .getResponseBody();
        assertNotNull(roles);
        assertEquals(5, roles.size());

        // Document Types: 6
        List<DocumentTypeDTO> documentTypes = webTestClient.get()
                .uri("/parametric/document-types")
                .header("Authorization", "Bearer " + token)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectBodyList(DocumentTypeDTO.class)
                .returnResult()
                .getResponseBody();
        assertNotNull(documentTypes);
        assertEquals(6, documentTypes.size());

        // Employment Types: 2
        List<EmploymentTypeDTO> employmentTypes = webTestClient.get()
                .uri("/parametric/employment-types")
                .header("Authorization", "Bearer " + token)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectBodyList(EmploymentTypeDTO.class)
                .returnResult()
                .getResponseBody();
        assertNotNull(employmentTypes);
        assertEquals(2, employmentTypes.size());

        // Modalities: 2
        List<ModalityDTO> modalities = webTestClient.get()
                .uri("/parametric/modalities")
                .header("Authorization", "Bearer " + token)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectBodyList(ModalityDTO.class)
                .returnResult()
                .getResponseBody();
        assertNotNull(modalities);
        assertEquals(2, modalities.size());

        // Classroom Types: 4
        List<ClassroomTypeDTO> classroomTypes = webTestClient.get()
                .uri("/parametric/classroom-types")
                .header("Authorization", "Bearer " + token)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectBodyList(ClassroomTypeDTO.class)
                .returnResult()
                .getResponseBody();
        assertNotNull(classroomTypes);
        assertEquals(4, classroomTypes.size());
    }

    @Test
    @DisplayName("All parametric entities should have unique IDs")
    void allParametricEntities_shouldHaveUniqueIds() {
        String token = jwtTokenProvider.generateToken("testAdmin@example.com", "ROLE_ADMIN");

        // Check statuses have unique IDs
        List<StatusDTO> statuses = webTestClient.get()
                .uri("/parametric/statuses")
                .header("Authorization", "Bearer " + token)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectBodyList(StatusDTO.class)
                .returnResult()
                .getResponseBody();
        
        assertNotNull(statuses);
        long uniqueStatusIds = statuses.stream().map(StatusDTO::getId).distinct().count();
        assertEquals(statuses.size(), uniqueStatusIds, "All status IDs should be unique");

        // Check roles have unique IDs
        List<RoleDTO> roles = webTestClient.get()
                .uri("/parametric/roles")
                .header("Authorization", "Bearer " + token)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectBodyList(RoleDTO.class)
                .returnResult()
                .getResponseBody();
        
        assertNotNull(roles);
        long uniqueRoleIds = roles.stream().map(RoleDTO::getId).distinct().count();
        assertEquals(roles.size(), uniqueRoleIds, "All role IDs should be unique");
    }

    @Test
    @DisplayName("Critical statuses should exist for application workflows")
    void criticalStatuses_shouldExist() {
        String token = jwtTokenProvider.generateToken("testAdmin@example.com", "ROLE_ADMIN");

        List<StatusDTO> statuses = webTestClient.get()
                .uri("/parametric/statuses")
                .header("Authorization", "Bearer " + token)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectBodyList(StatusDTO.class)
                .returnResult()
                .getResponseBody();

        assertNotNull(statuses);
        
        // Verify critical statuses for workflows
        assertTrue(statuses.stream().anyMatch(s -> s.getId().equals(1L) && s.getName().equals("Active")),
                "Active status (ID 1) should exist");
        assertTrue(statuses.stream().anyMatch(s -> s.getId().equals(4L) && s.getName().equals("Pending")),
                "Pending status (ID 4) should exist");
        assertTrue(statuses.stream().anyMatch(s -> s.getId().equals(6L) && s.getName().equals("In Progress")),
                "In Progress status (ID 6) should exist");
        assertTrue(statuses.stream().anyMatch(s -> s.getId().equals(8L) && s.getName().equals("Confirmed")),
                "Confirmed status (ID 8) should exist");
        assertTrue(statuses.stream().anyMatch(s -> s.getId().equals(9L) && s.getName().equals("Rejected")),
                "Rejected status (ID 9) should exist");
    }

    @Test
    @DisplayName("All application roles should exist")
    void allApplicationRoles_shouldExist() {
        String token = jwtTokenProvider.generateToken("testAdmin@example.com", "ROLE_ADMIN");

        List<RoleDTO> roles = webTestClient.get()
                .uri("/parametric/roles")
                .header("Authorization", "Bearer " + token)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectBodyList(RoleDTO.class)
                .returnResult()
                .getResponseBody();

        assertNotNull(roles);
        
        // Verify all required roles exist
        assertTrue(roles.stream().anyMatch(r -> r.getId().equals(1L) && r.getName().equals("ROLE_ADMIN")),
                "ROLE_ADMIN (ID 1) should exist");
        assertTrue(roles.stream().anyMatch(r -> r.getId().equals(2L) && r.getName().equals("ROLE_USER")),
                "ROLE_USER (ID 2) should exist");
        assertTrue(roles.stream().anyMatch(r -> r.getId().equals(3L) && r.getName().equals("ROLE_STUDENT")),
                "ROLE_STUDENT (ID 3) should exist");
        assertTrue(roles.stream().anyMatch(r -> r.getId().equals(4L) && r.getName().equals("ROLE_TEACHER")),
                "ROLE_TEACHER (ID 4) should exist");
        assertTrue(roles.stream().anyMatch(r -> r.getId().equals(5L) && r.getName().equals("ROLE_PROGRAM")),
                "ROLE_PROGRAM (ID 5) should exist");
    }

    @Test
    @DisplayName("Colombian document types should exist")
    void colombianDocumentTypes_shouldExist() {
        String token = jwtTokenProvider.generateToken("testAdmin@example.com", "ROLE_ADMIN");

        List<DocumentTypeDTO> documentTypes = webTestClient.get()
                .uri("/parametric/document-types")
                .header("Authorization", "Bearer " + token)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectBodyList(DocumentTypeDTO.class)
                .returnResult()
                .getResponseBody();

        assertNotNull(documentTypes);
        
        // Verify Colombian document types
        assertTrue(documentTypes.stream().anyMatch(d -> d.getName().equals("CC")),
                "CC (Cédula de Ciudadanía) should exist");
        assertTrue(documentTypes.stream().anyMatch(d -> d.getName().equals("TI")),
                "TI (Tarjeta de Identidad) should exist");
        assertTrue(documentTypes.stream().anyMatch(d -> d.getName().equals("CE")),
                "CE (Cédula de Extranjería) should exist");
        assertTrue(documentTypes.stream().anyMatch(d -> d.getName().equals("NIT")),
                "NIT should exist");
    }
}
