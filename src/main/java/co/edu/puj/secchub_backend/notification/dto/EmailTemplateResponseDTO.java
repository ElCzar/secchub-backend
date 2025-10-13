package co.edu.puj.secchub_backend.notification.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Data Transfer Object for email template responses.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class EmailTemplateResponseDTO {
    private Long id;
    private String name;
    private String subject;
    private String body;
}
