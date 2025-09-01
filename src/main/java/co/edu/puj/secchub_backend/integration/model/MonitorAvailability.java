package co.edu.puj.secchub_backend.integration.model;

import jakarta.persistence.*;
import lombok.*;

/**
 * Entity mapped to 'monitor_availability'.
 * Represents the availability time blocks a student declares in their request.
 */
@Entity
@Table(name = "monitor_availability")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MonitorAvailability {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** FK to the monitor request */
    @Column(name = "monitor_request_id", nullable = false)
    private Long monitorRequestId;

    /** Day of availability (e.g. MONDAY, TUESDAY) */
    @Column(name = "day", nullable = false)
    private String day;

    /** Start time of availability */
    @Column(name = "start_time", nullable = false)
    private java.sql.Time startTime;

    /** End time of availability */
    @Column(name = "end_time", nullable = false)
    private java.sql.Time endTime;

    /** Total hours in this availability block */
    @Column(name = "total_hours", nullable = false)
    private Integer totalHours;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "monitor_request_id", insertable = false, updatable = false)
    private MonitorRequest monitorRequest;
}
