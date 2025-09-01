package co.edu.puj.secchub_backend.integration.dto;

import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Builder;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CourseDTO {
    private Long id;
    private String name;
    private Integer credits;
    private String description;
    private Boolean isValid;
    private Long sectionId;
}
