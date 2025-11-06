package co.edu.puj.secchub_backend.log.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

/**
 * Entity mapped to 'audit_log'.
 * Represents an audit log entry for tracking create, update, and delete operations.
 */
@Table(name = "audit_log")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuditLog {
    @Id
    private Long id;

    @Column("email")
    private String email;

    @Column("action")
    private String action;

    @Column("method_name")
    private String methodName;

    @Column("timestamp")
    private LocalDateTime timestamp;
}
