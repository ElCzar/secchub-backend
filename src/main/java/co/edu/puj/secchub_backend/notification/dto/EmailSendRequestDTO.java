package co.edu.puj.secchub_backend.notification.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for sending email requests.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmailSendRequestDTO {
    private String to;
    private String subject;
    private String body;
}
