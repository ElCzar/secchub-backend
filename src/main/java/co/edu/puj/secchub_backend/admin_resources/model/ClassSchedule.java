package co.edu.puj.secchub_backend.admin_resources.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.persistence.*;
import java.time.LocalTime;

/**
 * Entity representing a Class Schedule in the university system.
 * 
 * <p>This entity maps to the "class_schedule" table in the database and represents
 * the specific time slots, days, and locations when an academic class takes place.
 * It defines the weekly schedule for each academic class including classroom assignments,
 * time slots, and accessibility considerations.</p>
 * 
 * <p>Class schedules are essential for academic planning, helping to avoid conflicts
 * in classroom usage, teacher assignments, and student enrollment planning.</p>
 * 
 * @author SecHub Development Team
 * @version 1.0
 * @since 1.0
 */
@Entity
@Table(name = "class_schedule")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClassSchedule {
    
    /**
     * Unique identifier for the class schedule.
     * Auto-generated using database identity strategy.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    /**
     * Foreign key reference to the academic class.
     * Links to the class table to identify which academic class
     * this schedule belongs to.
     */
    @Column(name = "class_id")
    private Long classId;
    
    /**
     * Foreign key reference to the classroom where the class takes place.
     * Links to the classroom table to identify the physical location.
     */
    @Column(name = "classroom_id")
    private Long classroomId;
    
    /**
     * Day of the week when the class occurs.
     * Expected values: Monday, Tuesday, Wednesday, Thursday, Friday, Saturday, Sunday.
     * Limited to 20 characters in the database.
     */
    @Column(length = 20)
    private String day;
    
    /**
     * Start time of the class session.
     * Represents the time when the class begins on the specified day.
     */
    @Column(name = "start_time")
    private LocalTime startTime;
    
    /**
     * End time of the class session.
     * Represents the time when the class concludes on the specified day.
     */
    @Column(name = "end_time")
    private LocalTime endTime;
    
    /**
     * Foreign key reference to the modality of the class.
     * Links to the modality table to indicate delivery method
     * (e.g., in-person, online, hybrid).
     */
    @Column(name = "modality_id")
    private Long modalityId;
    
    /**
     * Indicates if the class session has accessibility accommodations.
     * True if the session is adapted for students with disabilities,
     * false otherwise.
     */
    private Boolean disability;
}
