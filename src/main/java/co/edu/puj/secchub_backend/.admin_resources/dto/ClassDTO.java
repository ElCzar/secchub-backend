package co.edu.puj.secchub_backend.admin_resources.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.List;

/**
 * DTO (Data Transfer Object) para la gestión de clases académicas.
 * 
 * <p>Esta clase representa una clase académica completa en el sistema de planificación
 * educativa. Incluye información del curso, semestre, horarios y profesores asignados.
 * Es la entidad principal para la gestión de la oferta académica semestral.</p>
 * 
 * <p><strong>Funcionalidades principales:</strong></p>
 * <ul>
 *   <li>Gestión de clases académicas por semestre</li>
 *   <li>Asignación de cursos y capacidades</li>
 *   <li>Seguimiento de estado y observaciones</li>
 *   <li>Integración con horarios y profesores</li>
 * </ul>
 * 
 * <p><strong>Relaciones:</strong></p>
 * <ul>
 *   <li>Una clase pertenece a un curso específico</li>
 *   <li>Una clase se imparte en un semestre determinado</li>
 *   <li>Una clase puede tener múltiples horarios</li>
 *   <li>Una clase puede tener múltiples profesores asignados</li>
 * </ul>
 * 
 * @author SecHub Development Team
 * @version 1.0
 * @since 2024-09-11
 * 
 * @see co.edu.puj.secchub_backend.admin_resources.model.AcademicClass
 * @see co.edu.puj.secchub_backend.admin_resources.controller.PlanningController
 * @see ClassScheduleDTO
 * @see TeacherDTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClassDTO {
    
    /**
     * Identificador único de la clase académica.
     * Se genera automáticamente al crear una nueva clase.
     */
    private Long id;
    
    /**
     * Identificador del curso al que pertenece esta clase.
     * Referencia a la entidad Course en la base de datos.
     * 
     * @see #courseName
     * @see #courseDescription
     * @see #courseCredits
     */
    private Long courseId;
    
    /**
     * Nombre completo del curso.
     * Ejemplo: "Bases de Datos", "Ingeniería de Software", "Estructuras de Datos".
     */
    private String courseName;
    
    /**
     * Descripción detallada del contenido y objetivos del curso.
     * Proporciona información sobre los temas que se cubrirán.
     */
    private String courseDescription;
    
    /**
     * Número de créditos académicos que otorga el curso.
     * Determina la carga académica y el valor curricular.
     * 
     * @implNote Generalmente entre 1 y 6 créditos para cursos regulares.
     */
    private Integer courseCredits;
    
    /**
     * Identificador del semestre en que se imparte la clase.
     * Referencia a la entidad Semester en la base de datos.
     * 
     * @see #semesterPeriod
     * @see #semesterYear
     */
    private Long semesterId;
    
    /**
     * Período del semestre (1 = primer semestre, 2 = segundo semestre).
     * Junto con {@link #semesterYear} identifica el período académico.
     */
    private Integer semesterPeriod;
    
    /**
     * Año del semestre académico.
     * Ejemplo: 2024, 2025.
     */
    private Integer semesterYear;
    
    /**
     * Fecha de inicio de las clases.
     * Marca el comienzo del período académico para esta clase específica.
     * 
     * @implNote Formato: yyyy-MM-dd
     */
    private java.time.LocalDate startDate;
    
    /**
     * Fecha de finalización de las clases.
     * Marca el final del período académico, excluyendo exámenes finales.
     * 
     * @implNote Debe ser posterior a {@link #startDate}
     */
    private java.time.LocalDate endDate;
    
    /**
     * Observaciones o notas especiales sobre la clase.
     * 
     * <p>Puede incluir información sobre:</p>
     * <ul>
     *   <li>Requisitos especiales</li>
     *   <li>Metodología particular</li>
     *   <li>Recursos necesarios</li>
     *   <li>Restricciones o consideraciones</li>
     * </ul>
     */
    private String observation;
    
    /**
     * Capacidad máxima de estudiantes que pueden inscribirse en la clase.
     * 
     * <p>Este límite se determina por:</p>
     * <ul>
     *   <li>Capacidad del aula asignada</li>
     *   <li>Metodología del curso</li>
     *   <li>Recursos disponibles</li>
     *   <li>Política institucional</li>
     * </ul>
     */
    private Integer capacity;
    
    /**
     * Identificador del estado actual de la clase.
     * Referencia a la entidad Status en la base de datos.
     * 
     * @see #statusName
     */
    private Long statusId;
    
    /**
     * Nombre descriptivo del estado de la clase.
     * 
     * <p>Estados comunes:</p>
     * <ul>
     *   <li>"Activa" - Clase disponible para inscripción</li>
     *   <li>"Suspendida" - Temporalmente no disponible</li>
     *   <li>"Cancelada" - No se ofrecerá en este período</li>
     *   <li>"Completa" - Capacidad máxima alcanzada</li>
     * </ul>
     */
    private String statusName;
    
    /**
     * Nombre de la sección académica a la que pertenece el curso.
     * Ejemplo: "Ciencias de la Computación", "Matemáticas", "Física".
     */
    private String sectionName;
    
    /**
     * Lista de horarios programados para esta clase.
     * 
     * <p>Una clase puede tener múltiples horarios para:</p>
     * <ul>
     *   <li>Diferentes días de la semana</li>
     *   <li>Clases teóricas y prácticas</li>
     *   <li>Sesiones de laboratorio</li>
     * </ul>
     * 
     * @see ClassScheduleDTO
     */
    private List<ClassScheduleDTO> schedules;
    
    /**
     * Lista de profesores asignados a esta clase.
     * 
     * <p>Una clase puede tener múltiples profesores para:</p>
     * <ul>
     *   <li>Co-enseñanza (team teaching)</li>
     *   <li>Profesor principal y asistentes</li>
     *   <li>Especialistas para diferentes módulos</li>
     * </ul>
     * 
     * @see TeacherDTO
     */
    private List<TeacherDTO> teachers;
}
