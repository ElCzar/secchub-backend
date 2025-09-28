package co.edu.puj.secchub_backend.integration.repository;

import co.edu.puj.secchub_backend.security.model.Status;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository for Status entity.
 * Provides access to status records for tracking application states.
 */
@Repository
public interface StatusRepository extends JpaRepository<Status, Long> {
}