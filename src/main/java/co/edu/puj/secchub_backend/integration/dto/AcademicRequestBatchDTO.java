package co.edu.puj.secchub_backend.integration.dto;

import lombok.*;
import java.util.List;

/**
 * DTO que representa un lote de solicitudes académicas para un usuario y un semestre específico.
 * Contiene la información del usuario, el semestre y la lista de solicitudes individuales.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AcademicRequestBatchDTO {
    private Long userId;
    private Long semesterId;
    private List<AcademicRequestDTO> requests;
}
