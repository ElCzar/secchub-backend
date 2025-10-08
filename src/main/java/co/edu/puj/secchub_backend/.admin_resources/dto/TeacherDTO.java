package co.edu.puj.secchub_backend.admin_resources.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO (Data Transfer Object) para la gestión integral de profesores y sus asignaciones.
 * 
 * <p>Esta clase combina información de múltiples entidades relacionadas con los profesores:
 * datos personales del usuario, información laboral, asignaciones de clases y estadísticas
 * de carga horaria. Es utilizada tanto para operaciones de consulta como para la gestión
 * de asignaciones docentes.</p>
 * 
 * <p><strong>Funcionalidades principales:</strong></p>
 * <ul>
 *   <li>Gestión de información personal y académica de profesores</li>
 *   <li>Control de tipos de vinculación laboral</li>
 *   <li>Asignación y seguimiento de clases</li>
 *   <li>Cálculo automático de carga horaria</li>
 *   <li>Gestión de decisiones sobre asignaciones</li>
 * </ul>
 * 
 * <p><strong>Entidades relacionadas:</strong></p>
 * <ul>
 *   <li>Teacher - Información específica del docente</li>
 *   <li>User - Datos personales y de acceso</li>
 *   <li>TeacherAssignment - Asignaciones a clases específicas</li>
 * </ul>
 * 
 * @author SecHub Development Team
 * @version 1.0
 * @since 2024-09-11
 * 
 * @see co.edu.puj.secchub_backend.admin.model.Teacher
 * @see co.edu.puj.secchub_backend.admin_resources.model.TeacherAssignment
 * @see co.edu.puj.secchub_backend.admin_resources.controller.TeacherAssignmentController
 * @see ClassDTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TeacherDTO {
    
    // ==========================================
    // INFORMACIÓN BÁSICA DEL PROFESOR
    // ==========================================
    
    /**
     * Identificador único del profesor en la tabla Teacher.
     * Se genera automáticamente al registrar un nuevo profesor.
     */
    private Long id;
    
    /**
     * Identificador del usuario asociado al profesor.
     * Referencia a la tabla Users para obtener información personal.
     * 
     * @see #username
     * @see #name
     * @see #lastName
     * @see #email
     */
    private Long userId;
    
    /**
     * Identificador del tipo de vinculación laboral.
     * Referencia a la tabla EmploymentType.
     * 
     * @see #employmentTypeName
     */
    private Long employmentTypeId;
    
    /**
     * Nombre descriptivo del tipo de vinculación laboral.
     * 
     * <p>Tipos comunes:</p>
     * <ul>
     *   <li>"Tiempo Completo" - Dedicación exclusiva</li>
     *   <li>"Medio Tiempo" - Dedicación parcial</li>
     *   <li>"Cátedra" - Por horas específicas</li>
     * </ul>
     */
    private String employmentTypeName;
    
    /**
     * Número máximo de horas que el profesor puede dictar por semestre.
     * 
     * <p>Este límite se establece según:</p>
     * <ul>
     *   <li>Tipo de vinculación laboral</li>
     *   <li>Política institucional</li>
     *   <li>Normativa académica</li>
     *   <li>Disponibilidad del profesor</li>
     * </ul>
     */
    private Integer maxHours;
    
    // ==========================================
    // INFORMACIÓN PERSONAL DEL USUARIO
    // ==========================================
    
    /**
     * Nombre de usuario para acceso al sistema.
     * Debe ser único en toda la plataforma.
     */
    private String username;
    
    /**
     * Facultad o departamento académico al que pertenece el profesor.
     * Ejemplo: "Ingeniería", "Ciencias", "Humanidades".
     */
    private String faculty;
    
    /**
     * Nombre(s) del profesor.
     * Información personal extraída de la tabla Users.
     */
    private String name;
    
    /**
     * Apellido(s) del profesor.
     * Información personal extraída de la tabla Users.
     */
    private String lastName;
    
    /**
     * Dirección de correo electrónico institucional o personal.
     * Utilizada para comunicaciones oficiales y notificaciones.
     */
    private String email;
    
    /**
     * Identificador del estado actual del usuario.
     * Referencia a la tabla Status.
     * 
     * @see #statusName
     */
    private Long statusId;
    
    /**
     * Nombre descriptivo del estado del usuario.
     * Ejemplo: "Activo", "Inactivo", "Suspendido", "En licencia".
     */
    private String statusName;
    
    /**
     * Fecha y hora del último acceso del profesor al sistema.
     * Utilizada para auditoría y seguimiento de actividad.
     */
    private LocalDateTime lastAccess;
    
    /**
     * Identificador del rol del usuario en el sistema.
     * Referencia a la tabla Role.
     * 
     * @see #roleName
     */
    private Long roleId;
    
    /**
     * Nombre descriptivo del rol del usuario.
     * Ejemplo: "ROLE_TEACHER", "ROLE_ADMIN", "ROLE_COORDINATOR".
     */
    private String roleName;
    
    /**
     * Identificador del tipo de documento de identidad.
     * Referencia a la tabla DocumentType.
     * 
     * @see #documentTypeName
     * @see #documentNumber
     */
    private Long documentTypeId;
    
    /**
     * Nombre descriptivo del tipo de documento.
     * Ejemplo: "CC" (Cédula de Ciudadanía), "CE" (Cédula de Extranjería), "Pasaporte".
     */
    private String documentTypeName;
    
    /**
     * Número del documento de identidad del profesor.
     * Información confidencial utilizada para identificación oficial.
     */
    private String documentNumber;
    
    // ==========================================
    // INFORMACIÓN DE ASIGNACIÓN A CLASES
    // ==========================================
    
    /**
     * Identificador único de la asignación profesor-clase.
     * Referencia a la tabla TeacherAssignment (anteriormente teacher_class).
     */
    private Long teacherClassId;
    
    /**
     * Identificador de la clase asignada al profesor.
     * Referencia a {@link ClassDTO#getId()}.
     */
    private Long classId;
    
    /**
     * Número de horas semanales asignadas para esta clase específica.
     * Contribuye al cálculo de la carga horaria total del profesor.
     */
    private Integer workHours;
    
    /**
     * Horas extra para profesores de tiempo completo.
     * Horas adicionales que exceden la carga regular de tiempo completo.
     */
    private Integer fullTimeExtraHours;
    
    /**
     * Horas extra para profesores de cátedra.
     * Horas adicionales que exceden la carga regular de cátedra.
     */
    private Integer adjunctExtraHours;
    
    /**
     * Decisión del profesor sobre la asignación propuesta.
     * 
     * <p>Valores posibles:</p>
     * <ul>
     *   <li>{@code null} - Pendiente de respuesta</li>
     *   <li>{@code true} - Asignación aceptada</li>
     *   <li>{@code false} - Asignación rechazada</li>
     * </ul>
     */
    private Boolean decision;
    
    /**
     * Observaciones o comentarios sobre la asignación.
     * 
     * <p>Puede incluir:</p>
     * <ul>
     *   <li>Razones para aceptar/rechazar</li>
     *   <li>Condiciones especiales</li>
     *   <li>Comentarios administrativos</li>
     *   <li>Notas sobre disponibilidad</li>
     * </ul>
     */
    private String observation;
    
    /**
     * Identificador del estado de la asignación.
     * Referencia a la tabla Status.
     * 
     * @see #assignmentStatusName
     */
    private Long assignmentStatusId;
    
    /**
     * Nombre descriptivo del estado de la asignación.
     * 
     * <p>Estados típicos:</p>
     * <ul>
     *   <li>"Pendiente" - Esperando respuesta del profesor</li>
     *   <li>"Aceptada" - Profesor confirmó la asignación</li>
     *   <li>"Rechazada" - Profesor declinó la asignación</li>
     *   <li>"Confirmada" - Asignación aprobada administrativamente</li>
     * </ul>
     */
    private String assignmentStatusName;
    
    // ==========================================
    // CAMPOS CALCULADOS Y DERIVADOS
    // ==========================================
    
    /**
     * Nombre completo del profesor.
     * Concatenación de {@link #name} y {@link #lastName}.
     * 
     * @implNote Se calcula automáticamente: "{name} {lastName}"
     */
    private String fullName;
    
    /**
     * Total de horas asignadas al profesor en el semestre actual.
     * 
     * <p>Incluye:</p>
     * <ul>
     *   <li>Horas regulares de todas las clases asignadas</li>
     *   <li>Horas extra de tiempo completo</li>
     *   <li>Horas extra de cátedra</li>
     * </ul>
     * 
     * @implNote Se calcula como: workHours + fullTimeExtraHours + adjunctExtraHours
     */
    private Integer totalHours;
    
    /**
     * Número de horas disponibles del profesor.
     * 
     * <p>Diferencia entre las horas máximas permitidas y las horas ya asignadas:</p>
     * <pre>availableHours = maxHours - totalHours</pre>
     * 
     * <p>Utilizada para:</p>
     * <ul>
     *   <li>Determinar si el profesor puede aceptar más asignaciones</li>
     *   <li>Generar reportes de disponibilidad</li>
     *   <li>Optimizar la distribución de carga académica</li>
     * </ul>
     */
    private Integer availableHours;
}
