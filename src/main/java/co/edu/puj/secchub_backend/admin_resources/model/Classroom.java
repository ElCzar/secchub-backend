package co.edu.puj.secchub_backend.admin_resources.model;

import jakarta.persistence.*;
import lombok.*;

/**
 * Entity representing a Classroom in the university system.
 * 
 * <p>This entity maps to the "classroom" table in the database and represents
 * physical spaces where academic classes can take place. It includes information
 * about location, capacity, type, and available equipment.</p>
 * 
 * <p>Classrooms are essential for academic planning, helping to assign
 * appropriate spaces for different types of classes and ensuring optimal
 * resource utilization.</p>
 * 
 * @author SecHub Development Team
 * @version 1.0
 * @since 1.0
 */
@Entity
@Table(name = "classroom")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Classroom {
    
    /**
     * Unique identifier for the classroom.
     * Auto-generated using database identity strategy.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    /**
     * Foreign key reference to the classroom type.
     * Links to the classroom_type table to categorize the classroom
     * (e.g., lecture hall, laboratory, seminar room).
     */
    @Column(name = "classroom_type_id")
    private Long classroomTypeId;
    
    /**
     * Campus where the classroom is located.
     * Example: "Campus Principal", "Campus Norte", "Sede Centro".
     */
    @Column(length = 150)
    private String campus;
    
    /**
     * Specific location within the campus (building, floor).
     * Example: "Edificio A - Piso 2", "Torre B - Planta Baja".
     */
    @Column(length = 200)
    private String location;
    
    /**
     * Room number or identifier.
     * Example: "101", "A-205", "Laboratorio de Física".
     */
    @Column(length = 100)
    private String room;
    
    /**
     * Maximum capacity of students that the classroom can accommodate.
     * Used for class assignment and enrollment validation.
     */
    private Integer capacity;
    
    // Relationships
    
    /**
     * Many-to-One relationship with ClassroomType.
     * Provides detailed information about the classroom category.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "classroom_type_id", insertable = false, updatable = false)
    private ClassroomType classroomType;
}
