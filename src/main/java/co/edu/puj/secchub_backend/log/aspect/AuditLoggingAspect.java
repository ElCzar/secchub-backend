package co.edu.puj.secchub_backend.log.aspect;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.lang.reflect.Method;

/**
 * AOP Aspect for automatically logging create, update, and delete operations.
 * This aspect intercepts service method calls and creates audit log entries.
 */
@Aspect
@Component
@Slf4j
@RequiredArgsConstructor
public class AuditLoggingAspect {

    private static final String ANONYMOUS_USER = "anonymous";
    
    private final AuditLogPersistenceService auditLogPersistenceService;

    /**
     * Captures all CREATE operations (methods starting with create, save, add, insert).
     */
    @Around("execution(* co.edu.puj.secchub_backend..service.*.create*(..)) || " +
            "execution(* co.edu.puj.secchub_backend..service.*.save*(..)) || " +
            "execution(* co.edu.puj.secchub_backend..service.*.add*(..)) || " +
            "execution(* co.edu.puj.secchub_backend..service.*.insert*(..))")
    public Object logCreateOperations(ProceedingJoinPoint joinPoint) throws Throwable {
        return logOperation(joinPoint, "CREATE");
    }

    /**
     * Captures all UPDATE operations (methods starting with update, modify, edit, approve, reject).
     */
    @Around("execution(* co.edu.puj.secchub_backend..service.*.update*(..)) || " +
            "execution(* co.edu.puj.secchub_backend..service.*.modify*(..)) || " +
            "execution(* co.edu.puj.secchub_backend..service.*.edit*(..)) || " +
            "execution(* co.edu.puj.secchub_backend..service.*.approve*(..)) || " +
            "execution(* co.edu.puj.secchub_backend..service.*.reject*(..))")
    public Object logUpdateOperations(ProceedingJoinPoint joinPoint) throws Throwable {
        return logOperation(joinPoint, "UPDATE");
    }

    /**
     * Captures all DELETE operations (methods starting with delete, remove).
     */
    @Around("execution(* co.edu.puj.secchub_backend..service.*.delete*(..)) || " +
            "execution(* co.edu.puj.secchub_backend..service.*.remove*(..))")
    public Object logDeleteOperations(ProceedingJoinPoint joinPoint) throws Throwable {
        return logOperation(joinPoint, "DELETE");
    }

    /**
     * Executes the intercepted method and logs the operation asynchronously.
     * @param joinPoint The intercepted method call
     * @param action The action type (CREATE, UPDATE, DELETE)
     * @return The result of the method execution
     * @throws Throwable If the method throws an exception
     */
    private Object logOperation(ProceedingJoinPoint joinPoint, String action) throws Throwable {
        // Extract method information
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        String className = joinPoint.getTarget().getClass().getSimpleName();
        String methodName = className + "." + method.getName();
        
        // Execute the actual method
        Object result = joinPoint.proceed();
        
        // If result is a Mono, extract email from reactive context and pass it to logAuditEntry
        if (result instanceof Mono<?> mono) {
            result = mono.then(
                ReactiveSecurityContextHolder.getContext()
                    .map(securityContext -> securityContext.getAuthentication().getName())
                    .defaultIfEmpty(ANONYMOUS_USER)
                    .doOnNext(email -> {
                        log.debug("User {} made the action {} on {}", email, action, methodName);
                        logAuditEntry(email, action, methodName);
                    })
                    .onErrorResume(error -> {
                        log.warn("Error extracting security context for {}, using anonymous", methodName, error);
                        logAuditEntry(ANONYMOUS_USER, action, methodName);
                        return Mono.empty();
                    })
                    .then()
            );
        } else {
            // For non-reactive methods, log as anonymous (no reactive context available)
            log.debug("Non-reactive method {}, logging as anonymous", methodName);
            logAuditEntry(ANONYMOUS_USER, action, methodName);
        }
        
        return result;
    }
    
    /**
     * Logs the audit entry to the database.
     * @param email User email from security context
     * @param action The action type (CREATE, UPDATE, DELETE)
     * @param methodName The fully qualified method name
     */
    private void logAuditEntry(String email, String action, String methodName) {
        auditLogPersistenceService.saveAuditLogAsync(email, action, methodName);
    }
}