package co.edu.puj.secchub_backend.notification.service;

import org.modelmapper.ModelMapper;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import co.edu.puj.secchub_backend.notification.dto.EmailSendRequestDTO;
import co.edu.puj.secchub_backend.notification.dto.EmailTemplateRequestDTO;
import co.edu.puj.secchub_backend.notification.dto.EmailTemplateResponseDTO;
import co.edu.puj.secchub_backend.notification.exception.EmailSendingException;
import co.edu.puj.secchub_backend.notification.exception.EmailTemplateNotFoundException;
import co.edu.puj.secchub_backend.notification.model.EmailTemplate;
import co.edu.puj.secchub_backend.notification.repository.EmailTemplateRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Service to handle email-related operations.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class EmailService {
    private final EmailTemplateRepository emailTemplateRepository;
    private final JavaMailSender javaMailSender;
    private final ModelMapper modelMapper;

    /**
     * Fetches all email templates.
     * @return A list of email templates.
     */
    public Flux<EmailTemplateResponseDTO> getAllEmailTemplates() {
        return emailTemplateRepository.findAll()
                .map(template -> modelMapper.map(template, EmailTemplateResponseDTO.class));
    }

    /**
     * Sends an email using the provided details.
     * @param emailSendRequestDTO containing email details
     * @throws EmailSendingException if sending the email fails
     */
    public Mono<Void> sendEmail(EmailSendRequestDTO emailSendRequestDTO) {
        return Mono.fromRunnable(() -> {
            try {
                SimpleMailMessage message = new SimpleMailMessage();
                message.setTo(emailSendRequestDTO.getTo());
                message.setSubject(emailSendRequestDTO.getSubject());
                message.setText(emailSendRequestDTO.getBody());
                javaMailSender.send(message);
                log.info("Email sent to {}", emailSendRequestDTO.getTo());
            } catch (Exception e) {
                log.error("Error sending email: {}", e.getMessage());
                throw new EmailSendingException("Failed to send email: " + e.getMessage());
            }
        });
    }

    /**
     * Creates a new email template.
     * @param emailTemplateRequestDTO DTO with template information
     * @return Created template DTO
     */
    public Mono<EmailTemplateResponseDTO> createEmailTemplate(EmailTemplateRequestDTO emailTemplateRequestDTO) {
        EmailTemplate emailTemplate = modelMapper.map(emailTemplateRequestDTO, EmailTemplate.class);
        return emailTemplateRepository.save(emailTemplate)
                .doOnSuccess(savedTemplate -> log.info("Email template created with ID: {}", savedTemplate.getId()))
                .map(savedTemplate -> modelMapper.map(savedTemplate, EmailTemplateResponseDTO.class));
    }

    /**
     * Gets an email template by ID.
     * @param templateId Template ID
     * @return Email template found
     */
    public Mono<EmailTemplateResponseDTO> getEmailTemplateById(Long templateId) {
        return emailTemplateRepository.findById(templateId)
                .map(template -> modelMapper.map(template, EmailTemplateResponseDTO.class))
                .switchIfEmpty(Mono.error(new EmailTemplateNotFoundException("Email template not found for ID: " + templateId)));
    }

    /**
     * Gets an email template by name.
     * @param templateName Template name
     * @return Email template found
     */
    public Mono<EmailTemplateResponseDTO> getEmailTemplateByName(String templateName) {
        return emailTemplateRepository.findByName(templateName)
                .map(template -> modelMapper.map(template, EmailTemplateResponseDTO.class))
                .switchIfEmpty(Mono.error(new EmailTemplateNotFoundException("Email template not found for name: " + templateName)));
    }

    /**
     * Updates an email template.
     * @param templateId Template ID
     * @param emailTemplateRequestDTO DTO with updated data
     * @return Updated template
     */
    public Mono<EmailTemplateResponseDTO> updateEmailTemplate(Long templateId, EmailTemplateRequestDTO emailTemplateRequestDTO) {
        return emailTemplateRepository.findById(templateId)
                .flatMap(template -> {
                    ModelMapper notNullMapper = new ModelMapper();
                    notNullMapper.getConfiguration().setPropertyCondition(context -> context.getSource() != null);
                    notNullMapper.map(emailTemplateRequestDTO, template);
                    return emailTemplateRepository.save(template);
                })
                .map(savedTemplate -> modelMapper.map(savedTemplate, EmailTemplateResponseDTO.class))
                .switchIfEmpty(Mono.error(new EmailTemplateNotFoundException("Email template for update not found for ID: " + templateId)));
    }

    /**
     * Deletes an email template by ID.
     * @param templateId Template ID
     */
    public Mono<Void> deleteEmailTemplate(Long templateId) {
        return emailTemplateRepository.findById(templateId)
                .switchIfEmpty(Mono.error(new EmailTemplateNotFoundException("Email template for deletion not found for ID: " + templateId)))
                .flatMap(emailTemplateRepository::delete);
    }
}
