package co.edu.puj.secchub_backend.integration.model;

import java.time.LocalDate;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Entity mapped to 'teacher_class'.
 * Represents the assignment of a teacher to a class along with their workload and decision status.
 */
@Table("teacher_class")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TeacherClass {
    @Id
    private Long id;

    @Column("semester_id")
    private Long semesterId;

    @Column("teacher_id")
    private Long teacherId;

    @Column("class_id")
    private Long classId;

    @Column("work_hours")
    private Integer workHours;

    @Column("full_time_extra_hours")
    private Integer fullTimeExtraHours;

    @Column("adjunct_extra_hours")
    private Integer adjunctExtraHours;
    
    @Column("decision")
    private Boolean decision;

    @Column("observation")
    private String observation;
    
    @Column("status_id")
    private Long statusId;

    @Column("start_date")
    private LocalDate startDate;

    @Column("end_date")
    private LocalDate endDate;
}
