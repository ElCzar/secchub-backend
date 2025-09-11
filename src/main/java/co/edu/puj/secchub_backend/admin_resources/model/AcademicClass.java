package co.edu.puj.secchub_backend.admin_resources.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.persistence.*;
import java.time.LocalDate;

/**
 * Entity representing an Academic Class in the university system.
 * 
 * <p>This entity maps to the "class" table in the database and represents
 * a specific instance of a course being offered in a particular semester.
 * It contains information about the course, semester, dates, capacity, and status.</p>
 * 
 * <p>Academic classes are the core unit of the academic planning module,
 * linking courses to semesters and providing the foundation for scheduling
 * and teacher assignments.</p>
 * 
 * @author SecHub Development Team
 * @version 1.0
 * @since 1.0
 */
@Entity
@Table(name = "class")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AcademicClass {
    
    /**
     * Unique identifier for the academic class.
     * Auto-generated using database identity strategy.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    /**
     * Foreign key reference to the course being offered.
     * Links to the course table to identify which course this class represents.
     */
    @Column(name = "course_id")
    private Long courseId;
    
    /**
     * Foreign key reference to the semester when this class is offered.
     * Links to the semester table to identify the academic period.
     */
    @Column(name = "semester_id")
    private Long semesterId;
    
    /**
     * Start date of the academic class.
     * Represents when the class begins within the semester.
     */
    @Column(name = "start_date")
    private LocalDate startDate;
    
    /**
     * End date of the academic class.
     * Represents when the class concludes within the semester.
     */
    @Column(name = "end_date")
    private LocalDate endDate;
    
    /**
     * Additional observations or notes about the academic class.
     * Can contain special instructions, requirements, or comments
     * relevant to the class administration.
     */
    @Column(columnDefinition = "TEXT")
    private String observation;
    
    /**
     * Maximum number of students that can enroll in this class.
     * Used for enrollment management and resource planning.
     */
    private Integer capacity;
    
    /**
     * Foreign key reference to the status of the academic class.
     * Links to the status table to indicate the current state
     * (e.g., active, inactive, cancelled).
     */
    @Column(name = "status_id")
    private Long statusId;
}
