package co.edu.puj.secchub_backend.integration.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for program context information.
 * Contains career name and current semester information.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProgramContextDTO {
    
    /**
     * Name of the career (program) of the authenticated user
     */
    private String careerName;
    
    /**
     * Current semester information formatted as "YYYY-{period}"
     * Example: "2025-1", "2025-2"
     */
    private String semester;
}