package co.edu.puj.secchub_backend.admin.service;

import org.modelmapper.ModelMapper;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.reactive.TransactionalOperator;

import co.edu.puj.secchub_backend.admin.contract.AdminModuleSemesterContract;
import co.edu.puj.secchub_backend.admin.dto.SemesterRequestDTO;
import co.edu.puj.secchub_backend.admin.dto.SemesterResponseDTO;
import co.edu.puj.secchub_backend.admin.exception.SemesterBadRequestException;
import co.edu.puj.secchub_backend.admin.exception.SemesterNotFoundException;
import co.edu.puj.secchub_backend.admin.model.Semester;
import co.edu.puj.secchub_backend.admin.repository.SemesterRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Service class for managing semesters.
 * Provides methods to create, query, update and delete semesters.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class SemesterService implements AdminModuleSemesterContract {
    private final TransactionalOperator transactionalOperator;
    private final SemesterRepository semesterRepository;
    private final ModelMapper modelMapper;
    private final SectionService sectionService;

    /**
     * Creates a new semester and sets to false the last semester's active field.
     * Also changes all sections' planningClosed field to false.
     * @param semesterRequestDTO with semester data
     * @return semesterResponseDTO with created semester data
     * @throws SemesterBadRequestException if semester data is invalid
     */
    @CacheEvict(value = {"current-semester", "current-semester-id"}, allEntries = true)
    public Mono<SemesterResponseDTO> createSemester(SemesterRequestDTO semesterRequestDTO) {
        if (semesterRequestDTO.getYear() == null ||
            semesterRequestDTO.getStartDate() == null ||
            semesterRequestDTO.getEndDate() == null ||
            semesterRequestDTO.getPeriod() == null) {
            return Mono.error(new SemesterBadRequestException(
                "Semester year, start date, end date and period cannot be null"
            ));
        }

        return semesterRepository.findByIsCurrentTrue()
        .flatMap(existingSemester -> {
            existingSemester.setIsCurrent(false);
            return semesterRepository.save(existingSemester);
        })
        .then(Mono.defer(() -> {
            Semester semester = modelMapper.map(semesterRequestDTO, Semester.class);
            semester.setIsCurrent(true);
            return semesterRepository.save(semester);
        }))
        .flatMap(savedSemester ->
            Mono.fromRunnable(sectionService::openPlanningForAllSections)
                .thenReturn(savedSemester)
        )
        .map(savedSemester -> modelMapper.map(savedSemester, SemesterResponseDTO.class))
        .as(transactionalOperator::transactional);

    }

    /**
     * Obtains the current active semester.
     * @return semesterResponseDTO with current semester data
     * @throws SemesterBadRequestException if no current semester is found
     */
    @Cacheable("current-semester")
    public Mono<SemesterResponseDTO> getCurrentSemester() {
        return semesterRepository.findByIsCurrentTrue()
                .map(semester -> modelMapper.map(semester, SemesterResponseDTO.class))
                .switchIfEmpty(Mono.error(new SemesterNotFoundException("No current semester found")));
    }

    /**
     * Implementation of AdminModuleSemesterContract.
     * Gets the current semester ID.
     */
    @Override
    @Cacheable("current-semester-id")
    public Mono<Long> getCurrentSemesterId() {
        return semesterRepository.findByIsCurrentTrue()
                .map(Semester::getId)
                .switchIfEmpty(Mono.error(new SemesterNotFoundException("No current semester found")));
    }

    /**
     * Gets all semesters.
     * @return List of semesterResponseDTO with all semesters data
     */
    public Flux<SemesterResponseDTO> getAllSemesters() {
        return semesterRepository.findAll()
                .map(semester -> modelMapper.map(semester, SemesterResponseDTO.class));
    }

    /**
     * Gets semester by year and period.
     * @param year The year of the semester
     * @param period The period of the semester (1 or 2)
     * @return SemesterResponseDTO with semester data or null if not found
     */
    public Mono<SemesterResponseDTO> getSemesterByYearAndPeriod(Integer year, Integer period) {
        return semesterRepository.findByYearAndPeriod(year, period)
                .map(semester -> modelMapper.map(semester, SemesterResponseDTO.class));
    }
}
