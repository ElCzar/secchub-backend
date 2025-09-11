package co.edu.puj.secchub_backend.admin_resources.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.persistence.*;

/**
 * Entity representing a Teacher in the university system.
 * 
 * <p>This entity maps to the "teacher" table in the database and represents
 * university faculty members who can be assigned to teach academic classes.
 * It contains information about the teacher's employment details, user association,
 * and workload constraints.</p>
 * 
 * <p>Teachers are central to the academic planning module, as they are assigned
 * to academic classes based on their availability, expertise, and employment type.</p>
 * 
 * @author SecHub Development Team
 * @version 1.0
 * @since 1.0
 */
@Entity
@Table(name = "teacher")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Teacher {
    
    /**
     * Unique identifier for the teacher.
     * Auto-generated using database identity strategy.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    /**
     * Foreign key reference to the user account associated with this teacher.
     * Links to the user table to connect teacher profile with system authentication.
     */
    @Column(name = "user_id")
    private Long userId;
    
    /**
     * Foreign key reference to the employment type of the teacher.
     * Links to the employment_type table to indicate the type of employment
     * (e.g., full-time, part-time, adjunct, visiting).
     */
    @Column(name = "employment_type_id")
    private Long employmentTypeId;
    
    /**
     * Maximum number of hours the teacher can work per academic period.
     * Used for workload management and assignment optimization.
     * This value is determined by the teacher's employment type and contract.
     */
    @Column(name = "max_hours")
    private Integer maxHours;
}
