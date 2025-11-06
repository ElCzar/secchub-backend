package co.edu.puj.secchub_backend.notification.controller;

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
import co.edu.puj.secchub_backend.notification.dto.EmailSendRequestDTO;
import co.edu.puj.secchub_backend.notification.dto.EmailTemplateRequestDTO;
import co.edu.puj.secchub_backend.notification.dto.EmailTemplateResponseDTO;
import co.edu.puj.secchub_backend.security.jwt.JwtTokenProvider;
import io.r2dbc.spi.ConnectionFactory;

@SpringBootTest
@AutoConfigureWebTestClient
@Testcontainers
@DisplayName("Email Controller Integration Tests")
class EmailControllerIntegrationTest extends DatabaseContainerIntegration {
    
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
                "/test-email-templates.sql"
        );
    }

    // ==========================================
    // Test Data Providers
    // ==========================================
    
    /**
     * Provides admin role for creating/updating/deleting templates
     */
    private static Stream<Arguments> adminRoleProvider() {
        return Stream.of(
            Arguments.of("testAdmin@example.com", "ROLE_ADMIN")
        );
    }

    /**
     * Provides admin and user roles for viewing templates and sending emails
     */
    private static Stream<Arguments> adminAndUserRolesProvider() {
        return Stream.of(
            Arguments.of("testAdmin@example.com", "ROLE_ADMIN"),
            Arguments.of("testUser@example.com", "ROLE_USER")
        );
    }

    /**
     * Provides non-admin/user roles that cannot access endpoints
     */
    private static Stream<Arguments> unauthorizedRolesProvider() {
        return Stream.of(
            Arguments.of("testTeacher@example.com", "ROLE_TEACHER"),
            Arguments.of("testStudent@example.com", "ROLE_STUDENT"),
            Arguments.of("testProgram@example.com", "ROLE_PROGRAM")
        );
    }

    /**
     * Provides non-admin roles that cannot create/update/delete templates
     */
    private static Stream<Arguments> nonAdminRolesProvider() {
        return Stream.of(
            Arguments.of("testUser@example.com", "ROLE_USER"),
            Arguments.of("testTeacher@example.com", "ROLE_TEACHER"),
            Arguments.of("testStudent@example.com", "ROLE_STUDENT"),
            Arguments.of("testProgram@example.com", "ROLE_PROGRAM")
        );
    }

    // ==========================================
    // POST Create Email Template Tests
    // ==========================================

    @ParameterizedTest
    @MethodSource("adminRoleProvider")
    @DisplayName("POST /emails/templates - Admin can create email template")
    void createEmailTemplate_asAdmin_shouldSucceed(String email, String role) {
        String token = jwtTokenProvider.generateToken(email, role);

        EmailTemplateRequestDTO request = EmailTemplateRequestDTO.builder()
                .name("test_template")
                .subject("Test Subject")
                .body("Test body content with {placeholder}")
                .build();

        webTestClient.post()
                .uri("/emails/templates")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isCreated()
                .expectBody(EmailTemplateResponseDTO.class)
                .value(response -> {
                    assertNotNull(response.getId());
                    assertEquals("test_template", response.getName());
                    assertEquals("Test Subject", response.getSubject());
                    assertEquals("Test body content with {placeholder}", response.getBody());
                });
    }

    @ParameterizedTest
    @MethodSource("nonAdminRolesProvider")
    @DisplayName("POST /emails/templates - Non-admin cannot create template")
    void createEmailTemplate_asNonAdmin_shouldReturn403(String email, String role) {
        String token = jwtTokenProvider.generateToken(email, role);

        EmailTemplateRequestDTO request = EmailTemplateRequestDTO.builder()
                .name("test_template")
                .subject("Test Subject")
                .body("Test body")
                .build();

        webTestClient.post()
                .uri("/emails/templates")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isForbidden();
    }

    @Test
    @DisplayName("POST /emails/templates - Unauthorized without token")
    void createEmailTemplate_withoutToken_shouldReturn401() {
        EmailTemplateRequestDTO request = EmailTemplateRequestDTO.builder()
                .name("test_template")
                .subject("Test Subject")
                .body("Test body")
                .build();

        webTestClient.post()
                .uri("/emails/templates")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isUnauthorized();
    }

    // ==========================================
    // GET All Email Templates Tests
    // ==========================================

    @ParameterizedTest
    @MethodSource("adminAndUserRolesProvider")
    @DisplayName("GET /emails/templates - Admin/User can retrieve all templates")
    void getAllEmailTemplates_asAdminOrUser_shouldSucceed(String email, String role) {
        String token = jwtTokenProvider.generateToken(email, role);

        List<EmailTemplateResponseDTO> templates = webTestClient.get()
                .uri("/emails/templates")
                .header("Authorization", "Bearer " + token)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(EmailTemplateResponseDTO.class)
                .returnResult()
                .getResponseBody();

        assertNotNull(templates);
        assertFalse(templates.isEmpty());
        assertEquals(3, templates.size(), "Should have 3 test templates");
    }

    @ParameterizedTest
    @MethodSource("unauthorizedRolesProvider")
    @DisplayName("GET /emails/templates - Unauthorized roles cannot access")
    void getAllEmailTemplates_asUnauthorizedRole_shouldReturn403(String email, String role) {
        String token = jwtTokenProvider.generateToken(email, role);

        webTestClient.get()
                .uri("/emails/templates")
                .header("Authorization", "Bearer " + token)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isForbidden();
    }

    @Test
    @DisplayName("GET /emails/templates - Unauthorized without token")
    void getAllEmailTemplates_withoutToken_shouldReturn401() {
        webTestClient.get()
                .uri("/emails/templates")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isUnauthorized();
    }

    // ==========================================
    // GET Email Template by ID Tests
    // ==========================================

    @ParameterizedTest
    @MethodSource("adminAndUserRolesProvider")
    @DisplayName("GET /emails/templates/{id} - Should retrieve welcome template by ID")
    void getEmailTemplateById_welcomeTemplate_shouldSucceed(String email, String role) {
        String token = jwtTokenProvider.generateToken(email, role);
        Long templateId = 1L;

        EmailTemplateResponseDTO template = webTestClient.get()
                .uri("/emails/templates/{templateId}", templateId)
                .header("Authorization", "Bearer " + token)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody(EmailTemplateResponseDTO.class)
                .returnResult()
                .getResponseBody();

        assertNotNull(template);
        assertEquals(templateId, template.getId());
        assertEquals("welcome_email", template.getName());
        assertEquals("Welcome to SecHub", template.getSubject());
        assertTrue(template.getBody().contains("secchubnoreply@gmail.com"));
    }

    @ParameterizedTest
    @MethodSource("adminAndUserRolesProvider")
    @DisplayName("GET /emails/templates/{id} - Should retrieve password reset template")
    void getEmailTemplateById_passwordResetTemplate_shouldSucceed(String email, String role) {
        String token = jwtTokenProvider.generateToken(email, role);
        Long templateId = 2L;

        EmailTemplateResponseDTO template = webTestClient.get()
                .uri("/emails/templates/{templateId}", templateId)
                .header("Authorization", "Bearer " + token)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody(EmailTemplateResponseDTO.class)
                .returnResult()
                .getResponseBody();

        assertNotNull(template);
        assertEquals(templateId, template.getId());
        assertEquals("password_reset", template.getName());
        assertTrue(template.getSubject().contains("Password Reset"));
        assertTrue(template.getBody().contains("{reset_link}"));
    }

    @ParameterizedTest
    @MethodSource("adminAndUserRolesProvider")
    @DisplayName("GET /emails/templates/{id} - Should retrieve class assignment template")
    void getEmailTemplateById_classAssignmentTemplate_shouldSucceed(String email, String role) {
        String token = jwtTokenProvider.generateToken(email, role);
        Long templateId = 3L;

        EmailTemplateResponseDTO template = webTestClient.get()
                .uri("/emails/templates/{templateId}", templateId)
                .header("Authorization", "Bearer " + token)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody(EmailTemplateResponseDTO.class)
                .returnResult()
                .getResponseBody();

        assertNotNull(template);
        assertEquals(templateId, template.getId());
        assertEquals("class_assignment", template.getName());
        assertTrue(template.getSubject().contains("{course_name}"));
        assertTrue(template.getBody().contains("{teacher_name}"));
    }

    @ParameterizedTest
    @MethodSource("unauthorizedRolesProvider")
    @DisplayName("GET /emails/templates/{id} - Unauthorized roles cannot access")
    void getEmailTemplateById_asUnauthorizedRole_shouldReturn403(String email, String role) {
        String token = jwtTokenProvider.generateToken(email, role);

        webTestClient.get()
                .uri("/emails/templates/{templateId}", 1L)
                .header("Authorization", "Bearer " + token)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isForbidden();
    }

    // ==========================================
    // GET Email Template by Name Tests
    // ==========================================

    @ParameterizedTest
    @MethodSource("adminAndUserRolesProvider")
    @DisplayName("GET /emails/templates/name/{name} - Should retrieve by welcome_email name")
    void getEmailTemplateByName_welcomeEmail_shouldSucceed(String email, String role) {
        String token = jwtTokenProvider.generateToken(email, role);
        String templateName = "welcome_email";

        EmailTemplateResponseDTO template = webTestClient.get()
                .uri("/emails/templates/name/{templateName}", templateName)
                .header("Authorization", "Bearer " + token)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody(EmailTemplateResponseDTO.class)
                .returnResult()
                .getResponseBody();

        assertNotNull(template);
        assertEquals(templateName, template.getName());
        assertEquals("Welcome to SecHub", template.getSubject());
    }

    @ParameterizedTest
    @MethodSource("adminAndUserRolesProvider")
    @DisplayName("GET /emails/templates/name/{name} - Should retrieve by password_reset name")
    void getEmailTemplateByName_passwordReset_shouldSucceed(String email, String role) {
        String token = jwtTokenProvider.generateToken(email, role);
        String templateName = "password_reset";

        EmailTemplateResponseDTO template = webTestClient.get()
                .uri("/emails/templates/name/{templateName}", templateName)
                .header("Authorization", "Bearer " + token)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody(EmailTemplateResponseDTO.class)
                .returnResult()
                .getResponseBody();

        assertNotNull(template);
        assertEquals(templateName, template.getName());
        assertTrue(template.getSubject().contains("Password Reset"));
    }

    @ParameterizedTest
    @MethodSource("unauthorizedRolesProvider")
    @DisplayName("GET /emails/templates/name/{name} - Unauthorized roles cannot access")
    void getEmailTemplateByName_asUnauthorizedRole_shouldReturn403(String email, String role) {
        String token = jwtTokenProvider.generateToken(email, role);

        webTestClient.get()
                .uri("/emails/templates/name/{templateName}", "welcome_email")
                .header("Authorization", "Bearer " + token)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isForbidden();
    }

    // ==========================================
    // PUT Update Email Template Tests
    // ==========================================

    @ParameterizedTest
    @MethodSource("adminRoleProvider")
    @DisplayName("PUT /emails/templates/{id} - Admin can update template")
    void updateEmailTemplate_asAdmin_shouldSucceed(String email, String role) {
        String token = jwtTokenProvider.generateToken(email, role);
        Long templateId = 1L;

        EmailTemplateRequestDTO updateRequest = EmailTemplateRequestDTO.builder()
                .name("welcome_email_updated")
                .subject("Updated Welcome Subject")
                .body("Updated body content")
                .build();

        EmailTemplateResponseDTO updated = webTestClient.put()
                .uri("/emails/templates/{templateId}", templateId)
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(updateRequest)
                .exchange()
                .expectStatus().isOk()
                .expectBody(EmailTemplateResponseDTO.class)
                .returnResult()
                .getResponseBody();

        assertNotNull(updated);
        assertEquals(templateId, updated.getId());
        assertEquals("welcome_email_updated", updated.getName());
        assertEquals("Updated Welcome Subject", updated.getSubject());
        assertEquals("Updated body content", updated.getBody());
    }

    @ParameterizedTest
    @MethodSource("nonAdminRolesProvider")
    @DisplayName("PUT /emails/templates/{id} - Non-admin cannot update template")
    void updateEmailTemplate_asNonAdmin_shouldReturn403(String email, String role) {
        String token = jwtTokenProvider.generateToken(email, role);

        EmailTemplateRequestDTO updateRequest = EmailTemplateRequestDTO.builder()
                .name("updated_name")
                .subject("Updated Subject")
                .body("Updated body")
                .build();

        webTestClient.put()
                .uri("/emails/templates/{templateId}", 1L)
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(updateRequest)
                .exchange()
                .expectStatus().isForbidden();
    }

    // ==========================================
    // DELETE Email Template Tests
    // ==========================================

    @ParameterizedTest
    @MethodSource("adminRoleProvider")
    @DisplayName("DELETE /emails/templates/{id} - Admin can delete template")
    void deleteEmailTemplate_asAdmin_shouldSucceed(String email, String role) {
        String token = jwtTokenProvider.generateToken(email, role);
        Long templateId = 1L;

        // Delete the template
        webTestClient.delete()
                .uri("/emails/templates/{templateId}", templateId)
                .header("Authorization", "Bearer " + token)
                .exchange()
                .expectStatus().isOk();

        // Verify it's deleted by trying to retrieve it
        webTestClient.get()
                .uri("/emails/templates/{templateId}", templateId)
                .header("Authorization", "Bearer " + token)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isNotFound();
    }

    @ParameterizedTest
    @MethodSource("nonAdminRolesProvider")
    @DisplayName("DELETE /emails/templates/{id} - Non-admin cannot delete template")
    void deleteEmailTemplate_asNonAdmin_shouldReturn403(String email, String role) {
        String token = jwtTokenProvider.generateToken(email, role);

        webTestClient.delete()
                .uri("/emails/templates/{templateId}", 1L)
                .header("Authorization", "Bearer " + token)
                .exchange()
                .expectStatus().isForbidden();
    }

    // ==========================================
    // POST Send Email Tests
    // ==========================================

    @ParameterizedTest
    @MethodSource("unauthorizedRolesProvider")
    @DisplayName("POST /emails/send - Unauthorized roles cannot send email")
    void sendEmail_asUnauthorizedRole_shouldReturn403(String email, String role) {
        String token = jwtTokenProvider.generateToken(email, role);

        EmailSendRequestDTO sendRequest = EmailSendRequestDTO.builder()
                .to("recipient@example.com")
                .subject("Test Email")
                .body("Test body")
                .build();

        webTestClient.post()
                .uri("/emails/send")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(sendRequest)
                .exchange()
                .expectStatus().isForbidden();
    }

    @Test
    @DisplayName("POST /emails/send - Unauthorized without token")
    void sendEmail_withoutToken_shouldReturn401() {
        EmailSendRequestDTO sendRequest = EmailSendRequestDTO.builder()
                .to("recipient@example.com")
                .subject("Test Email")
                .body("Test body")
                .build();

        webTestClient.post()
                .uri("/emails/send")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(sendRequest)
                .exchange()
                .expectStatus().isUnauthorized();
    }

    // ==========================================
    // Business Logic Tests
    // ==========================================

    @Test
    @DisplayName("All templates should have unique names")
    void emailTemplates_shouldHaveUniqueNames() {
        String token = jwtTokenProvider.generateToken("testAdmin@example.com", "ROLE_ADMIN");

        List<EmailTemplateResponseDTO> templates = webTestClient.get()
                .uri("/emails/templates")
                .header("Authorization", "Bearer " + token)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectBodyList(EmailTemplateResponseDTO.class)
                .returnResult()
                .getResponseBody();

        assertNotNull(templates);
        long uniqueNames = templates.stream()
                .map(EmailTemplateResponseDTO::getName)
                .distinct()
                .count();

        assertEquals(templates.size(), uniqueNames, "All template names should be unique");
    }

    @Test
    @DisplayName("All templates should contain sender email")
    void emailTemplates_shouldContainSenderEmail() {
        String token = jwtTokenProvider.generateToken("testAdmin@example.com", "ROLE_ADMIN");

        List<EmailTemplateResponseDTO> templates = webTestClient.get()
                .uri("/emails/templates")
                .header("Authorization", "Bearer " + token)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectBodyList(EmailTemplateResponseDTO.class)
                .returnResult()
                .getResponseBody();

        assertNotNull(templates);
        assertTrue(templates.stream()
                .allMatch(t -> t.getBody().contains("secchubnoreply@gmail.com")),
                "All templates should contain sender email");
    }

    @Test
    @DisplayName("Template retrieval by ID and name should return same template")
    void getTemplateByIdAndName_shouldReturnSameTemplate() {
        String token = jwtTokenProvider.generateToken("testAdmin@example.com", "ROLE_ADMIN");
        Long templateId = 1L;
        String templateName = "welcome_email";

        // Get by ID
        EmailTemplateResponseDTO byId = webTestClient.get()
                .uri("/emails/templates/{templateId}", templateId)
                .header("Authorization", "Bearer " + token)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectBody(EmailTemplateResponseDTO.class)
                .returnResult()
                .getResponseBody();

        // Get by name
        EmailTemplateResponseDTO byName = webTestClient.get()
                .uri("/emails/templates/name/{templateName}", templateName)
                .header("Authorization", "Bearer " + token)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectBody(EmailTemplateResponseDTO.class)
                .returnResult()
                .getResponseBody();

        assertNotNull(byId);
        assertNotNull(byName);
        assertEquals(byId.getId(), byName.getId());
        assertEquals(byId.getName(), byName.getName());
        assertEquals(byId.getSubject(), byName.getSubject());
        assertEquals(byId.getBody(), byName.getBody());
    }

    @Test
    @DisplayName("Templates should contain expected placeholders")
    void emailTemplates_shouldContainPlaceholders() {
        String token = jwtTokenProvider.generateToken("testAdmin@example.com", "ROLE_ADMIN");

        // Welcome template should have {name} and {email} placeholders
        EmailTemplateResponseDTO welcomeTemplate = webTestClient.get()
                .uri("/emails/templates/name/{templateName}", "welcome_email")
                .header("Authorization", "Bearer " + token)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectBody(EmailTemplateResponseDTO.class)
                .returnResult()
                .getResponseBody();

        assertNotNull(welcomeTemplate);
        assertTrue(welcomeTemplate.getBody().contains("{name}"));
        assertTrue(welcomeTemplate.getBody().contains("{email}"));

        // Password reset template should have {reset_link} placeholder
        EmailTemplateResponseDTO resetTemplate = webTestClient.get()
                .uri("/emails/templates/name/{templateName}", "password_reset")
                .header("Authorization", "Bearer " + token)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectBody(EmailTemplateResponseDTO.class)
                .returnResult()
                .getResponseBody();

        assertNotNull(resetTemplate);
        assertTrue(resetTemplate.getBody().contains("{reset_link}"));

        // Class assignment template should have multiple placeholders
        EmailTemplateResponseDTO assignmentTemplate = webTestClient.get()
                .uri("/emails/templates/name/{templateName}", "class_assignment")
                .header("Authorization", "Bearer " + token)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectBody(EmailTemplateResponseDTO.class)
                .returnResult()
                .getResponseBody();

        assertNotNull(assignmentTemplate);
        assertTrue(assignmentTemplate.getSubject().contains("{course_name}"));
        assertTrue(assignmentTemplate.getBody().contains("{teacher_name}"));
        assertTrue(assignmentTemplate.getBody().contains("{course_name}"));
        assertTrue(assignmentTemplate.getBody().contains("{schedule}"));
    }
}
