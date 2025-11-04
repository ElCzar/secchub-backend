package co.edu.puj.secchub_backend.integration.model;

import java.time.LocalDate;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Entity mapped to 'academic_request'.
 * Represents a request made by a program for a class in a specific course and semester.
 */
@Table("academic_request")
@Getter 
@Setter
@Builder
@NoArgsConstructor 
@AllArgsConstructor
public class AcademicRequest {
    @Id
    private Long id;

    @Column("user_id")
    private Long userId;

    @Column("course_id")
    private Long courseId;

    @Column("semester_id")
    private Long semesterId;

    @Column("start_date")
    private LocalDate startDate;

    @Column("end_date")
    private LocalDate endDate;

    @Column("capacity")
    private Integer capacity;

    @Column("request_date")
    private LocalDate requestDate;

    @Column("observation")
    private String observation;

    @Column("accepted")
    private Boolean accepted;

    @Column("combined")
    private Boolean combined;
}
