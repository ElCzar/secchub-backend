package co.edu.puj.secchub_backend.planning.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Represents a Teaching Assistant in the planning module.
 * Is an student who assists a teacher in managing and conducting classes.
 */
@Entity
@Data
@Table(name = "teaching_assistant")
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TeachingAssistant {
    @Id
    @GeneratedValue(strategy = jakarta.persistence.GenerationType.IDENTITY)
    private Long id;

    @Column(name = "class_id")
    private Long classId;

    @Column(name = "student_application_id")
    private Long studentApplicationId;

    @Column(name = "weekly_hours")
    private Long weeklyHours;

    @Column(name = "weeks")
    private Long weeks;

    @Column(name = "total_hours")
    private Long totalHours;
}
