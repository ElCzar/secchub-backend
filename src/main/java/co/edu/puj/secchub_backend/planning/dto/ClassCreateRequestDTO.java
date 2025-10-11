package co.edu.puj.secchub_backend.planning.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

/**
 * DTO for creating/updating academic classes.
 * Contains the information needed to submit a new class request.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClassCreateRequestDTO {
    private Long section;
    private Long courseId;
    private Long semesterId;
    private LocalDate startDate;
    private LocalDate endDate;
    private String observation;
    private Integer capacity;
    private Long statusId;
    private List<ClassScheduleRequestDTO> schedules;
}