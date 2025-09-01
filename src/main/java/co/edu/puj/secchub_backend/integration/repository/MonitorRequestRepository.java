package co.edu.puj.secchub_backend.integration.repository;

import co.edu.puj.secchub_backend.integration.model.MonitorRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository for MonitorRequest entity.
 */
public interface MonitorRequestRepository extends JpaRepository<MonitorRequest, Long> {
    List<MonitorRequest> findBySemesterId(Long semesterId);
    List<MonitorRequest> findByStudentId(Long studentId);
    List<MonitorRequest> findByStatusId(Long statusId);

    @Query(value = """
        SELECT mr.* 
        FROM monitor_request mr
        LEFT JOIN course c ON mr.course_id = c.id
        WHERE (mr.type = 'ACADEMIC' AND c.section_id = :sectionId)
           OR (mr.type = 'ADMINISTRATIVE' AND mr.section_id = :sectionId)
        """, nativeQuery = true)
    List<MonitorRequest> findRequestsForSection(@Param("sectionId") Long sectionId);
}



