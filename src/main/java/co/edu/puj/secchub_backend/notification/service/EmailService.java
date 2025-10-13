package co.edu.puj.secchub_backend.notification.service;

import java.util.List;

import org.modelmapper.ModelMapper;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import co.edu.puj.secchub_backend.notification.dto.EmailSendRequestDTO;
import co.edu.puj.secchub_backend.notification.dto.EmailTemplateRequestDTO;
import co.edu.puj.secchub_backend.notification.dto.EmailTemplateResponseDTO;
import co.edu.puj.secchub_backend.notification.exception.EmailSendingException;
import co.edu.puj.secchub_backend.notification.exception.EmailTemplateNotFoundException;
import co.edu.puj.secchub_backend.notification.model.EmailTemplate;
import co.edu.puj.secchub_backend.notification.repository.EmailTemplateRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

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
    public List<EmailTemplateResponseDTO> getAllEmailTemplates() {
        return emailTemplateRepository.findAll()
                .stream()
                .map(emailTemplate -> new EmailTemplateResponseDTO(
                        emailTemplate.getId(),
                        emailTemplate.getName(),
                        emailTemplate.getSubject(),
                        emailTemplate.getBody()))
                .toList();
    }

    /**
     * Sends an email using the provided details.
     * @param emailSendRequestDTO containing email details
     * @throws EmailSendingException if sending the email fails
     */
    public void sendEmail(EmailSendRequestDTO emailSendRequestDTO) {
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
    }

    /**
     * Creates a new email template.
     * @param emailTemplateRequestDTO DTO with template information
     * @return Created template DTO
     */
    @Transactional
    public EmailTemplateResponseDTO createEmailTemplate(EmailTemplateRequestDTO emailTemplateRequestDTO) {
        EmailTemplate emailTemplate = modelMapper.map(emailTemplateRequestDTO, EmailTemplate.class);
        EmailTemplate savedTemplate = emailTemplateRepository.save(emailTemplate);
        log.info("Email template created with ID: {}", savedTemplate.getId());
        return modelMapper.map(savedTemplate, EmailTemplateResponseDTO.class);
    }

    /**
     * Gets an email template by ID.
     * @param templateId Template ID
     * @return Email template found
     */
    public EmailTemplateResponseDTO getEmailTemplateById(Long templateId) {
        EmailTemplate template = emailTemplateRepository.findById(templateId)
                .orElseThrow(() -> new EmailTemplateNotFoundException("Email template for consult not found for ID: " + templateId));
        return modelMapper.map(template, EmailTemplateResponseDTO.class);
    }

    /**
     * Gets an email template by name.
     * @param templateName Template name
     * @return Email template found
     */
    public EmailTemplateResponseDTO getEmailTemplateByName(String templateName) {
        EmailTemplate template = emailTemplateRepository.findByName(templateName)
                .orElseThrow(() -> new EmailTemplateNotFoundException("Email template for consult not found for name: " + templateName));
        return modelMapper.map(template, EmailTemplateResponseDTO.class);
    }

    /**
     * Updates an email template.
     * @param templateId Template ID
     * @param emailTemplateRequestDTO DTO with updated data
     * @return Updated template
     */
    @Transactional
    public EmailTemplateResponseDTO updateEmailTemplate(Long templateId, EmailTemplateRequestDTO emailTemplateRequestDTO) {
        EmailTemplate template = emailTemplateRepository.findById(templateId)
                .orElseThrow(() -> new EmailTemplateNotFoundException("Email template for update not found for ID: " + templateId));

        modelMapper.getConfiguration().setPropertyCondition(context -> 
            context.getSource() != null);
        modelMapper.map(emailTemplateRequestDTO, template);
        
        EmailTemplate savedTemplate = emailTemplateRepository.save(template);
        log.info("Email template updated with ID: {}", savedTemplate.getId());
        return modelMapper.map(savedTemplate, EmailTemplateResponseDTO.class);
    }

    /**
     * Deletes an email template by ID.
     * @param templateId Template ID
     */
    @Transactional
    public void deleteEmailTemplate(Long templateId) {
        if (!emailTemplateRepository.existsById(templateId)) {
            throw new EmailTemplateNotFoundException("Email template for deletion not found for ID: " + templateId);
        }
        emailTemplateRepository.deleteById(templateId);
        log.info("Email template deleted with ID: {}", templateId);
    }
}
