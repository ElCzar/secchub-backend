package co.edu.puj.secchub_backend.admin_resources.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO (Data Transfer Object) para la gestión de aulas.
 * 
 * <p>Esta clase representa la información completa de un aula,
 * incluyendo su ubicación, capacidad, tipo y características.
 * Es utilizada para transferir información entre las capas de la aplicación
 * y para la comunicación con clientes externos a través de la API REST.</p>
 * 
 * <p><strong>Funcionalidades principales:</strong></p>
 * <ul>
 *   <li>Gestión de espacios físicos para clases académicas</li>
 *   <li>Información de capacidad y ubicación</li>
 *   <li>Categorización por tipo de aula</li>
 *   <li>Soporte para diferentes campus</li>
 * </ul>
 * 
 * @author SecHub Development Team
 * @version 1.0
 * @since 1.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClassroomDTO {
    
    /**
     * Identificador único del aula.
     */
    private Long id;
    
    /**
     * Identificador del tipo de aula.
     * Referencia a {@link ClassroomTypeDTO#getId()}.
     */
    private Long classroomTypeId;
    
    /**
     * Campus donde se encuentra el aula.
     * Ejemplo: "Bogotá", "Campus Principal", "Campus Norte".
     */
    private String campus;
    
    /**
     * Ubicación específica dentro del campus.
     * Ejemplo: "Edificio A - Piso 2", "Torre B - Planta Baja".
     */
    private String location;
    
    /**
     * Número o nombre específico del aula.
     * Ejemplo: "A-101", "Laboratorio de Física", "Auditorio Principal".
     */
    private String room;
    
    /**
     * Capacidad máxima de estudiantes que puede albergar el aula.
     */
    private Integer capacity;
    
    /**
     * Nombre del tipo de aula.
     * Ejemplo: "Aulas", "Laboratorio", "Aulas Moviles", "Aulas Accesibles".
     */
    private String typeName;
    
    /**
     * Nombre completo del aula para mostrar en interfaces.
     * Generalmente combina room + location.
     */
    private String name;
    
    /**
     * Indica si el aula tiene proyector disponible.
     */
    private Boolean hasProjector;
    
    /**
     * Indica si el aula tiene accesibilidad para personas con discapacidad.
     */
    private Boolean hasAccessibility;
    
    /**
     * Tipo de aula para compatibilidad.
     * Alias para typeName.
     */
    private String type;
    
    /**
     * Edificio donde se encuentra el aula.
     * Extraído de la ubicación para mejor organización.
     */
    private String building;
    
    /**
     * Piso del aula dentro del edificio.
     */
    private Integer floor;
}
