package co.edu.puj.secchub_backend.integration.service;


// Update the import to the correct package if AcademicRequestDTO exists elsewhere

import co.edu.puj.secchub_backend.integration.dto.AcademicRequestBatchDTO;

/**
 * Sends HU01 emails:
 * 1) Confirmation with PDF attachment.
 * 2) Validation email listing requested courses for confirmation.
 */
public interface EmailService {
    void sendConfirmationWithPdf(AcademicRequestBatchDTO payload, String pdfPath);
    void sendValidationList(AcademicRequestBatchDTO payload);
}

