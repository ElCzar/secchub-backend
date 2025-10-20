package co.edu.puj.secchub_backend.log.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Entity mapped to 'audit_log'.
 * Represents an audit log entry for tracking create, update, and delete operations.
 */
@Entity
@Table(name = "audit_log")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "email", nullable = false, length = 150)
    private String email;

    @Column(name = "action", nullable = false, length = 50)
    private String action; // CREATE, UPDATE, DELETE

    @Column(name = "method_name", nullable = false, length = 150)
    private String methodName;

    @Column(name = "timestamp", nullable = false)
    private LocalDateTime timestamp;
}
