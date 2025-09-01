package co.edu.puj.secchub_backend.integration.model;

import jakarta.persistence.*;
import lombok.*;

/**
 * Entity mapped to 'request_schedule'.
 * Represents requested time blocks (horarios) per course request.
 */
@Entity
@Table(name = "request_schedule")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RequestSchedule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** FK to academic request */
    @Column(name = "academic_request_id")
    private Long academicRequestId;

    /** Classroom type requested */
    @Column(name = "classroom_type_id")
    private Long classroomTypeId;

    /** Start time of the schedule */
    @Column(name = "start_time")
    private java.sql.Time startTime;

    /** End time of the schedule */
    @Column(name = "end_time")
    private java.sql.Time endTime;

    /** Day of the week (MONDAY, TUESDAY, etc.) */
    @Column(name = "day")
    private String day;

    /** Whether the schedule should accommodate disability */
    @Column(name = "disability")
    private Boolean disability;

    /** Modality (PRESENTIAL, VIRTUAL, etc.) */
    @Column(name = "modality_id")
    private Long modalityId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "academic_request_id", insertable = false, updatable = false)
    private AcademicRequest academicRequest;
}
