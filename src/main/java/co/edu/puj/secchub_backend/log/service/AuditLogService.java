package co.edu.puj.secchub_backend.log.service;

import co.edu.puj.secchub_backend.log.dto.AuditLogResponseDTO;
import co.edu.puj.secchub_backend.log.model.AuditLog;
import co.edu.puj.secchub_backend.log.repository.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;

import java.time.LocalDateTime;

/**
 * Service for managing and querying audit logs.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class AuditLogService {

    private final AuditLogRepository auditLogRepository;

    /**
     * Retrieves all audit logs as a reactive stream.
     * @return Flux of all audit logs
     */
    public Flux<AuditLogResponseDTO> getAllAuditLogs() {
        return Flux.defer(() -> Flux.fromIterable(auditLogRepository.findAll()))
                .map(this::mapToDTO)
                .subscribeOn(Schedulers.boundedElastic());
    }

    /**
     * Retrieves audit logs for a specific user email as a reactive stream.
     * @param email User email
     * @return Flux of audit logs for the user
     */
    public Flux<AuditLogResponseDTO> getAuditLogsByEmail(String email) {
        return Flux.defer(() -> Flux.fromIterable(auditLogRepository.findByEmail(email)))
                .map(this::mapToDTO)
                .subscribeOn(Schedulers.boundedElastic());
    }

    /**
     * Retrieves audit logs for a specific action type as a reactive stream.
     * @param action Action type (CREATE, UPDATE, DELETE)
     * @return Flux of audit logs for the action
     */
    public Flux<AuditLogResponseDTO> getAuditLogsByAction(String action) {
        return Flux.defer(() -> Flux.fromIterable(auditLogRepository.findByAction(action.toUpperCase())))
                .map(this::mapToDTO)
                .subscribeOn(Schedulers.boundedElastic());
    }

    /**
     * Retrieves audit logs within a date range as a reactive stream.
     * @param start Start timestamp
     * @param end End timestamp
     * @return Flux of audit logs within the range
     */
    public Flux<AuditLogResponseDTO> getAuditLogsByDateRange(LocalDateTime start, LocalDateTime end) {
        return Flux.defer(() -> Flux.fromIterable(auditLogRepository.findByTimestampBetween(start, end)))
                .map(this::mapToDTO)
                .subscribeOn(Schedulers.boundedElastic());
    }

    /**
     * Retrieves audit logs for a specific method name as a reactive stream.
     * @param methodName Method name
     * @return Flux of audit logs for the method
     */
    public Flux<AuditLogResponseDTO> getAuditLogsByMethodName(String methodName) {
        return Flux.defer(() -> Flux.fromIterable(auditLogRepository.findByMethodName(methodName)))
                .map(this::mapToDTO)
                .subscribeOn(Schedulers.boundedElastic());
    }

    /**
     * Retrieves audit logs for a specific email and action as a reactive stream.
     * @param email User email
     * @param action Action type
     * @return Flux of audit logs
     */
    public Flux<AuditLogResponseDTO> getAuditLogsByEmailAndAction(String email, String action) {
        return Flux.defer(() -> Flux.fromIterable(auditLogRepository.findByEmailAndAction(email, action.toUpperCase())))
                .map(this::mapToDTO)
                .subscribeOn(Schedulers.boundedElastic());
    }

    /**
     * Maps AuditLog entity to DTO.
     * @param auditLog Entity
     * @return DTO
     */
    private AuditLogResponseDTO mapToDTO(AuditLog auditLog) {
        return AuditLogResponseDTO.builder()
                .id(auditLog.getId())
                .email(auditLog.getEmail())
                .action(auditLog.getAction())
                .methodName(auditLog.getMethodName())
                .timestamp(auditLog.getTimestamp())
                .build();
    }
}
