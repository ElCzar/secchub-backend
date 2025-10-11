package co.edu.puj.secchub_backend.admin.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO that represents a section request.
 * It contains information uploaded by the user to create or update a section.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SectionCreateRequestDTO {
    private Long userId;
    private String name;
}