package co.edu.puj.secchub_backend.integration.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.List;

/**
 * Entity mapped to 'monitor_request'.
 * Represents a student's application to be a teaching assistant (academic or administrative).
 */
@Entity
@Table(name = "monitor_request")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MonitorRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** FK to student applying */
    @Column(name = "student_id", nullable = false)
    private Long studentId;

    /** FK to semester in which the request applies */
    @Column(name = "semester_id", nullable = false)
    private Long semesterId;

    /** Type of request: ACADEMIC or ADMINISTRATIVE */
    @Column(name = "type", nullable = false)
    private String type;

    /** Course reference (only for academic type) */
    @Column(name = "course_id")
    private Long courseId;

    /** Section reference (only for administrative type) */
    @Column(name = "section_id")
    private Long sectionId;

    /** Grade obtained in the subject (only academic) */
    @Column(name = "grade")
    private Double grade;

    /** Name of the professor with whom the student took the subject */
    @Column(name = "professor_name")
    private String professorName;

    /** Status of the request (pending, approved, rejected) */
    @Column(name = "status_id")
    private Long statusId;

    /** Date when the student submitted the request */
    @Column(name = "request_date")
    private LocalDate requestDate;

    /** Availability slots linked to this request */
    @OneToMany(mappedBy = "monitorRequest", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<MonitorAvailability> availabilities;
}
