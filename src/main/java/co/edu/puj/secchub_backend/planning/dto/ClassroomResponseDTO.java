package co.edu.puj.secchub_backend.planning.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO that represents a response containing information about a Classroom entity.
 * This DTO is used to transfer classroom data in API responses.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ClassroomResponseDTO {
    private Long id;
    private Long classroomTypeId;
    private String campus;
    private String location;
    private String room;
    private Integer capacity;
}