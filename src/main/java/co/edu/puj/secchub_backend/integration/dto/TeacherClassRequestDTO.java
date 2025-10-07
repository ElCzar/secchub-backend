package co.edu.puj.secchub_backend.integration.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO that represents a request to create or update a TeacherClass entity.
 * This DTO is used to transfer data related to a teacher's class.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TeacherClassRequestDTO {
    private Long teacherId;
    private Long classId;
    private Integer workHours;
    private Integer fullTimeExtraHours;
    private Integer adjunctExtraHours;
    private String observation;
}
