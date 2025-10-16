package co.edu.puj.secchub_backend.planning.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for transferring TeachingAssistant data in responses.
 * For response purposes.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TeachingAssistantResponseDTO {
    private Long id;
    private Long classId;
    private Long studentApplicationId;
    private Long weeklyHours;
    private Long weeks;
    private Long totalHours;
    private List<TeachingAssistantScheduleResponseDTO> schedules;
}