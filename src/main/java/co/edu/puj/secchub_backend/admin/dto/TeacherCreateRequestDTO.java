package co.edu.puj.secchub_backend.admin.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for creating a new teacher.
 * Used when registering a teacher with employment type and hour constraints.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TeacherCreateRequestDTO {
    private Long userId;
    private Long employmentTypeId;
    private Integer maxHours;
}