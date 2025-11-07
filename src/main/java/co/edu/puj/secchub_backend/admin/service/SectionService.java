package co.edu.puj.secchub_backend.admin.service;

import co.edu.puj.secchub_backend.admin.contract.AdminModuleSectionContract;
import co.edu.puj.secchub_backend.admin.dto.PlanningStatusStatsDTO;
import co.edu.puj.secchub_backend.admin.dto.SectionCreateRequestDTO;
import co.edu.puj.secchub_backend.admin.dto.SectionResponseDTO;
import co.edu.puj.secchub_backend.admin.dto.SectionSummaryDTO;
import co.edu.puj.secchub_backend.admin.exception.SectionNotFoundException;
import co.edu.puj.secchub_backend.admin.model.Section;
import co.edu.puj.secchub_backend.admin.repository.SectionRepository;
import co.edu.puj.secchub_backend.security.contract.SecurityModuleUserContract;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.modelmapper.ModelMapper;
import org.springframework.r2dbc.core.DatabaseClient;
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
@Slf4j
@RequiredArgsConstructor
public class SectionService implements AdminModuleSectionContract{
    
    private final ModelMapper modelMapper;
    private final DatabaseClient databaseClient;
    private final SectionRepository sectionRepository;
    private final SecurityModuleUserContract userService;

    /**
     * Creates a new section.
     * @param sectionCreateRequestDTO dto with section data
     * @return Created section
     */
    public Mono<SectionResponseDTO> createSection(SectionCreateRequestDTO sectionCreateRequestDTO) {
        log.debug("Creating section with name: {}, userId: {}", 
                    sectionCreateRequestDTO.getName(), 
                    sectionCreateRequestDTO.getUserId());
        Section section = modelMapper.map(sectionCreateRequestDTO, Section.class);
        log.debug("Mapped to Section entity: {}", section);
        return sectionRepository.save(section)
                .doOnSuccess(savedSection -> log.debug("Successfully saved section: {}", savedSection))
                .doOnError(error -> log.error("Error saving section", error))
                .map(savedSection -> {
                    SectionResponseDTO responseDTO = modelMapper.map(savedSection, SectionResponseDTO.class);
                    log.debug("Mapped to SectionResponseDTO: {}", responseDTO);
                    return responseDTO;
                })
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

    /**
     * Gets planning status statistics (count of open and closed sections)
     * @return PlanningStatusStatsDTO with openCount, closedCount and totalCount
     */
    public Mono<PlanningStatusStatsDTO> getPlanningStatusStats() {
        return sectionRepository.findAll()
                .collectList()
                .map(allSections -> {
                    int openCount = (int) allSections.stream()
                            .filter(section -> !section.isPlanningClosed())
                            .count();

                    int closedCount = (int) allSections.stream()
                            .filter(Section::isPlanningClosed)
                            .count();

                    return PlanningStatusStatsDTO.builder()
                            .openCount(openCount)
                            .closedCount(closedCount)
                            .totalCount(allSections.size())
                            .build();
                });
    }

    /**
     * Gets a summary of all sections.
     * @return Flux of SectionSummaryDTO
     */
    public Flux<SectionSummaryDTO> getSectionsSummary() {
        return sectionRepository.findAll()
            .flatMap(section -> {
                Mono<Integer> classCountMono = countClassesInCurrentSemesterForSection(section.getId());
                Mono<Integer> classesWithoutTeachersMono = countClassesWithoutTeachersInCurrentSemesterForSection(section.getId());

                return Mono.zip(classCountMono, classesWithoutTeachersMono)
                    .map(tuple -> SectionSummaryDTO.builder()
                        .name(section.getName())
                        .planningClosed(section.isPlanningClosed())
                        .assignedClasses(tuple.getT1())
                        .unconfirmedTeachers(tuple.getT2())
                        .build()
                    );
            });
    }

    /**
     * Counts the number of classes in the current semester for a given section.
     * Uses DatabaseClient to execute a native SQL query.
     * 
     * @param sectionId Section ID
     * @return Mono with the count of classes in current semester
     */
    private Mono<Integer> countClassesInCurrentSemesterForSection(Long sectionId) {
        String sql = """
            SELECT COUNT(c.id) as count
            FROM class c
            INNER JOIN course co ON c.course_id = co.id
            INNER JOIN semester s ON c.semester_id = s.id
            WHERE co.section_id = :sectionId
            AND s.is_current = TRUE
            """;

        return databaseClient.sql(sql)
            .bind("sectionId", sectionId)
            .map(row -> row.get("count", Long.class))
            .one()
            .map(Long::intValue)
            .defaultIfEmpty(0);
    }

    /**
     * Counts the number of classes without assigned teachers in the current semester for a given section.
     * A class is considered without teachers if it has no teacher_class records.
     * Uses DatabaseClient to execute a native SQL query.
     * 
     * @param sectionId Section ID
     * @return Mono with the count of classes without teachers in current semester
     */
    private Mono<Integer> countClassesWithoutTeachersInCurrentSemesterForSection(Long sectionId) {
        String sql = """
            SELECT COUNT(c.id) as count
            FROM class c
            INNER JOIN course co ON c.course_id = co.id
            INNER JOIN semester s ON c.semester_id = s.id
            LEFT JOIN teacher_class tc ON c.id = tc.class_id
            WHERE co.section_id = :sectionId
            AND s.is_current = TRUE
            AND tc.id IS NULL
            """;

        return databaseClient.sql(sql)
            .bind("sectionId", sectionId)
            .map(row -> row.get("count", Long.class))
            .one()
            .map(Long::intValue)
            .defaultIfEmpty(0);
    }
}