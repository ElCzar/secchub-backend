package co.edu.puj.secchub_backend.admin_resources.model;

import jakarta.persistence.*;
import lombok.*;

/**
 * Entity representing a Modality in the university system.
 * 
 * <p>This entity maps to the "modality" table in the database and represents
 * different delivery methods for academic classes. It defines how a class
 * is conducted (in-person, online, hybrid).</p>
 * 
 * <p>Modalities are essential for academic planning, especially in determining
 * classroom requirements and scheduling conflicts.</p>
 * 
 * @author SecHub Development Team
 * @version 1.0
 * @since 1.0
 */
@Entity
@Table(name = "modality")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Modality {
    
    /**
     * Unique identifier for the modality.
     * Auto-generated using database identity strategy.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    /**
     * Name of the modality.
     * Example: "In-Person", "Online", "Hybrid".
     * Limited to 100 characters and must be unique.
     */
    @Column(nullable = false, length = 100, unique = true)
    private String name;
}
