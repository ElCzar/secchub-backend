package co.edu.puj.secchub_backend.planning.model;

import java.sql.Time;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Entity mapped to 'teaching_assistant_schedule'.
 * Represents the scheduled time blocks for a teaching assistant.
 */
@Table("teaching_assistant_schedule")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TeachingAssistantSchedule {
    @Id
    private Long id;

    @Column("teaching_assistant_id")
    private Long teachingAssistantId;

    @Column("day")
    private String day;

    @Column("start_time")
    private Time startTime;

    @Column("end_time")
    private Time endTime;
}