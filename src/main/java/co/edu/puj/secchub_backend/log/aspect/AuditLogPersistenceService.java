/**
 * Service for persisting audit logs asynchronously.
 * This is separate from AuditLoggingAspect to allow proper proxy-based async execution.
 */
package co.edu.puj.secchub_backend.log.aspect;

import co.edu.puj.secchub_backend.log.model.AuditLog;
import co.edu.puj.secchub_backend.log.repository.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Component
@Slf4j
@RequiredArgsConstructor
class AuditLogPersistenceService {
    
    private final AuditLogRepository auditLogRepository;
    
    /**
     * Saves the audit log entry asynchronously in a separate transaction.
     * This ensures that audit logs are persisted even if the main transaction fails.
     */
    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void saveAuditLogAsync(String email, String action, String methodName) {
        try {
            AuditLog auditLog = AuditLog.builder()
                    .email(email)
                    .action(action)
                    .methodName(methodName)
                    .timestamp(LocalDateTime.now())
                    .build();
            
            auditLogRepository.save(auditLog);
            
            log.debug("Audit log created: {} - {} - {}", action, methodName, email);
            
        } catch (Exception e) {
            log.error("Error saving audit log for method: {}", methodName, e);
        }
    }
}
