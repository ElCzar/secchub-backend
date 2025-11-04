package co.edu.puj.secchub_backend.log.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import co.edu.puj.secchub_backend.log.dto.AuditLogResponseDTO;
import co.edu.puj.secchub_backend.log.model.AuditLog;
import co.edu.puj.secchub_backend.log.repository.AuditLogRepository;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuditLogService Unit Test")
class AuditLogServiceTest {

    @Mock
    private AuditLogRepository auditLogRepository;

    @InjectMocks
    private AuditLogService auditLogService;

    // ==========================================
    // Get All Audit Logs Tests
    // ==========================================

    @Test
    @DisplayName("getAllAuditLogs - Should return all audit logs as DTOs")
    void testGetAllAuditLogs_ReturnsAllLogs() {
        LocalDateTime now = LocalDateTime.now();
        
        AuditLog log1 = AuditLog.builder()
                .id(1L)
                .email("user1@example.com")
                .action("CREATE")
                .methodName("createUser")
                .timestamp(now)
                .build();
        
        AuditLog log2 = AuditLog.builder()
                .id(2L)
                .email("user2@example.com")
                .action("UPDATE")
                .methodName("updateUser")
                .timestamp(now.plusMinutes(5))
                .build();

        when(auditLogRepository.findAll()).thenReturn(Flux.just(log1, log2));

        List<AuditLogResponseDTO> result = auditLogService.getAllAuditLogs().collectList().block();

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("user1@example.com", result.get(0).getEmail());
        assertEquals("CREATE", result.get(0).getAction());
        assertEquals("user2@example.com", result.get(1).getEmail());
        assertEquals("UPDATE", result.get(1).getAction());
        verify(auditLogRepository).findAll();
    }

    @Test
    @DisplayName("getAllAuditLogs - Using StepVerifier should emit all logs")
    void testGetAllAuditLogs_StepVerifier_EmitsAllLogs() {
        LocalDateTime now = LocalDateTime.now();
        
        AuditLog log1 = AuditLog.builder()
                .id(1L)
                .email("user@example.com")
                .action("DELETE")
                .methodName("deleteUser")
                .timestamp(now)
                .build();

        when(auditLogRepository.findAll()).thenReturn(Flux.just(log1));

        StepVerifier.create(auditLogService.getAllAuditLogs())
                .assertNext(dto -> {
                    assertEquals(1L, dto.getId());
                    assertEquals("user@example.com", dto.getEmail());
                    assertEquals("DELETE", dto.getAction());
                })
                .verifyComplete();
    }

    // ==========================================
    // Get Audit Logs by Email Tests
    // ==========================================

    @Test
    @DisplayName("getAuditLogsByEmail - Should return logs for specific email")
    void testGetAuditLogsByEmail_ReturnsLogsForEmail() {
        String email = "test@example.com";
        LocalDateTime now = LocalDateTime.now();
        
        AuditLog log1 = AuditLog.builder()
                .id(1L)
                .email(email)
                .action("CREATE")
                .methodName("createResource")
                .timestamp(now)
                .build();
        
        AuditLog log2 = AuditLog.builder()
                .id(2L)
                .email(email)
                .action("UPDATE")
                .methodName("updateResource")
                .timestamp(now.plusMinutes(10))
                .build();

        when(auditLogRepository.findByEmail(email)).thenReturn(Flux.just(log1, log2));

        List<AuditLogResponseDTO> result = auditLogService.getAuditLogsByEmail(email).collectList().block();

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(email, result.get(0).getEmail());
        assertEquals(email, result.get(1).getEmail());
        verify(auditLogRepository).findByEmail(email);
    }

    @Test
    @DisplayName("getAuditLogsByEmail - When no logs exist returns empty")
    void testGetAuditLogsByEmail_NoLogs_ReturnsEmpty() {
        String email = "nonexistent@example.com";
        
        when(auditLogRepository.findByEmail(email)).thenReturn(Flux.empty());

        List<AuditLogResponseDTO> result = auditLogService.getAuditLogsByEmail(email).collectList().block();

        assertNotNull(result);
        assertEquals(0, result.size());
        verify(auditLogRepository).findByEmail(email);
    }

    // ==========================================
    // Get Audit Logs by Action Tests
    // ==========================================

    @Test
    @DisplayName("getAuditLogsByAction - Should return logs for specific action")
    void testGetAuditLogsByAction_ReturnsLogsForAction() {
        LocalDateTime now = LocalDateTime.now();
        
        AuditLog log1 = AuditLog.builder()
                .id(1L)
                .email("user1@example.com")
                .action("CREATE")
                .methodName("createA")
                .timestamp(now)
                .build();
        
        AuditLog log2 = AuditLog.builder()
                .id(2L)
                .email("user2@example.com")
                .action("CREATE")
                .methodName("createB")
                .timestamp(now.plusMinutes(5))
                .build();

        when(auditLogRepository.findByAction("CREATE")).thenReturn(Flux.just(log1, log2));

        List<AuditLogResponseDTO> result = auditLogService.getAuditLogsByAction("create").collectList().block();

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("CREATE", result.get(0).getAction());
        assertEquals("CREATE", result.get(1).getAction());
        verify(auditLogRepository).findByAction("CREATE");
    }

    @Test
    @DisplayName("getAuditLogsByAction - Should convert action to uppercase")
    void testGetAuditLogsByAction_ConvertsToUppercase() {
        when(auditLogRepository.findByAction("DELETE")).thenReturn(Flux.empty());

        auditLogService.getAuditLogsByAction("delete").collectList().block();

        verify(auditLogRepository).findByAction("DELETE");
    }

    // ==========================================
    // Get Audit Logs by Date Range Tests
    // ==========================================

    @Test
    @DisplayName("getAuditLogsByDateRange - Should return logs within date range")
    void testGetAuditLogsByDateRange_ReturnsLogsInRange() {
        LocalDateTime start = LocalDateTime.of(2024, 1, 1, 0, 0);
        LocalDateTime end = LocalDateTime.of(2024, 1, 31, 23, 59);
        LocalDateTime withinRange = LocalDateTime.of(2024, 1, 15, 12, 0);
        
        AuditLog log1 = AuditLog.builder()
                .id(1L)
                .email("user@example.com")
                .action("CREATE")
                .methodName("createSomething")
                .timestamp(withinRange)
                .build();

        when(auditLogRepository.findByTimestampBetween(start, end)).thenReturn(Flux.just(log1));

        List<AuditLogResponseDTO> result = auditLogService.getAuditLogsByDateRange(start, end).collectList().block();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(withinRange, result.get(0).getTimestamp());
        verify(auditLogRepository).findByTimestampBetween(start, end);
    }

    @Test
    @DisplayName("getAuditLogsByDateRange - When no logs in range returns empty")
    void testGetAuditLogsByDateRange_NoLogsInRange_ReturnsEmpty() {
        LocalDateTime start = LocalDateTime.of(2024, 1, 1, 0, 0);
        LocalDateTime end = LocalDateTime.of(2024, 1, 31, 23, 59);
        
        when(auditLogRepository.findByTimestampBetween(start, end)).thenReturn(Flux.empty());

        List<AuditLogResponseDTO> result = auditLogService.getAuditLogsByDateRange(start, end).collectList().block();

        assertNotNull(result);
        assertEquals(0, result.size());
        verify(auditLogRepository).findByTimestampBetween(start, end);
    }

    // ==========================================
    // Get Audit Logs by Method Name Tests
    // ==========================================

    @Test
    @DisplayName("getAuditLogsByMethodName - Should return logs for specific method")
    void testGetAuditLogsByMethodName_ReturnsLogsForMethod() {
        String methodName = "createUser";
        LocalDateTime now = LocalDateTime.now();
        
        AuditLog log1 = AuditLog.builder()
                .id(1L)
                .email("user1@example.com")
                .action("CREATE")
                .methodName(methodName)
                .timestamp(now)
                .build();
        
        AuditLog log2 = AuditLog.builder()
                .id(2L)
                .email("user2@example.com")
                .action("CREATE")
                .methodName(methodName)
                .timestamp(now.plusHours(1))
                .build();

        when(auditLogRepository.findByMethodName(methodName)).thenReturn(Flux.just(log1, log2));

        List<AuditLogResponseDTO> result = auditLogService.getAuditLogsByMethodName(methodName).collectList().block();

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(methodName, result.get(0).getMethodName());
        assertEquals(methodName, result.get(1).getMethodName());
        verify(auditLogRepository).findByMethodName(methodName);
    }

    // ==========================================
    // Get Audit Logs by Email and Action Tests
    // ==========================================

    @Test
    @DisplayName("getAuditLogsByEmailAndAction - Should return logs for email and action")
    void testGetAuditLogsByEmailAndAction_ReturnsMatchingLogs() {
        String email = "admin@example.com";
        String action = "DELETE";
        LocalDateTime now = LocalDateTime.now();
        
        AuditLog log1 = AuditLog.builder()
                .id(1L)
                .email(email)
                .action(action)
                .methodName("deleteUser")
                .timestamp(now)
                .build();

        when(auditLogRepository.findByEmailAndAction(email, action)).thenReturn(Flux.just(log1));

        List<AuditLogResponseDTO> result = auditLogService.getAuditLogsByEmailAndAction(email, "delete").collectList().block();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(email, result.get(0).getEmail());
        assertEquals(action, result.get(0).getAction());
        verify(auditLogRepository).findByEmailAndAction(email, "DELETE");
    }

    @Test
    @DisplayName("getAuditLogsByEmailAndAction - Should convert action to uppercase")
    void testGetAuditLogsByEmailAndAction_ConvertsActionToUppercase() {
        String email = "test@example.com";
        
        when(auditLogRepository.findByEmailAndAction(email, "UPDATE")).thenReturn(Flux.empty());

        auditLogService.getAuditLogsByEmailAndAction(email, "update").collectList().block();

        verify(auditLogRepository).findByEmailAndAction(email, "UPDATE");
    }

    @Test
    @DisplayName("getAuditLogsByEmailAndAction - Using StepVerifier for multiple results")
    void testGetAuditLogsByEmailAndAction_StepVerifier_MultipleResults() {
        String email = "user@example.com";
        String action = "CREATE";
        LocalDateTime now = LocalDateTime.now();
        
        AuditLog log1 = AuditLog.builder()
                .id(1L)
                .email(email)
                .action(action)
                .methodName("createResource1")
                .timestamp(now)
                .build();
        
        AuditLog log2 = AuditLog.builder()
                .id(2L)
                .email(email)
                .action(action)
                .methodName("createResource2")
                .timestamp(now.plusMinutes(5))
                .build();

        when(auditLogRepository.findByEmailAndAction(email, action)).thenReturn(Flux.just(log1, log2));

        StepVerifier.create(auditLogService.getAuditLogsByEmailAndAction(email, "create"))
                .assertNext(dto -> {
                    assertEquals(1L, dto.getId());
                    assertEquals("createResource1", dto.getMethodName());
                })
                .assertNext(dto -> {
                    assertEquals(2L, dto.getId());
                    assertEquals("createResource2", dto.getMethodName());
                })
                .verifyComplete();
    }
}