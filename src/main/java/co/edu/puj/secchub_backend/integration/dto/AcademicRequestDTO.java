package co.edu.puj.secchub_backend.integration.dto;

import lombok.*;
import java.time.LocalDate;
import java.util.List;

/**
 * DTO que representa una solicitud académica individual.
 */
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class AcademicRequestDTO {
    private Long courseId;
    private Integer capacity; // cupo solicitado
    private LocalDate startDate;
    private LocalDate endDate;
    private String observation;
    private List<RequestScheduleDTO> schedules;

    // Campos calculados (no en DB)
    private Integer weeks;    // se calcula en service
    private Long sectionId;   // se deriva del curso
}
