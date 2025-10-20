package co.edu.puj.secchub_backend.log.controller;

import co.edu.puj.secchub_backend.log.dto.AuditLogResponseDTO;
import co.edu.puj.secchub_backend.log.service.AuditLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

import java.time.LocalDateTime;

/**
 * REST controller for audit log operations.
 * Provides endpoints to query audit logs.
 */
@RestController
@RequestMapping("/audit-logs")
@RequiredArgsConstructor
@Slf4j
@PreAuthorize("hasRole('ADMIN')")
public class AuditLogController {

    private final AuditLogService auditLogService;

    /**
     * Retrieves all audit logs as a stream.
     * @return Flux of all audit logs (streaming response)
     */
    @GetMapping(produces = MediaType.APPLICATION_NDJSON_VALUE)
    public Flux<AuditLogResponseDTO> getAllAuditLogs() {
        log.info("Streaming all audit logs");
        return auditLogService.getAllAuditLogs();
    }

    /**
     * Retrieves audit logs for a specific user email as a stream.
     * @param email User email
     * @return Flux of audit logs for the user (streaming response)
     */
    @GetMapping(value = "/email/{email}", produces = MediaType.APPLICATION_NDJSON_VALUE)
    public Flux<AuditLogResponseDTO> getAuditLogsByEmail(@PathVariable String email) {
        log.info("Streaming audit logs for email: {}", email);
        return auditLogService.getAuditLogsByEmail(email);
    }

    /**
     * Retrieves audit logs for a specific action type as a stream.
     * @param action Action type (CREATE, UPDATE, DELETE)
     * @return Flux of audit logs for the action (streaming response)
     */
    @GetMapping(value = "/action/{action}", produces = MediaType.APPLICATION_NDJSON_VALUE)
    public Flux<AuditLogResponseDTO> getAuditLogsByAction(@PathVariable String action) {
        log.info("Streaming audit logs for action: {}", action);
        return auditLogService.getAuditLogsByAction(action);
    }

    /**
     * Retrieves audit logs within a date range as a stream.
     * @param start Start timestamp (ISO format: yyyy-MM-dd'T'HH:mm:ss)
     * @param end End timestamp (ISO format: yyyy-MM-dd'T'HH:mm:ss)
     * @return Flux of audit logs within the range (streaming response)
     */
    @GetMapping(value = "/date-range", produces = MediaType.APPLICATION_NDJSON_VALUE)
    public Flux<AuditLogResponseDTO> getAuditLogsByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end) {
        log.info("Streaming audit logs between {} and {}", start, end);
        return auditLogService.getAuditLogsByDateRange(start, end);
    }

    /**
     * Retrieves audit logs for a specific method name as a stream.
     * @param methodName Method name
     * @return Flux of audit logs for the method (streaming response)
     */
    @GetMapping(value = "/method/{methodName}", produces = MediaType.APPLICATION_NDJSON_VALUE)
    public Flux<AuditLogResponseDTO> getAuditLogsByMethodName(@PathVariable String methodName) {
        log.info("Streaming audit logs for method: {}", methodName);
        return auditLogService.getAuditLogsByMethodName(methodName);
    }

    /**
     * Retrieves audit logs for a specific email and action as a stream.
     * @param email User email
     * @param action Action type
     * @return Flux of audit logs (streaming response)
     */
    @GetMapping(value = "/email/{email}/action/{action}", produces = MediaType.APPLICATION_NDJSON_VALUE)
    public Flux<AuditLogResponseDTO> getAuditLogsByEmailAndAction(
            @PathVariable String email,
            @PathVariable String action) {
        log.info("Streaming audit logs for email: {} and action: {}", email, action);
        return auditLogService.getAuditLogsByEmailAndAction(email, action);
    }
}
