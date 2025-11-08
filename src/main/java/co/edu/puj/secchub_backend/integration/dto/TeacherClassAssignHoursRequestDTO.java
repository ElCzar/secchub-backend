package co.edu.puj.secchub_backend.integration.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for assigning hours to a teacher's class.
 */
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
public class TeacherClassAssignHoursRequestDTO {
    private Integer workHoursToAssign;
}
