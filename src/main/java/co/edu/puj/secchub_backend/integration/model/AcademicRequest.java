package co.edu.puj.secchub_backend.integration.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonManagedReference;

/**
 * Entidad que representa una solicitud académica en la tabla 'academic_request'.
 * Cada instancia corresponde a una solicitud de curso para un usuario y semestre específico.
 */
@Entity
@Table(name = "academic_request")
@Getter @Setter @Builder
@NoArgsConstructor @AllArgsConstructor
public class AcademicRequest {
    /** Identificador único de la solicitud académica. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Identificador del usuario que realiza la solicitud. */
    @Column(name = "user_id")
    private Long userId;

    /** Identificador del curso solicitado. */
    @Column(name = "course_id")
    private Long courseId;

    /** Identificador del semestre al que pertenece la solicitud. */
    @Column(name = "semester_id")
    private Long semesterId;

    /** Fecha de inicio de la solicitud. */
    @Column(name = "start_date")
    private LocalDate startDate;

    /** Fecha de fin de la solicitud. */
    @Column(name = "end_date")
    private LocalDate endDate;

    /** Cupo solicitado para el curso. */
    @Column(name = "capacity")
    private Integer capacity;

    /** Fecha en que se realiza la solicitud. */
    @Column(name = "request_date")
    private LocalDate requestDate;

    /** Observaciones adicionales de la solicitud. */
    @Column(name = "observation")
    private String observation;

    /** Lista de horarios asociados a la solicitud académica. */
    @OneToMany(mappedBy = "academicRequest", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    private List<RequestSchedule> schedules;

    /**
     * Asigna la fecha de solicitud al momento de persistir si no está definida.
     */
    @PrePersist
    public void onCreate() {
        if (requestDate == null) requestDate = LocalDate.now();
    }
}
