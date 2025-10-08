package co.edu.puj.secchub_backend.admin.service;

import java.util.List;
import java.util.Optional;

import org.modelmapper.ModelMapper;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import co.edu.puj.secchub_backend.admin.contract.AdminModuleSemesterContract;
import co.edu.puj.secchub_backend.admin.dto.SemesterRequestDTO;
import co.edu.puj.secchub_backend.admin.dto.SemesterResponseDTO;
import co.edu.puj.secchub_backend.admin.exception.SemesterBadRequestException;
import co.edu.puj.secchub_backend.admin.model.Semester;
import co.edu.puj.secchub_backend.admin.repository.SemesterRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

/**
 * Service class for managing semesters.
 * Provides methods to create, query, update and delete semesters.
 */
@Service
@RequiredArgsConstructor
public class SemesterService implements AdminModuleSemesterContract {

    private final SemesterRepository semesterRepository;
    private final ModelMapper modelMapper;

    /**
     * Creates a new semester and sets to false the last semester's active field.
     * @param semesterRequestDTO with semester data
     * @return semesterResponseDTO with created semester data
     * @throws SemesterBadRequestException if semester data is invalid
     */
    @Transactional
    @CacheEvict(value = {"current-semester", "current-semester-id"}, allEntries = true)
    public Mono<SemesterResponseDTO> createSemester(SemesterRequestDTO semesterRequestDTO) {
        return Mono.fromCallable(() -> {
            if (semesterRequestDTO.getYear() == null || semesterRequestDTO.getStartDate() == null || semesterRequestDTO.getEndDate() == null || semesterRequestDTO.getPeriod() == null) {
                throw new SemesterBadRequestException("Semester year, start date, end date and period cannot be null");
            }

            Optional<Semester> currentSemester = semesterRepository.findByIsCurrentTrue();
            currentSemester.ifPresent(semester -> {
                semester.setIsCurrent(false);
                semesterRepository.save(semester);
            });

            Semester semester = modelMapper.map(semesterRequestDTO, Semester.class);
            semester.setIsCurrent(true);
            semesterRepository.save(semester);
            return modelMapper.map(semester, SemesterResponseDTO.class);
        });
    }

    /**
     * Obtains the current active semester.
     * @return semesterResponseDTO with current semester data
     * @throws SemesterBadRequestException if no current semester is found
     */
    @Cacheable("current-semester")
    public Mono<SemesterResponseDTO> getCurrentSemester() {
        return Mono.fromCallable(() -> {
            Semester currentSemester = semesterRepository.findByIsCurrentTrue()
                    .orElseThrow(() -> new SemesterBadRequestException("No current semester found"));
            return modelMapper.map(currentSemester, SemesterResponseDTO.class);
        });
    }

    @Override
    @Cacheable("current-semester-id")
    public Long getCurrentSemesterId() {
        Semester currentSemester = semesterRepository.findByIsCurrentTrue()
                .orElseThrow(() -> new SemesterBadRequestException("No current semester found"));
        return currentSemester.getId();
    }

    /**
     * Obtains all semesters.
     * @return List of semesterResponseDTO with all semesters data
     */
    public Mono<List<SemesterResponseDTO>> getAllSemesters() {
        return Mono.fromCallable(() -> {
            List<Semester> semesters = semesterRepository.findAll();
            return semesters.stream()
                    .map(semester -> modelMapper.map(semester, SemesterResponseDTO.class))
                    .toList();
        });
    }
}
