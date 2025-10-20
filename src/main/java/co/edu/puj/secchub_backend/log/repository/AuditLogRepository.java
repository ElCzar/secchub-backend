package co.edu.puj.secchub_backend.log.repository;

import co.edu.puj.secchub_backend.log.model.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repository for accessing audit log data.
 */
@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {

    /**
     * Finds all audit logs for a specific user email.
     * @param email User email
     * @return List of audit logs
     */
    List<AuditLog> findByEmail(String email);

    /**
     * Finds all audit logs for a specific action type.
     * @param action Action type (CREATE, UPDATE, DELETE)
     * @return List of audit logs
     */
    List<AuditLog> findByAction(String action);

    /**
     * Finds all audit logs within a date range.
     * @param start Start timestamp
     * @param end End timestamp
     * @return List of audit logs
     */
    List<AuditLog> findByTimestampBetween(LocalDateTime start, LocalDateTime end);

    /**
     * Finds all audit logs for a specific method name.
     * @param methodName Method name
     * @return List of audit logs
     */
    List<AuditLog> findByMethodName(String methodName);

    /**
     * Finds all audit logs for a specific email and action.
     * @param email User email
     * @param action Action type
     * @return List of audit logs
     */
    List<AuditLog> findByEmailAndAction(String email, String action);
}
