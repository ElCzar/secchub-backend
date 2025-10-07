package co.edu.puj.secchub_backend.integration.dto;

import lombok.*;
import java.time.LocalDate;
import java.util.List;

/**
 * DTO que representa una solicitud académica individual.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AcademicRequestDTO {
    private Long courseId;
    private Integer capacity;
    private LocalDate startDate;
    private LocalDate endDate;
    private String observation;
    private List<RequestScheduleDTO> schedules;
    private Integer weeks;
    private Long sectionId;
}
