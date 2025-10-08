package co.edu.puj.secchub_backend.admin_resources.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO (Data Transfer Object) para la gestión de tipos de aula.
 * 
 * <p>Esta clase representa la información de un tipo de aula,
 * definiendo la categoría y propósito de los espacios académicos.
 * Es utilizada para transferir información entre las capas de la aplicación
 * y para la comunicación con clientes externos a través de la API REST.</p>
 * 
 * <p><strong>Funcionalidades principales:</strong></p>
 * <ul>
 *   <li>Gestión de tipos de aula</li>
 *   <li>Categorización de espacios académicos</li>
 *   <li>Soporte para planificación de recursos</li>
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
public class ClassroomTypeDTO {
    
    /**
     * Identificador único del tipo de aula.
     */
    private Long id;
    
    /**
     * Nombre del tipo de aula.
     * Ejemplo: "Aulas", "Laboratorio", "Aulas Moviles", "Aulas Accesibles".
     */
    private String name;
    
    /**
     * Descripción detallada del tipo de aula.
     * Ejemplo: "Aulas tradicionales", "Laboratorios", "Aulas móviles", "Aulas con accesibilidad".
     */
    private String description;
}
