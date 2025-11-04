package co.edu.puj.secchub_backend.admin.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Entity representing a course (asignatura) in the system.
 * A course belongs to a section and contains information such as
 * name, credits, description, status (active/inactive).
 */
@Table("course")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Course {

    @Id
    private Long id;

    @Column("section_id")
    private Long sectionId;

    @Column
    private String name;

    @Column
    private Integer credits;

    @Column
    private String description;

    @Column("is_valid")
    private Boolean isValid;
    
    @Column
    private String recommendation;

    @Column("status_id")
    private Long statusId;
}
