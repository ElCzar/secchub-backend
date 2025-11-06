package co.edu.puj.secchub_backend.parametric.contracts;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for Status information exposed to other modules.
 * Contains status data without exposing internal entity structure.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class StatusDTO {
    private Long id;
    private String name;
}