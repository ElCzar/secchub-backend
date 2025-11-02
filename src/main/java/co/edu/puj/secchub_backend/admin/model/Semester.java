package co.edu.puj.secchub_backend.admin.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Entity representing an academic semester.
 * Contains information about the semester such as its name, start and end dates, and status.
 */

@Entity
@Table(name = "semester")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Semester {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column
    private Integer period;

    @Column
    private Integer year;

    @Column(name = "is_current")
    private Boolean isCurrent;

    @Column(name = "start_date")
    private String startDate;

    @Column(name = "end_date")
    private String endDate;

    @Column(name = "start_special_week")
    private String startSpecialWeek;
}