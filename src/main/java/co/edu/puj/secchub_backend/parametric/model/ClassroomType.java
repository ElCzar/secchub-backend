package co.edu.puj.secchub_backend.parametric.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Entity represents different types of classrooms.
 */
@Table("classroom_type")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClassroomType {
    @Id
    private Long id;

    @Column
    private String name;
}
