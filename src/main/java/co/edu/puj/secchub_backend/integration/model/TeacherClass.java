package co.edu.puj.secchub_backend.integration.model;

import jakarta.persistence.*;
import lombok.*;

/**
 * Entity representing the relation between a Teacher and a Class.
 * Used in HU17 (professor availability) for accepting or rejecting assigned classes.
 */
@Entity
@Table(name = "teacher_class")
@Getter @Setter @Builder
@NoArgsConstructor @AllArgsConstructor
public class TeacherClass {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "teacher_id")
    private Long teacherId;

    @Column(name = "class_id")
    private Long classId;

    @Column(name = "work_hours")
    private Integer workHours;

    @Column(name = "full_time_extra_hours")
    private Integer fullTimeExtraHours;

    @Column(name = "adjunct_extra_hours")
    private Integer adjunctExtraHours;

    /** true = accepted, false = rejected, null = pending */
    @Column(name = "decision")
    private Boolean decision;

    @Column(name = "observation")
    private String observation;

    /** Status: 1=Pending, 2=Accepted, 3=Rejected */
    @Column(name = "status_id")
    private Long statusId;
}
