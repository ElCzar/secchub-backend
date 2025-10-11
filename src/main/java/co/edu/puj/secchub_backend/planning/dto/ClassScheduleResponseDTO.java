package co.edu.puj.secchub_backend.planning.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalTime;

/**
 * DTO for class schedule responses.
 * Contains complete information about a class schedule including system-generated data.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClassScheduleResponseDTO {
    private Long id;
    private Long classId;
    private Long classroomId;
    private String day;
    private LocalTime startTime;
    private LocalTime endTime;
    private Long modalityId;
    private Boolean disability;
}