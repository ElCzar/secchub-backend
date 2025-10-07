package co.edu.puj.secchub_backend.integration.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.List;

/**
 * Entity mapped to 'student_application'.
 * Represents a student's application to be a teaching assistant (academic or administrative).
 */
@Entity
@Table(name = "student_application")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StudentApplication {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "course_id")
    private Long courseId;

    @Column(name = "section_id")
    private Long sectionId;

    @Column(name = "program")
    private String program;

    @Column(name = "semester")
    private Integer semester;

    @Column(name = "academic_average")
    private Double academicAverage;

    @Column(name = "phone_number")
    private String phoneNumber;

    @Column(name = "alternate_phone_number")
    private String alternatePhoneNumber;

    @Column(name = "address")
    private String address;

    @Column(name = "personal_email")
    private String personalEmail;

    @Column(name = "was_teaching_assistant")
    private Boolean wasTeachingAssistant;

    @Column(name = "course_average")
    private Double courseAverage;

    @Column(name = "course_teacher")
    private String courseTeacher;

    @Column(name = "application_date")
    private LocalDate applicationDate;

    @Column(name = "status_id")
    private Long statusId;

    @OneToMany(mappedBy = "studentApplication", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<StudentApplicationSchedule> schedules;

    /**
     * Assigns default values when persisting if not defined.
     */
    @PrePersist
    public void onCreate() {
        if (applicationDate == null) {
            applicationDate = LocalDate.now();
        }
    }
}
