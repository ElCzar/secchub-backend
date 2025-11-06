package co.edu.puj.secchub_backend.admin.dto;

import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for returning semester information.
 * Contains details about a semester period.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SemesterResponseDTO {
    private Long id;
    private Integer period;
    private Integer year;
    private LocalDate startDate;
    private LocalDate endDate;
    private Boolean isCurrent;
}
