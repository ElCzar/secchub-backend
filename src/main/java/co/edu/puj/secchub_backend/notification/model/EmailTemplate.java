package co.edu.puj.secchub_backend.notification.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Entity mapped to 'email_template'.
 * Represents an email template used for notifications.
 */
@Table("email_template")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmailTemplate {
    @Id
    private Long id;

    @Column
    private String name;

    @Column
    private String subject;

    @Column
    private String body;
}
