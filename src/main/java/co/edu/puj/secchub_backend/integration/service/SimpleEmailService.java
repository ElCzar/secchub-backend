package co.edu.puj.secchub_backend.integration.service;

import co.edu.puj.secchub_backend.integration.dto.AcademicRequestBatchDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Implementación mínima de EmailService para desarrollo y pruebas.
 * En lugar de enviar correos reales, registra la acción en el log.
 */
@Service
@Slf4j
public class SimpleEmailService implements EmailService {

    /**
     * Simula el envío de un correo de confirmación con PDF adjunto.
     * @param payload Información de la solicitud académica
     * @param pdfPath Ruta al archivo PDF generado
     */
    @Override
    public void sendConfirmationWithPdf(AcademicRequestBatchDTO payload, String pdfPath) {
        log.info("HU01 email (dev): sending confirmation with PDF [{}] to program userId={}",
                pdfPath, payload.getUserId());
    }

    /**
     * Simula el envío de una lista de validación por correo.
     * @param payload Información de la solicitud académica
     */
    @Override
    public void sendValidationList(AcademicRequestBatchDTO payload) {
        log.info("HU01 email (dev): sending validation list for semesterId={} with {} request(s)",
                payload.getSemesterId(),
                payload.getRequests() == null ? 0 : payload.getRequests().size());
    }
}
