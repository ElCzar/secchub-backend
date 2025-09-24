package co.edu.puj.secchub_backend.admin_resources.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO (Data Transfer Object) para la gestión de modalidades de enseñanza.
 * 
 * <p>Esta clase representa la información de una modalidad de enseñanza,
 * definiendo cómo se imparte una clase académica (presencial, virtual, híbrida).
 * Es utilizada para transferir información entre las capas de la aplicación
 * y para la comunicación con clientes externos a través de la API REST.</p>
 * 
 * <p><strong>Funcionalidades principales:</strong></p>
 * <ul>
 *   <li>Gestión de modalidades de enseñanza</li>
 *   <li>Clasificación de métodos de entrega de clases</li>
 *   <li>Soporte para planificación académica</li>
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
public class ModalityDTO {
    
    /**
     * Identificador único de la modalidad.
     */
    private Long id;
    
    /**
     * Nombre de la modalidad.
     * Ejemplo: "PRESENCIAL", "VIRTUAL", "HIBRIDO".
     */
    private String name;
    
    /**
     * Descripción detallada de la modalidad.
     * Ejemplo: "Clase presencial", "Clase virtual", "Clase híbrida".
     */
    private String description;
}
