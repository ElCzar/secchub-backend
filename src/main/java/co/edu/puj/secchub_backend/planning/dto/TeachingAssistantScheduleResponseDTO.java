package co.edu.puj.secchub_backend.planning.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO that represents a schedule block in the response for teaching assistants.
 * Includes identifiers and time details.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TeachingAssistantScheduleResponseDTO {
    private Long id;
    private Long teachingAssistantId;
    private String day;
    private String startTime;
    private String endTime;
}