package co.edu.puj.secchub_backend.planning.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO representing a schedule conflict for a teacher.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TeacherScheduleConflictResponseDTO {
    private Long userId;
    private String userName;
    private String conflictDay;
    private String conflictStartTime;
    private String conflictEndTime;
    private List<Long> conflictingClassesIds;
}
