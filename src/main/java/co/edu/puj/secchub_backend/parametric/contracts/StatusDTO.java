package co.edu.puj.secchub_backend.parametric.contracts;

import lombok.Builder;
import lombok.Data;

/**
 * DTO for Status information exposed to other modules.
 * Contains status data without exposing internal entity structure.
 */
@Data
@Builder
public class StatusDTO {
    private Long id;
    private String name;
}