package co.edu.puj.secchub_backend.planning.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * DTO representing a schedule conflict (teacher or classroom).
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ScheduleConflictDTO {
    private String type; // "teacher" or "classroom"
    private Long resourceId;
    private String resourceName;
    private List<ConflictingClassDTO> conflictingClasses;
}
