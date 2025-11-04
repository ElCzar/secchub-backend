package co.edu.puj.secchub_backend.notification.repository;


import org.springframework.data.r2dbc.repository.R2dbcRepository;

import co.edu.puj.secchub_backend.notification.model.EmailTemplate;
import reactor.core.publisher.Mono;

public interface EmailTemplateRepository extends R2dbcRepository<EmailTemplate, Long> {
    Mono<EmailTemplate> findByName(String name);
}
