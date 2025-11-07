package co.edu.puj.secchub_backend.admin.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO that represents a summary of a section.
 * It contains key information about the section for overview purposes.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SectionSummaryDTO {
    private String name;
    private Boolean planningClosed;
    private Integer assignedClasses;
    private Integer unconfirmedTeachers;
}
