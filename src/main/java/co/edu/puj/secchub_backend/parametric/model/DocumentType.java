package co.edu.puj.secchub_backend.parametric.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Entity representing document types in the system.
 * Used for user identification documents.
 */
@Table("document_type")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DocumentType {
    @Id
    private Long id;

    @Column("name")
    private String name;
}