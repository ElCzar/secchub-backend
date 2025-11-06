package co.edu.puj.secchub_backend.admin.service;

import co.edu.puj.secchub_backend.admin.dto.SemesterRequestDTO;
import co.edu.puj.secchub_backend.admin.dto.SemesterResponseDTO;
import co.edu.puj.secchub_backend.admin.exception.SemesterBadRequestException;
import co.edu.puj.secchub_backend.admin.exception.SemesterNotFoundException;
import co.edu.puj.secchub_backend.admin.model.Semester;
import co.edu.puj.secchub_backend.admin.repository.SemesterRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.modelmapper.ModelMapper;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.transaction.reactive.TransactionalOperator;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link SemesterService}.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("SemesterService Unit Test")
class SemesterServiceTest {

    @Mock
    private TransactionalOperator transactionalOperator;

    @Mock
    private SemesterRepository semesterRepository;

    @Mock
    private ModelMapper modelMapper;

    @Mock
    private SectionService sectionService;

    @InjectMocks
    private SemesterService semesterService;

    @Test
    @DisplayName("createSemester - When valid request saves new semester and deactivates old")
    void testCreateSemester_ValidRequest_CreatesNewSemester() {
        SemesterRequestDTO request = SemesterRequestDTO.builder()
                .year(2025)
                .period(1)
                .startDate(LocalDate.of(2025, 1, 10))
                .endDate(LocalDate.of(2025, 6, 15))
                .startSpecialWeek(LocalDate.of(2025, 3, 10))
                .build();

        Semester currentSemester = Semester.builder()
                .id(1L)
                .year(2024)
                .period(2)
                .isCurrent(true)
                .build();

        Semester newSemester = Semester.builder()
                .id(2L)
                .year(2025)
                .period(1)
                .isCurrent(true)
                .build();

        SemesterResponseDTO responseDTO = SemesterResponseDTO.builder()
                .id(2L)
                .year(2025)
                .period(1)
                .isCurrent(true)
                .build();

        when(semesterRepository.findByIsCurrentTrue()).thenReturn(Mono.just(currentSemester));
        when(semesterRepository.save(currentSemester)).thenReturn(Mono.just(currentSemester));
        when(modelMapper.map(request, Semester.class)).thenReturn(newSemester);
        when(semesterRepository.save(newSemester)).thenReturn(Mono.just(newSemester));
        when(modelMapper.map(newSemester, SemesterResponseDTO.class)).thenReturn(responseDTO);
        lenient().when(transactionalOperator.transactional(ArgumentMatchers.<Mono<Object>>any()))
            .thenAnswer(invocation -> invocation.getArgument(0));

        SemesterResponseDTO result = semesterService.createSemester(request).block();

        assertNotNull(result);
        assertEquals(2025, result.getYear());
        assertEquals(1, result.getPeriod());
        assertTrue(result.getIsCurrent());
        verify(semesterRepository).findByIsCurrentTrue();
        verify(semesterRepository, times(2)).save(any(Semester.class));
        verify(modelMapper).map(request, Semester.class);
        verify(sectionService).openPlanningForAllSections();
    }

    @Test
    @DisplayName("createSemester - When missing fields throws SemesterBadRequestException")
    void testCreateSemester_InvalidRequest_ThrowsException() {
        SemesterRequestDTO invalid = SemesterRequestDTO.builder()
                .year(null)
                .period(1)
                .build();

        Mono<SemesterResponseDTO> result = semesterService.createSemester(invalid);

        assertThrows(SemesterBadRequestException.class, result::block);
        verify(semesterRepository, never()).save(any());
    }

    @Test
    @DisplayName("getCurrentSemester - When found returns mapped DTO")
    void testGetCurrentSemester_ReturnsMappedDTO() {
        Semester semester = Semester.builder()
                .id(3L)
                .year(2025)
                .period(1)
                .isCurrent(true)
                .build();

        SemesterResponseDTO dto = SemesterResponseDTO.builder()
                .id(3L)
                .year(2025)
                .period(1)
                .isCurrent(true)
                .build();

        when(semesterRepository.findByIsCurrentTrue()).thenReturn(Mono.just(semester));
        when(modelMapper.map(semester, SemesterResponseDTO.class)).thenReturn(dto);

        SemesterResponseDTO result = semesterService.getCurrentSemester().block();

        assertNotNull(result);
        assertEquals(2025, result.getYear());
        assertEquals(1, result.getPeriod());
        verify(semesterRepository).findByIsCurrentTrue();
        verify(modelMapper).map(semester, SemesterResponseDTO.class);
    }

    @Test
    @DisplayName("getCurrentSemester - When not found throws SemesterNotFoundException")
    void testGetCurrentSemester_NotFound_Throws() {
        when(semesterRepository.findByIsCurrentTrue()).thenReturn(Mono.empty());

        Mono<SemesterResponseDTO> result = semesterService.getCurrentSemester();

        assertThrows(SemesterNotFoundException.class, result::block);
        verify(semesterRepository).findByIsCurrentTrue();
    }

    @Test
    @DisplayName("getCurrentSemesterId - When found returns ID")
    void testGetCurrentSemesterId_ReturnsId() {
        Semester semester = Semester.builder().id(7L).year(2025).period(2).isCurrent(true).build();
        when(semesterRepository.findByIsCurrentTrue()).thenReturn(Mono.just(semester));

        Long result = semesterService.getCurrentSemesterId().block();

        assertNotNull(result);
        assertEquals(7L, result);
        verify(semesterRepository).findByIsCurrentTrue();
    }

    @Test
    @DisplayName("getCurrentSemesterId - When not found throws SemesterNotFoundException")
    void testGetCurrentSemesterId_NotFound_Throws() {
        when(semesterRepository.findByIsCurrentTrue()).thenReturn(Mono.empty());

        Mono<Long> result = semesterService.getCurrentSemesterId();

        assertThrows(SemesterNotFoundException.class, result::block);
        verify(semesterRepository).findByIsCurrentTrue();
    }

    @Test
    @DisplayName("getAllSemesters - Should return all mapped semesters")
    void testGetAllSemesters_ReturnsList() {
        Semester s1 = Semester.builder().id(1L).year(2024).period(2).isCurrent(false).build();
        Semester s2 = Semester.builder().id(2L).year(2025).period(1).isCurrent(true).build();

        SemesterResponseDTO dto1 = SemesterResponseDTO.builder().id(1L).year(2024).period(2).isCurrent(false).build();
        SemesterResponseDTO dto2 = SemesterResponseDTO.builder().id(2L).year(2025).period(1).isCurrent(true).build();

        when(semesterRepository.findAll()).thenReturn(Flux.just(s1, s2));
        when(modelMapper.map(s1, SemesterResponseDTO.class)).thenReturn(dto1);
        when(modelMapper.map(s2, SemesterResponseDTO.class)).thenReturn(dto2);

        List<SemesterResponseDTO> result = semesterService.getAllSemesters().collectList().block();

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(2024, result.get(0).getYear());
        assertEquals(2025, result.get(1).getYear());
        verify(semesterRepository).findAll();
    }

    @Test
    @DisplayName("getSemesterByYearAndPeriod - When found returns mapped DTO")
    void testGetSemesterByYearAndPeriod_ReturnsMappedDTO() {
        Semester semester = Semester.builder()
                .id(10L)
                .year(2025)
                .period(1)
                .isCurrent(true)
                .build();

        SemesterResponseDTO dto = SemesterResponseDTO.builder()
                .id(10L)
                .year(2025)
                .period(1)
                .isCurrent(true)
                .build();

        when(semesterRepository.findByYearAndPeriod(2025, 1)).thenReturn(Mono.just(semester));
        when(modelMapper.map(semester, SemesterResponseDTO.class)).thenReturn(dto);

        SemesterResponseDTO result = semesterService.getSemesterByYearAndPeriod(2025, 1).block();

        assertNotNull(result);
        assertEquals(2025, result.getYear());
        assertEquals(1, result.getPeriod());
        verify(semesterRepository).findByYearAndPeriod(2025, 1);
        verify(modelMapper).map(semester, SemesterResponseDTO.class);
    }

    @Test
    @DisplayName("getSemesterByYearAndPeriod - When not found throws SemesterNotFoundException")
    void testGetSemesterByYearAndPeriod_NotFound_ReturnsEmpty() {
        when(semesterRepository.findByYearAndPeriod(2025, 2)).thenReturn(Mono.empty());

        Mono<SemesterResponseDTO> result = semesterService.getSemesterByYearAndPeriod(2025, 2);

        assertThrows(SemesterNotFoundException.class, result::block);
    }

    @Test
    @DisplayName("getSemesterById - When found returns mapped DTO")
    void testGetSemesterById_ReturnsMappedDTO() {
        Long semesterId = 5L;
        Semester semester = Semester.builder()
                .id(semesterId)
                .year(2025)
                .period(1)
                .startDate(LocalDate.of(2025, 1, 10))
                .endDate(LocalDate.of(2025, 6, 15))
                .isCurrent(true)
                .build();

        SemesterResponseDTO dto = SemesterResponseDTO.builder()
                .id(semesterId)
                .year(2025)
                .period(1)
                .startDate(LocalDate.of(2025, 1, 10))
                .endDate(LocalDate.of(2025, 6, 15))
                .isCurrent(true)
                .build();

        when(semesterRepository.findById(semesterId)).thenReturn(Mono.just(semester));
        when(modelMapper.map(semester, SemesterResponseDTO.class)).thenReturn(dto);

        SemesterResponseDTO result = semesterService.getSemesterById(semesterId).block();

        assertNotNull(result);
        assertEquals(semesterId, result.getId());
        assertEquals(2025, result.getYear());
        assertEquals(1, result.getPeriod());
        assertTrue(result.getIsCurrent());
        verify(semesterRepository).findById(semesterId);
        verify(modelMapper).map(semester, SemesterResponseDTO.class);
    }

    @Test
    @DisplayName("getSemesterById - When not found throws SemesterNotFoundException")
    void testGetSemesterById_NotFound_ThrowsException() {
        Long semesterId = 999L;
        when(semesterRepository.findById(semesterId)).thenReturn(Mono.empty());

        Mono<SemesterResponseDTO> result = semesterService.getSemesterById(semesterId);

        SemesterNotFoundException exception = assertThrows(SemesterNotFoundException.class, result::block);
        assertTrue(exception.getMessage().contains("Semester was not found for id " + semesterId));
        verify(semesterRepository).findById(semesterId);
        verify(modelMapper, never()).map(any(), any());
    }

    @Test
    @DisplayName("getSemesterById - When found with different ID returns correct semester")
    void testGetSemesterById_DifferentId_ReturnsCorrectSemester() {
        Long semesterId = 15L;
        Semester semester = Semester.builder()
                .id(semesterId)
                .year(2024)
                .period(2)
                .startDate(LocalDate.of(2024, 7, 1))
                .endDate(LocalDate.of(2024, 12, 20))
                .isCurrent(false)
                .build();

        SemesterResponseDTO dto = SemesterResponseDTO.builder()
                .id(semesterId)
                .year(2024)
                .period(2)
                .startDate(LocalDate.of(2024, 7, 1))
                .endDate(LocalDate.of(2024, 12, 20))
                .isCurrent(false)
                .build();

        when(semesterRepository.findById(semesterId)).thenReturn(Mono.just(semester));
        when(modelMapper.map(semester, SemesterResponseDTO.class)).thenReturn(dto);

        SemesterResponseDTO result = semesterService.getSemesterById(semesterId).block();

        assertNotNull(result);
        assertEquals(semesterId, result.getId());
        assertEquals(2024, result.getYear());
        assertEquals(2, result.getPeriod());
        assertFalse(result.getIsCurrent());
        verify(semesterRepository).findById(semesterId);
        verify(modelMapper).map(semester, SemesterResponseDTO.class);
    }
}
