package co.edu.puj.secchub_backend.planning.model;

import jakarta.persistence.*;
import lombok.*;

import java.sql.Time;

/**
 * Entity mapped to 'teaching_assistant_schedule'.
 * Represents the scheduled time blocks for a teaching assistant.
 */
@Entity
@Table(name = "teaching_assistant_schedule")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TeachingAssistantSchedule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "teaching_assistant_id", nullable = false)
    private Long teachingAssistantId;

    @Column(name = "day", nullable = false)
    private String day;

    @Column(name = "start_time", nullable = false)
    private Time startTime;

    @Column(name = "end_time", nullable = false)
    private Time endTime;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "teaching_assistant_id", insertable = false, updatable = false)
    private TeachingAssistant teachingAssistant;
}