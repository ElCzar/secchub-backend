package co.edu.puj.secchub_backend.admin.contract;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface AdminModuleTeacherContract {
    /**
     * Obtains teacher ID by user ID.
     * @param userId User ID
     * @return Teacher ID
     */
    Mono<Long> getTeacherIdByUserId(Long userId);

    /**
     * Obtains TeacherResponseDTO by teacher ID.
     * @param teacherId Teacher ID
     * @return TeacherResponseDTO
     */
    Mono<TeacherResponseDTO> getTeacherById(Long teacherId);
    
    /**
     * Obtains all teachers.
     * @return All teachers
     */
    Flux<TeacherResponseDTO> getAllTeachers();
}
