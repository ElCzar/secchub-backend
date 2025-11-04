package co.edu.puj.secchub_backend.planning.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Represents a Teaching Assistant in the planning module.
 * Is an student who assists a teacher in managing and conducting classes.
 */
@Data
@Table("teaching_assistant")
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TeachingAssistant {
    @Id
    private Long id;

    @Column("class_id")
    private Long classId;

    @Column("student_application_id")
    private Long studentApplicationId;

    @Column("weekly_hours")
    private Long weeklyHours;

    @Column("weeks")
    private Long weeks;

    @Column("total_hours")
    private Long totalHours;
}
