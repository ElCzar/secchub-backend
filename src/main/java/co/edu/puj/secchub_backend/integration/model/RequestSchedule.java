package co.edu.puj.secchub_backend.integration.model;



import jakarta.persistence.*;
import lombok.*;

/**
 * Entidad que representa un bloque de horario solicitado para una solicitud académica.
 * Se mapea a la tabla 'horarios_solicitud' y almacena información sobre el día, hora, tipo de aula, modalidad y accesibilidad.
 */
@Entity
@Table(name = "request_schedule")
@Getter @Setter @Builder
@NoArgsConstructor @AllArgsConstructor
public class RequestSchedule {

    /**
     * Identificador único del horario.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Identificador de la solicitud académica asociada.
     */
    @Column(name = "academic_request_id", nullable = true)
    private Long academicRequestId;

    /**
     * Identificador del tipo de aula requerida.
     */
    @Column(name = "classroom_type_id")
    private Long classroomTypeId;

    /**
     * Hora de inicio del bloque horario.
     */
    @Column(name = "start_time", nullable = true)
    private java.sql.Time startTime;

    /**
     * Hora de fin del bloque horario.
     */
    @Column(name = "end_time", nullable = true)
    private java.sql.Time endTime;

    /**
     * Día de la semana del bloque horario.
     */
    @Column(name = "day", nullable = true)
    private String day;

    /**
     * Indica si se requiere accesibilidad para discapacidad.
     */
    @Column(name = "disability")
    private Boolean disability;

    /**
     * Identificador de la modalidad (presencial, virtual, etc.).
     */
    @Column(name = "modality_id")
    private Long modalityId;

    /**
     * Referencia a la solicitud académica asociada.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "academic_request_id", insertable = false, updatable = false)
    private AcademicRequest academicRequest;

    /**
     * Builder personalizado para asignar horas en formato entero HHmm.
     */
    @Builder
    public static class RequestScheduleBuilder {
        /**
         * Asigna la hora de inicio a partir de un entero HHmm.
         */
        public RequestScheduleBuilder startTime(int timeInt) {
            this.startTime = intToSqlTime(timeInt);
            return this;
        }
        /**
         * Asigna la hora de fin a partir de un entero HHmm.
         */
        public RequestScheduleBuilder endTime(int timeInt) {
            this.endTime = intToSqlTime(timeInt);
            return this;
        }
        /**
         * Convierte un entero HHmm a java.sql.Time.
         */
        private java.sql.Time intToSqlTime(int timeInt) {
            int hour = timeInt / 100;
            int minute = timeInt % 100;
            return java.sql.Time.valueOf(String.format("%02d:%02d:00", hour, minute));
        }
    }
}
