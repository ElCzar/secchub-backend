package co.edu.puj.secchub_backend.notification.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.modelmapper.ModelMapper;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

import co.edu.puj.secchub_backend.notification.dto.EmailSendRequestDTO;
import co.edu.puj.secchub_backend.notification.dto.EmailTemplateRequestDTO;
import co.edu.puj.secchub_backend.notification.dto.EmailTemplateResponseDTO;
import co.edu.puj.secchub_backend.notification.exception.EmailSendingException;
import co.edu.puj.secchub_backend.notification.exception.EmailTemplateNotFoundException;
import co.edu.puj.secchub_backend.notification.model.EmailTemplate;
import co.edu.puj.secchub_backend.notification.repository.EmailTemplateRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@ExtendWith(MockitoExtension.class)
@DisplayName("EmailService Unit Test")
@SuppressWarnings("null")
class EmailServiceTest {

    @Mock
    private EmailTemplateRepository emailTemplateRepository;

    @Mock
    private JavaMailSender javaMailSender;

    @Mock
    private ModelMapper modelMapper;

    @InjectMocks
    private EmailService emailService;

    // ==========================================
    // Get All Email Templates Tests
    // ==========================================

    @Test
    @DisplayName("getAllEmailTemplates - Should return mapped list of all templates")
    void testGetAllEmailTemplates_ReturnsMappedList() {
        EmailTemplate t1 = EmailTemplate.builder()
                .id(1L)
                .name("Welcome")
                .subject("Welcome!")
                .body("Welcome to our platform")
                .build();
        EmailTemplate t2 = EmailTemplate.builder()
                .id(2L)
                .name("Reset Password")
                .subject("Reset your password")
                .body("Click here to reset")
                .build();

        EmailTemplateResponseDTO dto1 = EmailTemplateResponseDTO.builder()
                .id(1L)
                .name("Welcome")
                .subject("Welcome!")
                .body("Welcome to our platform")
                .build();
        EmailTemplateResponseDTO dto2 = EmailTemplateResponseDTO.builder()
                .id(2L)
                .name("Reset Password")
                .subject("Reset your password")
                .body("Click here to reset")
                .build();

        when(emailTemplateRepository.findAll()).thenReturn(Flux.just(t1, t2));
        when(modelMapper.map(t1, EmailTemplateResponseDTO.class)).thenReturn(dto1);
        when(modelMapper.map(t2, EmailTemplateResponseDTO.class)).thenReturn(dto2);

        List<EmailTemplateResponseDTO> result = emailService.getAllEmailTemplates().collectList().block();

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("Welcome", result.get(0).getName());
        assertEquals("Reset Password", result.get(1).getName());
        verify(emailTemplateRepository).findAll();
        verify(modelMapper).map(t1, EmailTemplateResponseDTO.class);
        verify(modelMapper).map(t2, EmailTemplateResponseDTO.class);
    }

    // ==========================================
    // Send Email Tests
    // ==========================================

    @Test
    @DisplayName("sendEmail - Should send email successfully and complete")
    void testSendEmail_Success_SendsEmailAndCompletes() {
        EmailSendRequestDTO request = EmailSendRequestDTO.builder()
                .to("user@example.com")
                .subject("Test Subject")
                .body("Test Body")
                .build();

        doNothing().when(javaMailSender).send(any(SimpleMailMessage.class));

        StepVerifier.create(emailService.sendEmail(request))
                .verifyComplete();

        verify(javaMailSender).send(argThat((SimpleMailMessage message) -> 
            message.getTo() != null && 
            message.getTo()[0].equals("user@example.com") &&
            message.getSubject().equals("Test Subject") &&
            message.getText().equals("Test Body")
        ));
    }

    @Test
    @DisplayName("sendEmail - When sending fails throws EmailSendingException")
    void testSendEmail_SendingFails_ThrowsException() {
        EmailSendRequestDTO request = EmailSendRequestDTO.builder()
                .to("user@example.com")
                .subject("Test Subject")
                .body("Test Body")
                .build();

        doThrow(new RuntimeException("Mail server error"))
                .when(javaMailSender).send(any(SimpleMailMessage.class));

        StepVerifier.create(emailService.sendEmail(request))
                .expectError(EmailSendingException.class)
                .verify();

        verify(javaMailSender).send(any(SimpleMailMessage.class));
    }

    @Test
    @DisplayName("sendEmail - Alternative blocking test for email sending")
    void testSendEmail_Blocking_Success() {
        EmailSendRequestDTO request = EmailSendRequestDTO.builder()
                .to("user@example.com")
                .subject("Test Subject")
                .body("Test Body")
                .build();

        doNothing().when(javaMailSender).send(any(SimpleMailMessage.class));

        emailService.sendEmail(request).block();

        verify(javaMailSender).send(argThat((SimpleMailMessage message) -> 
            message.getTo() != null && 
            message.getTo()[0].equals("user@example.com")
        ));
    }

    // ==========================================
    // Create Email Template Tests
    // ==========================================

    @Test
    @DisplayName("createEmailTemplate - Should create and return template")
    void testCreateEmailTemplate_Success_ReturnsDTO() {
        EmailTemplateRequestDTO request = EmailTemplateRequestDTO.builder()
                .name("New Template")
                .subject("New Subject")
                .body("New Body")
                .build();

        EmailTemplate mapped = EmailTemplate.builder()
                .name("New Template")
                .subject("New Subject")
                .body("New Body")
                .build();

        EmailTemplate saved = EmailTemplate.builder()
                .id(10L)
                .name("New Template")
                .subject("New Subject")
                .body("New Body")
                .build();

        EmailTemplateResponseDTO responseDTO = EmailTemplateResponseDTO.builder()
                .id(10L)
                .name("New Template")
                .subject("New Subject")
                .body("New Body")
                .build();

        when(modelMapper.map(request, EmailTemplate.class)).thenReturn(mapped);
        when(emailTemplateRepository.save(mapped)).thenReturn(Mono.just(saved));
        when(modelMapper.map(saved, EmailTemplateResponseDTO.class)).thenReturn(responseDTO);

        EmailTemplateResponseDTO result = emailService.createEmailTemplate(request).block();

        assertNotNull(result);
        assertEquals(10L, result.getId());
        assertEquals("New Template", result.getName());
        verify(modelMapper).map(request, EmailTemplate.class);
        verify(emailTemplateRepository).save(mapped);
        verify(modelMapper).map(saved, EmailTemplateResponseDTO.class);
    }

    @Test
    @DisplayName("createEmailTemplate - Using StepVerifier to test reactive chain")
    void testCreateEmailTemplate_StepVerifier_Success() {
        EmailTemplateRequestDTO request = EmailTemplateRequestDTO.builder()
                .name("New Template")
                .subject("New Subject")
                .body("New Body")
                .build();

        EmailTemplate mapped = EmailTemplate.builder()
                .name("New Template")
                .subject("New Subject")
                .body("New Body")
                .build();

        EmailTemplate saved = EmailTemplate.builder()
                .id(10L)
                .name("New Template")
                .subject("New Subject")
                .body("New Body")
                .build();

        EmailTemplateResponseDTO responseDTO = EmailTemplateResponseDTO.builder()
                .id(10L)
                .name("New Template")
                .subject("New Subject")
                .body("New Body")
                .build();

        when(modelMapper.map(request, EmailTemplate.class)).thenReturn(mapped);
        when(emailTemplateRepository.save(mapped)).thenReturn(Mono.just(saved));
        when(modelMapper.map(saved, EmailTemplateResponseDTO.class)).thenReturn(responseDTO);

        StepVerifier.create(emailService.createEmailTemplate(request))
                .assertNext(result -> {
                    assertEquals(10L, result.getId());
                    assertEquals("New Template", result.getName());
                })
                .verifyComplete();
    }

    // ==========================================
    // Get Email Template by ID Tests
    // ==========================================

    @Test
    @DisplayName("getEmailTemplateById - When template exists returns DTO")
    void testGetEmailTemplateById_TemplateExists_ReturnsDTO() {
        EmailTemplate template = EmailTemplate.builder()
                .id(5L)
                .name("Test Template")
                .subject("Test Subject")
                .body("Test Body")
                .build();

        EmailTemplateResponseDTO dto = EmailTemplateResponseDTO.builder()
                .id(5L)
                .name("Test Template")
                .subject("Test Subject")
                .body("Test Body")
                .build();

        when(emailTemplateRepository.findById(5L)).thenReturn(Mono.just(template));
        when(modelMapper.map(template, EmailTemplateResponseDTO.class)).thenReturn(dto);

        EmailTemplateResponseDTO result = emailService.getEmailTemplateById(5L).block();

        assertNotNull(result);
        assertEquals(5L, result.getId());
        assertEquals("Test Template", result.getName());
        verify(emailTemplateRepository).findById(5L);
        verify(modelMapper).map(template, EmailTemplateResponseDTO.class);
    }

    @Test
    @DisplayName("getEmailTemplateById - When template not found throws EmailTemplateNotFoundException")
    void testGetEmailTemplateById_TemplateNotFound_Throws() {
        when(emailTemplateRepository.findById(99L)).thenReturn(Mono.empty());

        Mono<EmailTemplateResponseDTO> templateMono = emailService.getEmailTemplateById(99L);
        assertThrows(EmailTemplateNotFoundException.class, templateMono::block);
        verify(emailTemplateRepository).findById(99L);
    }

    @Test
    @DisplayName("getEmailTemplateById - Using StepVerifier to test error")
    void testGetEmailTemplateById_StepVerifier_NotFound() {
        when(emailTemplateRepository.findById(99L)).thenReturn(Mono.empty());

        StepVerifier.create(emailService.getEmailTemplateById(99L))
                .expectError(EmailTemplateNotFoundException.class)
                .verify();
    }

    // ==========================================
    // Get Email Template by Name Tests
    // ==========================================

    @Test
    @DisplayName("getEmailTemplateByName - When template exists returns DTO")
    void testGetEmailTemplateByName_TemplateExists_ReturnsDTO() {
        EmailTemplate template = EmailTemplate.builder()
                .id(3L)
                .name("Welcome")
                .subject("Welcome!")
                .body("Welcome message")
                .build();

        EmailTemplateResponseDTO dto = EmailTemplateResponseDTO.builder()
                .id(3L)
                .name("Welcome")
                .subject("Welcome!")
                .body("Welcome message")
                .build();

        when(emailTemplateRepository.findByName("Welcome")).thenReturn(Mono.just(template));
        when(modelMapper.map(template, EmailTemplateResponseDTO.class)).thenReturn(dto);

        EmailTemplateResponseDTO result = emailService.getEmailTemplateByName("Welcome").block();

        assertNotNull(result);
        assertEquals("Welcome", result.getName());
        verify(emailTemplateRepository).findByName("Welcome");
        verify(modelMapper).map(template, EmailTemplateResponseDTO.class);
    }

    @Test
    @DisplayName("getEmailTemplateByName - When template not found throws EmailTemplateNotFoundException")
    void testGetEmailTemplateByName_TemplateNotFound_Throws() {
        when(emailTemplateRepository.findByName("NonExistent")).thenReturn(Mono.empty());

        Mono<EmailTemplateResponseDTO> templateMono = emailService.getEmailTemplateByName("NonExistent");
        assertThrows(EmailTemplateNotFoundException.class, templateMono::block);
        verify(emailTemplateRepository).findByName("NonExistent");
    }

    // ==========================================
    // Update Email Template Tests
    // ==========================================

    @Test
    @DisplayName("updateEmailTemplate - When template exists updates and returns DTO")
    void testUpdateEmailTemplate_TemplateExists_UpdatesAndReturnsDTO() {
        EmailTemplateRequestDTO request = EmailTemplateRequestDTO.builder()
                .name("Updated Template")
                .subject("Updated Subject")
                .body("Updated Body")
                .build();

        EmailTemplate existing = EmailTemplate.builder()
                .id(7L)
                .name("Old Template")
                .subject("Old Subject")
                .body("Old Body")
                .build();

        EmailTemplate updated = EmailTemplate.builder()
                .id(7L)
                .name("Updated Template")
                .subject("Updated Subject")
                .body("Updated Body")
                .build();

        EmailTemplateResponseDTO responseDTO = EmailTemplateResponseDTO.builder()
                .id(7L)
                .name("Updated Template")
                .subject("Updated Subject")
                .body("Updated Body")
                .build();

        when(emailTemplateRepository.findById(7L)).thenReturn(Mono.just(existing));
        when(emailTemplateRepository.save(any(EmailTemplate.class))).thenReturn(Mono.just(updated));
        when(modelMapper.map(updated, EmailTemplateResponseDTO.class)).thenReturn(responseDTO);

        EmailTemplateResponseDTO result = emailService.updateEmailTemplate(7L, request).block();

        assertNotNull(result);
        assertEquals(7L, result.getId());
        assertEquals("Updated Template", result.getName());
        verify(emailTemplateRepository).findById(7L);
        verify(emailTemplateRepository).save(any(EmailTemplate.class));
        verify(modelMapper).map(updated, EmailTemplateResponseDTO.class);
    }

    @Test
    @DisplayName("updateEmailTemplate - When template not found throws EmailTemplateNotFoundException")
    void testUpdateEmailTemplate_TemplateNotFound_Throws() {
        EmailTemplateRequestDTO request = EmailTemplateRequestDTO.builder()
                .name("Updated Template")
                .build();

        when(emailTemplateRepository.findById(99L)).thenReturn(Mono.empty());

        Mono<EmailTemplateResponseDTO> updateMono = emailService.updateEmailTemplate(99L, request);
        assertThrows(EmailTemplateNotFoundException.class, updateMono::block);
        verify(emailTemplateRepository).findById(99L);
        verify(emailTemplateRepository, never()).save(any());
    }

    // ==========================================
    // Delete Email Template Tests
    // ==========================================

    @Test
    @DisplayName("deleteEmailTemplate - When template exists deletes successfully")
    void testDeleteEmailTemplate_TemplateExists_DeletesSuccessfully() {
        EmailTemplate template = EmailTemplate.builder()
                .id(8L)
                .name("To Delete")
                .build();

        when(emailTemplateRepository.findById(8L)).thenReturn(Mono.just(template));
        when(emailTemplateRepository.delete(template)).thenReturn(Mono.empty());

        emailService.deleteEmailTemplate(8L).block();

        verify(emailTemplateRepository).findById(8L);
        verify(emailTemplateRepository).delete(template);
    }

    @Test
    @DisplayName("deleteEmailTemplate - When template not found throws EmailTemplateNotFoundException")
    void testDeleteEmailTemplate_TemplateNotFound_Throws() {
        when(emailTemplateRepository.findById(99L)).thenReturn(Mono.empty());

        Mono<Void> deleteMono = emailService.deleteEmailTemplate(99L);
        assertThrows(EmailTemplateNotFoundException.class, deleteMono::block);
        verify(emailTemplateRepository).findById(99L);
        verify(emailTemplateRepository, never()).delete(any());
    }

    @Test
    @DisplayName("deleteEmailTemplate - Using StepVerifier to test deletion")
    void testDeleteEmailTemplate_StepVerifier_Success() {
        EmailTemplate template = EmailTemplate.builder()
                .id(8L)
                .name("To Delete")
                .build();

        when(emailTemplateRepository.findById(8L)).thenReturn(Mono.just(template));
        when(emailTemplateRepository.delete(template)).thenReturn(Mono.empty());

        StepVerifier.create(emailService.deleteEmailTemplate(8L))
                .verifyComplete();

        verify(emailTemplateRepository).findById(8L);
        verify(emailTemplateRepository).delete(template);
    }
}