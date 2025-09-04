package co.edu.puj.secchub_backend.integration.dto;
import lombok.*;

/**
 * DTO que representa un horario simple con día y horas de inicio y fin.
 * Utilizado para transferir información de bloques de horario.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ScheduleDTO {
    /** Día de la semana del horario. */
    private String day;
    /** Hora de inicio en formato "HH:mm:ss". */
    private String startTime; // HH:mm:ss
    /** Hora de fin en formato "HH:mm:ss". */
    private String endTime;   // HH:mm:ss
}
