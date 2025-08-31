package co.edu.puj.secchub_backend.integration.dto;

import lombok.*;
import java.time.LocalDate;
import java.util.List;

/**
 * DTO que representa una solicitud académica individual.
 * Contiene información sobre el curso, sección, tipo de aula, cupo solicitado, fechas, observaciones y horarios asociados.
 */
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class AcademicRequestDTO {
    /**
     * Identificador del curso solicitado.
     */
    private Long courseId;
    /**
     * Sección del curso.
     */
    private String section;
    /**
     * Identificador del tipo de aula requerida.
     */
    private Long classroomTypeId;
    /**
     * Cupo solicitado para el curso.
     */
    private Integer requestedQuota;
    /**
     * Fecha de inicio de la solicitud.
     */
    private LocalDate startDate;
    /**
     * Fecha de fin de la solicitud.
     */
    private LocalDate endDate;
    /**
     * Número de semanas solicitadas.
     */
    private Integer weeks;
    /**
     * Observaciones adicionales de la solicitud.
     */
    private String observation;
    /**
     * Lista de horarios asociados a la solicitud.
     */
    private List<RequestScheduleDTO> schedules;
}
