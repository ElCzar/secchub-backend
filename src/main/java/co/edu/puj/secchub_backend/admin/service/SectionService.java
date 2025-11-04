package co.edu.puj.secchub_backend.admin.service;

import co.edu.puj.secchub_backend.admin.dto.SectionCreateRequestDTO;
import co.edu.puj.secchub_backend.admin.dto.SectionResponseDTO;
import co.edu.puj.secchub_backend.admin.model.Section;
import co.edu.puj.secchub_backend.admin.repository.SectionRepository;
import co.edu.puj.secchub_backend.security.contract.SecurityModuleUserContract;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    private final JdbcTemplate jdbcTemplate;

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

    /**
     * Gets planning status statistics (count of open and closed sections)
     * @return Map with openCount (planning_closed = 0) and closedCount (planning_closed = 1)
     */
    public Mono<java.util.Map<String, Integer>> getPlanningStatusStats() {
        return Mono.fromCallable(() -> {
            List<Section> allSections = sectionRepository.findAll();
            
            int openCount = (int) allSections.stream()
                    .filter(section -> !section.isPlanningClosed())
                    .count();
            
            int closedCount = (int) allSections.stream()
                    .filter(Section::isPlanningClosed)
                    .count();
            
            return java.util.Map.of(
                    "openCount", openCount,
                    "closedCount", closedCount,
                    "totalSections", allSections.size()
            );
        }).subscribeOn(Schedulers.boundedElastic());
    }

    /**
     * Get sections summary for admin dashboard.
     * Returns for each section:
     * - name: section name
     * - planningClosed: boolean (planning_closed field)
     * - assignedClasses: count of classes in that academic section (using course.section_id)
     * - unconfirmedTeachers: count of teacher_class records with status_id = 4 for that section
     * 
     * @return List of maps with section summary data
     */
    public Mono<List<Map<String, Object>>> getSectionsSummary() {
        return Mono.fromCallable(() -> {
            // Get current semester
            String currentSemesterSql = "SELECT id FROM semester WHERE is_current = 1";
            List<Map<String, Object>> semesterResult = jdbcTemplate.queryForList(currentSemesterSql);
            
            if (semesterResult.isEmpty()) {
                return new ArrayList<Map<String, Object>>();
            }
            
            Long currentSemesterId = ((Number) semesterResult.get(0).get("id")).longValue();
            
            // Get all sections
            List<Section> sections = sectionRepository.findAll();
            List<Map<String, Object>> summaryList = new ArrayList<>();
            
            for (Section section : sections) {
                Map<String, Object> summary = new HashMap<>();
                
                // Add section name and planning status
                summary.put("name", section.getName());
                summary.put("planningClosed", section.isPlanningClosed());
                
                // Count assigned classes for this section
                // Remember: use course.section_id (academic section), NOT class.section (group number)
                String assignedClassesSql = """
                    SELECT COUNT(DISTINCT c.id) as count
                    FROM class c
                    INNER JOIN course co ON c.course_id = co.id
                    WHERE co.section_id = ?
                      AND c.semester_id = ?
                    """;
                List<Map<String, Object>> classCountResult = jdbcTemplate.queryForList(
                    assignedClassesSql, 
                    section.getId(), 
                    currentSemesterId
                );
                int assignedClasses = classCountResult.isEmpty() ? 0 : 
                    ((Number) classCountResult.get(0).get("count")).intValue();
                summary.put("assignedClasses", assignedClasses);
                
                // Count unconfirmed teachers (status_id = 4) for this section's classes
                String unconfirmedTeachersSql = """
                    SELECT COUNT(*) as count
                    FROM teacher_class tc
                    INNER JOIN class c ON tc.class_id = c.id
                    INNER JOIN course co ON c.course_id = co.id
                    WHERE co.section_id = ?
                      AND c.semester_id = ?
                      AND tc.status_id = 4
                    """;
                List<Map<String, Object>> unconfirmedResult = jdbcTemplate.queryForList(
                    unconfirmedTeachersSql,
                    section.getId(),
                    currentSemesterId
                );
                int unconfirmedTeachers = unconfirmedResult.isEmpty() ? 0 :
                    ((Number) unconfirmedResult.get(0).get("count")).intValue();
                summary.put("unconfirmedTeachers", unconfirmedTeachers);
                
                summaryList.add(summary);
            }
            
            return summaryList;
        }).subscribeOn(Schedulers.boundedElastic());
    }
}