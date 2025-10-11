package co.edu.puj.secchub_backend.planning.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO that represents a request to create or update a Classroom entity.
 * This DTO is used to transfer data when creating or updating classroom information.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ClassroomRequestDTO {
    private Long classroomTypeId;
    private String campus;
    private String location;
    private String room;
    private Integer capacity;
}