package co.edu.puj.secchub_backend.admin.dto;

import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Builder;

/**
 * DTO que representa una sección en el sistema.
 * Contiene información como identificador, nombre y usuario responsable de la sección.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SectionDTO {
    /** Identificador único de la sección. */
    private Long id;
    /** Nombre de la sección. */
    private String name;
    /** Identificador del usuario responsable de la sección. */
    private Long userId;
}