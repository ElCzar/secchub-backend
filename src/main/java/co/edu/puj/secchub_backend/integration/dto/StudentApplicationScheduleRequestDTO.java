package co.edu.puj.secchub_backend.integration.dto;
import lombok.*;

/**
 * DTO that represents a simple schedule with day and start/end times.
 * Used to request a schedule block for student applications.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StudentApplicationScheduleRequestDTO {
    private String day;
    private String startTime;
    private String endTime;
}
