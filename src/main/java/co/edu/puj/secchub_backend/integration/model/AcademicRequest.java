package co.edu.puj.secchub_backend.integration.model;


import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Entidad que representa una solicitud académica en la tabla 'solicitud_academica'.
 * Cada instancia corresponde a una solicitud de curso para un usuario y semestre específico.
 */
@Entity
@Table(name = "academic_request")
@Getter @Setter @Builder
@NoArgsConstructor @AllArgsConstructor
public class AcademicRequest {

    /**
     * Identificador único de la solicitud académica.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Identificador del usuario que realiza la solicitud.
     */
    @Column(name = "user_id", nullable = true)
    private Long userId;

    /**
     * Identificador del curso solicitado.
     */
    @Column(name = "course_id", nullable = true)
    private Long courseId;

    /**
     * Identificador del semestre al que pertenece la solicitud.
     */
    @Column(name = "semester_id", nullable = true)
    private Long semesterId;

    /**
     * Fecha en que se realiza la solicitud.
     */
    @Column(name = "request_date")
    private LocalDate requestDate;

    /**
     * Observaciones adicionales de la solicitud.
     */
    @Column(name = "observation")
    private String observation;

    /**
     * Sección del curso.
     */
    @Column(name = "section")
    private String section;

    /**
     * Identificador del tipo de aula requerida.
     */
    @Column(name = "classroom_type_id")
    private Long classroomTypeId;

    /**
     * Cupo solicitado para el curso.
     */
    @Column(name = "requested_quota")
    private Integer requestedQuota;

    /**
     * Fecha de inicio de la solicitud.
     */
    @Column(name = "start_date")
    private LocalDate startDate;

    /**
     * Fecha de fin de la solicitud.
     */
    @Column(name = "end_date")
    private LocalDate endDate;

    /**
     * Número de semanas solicitadas.
     */
    @Column(name = "weeks")
    private Integer weeks;

    /**
     * Lista de horarios asociados a la solicitud académica.
     */
    @OneToMany(mappedBy = "academicRequest", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<RequestSchedule> schedules;

    /**
     * Asigna la fecha de solicitud al momento de persistir si no está definida.
     */
    @PrePersist
    public void onCreate() {
        if (requestDate == null) requestDate = LocalDate.now();
    }
}
