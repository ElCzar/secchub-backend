package co.edu.puj.secchub_backend.integration.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO that represents a schedule block in the response for student applications.
 * Includes identifiers and time details.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StudentApplicationScheduleResponseDTO {
    private Long id;
    private Long studentApplicationId;
    private String day;
    private String startTime;
    private String endTime;
}
