package co.edu.puj.secchub_backend.integration.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

/**
 * DTO for academic request responses.
 * Contains complete information about an academic request including system-generated data.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AcademicRequestResponseDTO {
    private Long id;
    private Long userId;
    private Long courseId;
    private Long semesterId;
    private LocalDate startDate;
    private LocalDate endDate;
    private Integer capacity;
    private LocalDate requestDate;
    private String observation;
    private List<RequestScheduleResponseDTO> schedules;
}
