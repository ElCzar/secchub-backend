package co.edu.puj.secchub_backend.integration.model;

import jakarta.persistence.*;
import lombok.*;

import java.sql.Time;

/**
 * Entity mapped to 'student_application_schedule'.
 * Represents the availability time blocks a student declares in their application.
 */
@Entity
@Table(name = "student_application_schedule")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StudentApplicationSchedule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "day", nullable = false)
    private String day;

    @Column(name = "start_time", nullable = false)
    private Time startTime;

    @Column(name = "end_time", nullable = false)
    private Time endTime;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_application_id", nullable = false)
    private StudentApplication studentApplication;
    
    /**
     * Convenience method to get the student application ID.
     */
    public Long getStudentApplicationId() {
        return studentApplication != null ? studentApplication.getId() : null;
    }
}
