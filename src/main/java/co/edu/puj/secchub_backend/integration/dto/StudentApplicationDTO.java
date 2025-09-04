package co.edu.puj.secchub_backend.integration.dto;

import lombok.*;
import java.time.LocalDate;
import java.util.List;

/**
 * DTO que representa una solicitud de monitoría realizada por un estudiante.
 * Incluye información sobre el usuario, semestre, tipo de monitoría, curso/sección, promedios, docente, fecha, estado y horarios.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StudentApplicationDTO {
    /** Identificador único de la solicitud. */
    private Long id;
    /** Identificador del usuario que realiza la solicitud. */
    private Long userId;
    /** Identificador del semestre. */
    private Long semesterId;
    /** Tipo de monitoría: "ACADEMIC" o "ADMINISTRATIVE". */
    private String type;
    /** Identificador del curso (null si es administrativo). */
    private Long courseId;
    /** Identificador de la sección (null si es académico). */
    private Long sectionId;
    /** Promedio del curso. */
    private Double courseAverage;
    /** Docente del curso. */
    private String courseTeacher;
    /** Fecha de la solicitud. */
    private LocalDate applicationDate;
    /** Identificador del estado de la solicitud. */
    private Long statusId;
    /** Lista de horarios asociados a la solicitud. */
    private List<ScheduleDTO> schedules;
}

