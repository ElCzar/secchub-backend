package co.edu.puj.secchub_backend.admin.model;

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
 * Entity representing an academic semester.
 * Contains information about the semester such as its name, start and end dates, and status.
 */
@Table("semester")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Semester {
    @Id
    private Long id;

    @Column
    private Integer period;

    @Column
    private Integer year;

    @Column("is_current")
    private Boolean isCurrent;

    @Column("start_date")
    private LocalDate startDate;

    @Column("end_date")
    private LocalDate endDate;

    @Column("start_special_week")
    private LocalDate startSpecialWeek;
}