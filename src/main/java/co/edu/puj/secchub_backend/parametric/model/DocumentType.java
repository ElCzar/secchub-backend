package co.edu.puj.secchub_backend.parametric.model;

import jakarta.persistence.*;
import lombok.*;

/**
 * Entity representing document types in the system.
 * Used for user identification documents.
 */
@Entity
@Table(name = "document_type")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DocumentType {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false, unique = true, length = 100)
    private String name;

    @Override
    public String toString() {
        return "DocumentType{" +
                "id=" + id +
                ", name='" + name + '\'' +
                '}';
    }
}