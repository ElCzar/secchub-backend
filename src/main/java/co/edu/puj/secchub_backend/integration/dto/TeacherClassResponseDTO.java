package co.edu.puj.secchub_backend.integration.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO that represents a response containing information about a TeacherClass entity.
 * This DTO is used to transfer data related to a teacher's class.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TeacherClassResponseDTO {
    private Long id;
    private Long semesterId;
    private Long teacherId;
    private Long classId;
    private Integer workHours;
    private Integer fullTimeExtraHours;
    private Integer adjunctExtraHours;
    private Boolean decision;
    private String observation;
    private Long statusId;
    
    // Teacher information fields
    private String teacherName;
    private String teacherLastName;
    private String teacherEmail;
    private Integer teacherMaxHours;
    private String teacherContractType;
}