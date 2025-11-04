package co.edu.puj.secchub_backend.admin.contract;

import reactor.core.publisher.Mono;

public interface AdminModuleTeacherContract {
    /**
     * Obtains teacher ID by user ID.
     * @param userId User ID
     * @return Teacher ID
     */
    Mono<Long> getTeacherIdByUserId(Long userId);
}
