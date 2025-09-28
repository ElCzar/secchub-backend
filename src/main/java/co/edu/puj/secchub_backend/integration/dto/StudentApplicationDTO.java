package co.edu.puj.secchub_backend.integration.dto;

import lombok.*;
import java.time.LocalDate;
import java.util.List;

/**
 * DTO que representa una solicitud de monitoría realizada por un estudiante.
 * Incluye información completa del estudiante y su solicitud según la tabla student.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StudentApplicationDTO {
    /** Identificador único de la solicitud (será ignorado en la creación). */
    private Long id;
    
    /** Identificador del usuario que realiza la solicitud (se asigna automáticamente). */
    private Long userId;
    
    /** Identificador del curso (solo para monitores académicos). */
    private Long courseId;
    
    /** Identificador de la sección (solo para monitores administrativos). */
    private Long sectionId;
    
    /** Programa académico del estudiante. */
    private String program;
    
    /** Semestre actual del estudiante. */
    private Integer semester;
    
    /** Promedio académico general del estudiante. */
    private Double academicAverage;
    
    /** Número de teléfono principal. */
    private String phoneNumber;
    
    /** Número de teléfono alternativo. */
    private String alternatePhoneNumber;
    
    /** Dirección de residencia. */
    private String address;
    
    /** Correo electrónico personal. */
    private String personalEmail;
    
    /** Indica si el estudiante ya fue monitor antes. */
    private Boolean wasTeachingAssistant;
    
    /** Promedio en el curso específico (para monitores académicos). */
    private Double courseAverage;
    
    /** Docente del curso (para monitores académicos). */
    private String courseTeacher;
    
    /** Fecha de la solicitud (se asigna automáticamente). */
    private LocalDate applicationDate;
    
    /** Identificador del estado de la solicitud (se asigna automáticamente). */
    private Long statusId;
    
    /** Lista de horarios asociados a la solicitud. */
    private List<ScheduleDTO> schedules;
}

