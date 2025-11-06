package co.edu.puj.secchub_backend.log.repository;

import co.edu.puj.secchub_backend.log.model.AuditLog;
import reactor.core.publisher.Flux;

import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

/**
 * Repository for accessing audit log data.
 */
@Repository
public interface AuditLogRepository extends R2dbcRepository<AuditLog, Long> {

    /**
     * Finds all audit logs for a specific user email.
     * @param email User email
     * @return Flux of audit logs
     */
    Flux<AuditLog> findByEmail(String email);

    /**
     * Finds all audit logs for a specific action type.
     * @param action Action type (CREATE, UPDATE, DELETE)
     * @return Flux of audit logs
     */
    Flux<AuditLog> findByAction(String action);

    /**
     * Finds all audit logs within a date range.
     * @param start Start timestamp
     * @param end End timestamp
     * @return Flux of audit logs
     */
    Flux<AuditLog> findByTimestampBetween(LocalDateTime start, LocalDateTime end);

    /**
     * Finds all audit logs for a specific method name.
     * @param methodName Method name
     * @return Flux of audit logs
     */
    Flux<AuditLog> findByMethodName(String methodName);

    /**
     * Finds all audit logs for a specific email and action.
     * @param email User email
     * @param action Action type
     * @return Flux of audit logs
     */
    Flux<AuditLog> findByEmailAndAction(String email, String action);
}
