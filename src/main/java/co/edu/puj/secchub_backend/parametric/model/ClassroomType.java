package co.edu.puj.secchub_backend.parametric.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Entity represents different types of classrooms.
 */
@Entity
@Table(name = "classroom_type")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClassroomType {
    @Id
    @GeneratedValue(strategy = jakarta.persistence.GenerationType.IDENTITY)
    private Long id;

    @Column
    private String name;
}
