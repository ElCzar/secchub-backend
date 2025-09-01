package co.edu.puj.secchub_backend.integration.repository;

import co.edu.puj.secchub_backend.integration.model.MonitorAvailability;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * Repository for MonitorAvailability entity.
 */
public interface MonitorAvailabilityRepository extends JpaRepository<MonitorAvailability, Long> {
    List<MonitorAvailability> findByMonitorRequestId(Long monitorRequestId);
}
