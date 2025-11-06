package co.edu.puj.secchub_backend.planning.model;

import java.time.LocalTime;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Entity represents the schedule of classes.
 * They indicate the days and times when classes are held.
 */
@Table("class_schedule")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ClassSchedule {
    @Id
    private Long id;

    @Column("class_id")
    private Long classId;

    @Column("classroom_id")
    private Long classroomId;

    @Column
    private String day;

    @Column("start_time")
    private LocalTime startTime;

    @Column("end_time")
    private LocalTime endTime;

    @Column("modality_id")
    private Long modalityId;

    @Column
    private Boolean disability;
}
