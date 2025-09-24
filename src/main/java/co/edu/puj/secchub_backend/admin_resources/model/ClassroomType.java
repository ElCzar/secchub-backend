package co.edu.puj.secchub_backend.admin_resources.model;

import jakarta.persistence.*;
import lombok.*;

/**
 * Entity representing a Classroom Type in the university system.
 * 
 * <p>This entity maps to the "classroom_type" table in the database and represents
 * different categories of classrooms available in the university. It defines
 * the type and purpose of classroom spaces for proper academic planning.</p>
 * 
 * <p>Classroom types help categorize spaces based on their equipment,
 * capacity, and intended use for optimal class assignment.</p>
 * 
 * @author SecHub Development Team
 * @version 1.0
 * @since 1.0
 */
@Entity
@Table(name = "classroom_type")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClassroomType {
    
    /**
     * Unique identifier for the classroom type.
     * Auto-generated using database identity strategy.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    /**
     * Name of the classroom type.
     * Example: "Aulas", "Laboratorio", "Aulas Moviles", "Aulas Accesibles".
     * Limited to 100 characters and must be unique.
     */
    @Column(nullable = false, length = 100, unique = true)
    private String name;
}
