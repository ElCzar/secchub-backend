package co.edu.puj.secchub_backend.admin_resources.model;

import jakarta.persistence.*;
import lombok.*;

/**
 * Entity representing the assignment of a Teacher to an Academic Class.
 * 
 * <p>This entity maps to the "teacher_class" table in the database and represents
 * the relationship between teachers and academic classes. It manages the assignment
 * process, including workload calculations, teacher responses, and status tracking.</p>
 * 
 * <p>Teacher assignments are crucial for academic planning as they track:
 * <ul>
 * <li>Which teachers are assigned to which classes</li>
 * <li>Workload distribution and hour calculations</li>
 * <li>Teacher acceptance/rejection of assignments</li>
 * <li>Extra hours for different employment types</li>
 * </ul></p>
 * 
 * @author SecHub Development Team
 * @version 1.0
 * @since 1.0
 */
@Entity
@Table(name = "teacher_class")
@Getter 
@Setter 
@Builder
@NoArgsConstructor 
@AllArgsConstructor
public class TeacherAssignment {

    /**
     * Unique identifier for the teacher assignment.
     * Auto-generated using database identity strategy.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Foreign key reference to the assigned teacher.
     * Links to the teacher table to identify which teacher is assigned.
     * This field is required and cannot be null.
     */
    @Column(name = "teacher_id", nullable = false)
    private Long teacherId;

    /**
     * Foreign key reference to the academic class.
     * Links to the class table to identify which class the teacher is assigned to.
     * This field is required and cannot be null.
     */
    @Column(name = "class_id", nullable = false)
    private Long classId;

    /**
     * Number of work hours assigned to the teacher for this class.
     * Represents the regular teaching hours within the teacher's normal workload.
     */
    @Column(name = "work_hours")
    private Integer workHours;

    /**
     * Additional hours for full-time teachers beyond their regular workload.
     * These hours may be compensated differently or count toward tenure requirements.
     */
    @Column(name = "full_time_extra_hours")
    private Integer fullTimeExtraHours;

    /**
     * Additional hours for adjunct teachers beyond their regular workload.
     * These hours typically have different compensation rates than regular hours.
     */
    @Column(name = "adjunct_extra_hours")
    private Integer adjunctExtraHours;

    /**
     * Teacher's decision on the assignment.
     * <ul>
     * <li>true = accepted</li>
     * <li>false = rejected</li>
     * <li>null = pending response</li>
     * </ul>
     */
    @Column(name = "decision")
    private Boolean decision;

    /**
     * Additional observations or comments about the assignment.
     * Can contain notes from either the administrator or the teacher
     * regarding the assignment. Limited to 500 characters.
     */
    @Column(name = "observation", length = 500)
    private String observation;

    /**
     * Current status of the teacher assignment.
     * References the status table with typical values:
     * <ul>
     * <li>1 = Pending (waiting for teacher response)</li>
     * <li>2 = Accepted (teacher has accepted the assignment)</li>
     * <li>3 = Rejected (teacher has rejected the assignment)</li>
     * </ul>
     */
    @Column(name = "status_id")
    private Long statusId;
}
