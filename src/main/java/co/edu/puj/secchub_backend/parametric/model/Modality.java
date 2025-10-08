package co.edu.puj.secchub_backend.parametric.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Entity representing a Modality in the university system.
 * Modalities are used to categorize courses as in-person, online, or hybrid.
 */
@Entity
@Table(name = "modality")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Modality {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column
    private String name;
}
