package co.edu.puj.secchub_backend.integration.dto;

import lombok.Data;

import java.time.LocalDate;
import java.util.List;

/**
 * DTO que representa una solicitud académica para un curso específico.
 * Se utiliza para transferir datos entre la UI y el backend, y se convierte en una fila en la tabla 'solicitud_academica'.
 */
@Data
public class AcademicRequestItemDTO {
    /**
     * Identificador del curso solicitado.
     */
    private Long courseId;
    /**
     * Nombre del curso (opcional, para mostrar en la UI).
     */
    private String courseName;      // optional, for UI echo
    /**
     * Sección del curso (auto-completada en la UI).
     */
    private String section;         // auto-completed on UI from course
    /**
     * Identificador del tipo de aula solicitada a nivel de curso.
     */
    private Long classroomTypeId;   // requested type at course level
    /**
     * Cupo solicitado para el curso.
     */
    private Integer requestedQuota;
    /**
     * Fecha de inicio de la solicitud.
     */
    private LocalDate startDate;
    /**
     * Fecha de fin de la solicitud.
     */
    private LocalDate endDate;
    /**
     * Número de semanas solicitadas (opcional; el backend lo calcula si es null).
     */
    private Integer weeks;          // optional; backend will compute if null
    /**
     * Observaciones adicionales de la solicitud.
     */
    private String observation;
    /**
     * Lista de horarios asociados a la solicitud (debe tener al menos uno).
     */
    private List<RequestScheduleDTO> schedules; // must have >= 1
}
