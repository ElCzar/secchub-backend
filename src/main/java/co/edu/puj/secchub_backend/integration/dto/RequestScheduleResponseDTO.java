package co.edu.puj.secchub_backend.integration.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO that represents a schedule associated with an academic request.
 * Includes the complete information returned by the system after processing a request.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RequestScheduleResponseDTO {
    private Long id;
    private Long academicRequestId;
    private Long classRoomTypeId;
    private String startTime;
    private String endTime;
    private String day;
    private Long modalityId;
    private Boolean disability;
}
