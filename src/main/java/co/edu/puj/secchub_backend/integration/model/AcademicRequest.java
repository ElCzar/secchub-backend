package co.edu.puj.secchub_backend.integration.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonManagedReference;

/**
 * Entity mapped to 'academic_request'.
 * Represents a request made by a program for a class in a specific course and semester.
 */
@Entity
@Table(name = "academic_request")
@Getter @Setter @Builder
@NoArgsConstructor @AllArgsConstructor
public class AcademicRequest {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "course_id")
    private Long courseId;

    @Column(name = "semester_id")
    private Long semesterId;

    @Column(name = "start_date")
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    @Column(name = "capacity")
    private Integer capacity;

    @Column(name = "request_date")
    private LocalDate requestDate;

    @Column(name = "observation")
    private String observation;

    @Column(name = "accepted")
    private Boolean accepted;

    @Column(name = "combined")
    private Boolean combined;

    @OneToMany(mappedBy = "academicRequest", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    private List<RequestSchedule> schedules;

    @PrePersist
    public void onCreate() {
        if (requestDate == null) requestDate = LocalDate.now();
        if (accepted == null) accepted = false;
        if (combined == null) combined = false;
    }
}
