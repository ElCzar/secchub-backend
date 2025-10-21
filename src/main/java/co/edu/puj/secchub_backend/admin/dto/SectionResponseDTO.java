package co.edu.puj.secchub_backend.admin.dto;

import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Builder;

/**
 * DTO that represents a section in the system.
 * Contains response information for the user.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SectionResponseDTO {
    private Long id;
    private String name;
    private Long userId;
    private boolean planningClosed;
}