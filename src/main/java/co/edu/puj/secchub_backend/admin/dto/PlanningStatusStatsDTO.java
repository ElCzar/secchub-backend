package co.edu.puj.secchub_backend.admin.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for planning status statistics.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PlanningStatusStatsDTO {
    private Integer openCount;
    private Integer closedCount;
    private Integer totalCount;
}
