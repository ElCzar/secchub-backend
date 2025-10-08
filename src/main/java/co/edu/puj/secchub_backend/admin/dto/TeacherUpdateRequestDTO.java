package co.edu.puj.secchub_backend.admin.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for updating teacher information.
 * Only allows updating employment type and max hours.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TeacherUpdateRequestDTO {
    private Long employmentTypeId;
    private Integer maxHours;
}