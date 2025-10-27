/**
 * This package contains the audit logging functionality for the application.
 * It provides automatic logging of create, update, and delete operations
 * across all service methods using AOP (Aspect-Oriented Programming).
 *
 * <p>Main components:</p>
 * <ul>
 *   <li>{@link co.edu.puj.secchub_backend.log.model.AuditLog} - Entity representing an audit log entry</li>
 *   <li>{@link co.edu.puj.secchub_backend.log.aspect.AuditLoggingAspect} - AOP aspect that intercepts service methods</li>
 *   <li>{@link co.edu.puj.secchub_backend.log.service.AuditLogService} - Service for querying audit logs</li>
 *   <li>{@link co.edu.puj.secchub_backend.log.controller.AuditLogController} - REST controller for audit log endpoints</li>
 * </ul>
 *
 */
@org.springframework.modulith.ApplicationModule(
    displayName = "Audit Logging Module",
    allowedDependencies = {}
)
package co.edu.puj.secchub_backend.log;
