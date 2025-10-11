package co.edu.puj.secchub_backend.admin.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO that represents a teacher registration request.
 * It contains information uploaded by the user to create a teacher profile,
 * including employment type, maximum hours, and associated user details.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TeacherRegisterRequestDTO {
    private Long employmentTypeId;
    private Integer maxHours;
    private UserRegisterRequestDTO user;
}
