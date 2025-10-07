package co.edu.puj.secchub_backend.integration.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

/**
 * DTO for creating/updating academic requests.
 * Contains the information needed to submit a new academic request.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AcademicRequestRequestDTO {
    private Long courseId;
    private LocalDate startDate;
    private LocalDate endDate;
    private Integer capacity;
    private String observation;
    private List<RequestScheduleRequestDTO> schedules;
}
