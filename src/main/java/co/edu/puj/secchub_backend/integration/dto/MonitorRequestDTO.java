package co.edu.puj.secchub_backend.integration.dto;

import lombok.*;

import java.time.LocalDate;
import java.util.List;

/**
 * DTO for transporting data of a MonitorRequest.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MonitorRequestDTO {
    private Long id;
    private Long studentId;
    private Long semesterId;
    private String type; // ACADEMIC or ADMINISTRATIVE
    private Long courseId;
    private Long sectionId;
    private Double grade;
    private String professorName;
    private Long statusId;
    private LocalDate requestDate;
    private List<MonitorAvailabilityDTO> availabilities;
}
