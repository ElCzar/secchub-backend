package co.edu.puj.secchub_backend.admin.service;

import co.edu.puj.secchub_backend.admin.contract.AdminModuleSectionContract;
import co.edu.puj.secchub_backend.admin.dto.SectionCreateRequestDTO;
import co.edu.puj.secchub_backend.admin.dto.SectionResponseDTO;
import co.edu.puj.secchub_backend.admin.exception.SectionNotFoundException;
import co.edu.puj.secchub_backend.admin.model.Section;
import co.edu.puj.secchub_backend.admin.repository.SectionRepository;
import co.edu.puj.secchub_backend.security.contract.SecurityModuleUserContract;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.stereotype.Service;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

/**
 * Service for managing sections business logic.
 * Handles operations for creating, querying, updating sections.
 */
@Service
@RequiredArgsConstructor
public class SectionService implements AdminModuleSectionContract{
    
    private final ModelMapper modelMapper;
    private final SectionRepository sectionRepository;
    private final SecurityModuleUserContract userService;

    /**
     * Creates a new section.
     * @param sectionCreateRequestDTO dto with section data
     * @return Created section
     */
    public Mono<SectionResponseDTO> createSection(SectionCreateRequestDTO sectionCreateRequestDTO) {
        Section section = modelMapper.map(sectionCreateRequestDTO, Section.class);
        return sectionRepository.save(section)
                .map(savedSection -> modelMapper.map(savedSection, SectionResponseDTO.class))
                .subscribeOn(Schedulers.boundedElastic());
    }

    /**
     * Lists all existing sections.
     * @return List of sections
     */
    public Flux<SectionResponseDTO> findAllSections() {
        return sectionRepository.findAll()
                .map(section -> modelMapper.map(section, SectionResponseDTO.class));
    }

    /**
     * Get a section by its ID.
     * @param sectionId Section ID
     * @return Section with the given ID
     */
    public Mono<SectionResponseDTO> findSectionById(Long sectionId) {
        return sectionRepository.findById(sectionId)
                .switchIfEmpty(Mono.error(new SectionNotFoundException("Section not found with id: " + sectionId)))
                .map(section -> modelMapper.map(section, SectionResponseDTO.class));
    }

    /**
     * Mono sections by user ID.
     * @param userId User ID
     * @return Mono of sections managed by the user
     */
    public Mono<SectionResponseDTO> findSectionsByUserId(Long userId) {
        return sectionRepository.findByUserId(userId)
                .switchIfEmpty(Mono.error(new SectionNotFoundException("Section not found for user id: " + userId)))
                .map(section -> modelMapper.map(section, SectionResponseDTO.class));
    }

    /**
     * Close planning for current user's section
     * @return Updated section
     */
    public Mono<SectionResponseDTO> closePlanningForCurrentUser() {
        return ReactiveSecurityContextHolder.getContext()
                .map(securityContext -> securityContext.getAuthentication().getName())
                .flatMap(username ->
                        userService.getUserIdByEmail(username)
                                .flatMap(sectionRepository::findByUserId)
                                .flatMap(section -> {
                                    section.setPlanningClosed(true);
                                    return sectionRepository.save(section);
                                })
                )
                .map(updatedSection -> modelMapper.map(updatedSection, SectionResponseDTO.class));
    }

    /**
     * Opens planning for all sections.
     * Used when a new semester is created.
     * @return void
     */
    public void openPlanningForAllSections() {
        sectionRepository.findAll()
                .flatMap(section -> {
                    section.setPlanningClosed(false);
                    return sectionRepository.save(section);
                })
                .subscribe();
    }

    /**
     * Gets if planning is closed for current user's section
     * @return true if planning is closed, false otherwise
     */
    public Mono<Boolean> isPlanningClosedForCurrentUser() {
        return ReactiveSecurityContextHolder.getContext()
                .map(securityContext -> securityContext.getAuthentication().getName())
                .flatMap(username -> 
                    userService.getUserIdByEmail(username)
                        .flatMap(sectionRepository::findByUserId)
                        .map(Section::isPlanningClosed)
                );
    }
    
    /**
     * Implements method to get section ID by user ID.
     * @param userId User ID
     * @return Section ID
     */
    @Override
    public Mono<Long> getSectionIdByUserId(Long userId) {
        return sectionRepository.findByUserId(userId)
                .map(Section::getId);
    }
}