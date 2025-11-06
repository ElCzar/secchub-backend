package co.edu.puj.secchub_backend.parametric.contracts;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for DocumentType entity.
 * Used for transferring document type data across module boundaries.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DocumentTypeDTO {
    private Long id;
    private String name;
}