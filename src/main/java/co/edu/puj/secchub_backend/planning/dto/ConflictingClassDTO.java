package co.edu.puj.secchub_backend.planning.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalTime;

/**
 * DTO representing a class involved in a schedule conflict.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ConflictingClassDTO {
    private Long classId;
    private String className;
    private Long section;
    private String day;
    private LocalTime startTime;
    private LocalTime endTime;
}
