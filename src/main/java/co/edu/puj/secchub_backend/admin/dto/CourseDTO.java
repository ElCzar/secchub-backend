package co.edu.puj.secchub_backend.admin.dto;

import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Builder;

/**
 * DTO que representa una asignatura (curso) en el sistema.
 * Contiene información como identificador, nombre, créditos, descripción, estado y sección asociada.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CourseDTO {
    /** Identificador único del curso. */
    private Long id;
    /** Nombre del curso. */
    private String name;
    /** Número de créditos del curso. */
    private Integer credits;
    /** Descripción del curso. */
    private String description;
    /** Indica si el curso está activo o válido. */
    private Boolean isValid;
    /** Identificador de la sección a la que pertenece el curso. */
    private Long sectionId;
}
