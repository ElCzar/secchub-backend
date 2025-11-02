package co.edu.puj.secchub_backend.admin.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for creating or updating a semester.
 * Contains the information needed to define a semester period.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SemesterRequestDTO {
    private Integer period;
    private Integer year;
    private String startDate;
    private String endDate;
    private String startSpecialWeek; // Fecha de inicio de semana especial (receso/semana santa)
}
