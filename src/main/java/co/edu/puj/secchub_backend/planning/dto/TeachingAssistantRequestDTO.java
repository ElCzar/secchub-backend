package co.edu.puj.secchub_backend.planning.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for transferring TeachingAssistant data in requests.
 * Includes fields for teaching assistant details and associated schedules.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TeachingAssistantRequestDTO {
    private Long classId;
    private Long studentApplicationId;
    private Long weeklyHours;
    private Long weeks;
    private Long totalHours;
    private List<TeachingAssistantScheduleRequestDTO> schedules;
}