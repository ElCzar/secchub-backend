package co.edu.puj.secchub_backend.planning.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalTime;
import java.util.List;

/**
 * DTO representing a schedule conflict for a teaching assistant.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TeachingAssistantScheduleConflictResponseDTO {
    private Long userId;
    private String userName;
    private LocalTime conflictStartTime;
    private LocalTime conflictEndTime;
    private String day;
    private List<Long> conflictTeachingAssistants;
}
