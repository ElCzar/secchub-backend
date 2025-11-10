package co.edu.puj.secchub_backend.planning.dto;

import java.time.LocalTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO representing a teaching assistant schedule with additional details.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TeachingAssistantScheduleWithDetailsDTO {
    private Long scheduleId;
    private Long teachingAssistantId;
    private Long studentApplicationId;
    private Long userId;
    private Long classId;
    private Long sectionId;
    private String day;
    private LocalTime startTime;
    private LocalTime endTime;
}