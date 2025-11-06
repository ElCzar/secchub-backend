package co.edu.puj.secchub_backend.parametric.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Entity representing a Modality in the university system.
 * Modalities are used to categorize courses as in-person, online, or hybrid.
 */
@Table("modality")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Modality {
    @Id
    private Long id;

    @Column
    private String name;
}
