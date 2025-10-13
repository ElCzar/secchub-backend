package co.edu.puj.secchub_backend.notification.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for email template requests.
 * Contains information needed to create or update an email template.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmailTemplateRequestDTO {
    private String name;
    private String subject;
    private String body;
}