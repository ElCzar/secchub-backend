package co.edu.puj.secchub_backend.integration.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;

@Entity
@Table(name = "student")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Student {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "course_id")
    private Long courseId; // solo si es monitor académico

    @Column(name = "section_id")
    private Long sectionId; // solo si es monitor administrativo

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

    /**
     * Asigna valores por defecto al momento de persistir si no están definidos.
     */
    @PrePersist
    public void onCreate() {
        if (applicationDate == null) {
            applicationDate = LocalDate.now();
        }
        if (statusId == null) {
            statusId = 1L; // Default status: "Active"
        }
    }
}
