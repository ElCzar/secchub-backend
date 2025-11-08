package co.edu.puj.secchub_backend.admin.service;

import co.edu.puj.secchub_backend.admin.contract.TeacherResponseDTO;
import co.edu.puj.secchub_backend.admin.dto.TeacherCreateRequestDTO;
import co.edu.puj.secchub_backend.admin.dto.TeacherUpdateRequestDTO;
import co.edu.puj.secchub_backend.admin.exception.TeacherNotFoundException;
import co.edu.puj.secchub_backend.admin.model.Teacher;
import co.edu.puj.secchub_backend.admin.repository.TeacherRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.modelmapper.ModelMapper;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link TeacherService}.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("TeacherService Unit Test")
class TeacherServiceTest {

    @Mock
    private TeacherRepository teacherRepository;

    @Mock
    private ModelMapper modelMapper;

    @InjectMocks
    private TeacherService teacherService;

    @Test
    @DisplayName("getAllTeachers - Should return all mapped teachers")
    void testGetAllTeachers_ReturnsMappedList() {
        Teacher t1 = Teacher.builder().id(1L).userId(10L).employmentTypeId(1L).maxHours(20).build();
        Teacher t2 = Teacher.builder().id(2L).userId(11L).employmentTypeId(2L).maxHours(40).build();
        List<Teacher> teachers = Arrays.asList(t1, t2);

        TeacherResponseDTO dto1 = TeacherResponseDTO.builder().id(1L).userId(10L).employmentTypeId(1L).maxHours(20).build();
        TeacherResponseDTO dto2 = TeacherResponseDTO.builder().id(2L).userId(11L).employmentTypeId(2L).maxHours(40).build();

        when(teacherRepository.findAll()).thenReturn(Flux.fromIterable(teachers));
        when(modelMapper.map(t1, TeacherResponseDTO.class)).thenReturn(dto1);
        when(modelMapper.map(t2, TeacherResponseDTO.class)).thenReturn(dto2);

        List<TeacherResponseDTO> result = teacherService.getAllTeachers().collectList().block();

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(10L, result.get(0).getUserId());
        assertEquals(11L, result.get(1).getUserId());
        verify(teacherRepository).findAll();
        verify(modelMapper).map(t1, TeacherResponseDTO.class);
        verify(modelMapper).map(t2, TeacherResponseDTO.class);
    }

    @Test
    @DisplayName("getTeacherById - When teacher exists returns DTO")
    void testGetTeacherById_TeacherExists_ReturnsDTO() {
        Teacher teacher = Teacher.builder().id(5L).userId(20L).employmentTypeId(2L).maxHours(30).build();
        TeacherResponseDTO dto = TeacherResponseDTO.builder().id(5L).userId(20L).employmentTypeId(2L).maxHours(30).build();

        when(teacherRepository.findById(5L)).thenReturn(Mono.just(teacher));
        when(modelMapper.map(teacher, TeacherResponseDTO.class)).thenReturn(dto);

        TeacherResponseDTO result = teacherService.getTeacherById(5L).block();

        assertNotNull(result);
        assertEquals(5L, result.getId());
        assertEquals(20L, result.getUserId());
        assertEquals(2L, result.getEmploymentTypeId());
        assertEquals(30, result.getMaxHours());
        verify(teacherRepository).findById(5L);
        verify(modelMapper).map(teacher, TeacherResponseDTO.class);
    }

    @Test
    @DisplayName("getTeacherById - When teacher not found throws TeacherNotFoundException")
    void testGetTeacherById_TeacherNotFound_Throws() {
        when(teacherRepository.findById(99L)).thenReturn(Mono.empty());

        Mono<TeacherResponseDTO> result = teacherService.getTeacherById(99L);

        assertThrows(TeacherNotFoundException.class, result::block);
        verify(teacherRepository).findById(99L);
    }

    @Test
    @DisplayName("createTeacher - Maps request, saves, and returns DTO")
    void testCreateTeacher_MapsAndSaves_ReturnsDTO() {
        TeacherCreateRequestDTO request = TeacherCreateRequestDTO.builder()
                .userId(100L)
                .employmentTypeId(2L)
                .maxHours(35)
                .build();

        Teacher mapped = Teacher.builder().userId(100L).employmentTypeId(2L).maxHours(35).build();
        Teacher saved = Teacher.builder().id(1L).userId(100L).employmentTypeId(2L).maxHours(35).build();
        TeacherResponseDTO responseDTO = TeacherResponseDTO.builder().id(1L).userId(100L).employmentTypeId(2L).maxHours(35).build();

        when(modelMapper.map(request, Teacher.class)).thenReturn(mapped);
        when(teacherRepository.save(mapped)).thenReturn(Mono.just(saved));
        when(modelMapper.map(saved, TeacherResponseDTO.class)).thenReturn(responseDTO);

        TeacherResponseDTO result = teacherService.createTeacher(request).block();

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals(100L, result.getUserId());
        assertEquals(35, result.getMaxHours());
        verify(modelMapper).map(request, Teacher.class);
        verify(teacherRepository).save(mapped);
        verify(modelMapper).map(saved, TeacherResponseDTO.class);
    }

    @Test
    @DisplayName("updateTeacher - When teacher exists updates and returns DTO")
    void testUpdateTeacher_TeacherExists_UpdatesAndReturnsDTO() {
        Teacher existing = Teacher.builder().id(1L).userId(10L).employmentTypeId(1L).maxHours(20).build();
        Teacher updated = Teacher.builder().id(1L).userId(10L).employmentTypeId(3L).maxHours(40).build();

        TeacherUpdateRequestDTO updateDTO = TeacherUpdateRequestDTO.builder()
                .employmentTypeId(3L)
                .maxHours(40)
                .build();

        TeacherResponseDTO responseDTO = TeacherResponseDTO.builder()
                .id(1L)
                .userId(10L)
                .employmentTypeId(3L)
                .maxHours(40)
                .build();

        when(teacherRepository.findById(1L)).thenReturn(Mono.just(existing));
        when(teacherRepository.save(existing)).thenReturn(Mono.just(updated));
        when(modelMapper.map(updated, TeacherResponseDTO.class)).thenReturn(responseDTO);

        TeacherResponseDTO result = teacherService.updateTeacher(1L, updateDTO).block();

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals(3L, result.getEmploymentTypeId());
        assertEquals(40, result.getMaxHours());
        verify(teacherRepository).findById(1L);
        verify(teacherRepository).save(existing);
        verify(modelMapper).map(updated, TeacherResponseDTO.class);
    }

    @Test
    @DisplayName("updateTeacher - When teacher not found throws TeacherNotFoundException")
    void testUpdateTeacher_TeacherNotFound_Throws() {
        when(teacherRepository.findById(99L)).thenReturn(Mono.empty());

        TeacherUpdateRequestDTO updateDTO = TeacherUpdateRequestDTO.builder()
                .employmentTypeId(2L)
                .maxHours(25)
                .build();

        Mono<TeacherResponseDTO> result = teacherService.updateTeacher(99L, updateDTO);

        assertThrows(TeacherNotFoundException.class, result::block);
        verify(teacherRepository).findById(99L);
        verify(teacherRepository, never()).save(any());
    }

    @Test
    @DisplayName("getTeacherByUserId - When teacher exists returns DTO")
    void testGetTeacherByUserId_TeacherExists_ReturnsDTO() {
        Teacher teacher = Teacher.builder().id(10L).userId(200L).employmentTypeId(3L).maxHours(40).build();
        TeacherResponseDTO dto = TeacherResponseDTO.builder().id(10L).userId(200L).employmentTypeId(3L).maxHours(40).build();

        when(teacherRepository.findByUserId(200L)).thenReturn(Mono.just(teacher));
        when(modelMapper.map(teacher, TeacherResponseDTO.class)).thenReturn(dto);

        TeacherResponseDTO result = teacherService.getTeacherByUserId(200L).block();

        assertNotNull(result);
        assertEquals(10L, result.getId());
        assertEquals(200L, result.getUserId());
        verify(teacherRepository).findByUserId(200L);
        verify(modelMapper).map(teacher, TeacherResponseDTO.class);
    }

    @Test
    @DisplayName("getTeacherByUserId - When teacher not found throws TeacherNotFoundException")
    void testGetTeacherByUserId_TeacherNotFound_Throws() {
        when(teacherRepository.findByUserId(404L)).thenReturn(Mono.empty());

        Mono<TeacherResponseDTO> result = teacherService.getTeacherByUserId(404L);

        assertThrows(TeacherNotFoundException.class, result::block);
        verify(teacherRepository).findByUserId(404L);
    }

    @Test
    @DisplayName("getTeachersByEmploymentType - Should return mapped list of teachers")
    void testGetTeachersByEmploymentType_ReturnsMappedList() {
        Teacher t1 = Teacher.builder().id(1L).employmentTypeId(2L).userId(10L).maxHours(25).build();
        Teacher t2 = Teacher.builder().id(2L).employmentTypeId(2L).userId(11L).maxHours(30).build();

        TeacherResponseDTO dto1 = TeacherResponseDTO.builder().id(1L).employmentTypeId(2L).userId(10L).maxHours(25).build();
        TeacherResponseDTO dto2 = TeacherResponseDTO.builder().id(2L).employmentTypeId(2L).userId(11L).maxHours(30).build();

        when(teacherRepository.findByEmploymentTypeId(2L)).thenReturn(Flux.just(t1, t2));
        when(modelMapper.map(t1, TeacherResponseDTO.class)).thenReturn(dto1);
        when(modelMapper.map(t2, TeacherResponseDTO.class)).thenReturn(dto2);

        List<TeacherResponseDTO> result = teacherService.getTeachersByEmploymentType(2L).collectList().block();

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(2L, result.get(0).getEmploymentTypeId());
        assertEquals(2L, result.get(1).getEmploymentTypeId());
        verify(teacherRepository).findByEmploymentTypeId(2L);
        verify(modelMapper).map(t1, TeacherResponseDTO.class);
        verify(modelMapper).map(t2, TeacherResponseDTO.class);
    }

    @Test
    @DisplayName("getTeachersWithMinHours - Should return teachers with min required hours")
    void testGetTeachersWithMinHours_ReturnsMappedList() {
        Teacher t1 = Teacher.builder().id(1L).userId(10L).employmentTypeId(1L).maxHours(20).build();
        Teacher t2 = Teacher.builder().id(2L).userId(11L).employmentTypeId(2L).maxHours(25).build();

        TeacherResponseDTO dto1 = TeacherResponseDTO.builder().id(1L).userId(10L).employmentTypeId(1L).maxHours(20).build();
        TeacherResponseDTO dto2 = TeacherResponseDTO.builder().id(2L).userId(11L).employmentTypeId(2L).maxHours(25).build();

        when(teacherRepository.findByMaxHoursGreaterThanEqual(20)).thenReturn(Flux.just(t1, t2));
        when(modelMapper.map(t1, TeacherResponseDTO.class)).thenReturn(dto1);
        when(modelMapper.map(t2, TeacherResponseDTO.class)).thenReturn(dto2);

        List<TeacherResponseDTO> result = teacherService.getTeachersWithMinHours(20).collectList().block();

        assertNotNull(result);
        assertEquals(2, result.size());
        assertTrue(result.stream().allMatch(dto -> dto.getMaxHours() >= 20));
        verify(teacherRepository).findByMaxHoursGreaterThanEqual(20);
    }

    @Test
    @DisplayName("getTeacherIdByUserId - When teacher exists returns teacher ID")
    void testGetTeacherIdByUserId_TeacherExists_ReturnsTeacherId() {
        Teacher teacher = Teacher.builder().id(15L).userId(300L).employmentTypeId(2L).maxHours(35).build();

        when(teacherRepository.findByUserId(300L)).thenReturn(Mono.just(teacher));

        Long result = teacherService.getTeacherIdByUserId(300L).block();

        assertNotNull(result);
        assertEquals(15L, result);
        verify(teacherRepository).findByUserId(300L);
    }

    @Test
    @DisplayName("getTeacherIdByUserId - When teacher not found throws TeacherNotFoundException")
    void testGetTeacherIdByUserId_TeacherNotFound_Throws() {
        when(teacherRepository.findByUserId(999L)).thenReturn(Mono.empty());

        Mono<Long> result = teacherService.getTeacherIdByUserId(999L);

        assertThrows(TeacherNotFoundException.class, result::block);
        verify(teacherRepository).findByUserId(999L);
    }
}
