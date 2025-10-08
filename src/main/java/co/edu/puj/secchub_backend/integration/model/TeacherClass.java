package co.edu.puj.secchub_backend.integration.model;

import jakarta.persistence.*;
import lombok.*;

/**
 * Entity mapped to 'teacher_class'.
 * Represents the assignment of a teacher to a class along with their workload and decision status.
 */
@Entity
@Table(name = "teacher_class")
@Getter @Setter @Builder
@NoArgsConstructor @AllArgsConstructor
public class TeacherClass {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "semester_id")
    private Long semesterId;

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
    
    @Column(name = "decision")
    private Boolean decision;

    @Column(name = "observation")
    private String observation;
    
    @Column(name = "status_id")
    private Long statusId;
}
