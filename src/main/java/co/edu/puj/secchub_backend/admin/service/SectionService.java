package co.edu.puj.secchub_backend.admin.service;

import co.edu.puj.secchub_backend.admin.dto.SectionCreateRequestDTO;
import co.edu.puj.secchub_backend.admin.dto.SectionResponseDTO;
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
     * @param SectionCreateRequestDTO dto with section data
     * @return Created section
     */
    public SectionResponseDTO createSection(SectionCreateRequestDTO SectionCreateRequestDTO) {
        Section section = modelMapper.map(SectionCreateRequestDTO, Section.class);
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
}