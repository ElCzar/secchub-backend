package co.edu.puj.secchub_backend.parametric.contracts;

import lombok.Builder;
import lombok.Data;

/**
 * DTO for Role information exposed to other modules.
 */
@Data
@Builder
public class RoleDTO {
    private Long id;
    private String name;
}