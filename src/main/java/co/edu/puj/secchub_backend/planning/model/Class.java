package co.edu.puj.secchub_backend.planning.model;

import java.time.LocalDate;

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
 * Entity representing a class in the academic planning system.
 * The class is used to define the courses that users can plan for the next academic semester.
 */
@Entity
@Table(name = "class")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Class {
    @Id
    @GeneratedValue(strategy = jakarta.persistence.GenerationType.IDENTITY)
    private Long id;

    @Column
    private Long section;
    
    @Column(name = "course_id")
    private Long courseId;

    @Column(name = "semester_id")
    private Long semesterId;

    @Column(name = "start_date")
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    @Column
    private String observation;

    @Column
    private Integer capacity;

    @Column(name = "status_id")
    private Long statusId;
}
