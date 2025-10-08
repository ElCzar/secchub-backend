package co.edu.puj.secchub_backend.admin_resources.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO (Data Transfer Object) para la gestión de asignaciones profesor-clase.
 * 
 * <p>Esta clase representa una asignación específica de un profesor a una clase académica,
 * incluyendo toda la información relevante sobre el profesor, la clase y los detalles
 * de la asignación. Es utilizada para operaciones que requieren una vista completa
 * de la relación profesor-clase.</p>
 * 
 * <p><strong>Funcionalidades principales:</strong></p>
 * <ul>
 *   <li>Gestión completa de asignaciones docentes</li>
 *   <li>Seguimiento de decisiones y estados</li>
 *   <li>Cálculo de cargas horarias específicas</li>
 *   <li>Información contextual de profesor y clase</li>
 * </ul>
 * 
 * <p><strong>Casos de uso:</strong></p>
 * <ul>
 *   <li>Reportes detallados de asignaciones</li>
 *   <li>Procesos de aprobación/rechazo</li>
 *   <li>Análisis de carga académica</li>
 *   <li>Comunicación con profesores</li>
 * </ul>
 * 
 * @author SecHub Development Team
 * @version 1.0
 * @since 2024-09-11
 * 
 * @see co.edu.puj.secchub_backend.admin_resources.model.TeacherAssignment
 * @see TeacherDTO
 * @see ClassDTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TeacherAssignmentDTO {
    
    // ==========================================
    // INFORMACIÓN DE LA ASIGNACIÓN
    // ==========================================
    
    /**
     * Identificador único de la asignación.
     * Se genera automáticamente al crear una nueva asignación profesor-clase.
     */
    private Long id;
    
    /**
     * Identificador del profesor asignado.
     * Referencia a {@link TeacherDTO#getId()}.
     */
    private Long teacherId;
    
    /**
     * Identificador de la clase asignada.
     * Referencia a {@link ClassDTO#getId()}.
     */
    private Long classId;
    
    /**
     * Número de horas semanales regulares asignadas para esta clase.
     * Representa la carga horaria base del profesor para esta asignación específica.
     */
    private Integer workHours;
    
    /**
     * Horas extra asignadas para profesores de tiempo completo.
     * Horas adicionales que exceden la carga regular de tiempo completo para esta clase.
     */
    private Integer fullTimeExtraHours;
    
    /**
     * Horas extra asignadas para profesores de cátedra/adjuntos.
     * Horas adicionales que exceden la carga regular de cátedra para esta clase.
     */
    private Integer adjunctExtraHours;
    
    /**
     * Decisión del profesor sobre esta asignación específica.
     * 
     * <p>Valores posibles:</p>
     * <ul>
     *   <li>{@code null} - Pendiente de respuesta del profesor</li>
     *   <li>{@code true} - Asignación aceptada por el profesor</li>
     *   <li>{@code false} - Asignación rechazada por el profesor</li>
     * </ul>
     */
    private Boolean decision;
    
    /**
     * Observaciones específicas sobre esta asignación.
     * 
     * <p>Puede incluir:</p>
     * <ul>
     *   <li>Razones para la decisión del profesor</li>
     *   <li>Condiciones especiales para la asignación</li>
     *   <li>Comentarios administrativos</li>
     *   <li>Notas sobre coordinación</li>
     * </ul>
     */
    private String observation;
    
    /**
     * Identificador del estado actual de la asignación.
     * Referencia a la tabla Status.
     * 
     * @see #statusName
     */
    private Long statusId;
    
    /**
     * Nombre descriptivo del estado de la asignación.
     * 
     * <p>Estados típicos:</p>
     * <ul>
     *   <li>"Pendiente" - Esperando respuesta del profesor</li>
     *   <li>"Aceptada" - Confirmada por el profesor</li>
     *   <li>"Rechazada" - Declinada por el profesor</li>
     *   <li>"Aprobada" - Validada administrativamente</li>
     * </ul>
     */
    private String statusName;
    
    // ==========================================
    // INFORMACIÓN DEL PROFESOR
    // ==========================================
    
    /**
     * Nombre del profesor asignado.
     * Información extraída del perfil del profesor para facilitar la visualización.
     */
    private String teacherName;
    
    /**
     * Apellido del profesor asignado.
     * Información extraída del perfil del profesor para facilitar la visualización.
     */
    private String teacherLastName;
    
    /**
     * Correo electrónico del profesor.
     * Utilizado para comunicaciones relacionadas con esta asignación específica.
     */
    private String teacherEmail;
    
    /**
     * Tipo de vinculación laboral del profesor.
     * Ejemplo: "Tiempo Completo", "Medio Tiempo", "Cátedra".
     * Influye en el cálculo de horas y restricciones.
     */
    private String employmentTypeName;
    
    /**
     * Número máximo de horas que puede dictar el profesor por semestre.
     * Límite establecido según el tipo de vinculación y políticas institucionales.
     */
    private Integer teacherMaxHours;
    
    /**
     * Horas disponibles del profesor después de todas sus asignaciones.
     * Utilizado para determinar la viabilidad de asignaciones adicionales.
     */
    private Integer teacherAvailableHours;
    
    // ==========================================
    // INFORMACIÓN DE LA CLASE
    // ==========================================
    
    /**
     * Nombre del curso al que pertenece la clase asignada.
     * Ejemplo: "Bases de Datos", "Ingeniería de Software".
     */
    private String courseName;
    
    /**
     * Descripción detallada del curso.
     * Proporciona contexto sobre el contenido y objetivos del curso.
     */
    private String courseDescription;
    
    /**
     * Número de créditos académicos del curso.
     * Indica la importancia y carga académica del curso.
     */
    private Integer courseCredits;
    
    /**
     * Identificación del semestre académico.
     * Ejemplo: "2024-1", "2025-2".
     */
    private String semesterName;
    
    /**
     * Capacidad máxima de estudiantes para la clase asignada.
     * Información relevante para la planificación y gestión del aula.
     */
    private Integer classCapacity;
    
    /**
     * Nombre específico o identificador de la clase.
     * Puede incluir grupo, sección o denominación particular.
     */
    private String className;
    
    // ==========================================
    // MÉTODOS UTILITARIOS PARA CAMPOS CALCULADOS
    // ==========================================
    
    /**
     * Obtiene el nombre completo del profesor.
     * 
     * @return Concatenación de nombre y apellido, o null si alguno falta
     */
    public String getTeacherFullName() {
        if (teacherName != null && teacherLastName != null) {
            return teacherName + " " + teacherLastName;
        }
        return null;
    }
    
    /**
     * Calcula el total de horas extra asignadas.
     * 
     * @return Suma de horas extra de tiempo completo y cátedra
     */
    public Integer getTotalExtraHours() {
        int fullTime = fullTimeExtraHours != null ? fullTimeExtraHours : 0;
        int adjunct = adjunctExtraHours != null ? adjunctExtraHours : 0;
        return fullTime + adjunct;
    }
    
    /**
     * Obtiene una representación textual de la decisión del profesor.
     * 
     * @return "Pendiente", "Aceptada" o "Rechazada" según el valor de decision
     */
    public String getDecisionText() {
        if (decision == null) return "Pendiente";
        return decision ? "Aceptada" : "Rechazada";
    }
    
    /**
     * Verifica si la asignación está en estado pendiente.
     * 
     * @return true si el statusId corresponde al estado pendiente (ID = 1)
     */
    public Boolean getIsPending() {
        return statusId != null && statusId == 1L;
    }
    
    /**
     * Verifica si la asignación ha sido aceptada.
     * 
     * @return true si el statusId corresponde al estado aceptada (ID = 2)
     */
    public Boolean getIsAccepted() {
        return statusId != null && statusId == 2L;
    }
    
    /**
     * Verifica si la asignación ha sido rechazada.
     * 
     * @return true si el statusId corresponde al estado rechazada (ID = 3)
     */
    public Boolean getIsRejected() {
        return statusId != null && statusId == 3L;
    }
}
