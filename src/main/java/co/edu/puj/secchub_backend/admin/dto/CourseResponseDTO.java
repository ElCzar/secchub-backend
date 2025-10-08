package co.edu.puj.secchub_backend.admin.dto;

import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Builder;

/**
 * DTO that represents a course inside the system.
 * Contains response information for user.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CourseResponseDTO {
    private Long id;
    private Long sectionId;
    private String name;
    private Integer credits;
    private String description;
    private Boolean isValid;
    private String recommendation;
    private Long statusId;
}
