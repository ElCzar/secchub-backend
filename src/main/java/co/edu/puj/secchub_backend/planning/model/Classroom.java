package co.edu.puj.secchub_backend.planning.model;


import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Represents a classroom in the planning module.
 * It represents a physical or virtual space where classes are held.
 */
@Table("classroom")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Classroom {
    @Id
    private Long id;

    @Column("classroom_type_id")
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
