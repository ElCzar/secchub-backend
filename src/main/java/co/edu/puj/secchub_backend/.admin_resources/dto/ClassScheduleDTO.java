package co.edu.puj.secchub_backend.admin_resources.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalTime;

/**
 * DTO (Data Transfer Object) para la gestión de horarios de clases académicas.
 * 
 * <p>Esta clase representa la información completa de un horario de clase,
 * incluyendo los datos del aula asignada, modalidad y configuración de accesibilidad.
 * Es utilizada para transferir información entre las capas de la aplicación
 * y para la comunicación con clientes externos a través de la API REST.</p>
 * 
 * <p><strong>Funcionalidades principales:</strong></p>
 * <ul>
 *   <li>Gestión de horarios semanales para clases académicas</li>
 *   <li>Asignación de aulas específicas con información detallada</li>
 *   <li>Configuración de modalidad (presencial/virtual)</li>
 *   <li>Soporte para accesibilidad y necesidades especiales</li>
 * </ul>
 * 
 * @author SecHub Development Team
 * @version 1.0
 * @since 2024-09-11
 * 
 * @see co.edu.puj.secchub_backend.admin_resources.model.ClassSchedule
 * @see co.edu.puj.secchub_backend.admin_resources.controller.PlanningController
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClassScheduleDTO {
    
    /**
     * Identificador único del horario de clase.
     * Se genera automáticamente al crear un nuevo horario.
     */
    private Long id;
    
    /**
     * Identificador de la clase académica a la que pertenece este horario.
     * Referencia a {@link ClassDTO#getId()}.
     */
    private Long classId;
    
    /**
     * Identificador del aula asignada para este horario.
     * Referencia a la entidad Classroom en la base de datos.
     */
    private Long classroomId;
    
    /**
     * Ubicación específica del aula (edificio, piso, etc.).
     * Información descriptiva para facilitar la localización.
     */
    private String classroomLocation;
    
    /**
     * Número o nombre específico del aula.
     * Ejemplo: "101", "Laboratorio A", "Auditorio Principal".
     */
    private String classroomRoom;
    
    /**
     * Campus donde se encuentra el aula.
     * Ejemplo: "Campus Principal", "Campus Norte", "Sede Centro".
     */
    private String classroomCampus;
    
    /**
     * Capacidad máxima de estudiantes que puede albergar el aula.
     * Utilizado para validar que no se excedan los límites de ocupación.
     */
    private Integer classroomCapacity;
    
    /**
     * Tipo de aula según su equipamiento y propósito.
     * Ejemplo: "Aula Magistral", "Laboratorio", "Sala de Conferencias".
     */
    private String classroomTypeName;
    
    /**
     * Día de la semana en que se imparte la clase.
     * Formato esperado: "Monday", "Tuesday", "Wednesday", etc.
     * 
     * @implNote Se recomienda usar valores estandarizados en inglés
     *           para mantener consistencia con estándares internacionales.
     */
    private String day;
    
    /**
     * Hora de inicio de la clase.
     * Formato: HH:mm (24 horas).
     * 
     * @example 08:00, 14:30, 18:15
     */
    private LocalTime startTime;
    
    /**
     * Hora de finalización de la clase.
     * Formato: HH:mm (24 horas).
     * Debe ser posterior a {@link #startTime}.
     * 
     * @example 10:00, 16:30, 20:15
     */
    private LocalTime endTime;
    
    /**
     * Identificador de la modalidad de enseñanza.
     * Referencia a la entidad Modality en la base de datos.
     * 
     * @see #modalityName
     */
    private Long modalityId;
    
    /**
     * Nombre descriptivo de la modalidad de enseñanza.
     * Ejemplo: "Presencial", "Virtual", "Híbrida".
     */
    private String modalityName;
    
    /**
     * Indica si el horario está configurado para personas con discapacidad.
     * 
     * <p>Cuando es {@code true}, indica que:</p>
     * <ul>
     *   <li>El aula cuenta con accesibilidad especial</li>
     *   <li>Se han realizado adaptaciones curriculares</li>
     *   <li>Hay recursos de apoyo disponibles</li>
     * </ul>
     * 
     * @default false
     */
    private Boolean disability;
}
