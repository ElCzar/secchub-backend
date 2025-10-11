package co.edu.puj.secchub_backend.parametric.contracts;

import lombok.Builder;
import lombok.Data;

/**
 * DTO for EmploymentType entity.
 * Used for transferring employment type data across module boundaries.
 */
@Data
@Builder
public class EmploymentTypeDTO {
    private Long id;
    private String name;
}