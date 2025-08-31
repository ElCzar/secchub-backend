package co.edu.puj.secchub_backend.integration.dto;

import lombok.*;

/**
 * DTO que representa un horario asociado a una solicitud académica.
 * Incluye información sobre el día, hora, tipo de aula, modalidad y accesibilidad.
 */
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class RequestScheduleDTO {
    /**
     * Identificador único del horario.
     */
    private Long id;              // 👈 agrega este campo
    /**
     * Día de la semana del horario.
     */
    private String day;
    /**
     * Hora de inicio en formato "HH:mm:ss".
     */
    private String startTime;
    /**
     * Hora de fin en formato "HH:mm:ss".
     */
    private String endTime;
    /**
     * Identificador del tipo de aula requerida.
     */
    private Long classroomTypeId;
    /**
     * Identificador de la modalidad (presencial, virtual, etc.).
     */
    private Long modalityId;
    /**
     * Indica si se requiere accesibilidad para discapacidad.
     */
    private Boolean disability;
}

