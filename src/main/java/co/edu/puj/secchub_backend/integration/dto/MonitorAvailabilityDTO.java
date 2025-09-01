package co.edu.puj.secchub_backend.integration.dto;

import lombok.*;

/**
 * DTO for representing availability slots in requests.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MonitorAvailabilityDTO {
    private Long id;
    private String day;
    private String startTime;  // "HH:mm:ss"
    private String endTime;    // "HH:mm:ss"
    private Integer totalHours;
}
