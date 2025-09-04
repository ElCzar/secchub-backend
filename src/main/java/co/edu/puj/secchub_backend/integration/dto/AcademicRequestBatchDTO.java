package co.edu.puj.secchub_backend.integration.dto;

import lombok.*;
import java.util.List;

/**
 * DTO que representa un lote de solicitudes académicas para un usuario y un semestre específico.
 * Contiene la información del usuario, el semestre y la lista de solicitudes individuales.
 */
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class AcademicRequestBatchDTO {
    /**
     * Identificador del usuario que realiza las solicitudes.
     */
    private Long userId;
    /**
     * Identificador del semestre al que pertenecen las solicitudes.
     */
    private Long semesterId;
    /**
     * Lista de solicitudes académicas individuales.
     */
    private List<AcademicRequestDTO> requests;
}
