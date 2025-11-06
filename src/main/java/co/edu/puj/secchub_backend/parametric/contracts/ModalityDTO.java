package co.edu.puj.secchub_backend.parametric.contracts;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for Modality entity.
 * Used for transferring modality data across module boundaries.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ModalityDTO {
    private Long id;
    private String name;
}