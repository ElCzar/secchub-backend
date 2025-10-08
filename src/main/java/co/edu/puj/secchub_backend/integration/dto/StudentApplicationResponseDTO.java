package co.edu.puj.secchub_backend.integration.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for transferring StudentApplication data.
 * For response purposes.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class StudentApplicationResponseDTO {
    private Long id;
    private Long userId;
    private Long courseId;
    private Long sectionId;
    private Long semesterId;
    private String program;
    private Integer studentSemester;
    private Double academicAverage;
    private String phoneNumber;
    private String alternatePhoneNumber;
    private String address;
    private String personalEmail;
    private Boolean wasTeachingAssistant;
    private Double courseAverage;
    private String courseTeacher;
    private String applicationDate;
    private Long statusId;
    private List<StudentApplicationScheduleResponseDTO> schedules;
}
