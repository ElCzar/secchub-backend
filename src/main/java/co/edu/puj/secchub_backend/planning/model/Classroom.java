package co.edu.puj.secchub_backend.planning.model;

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
 * Represents a classroom in the planning module.
 * It represents a physical or virtual space where classes are held.
 */
@Entity
@Table(name = "classroom")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Classroom {
    @Id
    @GeneratedValue(strategy = jakarta.persistence.GenerationType.IDENTITY)
    private Long id;

    @Column(name = "classroom_type_id")
    private Long classroomTypeId;

    @Column
    private String campus;

    @Column
    private String location;

    @Column
    private String room;

    @Column
    private Integer capacity;
}
