package co.edu.puj.secchub_backend.notification.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import co.edu.puj.secchub_backend.notification.dto.EmailSendRequestDTO;
import co.edu.puj.secchub_backend.notification.dto.EmailTemplateRequestDTO;
import co.edu.puj.secchub_backend.notification.dto.EmailTemplateResponseDTO;
import co.edu.puj.secchub_backend.notification.service.EmailService;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

/**
 * REST Controller for email-related operations.
 * Provides endpoints for managing email templates and sending emails following reactive patterns.
 */
@RestController
@RequestMapping("/api/emails")
@RequiredArgsConstructor
public class EmailController {
    private final EmailService emailService;

    /**
     * Creates a new email template.
     * @param emailTemplateRequestDTO the template data transfer object containing template information
     * @return ResponseEntity containing the created template and HTTP 201 status
     */
    @PostMapping("/templates")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public Mono<ResponseEntity<EmailTemplateResponseDTO>> createEmailTemplate(@RequestBody EmailTemplateRequestDTO emailTemplateRequestDTO) {
        return Mono.fromCallable(() -> emailService.createEmailTemplate(emailTemplateRequestDTO))
                .map(createdTemplate -> ResponseEntity.status(HttpStatus.CREATED).body(createdTemplate));
    }

    /**
     * Gets all email templates.
     * @return List of email templates
     */
    @GetMapping("/templates")
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_USER')")
    public Mono<ResponseEntity<List<EmailTemplateResponseDTO>>> getAllEmailTemplates() {
        return Mono.fromCallable(emailService::getAllEmailTemplates)
                .map(ResponseEntity::ok);
    }

    /**
     * Gets a specific email template by ID.
     * @param templateId Template ID
     * @return Email template found
     */
    @GetMapping("/templates/{templateId}")
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_USER')")
    public Mono<ResponseEntity<EmailTemplateResponseDTO>> getEmailTemplateById(@PathVariable Long templateId) {
        return Mono.fromCallable(() -> emailService.getEmailTemplateById(templateId))
                .map(ResponseEntity::ok);
    }

    /**
     * Gets a specific email template by name.
     * @param templateName Template name
     * @return Email template found
     */
    @GetMapping("/templates/name/{templateName}")
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_USER')")
    public Mono<ResponseEntity<EmailTemplateResponseDTO>> getEmailTemplateByName(@PathVariable String templateName) {
        return Mono.fromCallable(() -> emailService.getEmailTemplateByName(templateName))
                .map(ResponseEntity::ok);
    }

    /**
     * Updates an existing email template.
     * @param templateId Template ID
     * @param emailTemplateRequestDTO DTO with updated template data
     * @return Updated template
     */
    @PutMapping("/templates/{templateId}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public Mono<ResponseEntity<EmailTemplateResponseDTO>> updateEmailTemplate(
            @PathVariable Long templateId,
            @RequestBody EmailTemplateRequestDTO emailTemplateRequestDTO) {
        return Mono.fromCallable(() -> emailService.updateEmailTemplate(templateId, emailTemplateRequestDTO))
                .map(ResponseEntity::ok);
    }

    /**
     * Deletes an email template by ID.
     * @param templateId Template ID
     * @return Empty response with no content code 204
     */
    @DeleteMapping("/templates/{templateId}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public Mono<ResponseEntity<Void>> deleteEmailTemplate(@PathVariable Long templateId) {
        return Mono.fromRunnable(() -> emailService.deleteEmailTemplate(templateId))
                .then(Mono.just(ResponseEntity.noContent().<Void>build()));
    }

    /**
     * Sends an email using the provided details.
     * @param emailSendRequestDTO containing email details
     * @return A 200 OK response if the email was sent successfully
     */
    @PostMapping("/send")
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_USER')")
    public Mono<ResponseEntity<Void>> sendEmail(@RequestBody EmailSendRequestDTO emailSendRequestDTO) {
        return Mono.fromRunnable(() -> emailService.sendEmail(emailSendRequestDTO))
                .then(Mono.just(ResponseEntity.ok().<Void>build()));
    }
}
