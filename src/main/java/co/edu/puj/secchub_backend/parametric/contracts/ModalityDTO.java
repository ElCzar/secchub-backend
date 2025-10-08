package co.edu.puj.secchub_backend.parametric.contracts;

import lombok.Builder;
import lombok.Data;

/**
 * DTO for Modality entity.
 * Used for transferring modality data across module boundaries.
 */
@Data
@Builder
public class ModalityDTO {
    private Long id;
    private String name;
}