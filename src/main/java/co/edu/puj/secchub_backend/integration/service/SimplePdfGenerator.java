package co.edu.puj.secchub_backend.integration.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import co.edu.puj.secchub_backend.integration.dto.AcademicRequestDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

/**
 * Implementación mínima de PdfGenerator para desarrollo y pruebas.
 * Crea un archivo .pdf de ejemplo (no real) con el contenido JSON del payload y retorna la ruta absoluta.
 */
@Slf4j
@Service
public class SimplePdfGenerator implements PdfGenerator {

    /**
     * Mapper para convertir objetos a JSON.
     */
    private final ObjectMapper mapper = new ObjectMapper();

    /**
     * Genera un recibo en formato PDF (placeholder) con el contenido de la solicitud académica.
     *
     * @param payload DTO de la solicitud académica
     * @return Ruta absoluta del archivo PDF generado, o null si ocurre un error
     */
    @Override
    public String generateReceipt(AcademicRequestDTO payload) {
        try {
            Path dir = Path.of(System.getProperty("java.io.tmpdir"), "secchub");
            Files.createDirectories(dir);
            Path file = dir.resolve("request-receipt-" + UUID.randomUUID() + ".pdf");
            // Placeholder: write JSON content (you can swap for real PDF later)
            Files.writeString(file, mapper.writerWithDefaultPrettyPrinter().writeValueAsString(payload));
            log.info("HU01 placeholder PDF generated at: {}", file.toAbsolutePath());
            return file.toAbsolutePath().toString();
        } catch (IOException e) {
            log.warn("Could not generate placeholder PDF, returning null", e);
            return null; // caller should handle null safely
        }
    }
}
