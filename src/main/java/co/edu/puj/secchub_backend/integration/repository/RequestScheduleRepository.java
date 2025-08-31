package co.edu.puj.secchub_backend.integration.repository;

import co.edu.puj.secchub_backend.integration.model.RequestSchedule;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * Repositorio JPA para la entidad RequestSchedule.
 * Proporciona métodos para acceder y manipular los horarios asociados a solicitudes académicas.
 */
public interface RequestScheduleRepository extends JpaRepository<RequestSchedule, Long> {
    /**
     * Busca los horarios asociados a una solicitud académica específica.
     * @param academicRequestId ID de la solicitud académica
     * @return Lista de horarios asociados
     */
    List<RequestSchedule> findByAcademicRequestId(Long academicRequestId);
}
