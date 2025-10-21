package co.edu.puj.secchub_backend.admin.service;

import co.edu.puj.secchub_backend.admin.dto.SectionCreateRequestDTO;
import co.edu.puj.secchub_backend.admin.dto.SectionResponseDTO;
import co.edu.puj.secchub_backend.admin.model.Section;
import co.edu.puj.secchub_backend.admin.repository.SectionRepository;
import co.edu.puj.secchub_backend.security.contract.SecurityModuleUserContract;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.List;

/**
 * Service for managing sections business logic.
 * Handles operations for creating, querying, updating sections.
 */
@Service
@RequiredArgsConstructor
public class SectionService {
    
    private final ModelMapper modelMapper;
    private final SectionRepository sectionRepository;
    private final SecurityModuleUserContract securityModuleUserContract;

    /**
     * Creates a new section.
     * @param sectionCreateRequestDTO dto with section data
     * @return Created section
     */
    public SectionResponseDTO createSection(SectionCreateRequestDTO sectionCreateRequestDTO) {
        Section section = modelMapper.map(sectionCreateRequestDTO, Section.class);
        section.setPlanningClosed(false);
        Section saved = sectionRepository.save(section);
        return modelMapper.map(saved, SectionResponseDTO.class);
    }

    /**
     * Lists all existing sections.
     * @return List of sections
     */
    public List<SectionResponseDTO> findAllSections() {
        return sectionRepository.findAll()
                .stream()
                .map(section -> modelMapper.map(section, SectionResponseDTO.class))
                .toList();
    }

    /**
     * Get a section by its ID.
     * @param sectionId Section ID
     * @return Section with the given ID
     */
    public Mono<SectionResponseDTO> findSectionById(Long sectionId) {
        return Mono.fromCallable(() -> {
            Section section = sectionRepository.findById(sectionId)
                    .orElseThrow(() -> new RuntimeException("Section not found for consult: " + sectionId));
            return modelMapper.map(section, SectionResponseDTO.class);
        }).subscribeOn(Schedulers.boundedElastic());
    }

    /**
     * Lists sections by user ID.
     * @param userId User ID
     * @return List of sections managed by the user
     */
    public List<SectionResponseDTO> findSectionsByUserId(Long userId) {
        return sectionRepository.findByUserId(userId)
                .stream()
                .map(section -> modelMapper.map(section, SectionResponseDTO.class))
                .toList();
    }

    /**
     * Close planning for current user's section
     * @return Updated section
     */
    public Mono<SectionResponseDTO> closePlanningForCurrentUser() {
        return ReactiveSecurityContextHolder.getContext()
                .map(securityContext -> securityContext.getAuthentication().getName())
                .flatMap(username -> Mono.fromCallable(() -> {
                    Long userId = securityModuleUserContract.getUserIdByEmail(username);
                    Section section = sectionRepository.findByUserId(userId)
                            .orElseThrow(() -> new RuntimeException("Section not found for user: " + username));
                    section.setPlanningClosed(true);
                    Section updated = sectionRepository.save(section);
                    return modelMapper.map(updated, SectionResponseDTO.class);
                }).subscribeOn(Schedulers.boundedElastic()));
    }

    /**
     * Opens planning for all sections.
     * Used when a new semester is created.
     * @return void
     */
    public void openPlanningForAllSections() {
        List<Section> sections = sectionRepository.findAll();
        for (Section section : sections) {
            section.setPlanningClosed(false);
        }
        sectionRepository.saveAll(sections);
    }

    /**
     * Gets if planning is closed for current user's section
     * @return true if planning is closed, false otherwise
     */
    public Mono<Boolean> isPlanningClosedForCurrentUser() {
        return ReactiveSecurityContextHolder.getContext()
                .map(securityContext -> securityContext.getAuthentication().getName())
                .flatMap(username -> Mono.fromCallable(() -> {
                    Long userId = securityModuleUserContract.getUserIdByEmail(username);
                    Section section = sectionRepository.findByUserId(userId)
                            .orElseThrow(() -> new RuntimeException("Section not found for user: " + username));
                    return section.isPlanningClosed();
                }).subscribeOn(Schedulers.boundedElastic()));
    }
}