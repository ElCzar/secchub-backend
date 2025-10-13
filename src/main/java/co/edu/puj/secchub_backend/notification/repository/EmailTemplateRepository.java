package co.edu.puj.secchub_backend.notification.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import co.edu.puj.secchub_backend.notification.model.EmailTemplate;

public interface EmailTemplateRepository extends JpaRepository<EmailTemplate, Long> {
    EmailTemplate findByName(String name);
}
