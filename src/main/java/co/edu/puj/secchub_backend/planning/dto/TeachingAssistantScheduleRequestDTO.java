package co.edu.puj.secchub_backend.planning.dto;

import lombok.*;

/**
 * DTO that represents a simple schedule with day and start/end times.
 * Used to request a schedule block for teaching assistants.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TeachingAssistantScheduleRequestDTO {
    private String day;
    private String startTime;
    private String endTime;
}