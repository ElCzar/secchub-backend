package co.edu.puj.secchub_backend.admin.service;

import co.edu.puj.secchub_backend.admin.dto.SectionDTO;
import co.edu.puj.secchub_backend.admin.model.Section;
import co.edu.puj.secchub_backend.admin.repository.SectionRepository;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
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
     * @return List of sections
     */
    public List<SectionDTO> findAllSections() {
        return sectionRepository.findAll()
                .stream()
                .map(section -> modelMapper.map(section, SectionDTO.class))
                .toList();
    }

    /**
     * Get a section by its ID.
     * @param sectionId Section ID
     * @return Section with the given ID
     */
    public Mono<SectionDTO> findSectionById(Long sectionId) {
        return Mono.fromCallable(() -> {
            Section section = sectionRepository.findById(sectionId)
                    .orElseThrow(() -> new RuntimeException("Section not found for consult: " + sectionId));
            return modelMapper.map(section, SectionDTO.class);
        }).subscribeOn(Schedulers.boundedElastic());
    }

    /**
     * Lists sections by user ID.
     * @param userId User ID
     * @return List of sections managed by the user
     */
    public List<SectionDTO> findSectionsByUserId(Long userId) {
        return sectionRepository.findByUserId(userId)
                .stream()
                .map(section -> modelMapper.map(section, SectionDTO.class))
                .toList();
    }
}