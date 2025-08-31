package co.edu.puj.secchub_backend.integration.service;


// Update the import to the correct package, for example:
import co.edu.puj.secchub_backend.integration.dto.AcademicRequestDTO;

/**
 * Generates the PDF receipt required by HU01.
 * Provide a concrete implementation with your preferred library (iText/OpenPDF/Flying Saucer).
 */
public interface PdfGenerator {
    /**
     * Generates a receipt PDF and returns the file path or URL.
     */
    String generateReceipt(AcademicRequestDTO payload);
}
