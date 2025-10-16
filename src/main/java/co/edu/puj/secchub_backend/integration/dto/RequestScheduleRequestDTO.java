package co.edu.puj.secchub_backend.integration.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO that represents a schedule associated with an academic request.
 * Includes information that user provides when making a request.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RequestScheduleRequestDTO {
    private Long classRoomTypeId;
    private String startTime;
    private String endTime;
    private String day;
    private Long modalityId;
    private Boolean disability;
}
