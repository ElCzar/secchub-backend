package co.edu.puj.secchub_backend.planning.model;

import java.time.LocalDate;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Entity representing a class in the academic planning system.
 * The class is used to define the courses that users can plan for the next academic semester.
 */
@Table("class")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Class {
    @Id
    private Long id;

    @Column
    private Long section;
    
    @Column("course_id")
    private Long courseId;

    @Column("semester_id")
    private Long semesterId;

    @Column("start_date")
    private LocalDate startDate;

    @Column("end_date")
    private LocalDate endDate;

    @Column
    private String observation;

    @Column
    private Integer capacity;

    @Column("status_id")
    private Long statusId;
}
