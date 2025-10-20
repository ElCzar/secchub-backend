package co.edu.puj.secchub_backend.admin.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
            System.out.println("🔍 SemesterService: Buscando semestre actual...");
            Semester currentSemester = semesterRepository.findByIsCurrentTrue()
                    .orElseThrow(() -> new SemesterBadRequestException("No current semester found"));
            
            System.out.println("📅 SemesterService: Semestre encontrado - ID: " + currentSemester.getId());
            System.out.println("📅 SemesterService: Año: " + currentSemester.getYear());
            System.out.println("📅 SemesterService: Período: " + currentSemester.getPeriod());
            System.out.println("📅 SemesterService: Fecha inicio: " + currentSemester.getStartDate());
            System.out.println("📅 SemesterService: Fecha fin: " + currentSemester.getEndDate());
            System.out.println("📅 SemesterService: Es actual: " + currentSemester.getIsCurrent());
            
            SemesterResponseDTO response = modelMapper.map(currentSemester, SemesterResponseDTO.class);
            System.out.println("✅ SemesterService: DTO mapeado: " + response);
            return response;
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
     * Gets all semesters.
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

    /**
     * Gets semester by year and period.
     * @param year The year of the semester
     * @param period The period of the semester (1 or 2)
     * @return SemesterResponseDTO with semester data or null if not found
     */
    public Mono<SemesterResponseDTO> getSemesterByYearAndPeriod(Integer year, Integer period) {
        return Mono.fromCallable(() -> {
            Optional<Semester> semester = semesterRepository.findByYearAndPeriod(year, period);
            return semester.map(s -> modelMapper.map(s, SemesterResponseDTO.class))
                          .orElse(null);
        });
    }

    /**
     * Implementation of AdminModuleSemesterContract.
     * Gets all past semesters (excluding current active semester).
     */
    @Override
    public List<Map<String, Object>> getPastSemesters() {
        Long currentSemesterId = getCurrentSemesterId();
        List<Semester> allSemesters = semesterRepository.findAll();
        
        return allSemesters.stream()
                .filter(semester -> !semester.getId().equals(currentSemesterId))
                .map(semester -> {
                    Map<String, Object> semesterInfo = new HashMap<>();
                    semesterInfo.put("id", semester.getId());
                    // Generar nombre descriptivo basado en año y periodo
                    String name = String.format("%d-%d", semester.getYear(), semester.getPeriod());
                    semesterInfo.put("name", name);
                    semesterInfo.put("year", semester.getYear());
                    semesterInfo.put("period", semester.getPeriod());
                    semesterInfo.put("startDate", semester.getStartDate());
                    semesterInfo.put("endDate", semester.getEndDate());
                    return semesterInfo;
                })
                .toList();
    }
}
