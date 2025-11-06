package co.edu.puj.secchub_backend.integration.model;

import java.time.LocalTime;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Entity mapped to 'student_application_schedule'.
 * Represents the availability time blocks a student declares in their application.
 */
@Table("student_application_schedule")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StudentApplicationSchedule {

    @Id
    private Long id;

    @Column("day")
    private String day;

    @Column("start_time")
    private LocalTime startTime;

    @Column("end_time")
    private LocalTime endTime;

    @Column("student_application_id")
    private Long studentApplicationId;
}
