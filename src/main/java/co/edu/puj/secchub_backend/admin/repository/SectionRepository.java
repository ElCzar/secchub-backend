package co.edu.puj.secchub_backend.admin.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import co.edu.puj.secchub_backend.admin.model.Section;

import java.util.Optional;

public interface SectionRepository extends JpaRepository<Section, Long> {
    Optional<Section> findByUserId(Long userId);
}