package co.edu.puj.secchub_backend.admin.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO that represents a course request.
 * It contains information uploaded by the user to create or update a course.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CourseRequestDTO {
    private Long sectionId;
    private String name;
    private Integer credits;
    private String description;
    private Boolean isValid;
    private String recommendation;
    private Long statusId;
}
