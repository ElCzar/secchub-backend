package co.edu.puj.secchub_backend.planning.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO representing a teaching assistant schedule conflict.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TeachingAssistantConflictResponseDTO {
    private Long teachingAssistantId;
    private String teachingAssistantName;
    private List<Long> conflictingClassesIds;
    private List<Long> conflictingSectionsIds;
}
