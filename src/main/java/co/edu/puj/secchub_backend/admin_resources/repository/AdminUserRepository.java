package co.edu.puj.secchub_backend.admin_resources.repository;

import co.edu.puj.secchub_backend.admin_resources.model.AdminUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AdminUserRepository extends JpaRepository<AdminUser, Long> {
}
