package co.edu.puj.secchub_backend.integration.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Entity mapped to 'request_schedule'.
 * Represents the schedule details associated with an academic request.
 */
@Table("request_schedule")
@Getter 
@Setter 
@Builder
@NoArgsConstructor 
@AllArgsConstructor
public class RequestSchedule {
    @Id
    private Long id;

    @Column("academic_request_id")
    private Long academicRequestId;

    @Column("classroom_type_id")
    private Long classRoomTypeId;

    @Column("start_time")
    private java.sql.Time startTime;

    @Column("end_time")
    private java.sql.Time endTime;

    @Column("day")
    private String day;

    @Column("modality_id")
    private Long modalityId;

    @Column("disability")
    private Boolean disability;
}
