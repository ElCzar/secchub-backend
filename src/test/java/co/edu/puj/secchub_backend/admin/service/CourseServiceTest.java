package co.edu.puj.secchub_backend.admin.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.modelmapper.ModelMapper;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import co.edu.puj.secchub_backend.admin.dto.CourseRequestDTO;
import co.edu.puj.secchub_backend.admin.dto.CourseResponseDTO;
import co.edu.puj.secchub_backend.admin.dto.SectionResponseDTO;
import co.edu.puj.secchub_backend.admin.exception.CourseNotFoundException;
import co.edu.puj.secchub_backend.admin.model.Course;
import co.edu.puj.secchub_backend.admin.repository.CourseRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@ExtendWith(MockitoExtension.class)
@DisplayName("CourseService Unit Test")
class CourseServiceTest {

    @Mock
    private ModelMapper modelMapper;

    @Mock
    private CourseRepository courseRepository;

    @Mock
    private SectionService sectionService;

    @InjectMocks
    private CourseService courseService;

    // ==========================================
    // Create Course Tests
    // ==========================================

    @Test
    @DisplayName("createCourse - Should create course when name doesn't exist and section is valid")
    void testCreateCourse_Success_ReturnsCourseDTO() {
        CourseRequestDTO request = CourseRequestDTO.builder()
                .name("Introduction to Programming")
                .credits(3)
                .sectionId(5L)
                .build();

        Course mappedCourse = Course.builder()
                .name("Introduction to Programming")
                .credits(3)
                .sectionId(5L)
                .build();

        Course savedCourse = Course.builder()
                .id(1L)
                .name("Introduction to Programming")
                .credits(3)
                .sectionId(5L)
                .build();

        CourseResponseDTO responseDTO = CourseResponseDTO.builder()
                .id(1L)
                .name("Introduction to Programming")
                .credits(3)
                .sectionId(5L)
                .build();

        SectionResponseDTO sectionDTO = SectionResponseDTO.builder().id(5L).name("Engineering").build();

        when(courseRepository.existsByName("Introduction to Programming")).thenReturn(Mono.just(false));
        when(sectionService.findSectionById(5L)).thenReturn(Mono.just(sectionDTO));
        when(modelMapper.map(request, Course.class)).thenReturn(mappedCourse);
        when(courseRepository.save(mappedCourse)).thenReturn(Mono.just(savedCourse));
        when(modelMapper.map(savedCourse, CourseResponseDTO.class)).thenReturn(responseDTO);

        CourseResponseDTO result = courseService.createCourse(request).block();

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("Introduction to Programming", result.getName());
        verify(courseRepository).existsByName("Introduction to Programming");
        verify(sectionService).findSectionById(5L);
        verify(courseRepository).save(mappedCourse);
    }

    @Test
    @DisplayName("createCourse - When course name already exists throws IllegalArgumentException")
    void testCreateCourse_CourseNameExists_ThrowsException() {
        CourseRequestDTO request = CourseRequestDTO.builder()
                .name("Existing Course")
                .sectionId(5L)
                .build();

        when(courseRepository.existsByName("Existing Course")).thenReturn(Mono.just(true));

        Mono<CourseResponseDTO> courseMono = courseService.createCourse(request);
        
        assertThrows(IllegalArgumentException.class, courseMono::block);
        verify(courseRepository).existsByName("Existing Course");
        verify(sectionService, never()).findSectionById(any());
        verify(courseRepository, never()).save(any());
    }

    @Test
    @DisplayName("createCourse - When section not found throws IllegalArgumentException")
    void testCreateCourse_SectionNotFound_ThrowsException() {
        CourseRequestDTO request = CourseRequestDTO.builder()
                .name("New Course")
                .sectionId(999L)
                .build();

        when(courseRepository.existsByName("New Course")).thenReturn(Mono.just(false));
        when(sectionService.findSectionById(999L)).thenReturn(Mono.empty());

        Mono<CourseResponseDTO> courseMono = courseService.createCourse(request);
        
        assertThrows(IllegalArgumentException.class, courseMono::block);
        verify(courseRepository).existsByName("New Course");
        verify(sectionService).findSectionById(999L);
        verify(courseRepository, never()).save(any());
    }

    // ==========================================
    // Find All Courses Tests
    // ==========================================

    @Test
    @DisplayName("findAllCourses - Should return all courses as DTOs")
    void testFindAllCourses_ReturnsAllCourses() {
        Course c1 = Course.builder().id(1L).name("Course 1").credits(3).build();
        Course c2 = Course.builder().id(2L).name("Course 2").credits(4).build();

        CourseResponseDTO dto1 = CourseResponseDTO.builder().id(1L).name("Course 1").credits(3).build();
        CourseResponseDTO dto2 = CourseResponseDTO.builder().id(2L).name("Course 2").credits(4).build();

        when(courseRepository.findAll()).thenReturn(Flux.just(c1, c2));
        when(modelMapper.map(c1, CourseResponseDTO.class)).thenReturn(dto1);
        when(modelMapper.map(c2, CourseResponseDTO.class)).thenReturn(dto2);

        List<CourseResponseDTO> result = courseService.findAllCourses().collectList().block();

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("Course 1", result.get(0).getName());
        assertEquals("Course 2", result.get(1).getName());
        verify(courseRepository).findAll();
    }

    @Test
    @DisplayName("findAllCourses - Using StepVerifier should emit all courses")
    void testFindAllCourses_StepVerifier_EmitsAllCourses() {
        Course c1 = Course.builder().id(1L).name("Mathematics").credits(3).build();
        CourseResponseDTO dto1 = CourseResponseDTO.builder().id(1L).name("Mathematics").credits(3).build();

        when(courseRepository.findAll()).thenReturn(Flux.just(c1));
        when(modelMapper.map(c1, CourseResponseDTO.class)).thenReturn(dto1);

        StepVerifier.create(courseService.findAllCourses())
                .assertNext(dto -> {
                    assertEquals(1L, dto.getId());
                    assertEquals("Mathematics", dto.getName());
                })
                .verifyComplete();
    }

    // ==========================================
    // Find Course by ID Tests
    // ==========================================

    @Test
    @DisplayName("findCourseById - When course exists returns DTO")
    void testFindCourseById_CourseExists_ReturnsDTO() {
        Course course = Course.builder().id(10L).name("Physics").credits(4).build();
        CourseResponseDTO dto = CourseResponseDTO.builder().id(10L).name("Physics").credits(4).build();

        when(courseRepository.findById(10L)).thenReturn(Mono.just(course));
        when(modelMapper.map(course, CourseResponseDTO.class)).thenReturn(dto);

        CourseResponseDTO result = courseService.findCourseById(10L).block();

        assertNotNull(result);
        assertEquals(10L, result.getId());
        assertEquals("Physics", result.getName());
        verify(courseRepository).findById(10L);
    }

    @Test
    @DisplayName("findCourseById - When course not found throws CourseNotFoundException")
    void testFindCourseById_CourseNotFound_Throws() {
        when(courseRepository.findById(99L)).thenReturn(Mono.empty());

        Mono<CourseResponseDTO> courseMono = courseService.findCourseById(99L);
        
        assertThrows(CourseNotFoundException.class, courseMono::block);
        verify(courseRepository).findById(99L);
    }

    // ==========================================
    // Update Course Tests
    // ==========================================

    @Test
    @DisplayName("updateCourse - When course exists updates and returns DTO")
    void testUpdateCourse_CourseExists_UpdatesAndReturnsDTO() {
        CourseRequestDTO request = CourseRequestDTO.builder()
                .name("Updated Course")
                .credits(5)
                .sectionId(10L)
                .build();

        Course existing = Course.builder().id(5L).name("Old Course").credits(3).sectionId(5L).build();
        Course updated = Course.builder().id(5L).name("Updated Course").credits(5).sectionId(10L).build();
        CourseResponseDTO responseDTO = CourseResponseDTO.builder().id(5L).name("Updated Course").credits(5).sectionId(10L).build();

        when(courseRepository.findById(5L)).thenReturn(Mono.just(existing));
        doAnswer(invocation -> {
            // Simulate modelMapper updating existing course
            return null;
        }).when(modelMapper).map(request, existing);
        when(courseRepository.save(existing)).thenReturn(Mono.just(updated));
        when(modelMapper.map(updated, CourseResponseDTO.class)).thenReturn(responseDTO);

        CourseResponseDTO result = courseService.updateCourse(5L, request).block();

        assertNotNull(result);
        assertEquals(5L, result.getId());
        assertEquals("Updated Course", result.getName());
        verify(courseRepository).findById(5L);
        verify(courseRepository).save(existing);
    }

    @Test
    @DisplayName("updateCourse - When course not found throws CourseNotFoundException")
    void testUpdateCourse_CourseNotFound_Throws() {
        CourseRequestDTO request = CourseRequestDTO.builder().name("Updated").build();

        when(courseRepository.findById(99L)).thenReturn(Mono.empty());

        Mono<CourseResponseDTO> updateMono = courseService.updateCourse(99L, request);
        
        assertThrows(CourseNotFoundException.class, updateMono::block);
        verify(courseRepository).findById(99L);
        verify(courseRepository, never()).save(any());
    }

    // ==========================================
    // Patch Course Tests
    // ==========================================

    @Test
    @DisplayName("patchCourse - When course exists partially updates and returns DTO")
    void testPatchCourse_CourseExists_PatchesAndReturnsDTO() {
        Map<String, Object> updates = new HashMap<>();
        updates.put("name", "Patched Name");
        updates.put("credits", 4);

        Course existing = Course.builder().id(7L).name("Original").credits(3).build();
        Course patched = Course.builder().id(7L).name("Patched Name").credits(4).build();
        CourseResponseDTO responseDTO = CourseResponseDTO.builder().id(7L).name("Patched Name").credits(4).build();

        when(courseRepository.findById(7L)).thenReturn(Mono.just(existing));
        doAnswer(invocation -> null).when(modelMapper).map(updates, existing);
        when(courseRepository.save(existing)).thenReturn(Mono.just(patched));
        when(modelMapper.map(patched, CourseResponseDTO.class)).thenReturn(responseDTO);

        CourseResponseDTO result = courseService.patchCourse(7L, updates).block();

        assertNotNull(result);
        assertEquals(7L, result.getId());
        assertEquals("Patched Name", result.getName());
        verify(courseRepository).findById(7L);
        verify(courseRepository).save(existing);
    }

    @Test
    @DisplayName("patchCourse - When course not found throws CourseNotFoundException")
    void testPatchCourse_CourseNotFound_Throws() {
        Map<String, Object> updates = new HashMap<>();
        updates.put("name", "New Name");

        when(courseRepository.findById(99L)).thenReturn(Mono.empty());

        Mono<CourseResponseDTO> patchMono = courseService.patchCourse(99L, updates);
        
        assertThrows(CourseNotFoundException.class, patchMono::block);
        verify(courseRepository).findById(99L);
        verify(courseRepository, never()).save(any());
    }

    // ==========================================
    // Delete Course Tests
    // ==========================================

    @Test
    @DisplayName("deleteCourse - When course exists deletes successfully")
    void testDeleteCourse_CourseExists_DeletesSuccessfully() {
        Course course = Course.builder().id(8L).name("To Delete").build();

        when(courseRepository.findById(8L)).thenReturn(Mono.just(course));
        when(courseRepository.delete(course)).thenReturn(Mono.empty());

        courseService.deleteCourse(8L).block();

        verify(courseRepository).findById(8L);
        verify(courseRepository).delete(course);
    }

    @Test
    @DisplayName("deleteCourse - When course not found throws CourseNotFoundException")
    void testDeleteCourse_CourseNotFound_Throws() {
        when(courseRepository.findById(99L)).thenReturn(Mono.empty());

        Mono<Void> deleteMono = courseService.deleteCourse(99L);
        
        assertThrows(CourseNotFoundException.class, deleteMono::block);
        verify(courseRepository).findById(99L);
        verify(courseRepository, never()).delete(any());
    }

    // ==========================================
    // Get Course Name Tests (Contract Implementation)
    // ==========================================

    @Test
    @DisplayName("getCourseName - When course exists returns course name")
    void testGetCourseName_CourseExists_ReturnsName() {
        Course course = Course.builder().id(15L).name("Chemistry").build();

        when(courseRepository.findById(15L)).thenReturn(Mono.just(course));

        String result = courseService.getCourseName(15L).block();

        assertEquals("Chemistry", result);
        verify(courseRepository).findById(15L);
    }

    @Test
    @DisplayName("getCourseName - When course not found returns N/A")
    void testGetCourseName_CourseNotFound_ReturnsNA() {
        when(courseRepository.findById(99L)).thenReturn(Mono.empty());

        String result = courseService.getCourseName(99L).block();

        assertEquals("N/A", result);
        verify(courseRepository).findById(99L);
    }

    // ==========================================
    // Get Course Section ID Tests (Contract Implementation)
    // ==========================================

    @Test
    @DisplayName("getCourseSectionId - When course exists returns section ID")
    void testGetCourseSectionId_CourseExists_ReturnsSectionId() {
        Course course = Course.builder().id(20L).name("Biology").sectionId(10L).build();

        when(courseRepository.findById(20L)).thenReturn(Mono.just(course));

        Long result = courseService.getCourseSectionId(20L).block();

        assertEquals(10L, result);
        verify(courseRepository).findById(20L);
    }

    @Test
    @DisplayName("getCourseSectionId - When course not found throws CourseNotFoundException")
    void testGetCourseSectionId_CourseNotFound_Throws() {
        when(courseRepository.findById(99L)).thenReturn(Mono.empty());

        Mono<Long> sectionIdMono = courseService.getCourseSectionId(99L);
        
        assertThrows(CourseNotFoundException.class, sectionIdMono::block);
        verify(courseRepository).findById(99L);
    }

    @Test
    @DisplayName("getCourseSectionId - Using StepVerifier to verify section ID")
    void testGetCourseSectionId_StepVerifier_ReturnsSectionId() {
        Course course = Course.builder().id(25L).name("History").sectionId(15L).build();

        when(courseRepository.findById(25L)).thenReturn(Mono.just(course));

        StepVerifier.create(courseService.getCourseSectionId(25L))
                .expectNext(15L)
                .verifyComplete();
    }
}