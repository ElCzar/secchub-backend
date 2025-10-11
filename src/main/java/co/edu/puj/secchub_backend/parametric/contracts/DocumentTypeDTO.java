package co.edu.puj.secchub_backend.parametric.contracts;

import lombok.Builder;
import lombok.Data;

/**
 * DTO for DocumentType entity.
 * Used for transferring document type data across module boundaries.
 */
@Data
@Builder
public class DocumentTypeDTO {
    private Long id;
    private String name;
}