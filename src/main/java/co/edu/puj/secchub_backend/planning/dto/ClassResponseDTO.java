package co.edu.puj.secchub_backend.planning.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

/**
 * DTO for academic class responses.
 * Contains complete information about an academic class including system-generated data.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClassResponseDTO {
    private Long id;
    private Long section;
    private Long courseId;
    private String courseName;  // Name of the course
    private Long semesterId;
    private LocalDate startDate;
    private LocalDate endDate;
    private String observation;
    private Integer capacity;
    private Long statusId;
    private List<ClassScheduleResponseDTO> schedules;
}