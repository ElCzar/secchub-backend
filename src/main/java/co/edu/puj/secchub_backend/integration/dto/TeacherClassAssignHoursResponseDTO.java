package co.edu.puj.secchub_backend.integration.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for the response when assigning hours to a teacher's class.
 */
@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TeacherClassAssignHoursResponseDTO {
    private String teacherName;
    private Integer maxHours;
    private Integer totalAssignedHours;
    private Integer workHoursToAssign;
    private Integer exceedsMaxHours;
}
