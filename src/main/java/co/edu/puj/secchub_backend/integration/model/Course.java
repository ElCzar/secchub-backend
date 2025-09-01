package co.edu.puj.secchub_backend.integration.model;

import jakarta.persistence.*;
import lombok.*;

/**
 * Entity representing a course (asignatura) in the system.
 * A course belongs to a section and contains information such as
 * name, credits, description, status (active/inactive).
 */
@Entity
@Table(name = "course")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Course {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "section_id")
    private Long sectionId;

    @Column(nullable = false, length = 200)
    private String name;

    @Column
    private Integer credits;

    @Column
    private String description;

    @Column(name = "is_valid")
    private Boolean isValid;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "section_id", insertable = false, updatable = false)
    private Section section;
}
