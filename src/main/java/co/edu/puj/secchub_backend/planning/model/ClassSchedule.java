package co.edu.puj.secchub_backend.planning.model;

import java.time.LocalTime;

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
 * Entity represents the schedule of classes.
 * They indicate the days and times when classes are held.
 */
@Entity
@Table(name = "class_schedule")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ClassSchedule {
    @Id
    @GeneratedValue(strategy = jakarta.persistence.GenerationType.IDENTITY)
    private Long id;

    @Column(name = "class_id")
    private Long classId;

    @Column(name = "classroom_id")
    private Long classroomId;

    @Column
    private String day;

    @Column(name = "start_time")
    private LocalTime startTime;

    @Column(name = "end_time")
    private LocalTime endTime;

    @Column(name = "modality_id")
    private Long modalityId;

    @Column
    private Boolean disability;
}
