package co.edu.puj.secchub_backend.integration.service;

import co.edu.puj.secchub_backend.integration.dto.SectionDTO;
import co.edu.puj.secchub_backend.integration.model.Section;
import co.edu.puj.secchub_backend.integration.repository.SectionRepository;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
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
public class SectionService {
    
    private final ModelMapper modelMapper;
    private final SectionRepository sectionRepository;

    /**
     * Creates a new section.
     * @param sectionDTO dto with section data
     * @return Created section
     */
    public Mono<SectionDTO> createSection(SectionDTO sectionDTO) {
        return Mono.fromCallable(() -> {
            Section section = modelMapper.map(sectionDTO, Section.class);
            Section saved = sectionRepository.save(section);
            return modelMapper.map(saved, SectionDTO.class);
        }).subscribeOn(Schedulers.boundedElastic());
    }

    /**
     * Lists all existing sections.
     * @return Stream of sections
     */
    public Flux<SectionDTO> findAllSections() {
        return Mono.fromCallable(sectionRepository::findAll)
                .flatMapMany(Flux::fromIterable)
                .map(section -> modelMapper.map(section, SectionDTO.class))
                .subscribeOn(Schedulers.boundedElastic());
    }

    /**
     * Get a section by its ID.
     * @param sectionId Section ID
     * @return Section with the given ID
     */
    public Mono<SectionDTO> findSectionById(Long sectionId) {
        return Mono.fromCallable(() -> {
            Section section = sectionRepository.findById(sectionId)
                    .orElseThrow(() -> new RuntimeException("Section not found: " + sectionId));
            return modelMapper.map(section, SectionDTO.class);
        }).subscribeOn(Schedulers.boundedElastic());
    }

    /**
     * Lists sections by user ID.
     * @param userId User ID
     * @return Stream of sections managed by the user
     */
    public Flux<SectionDTO> findSectionsByUserId(Long userId) {
        return Mono.fromCallable(() -> sectionRepository.findByUserId(userId))
                .flatMapMany(Flux::fromIterable)
                .map(section -> modelMapper.map(section, SectionDTO.class))
                .subscribeOn(Schedulers.boundedElastic());
    }
}