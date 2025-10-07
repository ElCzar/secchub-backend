package co.edu.puj.secchub_backend.integration.model;

import com.fasterxml.jackson.annotation.JsonBackReference;

import jakarta.persistence.*;
import lombok.*;

/**
 * Entity mapped to 'request_schedule'.
 * Represents the schedule details associated with an academic request.
 */
@Entity
@Table(name = "request_schedule")
@Getter @Setter @Builder
@NoArgsConstructor @AllArgsConstructor
public class RequestSchedule {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "academic_request_id")
    private Long academicRequestId;

    @Column(name = "classroom_type_id")
    private Long classroomTypeId;

    @Column(name = "start_time")
    private java.sql.Time startTime;

    @Column(name = "end_time")
    private java.sql.Time endTime;

    @Column(name = "day")
    private String day;

    @Column(name = "modality_id")
    private Long modalityId;

    @Column(name = "disability")
    private Boolean disability;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "academic_request_id", insertable = false, updatable = false)
    @JsonBackReference
    private AcademicRequest academicRequest;

}
