package co.edu.puj.secchub_backend.planning.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalTime;

/**
 * DTO for creating/updating class schedules.
 * Contains the information needed to define when a class occurs.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClassScheduleRequestDTO {
    private Long classroomId;
    private String day;
    private LocalTime startTime;
    private LocalTime endTime;
    private Long modalityId;
    private Boolean disability;
}