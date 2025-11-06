package co.edu.puj.secchub_backend.parametric.contracts;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for EmploymentType entity.
 * Used for transferring employment type data across module boundaries.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class EmploymentTypeDTO {
    private Long id;
    private String name;
}