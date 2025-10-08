package co.edu.puj.secchub_backend.integration.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for transferring StudentApplication data.
 * Includes fields for student application details and associated schedules.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StudentApplicationRequestDTO {
    private Long courseId;
    private Long sectionId;
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
    private List<StudentApplicationScheduleRequestDTO> schedules;
}

