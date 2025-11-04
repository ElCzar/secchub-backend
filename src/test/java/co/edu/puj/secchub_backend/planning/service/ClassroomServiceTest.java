package co.edu.puj.secchub_backend.planning.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.modelmapper.ModelMapper;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import co.edu.puj.secchub_backend.planning.dto.ClassroomRequestDTO;
import co.edu.puj.secchub_backend.planning.dto.ClassroomResponseDTO;
import co.edu.puj.secchub_backend.planning.exception.ClassroomBadRequestException;
import co.edu.puj.secchub_backend.planning.exception.ClassroomNotFoundException;
import co.edu.puj.secchub_backend.planning.model.Classroom;
import co.edu.puj.secchub_backend.planning.repository.ClassroomRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@ExtendWith(MockitoExtension.class)
@DisplayName("ClassroomService Unit Test")
class ClassroomServiceTest {

    @Mock
    private ClassroomRepository classroomRepository;

    @Mock
    private ModelMapper modelMapper;

    @InjectMocks
    private ClassroomService classroomService;

    @Test
    @DisplayName("getAllClassrooms - Should return mapped list of all classrooms")
    void testGetAllClassrooms_ReturnsMappedList() {
        Classroom c1 = Classroom.builder()
                .id(1L)
                .classroomTypeId(1L)
                .campus("Campus A")
                .location("Building 1")
                .room("101")
                .capacity(30)
                .build();
        Classroom c2 = Classroom.builder()
                .id(2L)
                .classroomTypeId(2L)
                .campus("Campus B")
                .location("Building 2")
                .room("202")
                .capacity(50)
                .build();
        List<Classroom> classrooms = Arrays.asList(c1, c2);

        ClassroomResponseDTO dto1 = ClassroomResponseDTO.builder()
                .id(1L)
                .classroomTypeId(1L)
                .campus("Campus A")
                .location("Building 1")
                .room("101")
                .capacity(30)
                .build();
        ClassroomResponseDTO dto2 = ClassroomResponseDTO.builder()
                .id(2L)
                .classroomTypeId(2L)
                .campus("Campus B")
                .location("Building 2")
                .room("202")
                .capacity(50)
                .build();

        when(classroomRepository.findAll()).thenReturn(Flux.fromIterable(classrooms));
        when(modelMapper.map(c1, ClassroomResponseDTO.class)).thenReturn(dto1);
        when(modelMapper.map(c2, ClassroomResponseDTO.class)).thenReturn(dto2);

        List<ClassroomResponseDTO> result = classroomService.getAllClassrooms().collectList().block();

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("101", result.get(0).getRoom());
        assertEquals("202", result.get(1).getRoom());
        verify(classroomRepository).findAll();
        verify(modelMapper).map(c1, ClassroomResponseDTO.class);
        verify(modelMapper).map(c2, ClassroomResponseDTO.class);
    }

    @Test
    @DisplayName("getClassroomById - When classroom exists returns DTO")
    void testGetClassroomById_ClassroomExists_ReturnsDTO() {
        Classroom classroom = Classroom.builder()
                .id(10L)
                .classroomTypeId(1L)
                .campus("Main Campus")
                .location("North Wing")
                .room("305")
                .capacity(40)
                .build();
        ClassroomResponseDTO dto = ClassroomResponseDTO.builder()
                .id(10L)
                .classroomTypeId(1L)
                .campus("Main Campus")
                .location("North Wing")
                .room("305")
                .capacity(40)
                .build();

        when(classroomRepository.findById(10L)).thenReturn(Mono.just(classroom));
        when(modelMapper.map(classroom, ClassroomResponseDTO.class)).thenReturn(dto);

        ClassroomResponseDTO result = classroomService.getClassroomById(10L).block();

        assertNotNull(result);
        assertEquals(10L, result.getId());
        assertEquals("305", result.getRoom());
        assertEquals(40, result.getCapacity());
        verify(classroomRepository).findById(10L);
        verify(modelMapper).map(classroom, ClassroomResponseDTO.class);
    }

    @Test
    @DisplayName("getClassroomById - When classroom not found throws ClassroomNotFoundException")
    void testGetClassroomById_ClassroomNotFound_Throws() {
        when(classroomRepository.findById(99L)).thenReturn(Mono.empty());

        Mono<ClassroomResponseDTO> result = classroomService.getClassroomById(99L);
        assertThrows(ClassroomNotFoundException.class, result::block);

        verify(classroomRepository).findById(99L);
    }

    @Test
    @DisplayName("createClassroom - Maps and saves returning DTO")
    void testCreateClassroom_MapsAndSaves_ReturnsDTO() {
        ClassroomRequestDTO request = ClassroomRequestDTO.builder()
                .classroomTypeId(2L)
                .campus("South Campus")
                .location("Lab Building")
                .room("L-101")
                .capacity(25)
                .build();

        Classroom mapped = Classroom.builder()
                .classroomTypeId(2L)
                .campus("South Campus")
                .location("Lab Building")
                .room("L-101")
                .capacity(25)
                .build();

        Classroom saved = Classroom.builder()
                .id(15L)
                .classroomTypeId(2L)
                .campus("South Campus")
                .location("Lab Building")
                .room("L-101")
                .capacity(25)
                .build();

        ClassroomResponseDTO responseDTO = ClassroomResponseDTO.builder()
                .id(15L)
                .classroomTypeId(2L)
                .campus("South Campus")
                .location("Lab Building")
                .room("L-101")
                .capacity(25)
                .build();

        when(modelMapper.map(request, Classroom.class)).thenReturn(mapped);
        when(classroomRepository.save(mapped)).thenReturn(Mono.just(saved));
        when(modelMapper.map(saved, ClassroomResponseDTO.class)).thenReturn(responseDTO);

        ClassroomResponseDTO result = classroomService.createClassroom(request).block();

        assertNotNull(result);
        assertEquals(15L, result.getId());
        assertEquals("L-101", result.getRoom());
        assertEquals(25, result.getCapacity());
        verify(modelMapper).map(request, Classroom.class);
        verify(classroomRepository).save(mapped);
        verify(modelMapper).map(saved, ClassroomResponseDTO.class);
    }

    @ParameterizedTest(name = "Class request: {0}, {1}, {2}, {3}, {4} should not be valid")
    @MethodSource("invalidClassroomRequests")
    @DisplayName("createClassroom - When request is invalid throws ClassroomBadRequestException")
    void testCreateClassroom_InvalidRequest_ThrowsException(ClassroomRequestDTO request) {
        Mono<ClassroomResponseDTO> result = classroomService.createClassroom(request);
        assertThrows(ClassroomBadRequestException.class, result::block);
        
        verify(classroomRepository, never()).save(any());
        verify(modelMapper, never()).map(any(), any());
    }

    private static List<ClassroomRequestDTO> invalidClassroomRequests() {
        return Arrays.asList(
                ClassroomRequestDTO.builder().classroomTypeId(null).campus("Campus").location("Loc").room("R1").capacity(20).build(),
                ClassroomRequestDTO.builder().classroomTypeId(1L).campus(null).location("Loc").room("R1").capacity(20).build(),
                ClassroomRequestDTO.builder().classroomTypeId(1L).campus("Campus").location(null).room("R1").capacity(20).build(),
                ClassroomRequestDTO.builder().classroomTypeId(1L).campus("Campus").location("Loc").room(null).capacity(20).build(),
                ClassroomRequestDTO.builder().classroomTypeId(1L).campus("Campus").location("Loc").room("R1").capacity(-5).build(),
                ClassroomRequestDTO.builder().classroomTypeId(1L).campus("Campus").location("Loc").room("R1").capacity(0).build()
        );
    }

    @Test
    @DisplayName("updateClassroom - When classroom exists updates and returns DTO")
    void testUpdateClassroom_ClassroomExists_UpdatesAndReturnsDTO() {
        ClassroomRequestDTO request = ClassroomRequestDTO.builder()
                .classroomTypeId(3L)
                .campus("Updated Campus")
                .location("Updated Location")
                .room("U-999")
                .capacity(60)
                .build();

        Classroom existing = Classroom.builder()
                .id(20L)
                .classroomTypeId(1L)
                .campus("Old Campus")
                .location("Old Location")
                .room("O-100")
                .capacity(30)
                .build();

        Classroom updated = Classroom.builder()
                .id(20L)
                .classroomTypeId(3L)
                .campus("Updated Campus")
                .location("Updated Location")
                .room("U-999")
                .capacity(60)
                .build();

        ClassroomResponseDTO responseDTO = ClassroomResponseDTO.builder()
                .id(20L)
                .classroomTypeId(3L)
                .campus("Updated Campus")
                .location("Updated Location")
                .room("U-999")
                .capacity(60)
                .build();

        when(classroomRepository.findById(20L)).thenReturn(Mono.just(existing));
        when(classroomRepository.save(existing)).thenReturn(Mono.just(updated));
        when(modelMapper.map(updated, ClassroomResponseDTO.class)).thenReturn(responseDTO);

        ClassroomResponseDTO result = classroomService.updateClassroom(20L, request).block();

        assertNotNull(result);
        assertEquals(20L, result.getId());
        assertEquals("U-999", result.getRoom());
        assertEquals(60, result.getCapacity());
        assertEquals("Updated Campus", result.getCampus());
        verify(classroomRepository).findById(20L);
        verify(classroomRepository).save(existing);
        verify(modelMapper).map(updated, ClassroomResponseDTO.class);
    }

    @Test
    @DisplayName("updateClassroom - When classroom not found throws ClassroomNotFoundException")
    void testUpdateClassroom_ClassroomNotFound_Throws() {
        when(classroomRepository.findById(99L)).thenReturn(Mono.empty());

        Mono<ClassroomResponseDTO> result = classroomService.getClassroomById(99L);

        assertThrows(ClassroomNotFoundException.class, result::block);

        verify(classroomRepository).findById(99L);
        verify(classroomRepository, never()).save(any());
    }

    @ParameterizedTest(name = "Class request with invalid field should save only valid data")
    @MethodSource("invalidClassroomRequests")
    @DisplayName("updateClassroom - When data has invalid fields should save only valid data")
    void testUpdateClassroom_InvalidData_SavesOnlyValidData(ClassroomRequestDTO request) {
        Classroom existing = Classroom.builder()
                .id(10L)
                .classroomTypeId(1L)
                .campus("Original Campus")
                .location("Original Location")
                .room("O-100")
                .capacity(30)
                .build();
        
        Classroom expectedSaved = Classroom.builder()
                .id(10L)
                .classroomTypeId(request.getClassroomTypeId() != null ? request.getClassroomTypeId() : 1L)
                .campus(request.getCampus() != null && !request.getCampus().isEmpty() ? request.getCampus() : "Original Campus")
                .location(request.getLocation() != null && !request.getLocation().isEmpty() ? request.getLocation() : "Original Location")
                .room(request.getRoom() != null && !request.getRoom().isEmpty() ? request.getRoom() : "O-100")
                .capacity(request.getCapacity() != null && request.getCapacity() > 0 ? request.getCapacity() : 30)
                .build();
        
        ClassroomResponseDTO responseDTO = ClassroomResponseDTO.builder()
                .id(10L)
                .build();
        
        when(classroomRepository.findById(10L)).thenReturn(Mono.just(existing));
        when(classroomRepository.save(any(Classroom.class))).thenReturn(Mono.just(expectedSaved));
        when(modelMapper.map(any(Classroom.class), eq(ClassroomResponseDTO.class))).thenReturn(responseDTO);

        classroomService.updateClassroom(10L, request).block();

        verify(classroomRepository).findById(10L);
        verify(classroomRepository).save(argThat(classroom ->
            classroom.getId().equals(10L) &&
            (request.getClassroomTypeId() == null || classroom.getClassroomTypeId().equals(request.getClassroomTypeId())) &&
            (request.getCampus() == null || request.getCampus().isEmpty() || classroom.getCampus().equals(request.getCampus())) &&
            (request.getLocation() == null || request.getLocation().isEmpty() || classroom.getLocation().equals(request.getLocation())) &&
            (request.getRoom() == null || request.getRoom().isEmpty() || classroom.getRoom().equals(request.getRoom())) &&
            (request.getCapacity() == null || request.getCapacity() <= 0 || classroom.getCapacity().equals(request.getCapacity()))
        ));
    }

    @Test
    @DisplayName("deleteClassroom - When classroom exists deletes successfully")
    void testDeleteClassroom_ClassroomExists_DeletesSuccessfully() {
        when(classroomRepository.existsById(5L)).thenReturn(Mono.just(true));
        when(classroomRepository.deleteById(5L)).thenReturn(Mono.empty());

        classroomService.deleteClassroom(5L).block();

        verify(classroomRepository).existsById(5L);
        verify(classroomRepository).deleteById(5L);
    }

    @Test
    @DisplayName("deleteClassroom - When classroom not found mono empty")
    void testDeleteClassroom_ClassroomNotFound_Throws() {
        when(classroomRepository.existsById(99L)).thenReturn(Mono.just(false));

        Mono<Void> result = classroomService.deleteClassroom(99L);

        assertThrows(ClassroomNotFoundException.class, result::block);

        verify(classroomRepository, never()).deleteById(any(Long.class));
    }

    @Test
    @DisplayName("getClassroomsByType - Should return classrooms of specified type")
    void testGetClassroomsByType_ReturnsMappedList() {
        Classroom c1 = Classroom.builder()
                .id(1L)
                .classroomTypeId(5L)
                .campus("Campus A")
                .location("Building 1")
                .room("101")
                .capacity(30)
                .build();
        Classroom c2 = Classroom.builder()
                .id(2L)
                .classroomTypeId(5L)
                .campus("Campus B")
                .location("Building 2")
                .room("202")
                .capacity(35)
                .build();
        List<Classroom> classrooms = Arrays.asList(c1, c2);

        ClassroomResponseDTO dto1 = ClassroomResponseDTO.builder()
                .id(1L)
                .classroomTypeId(5L)
                .campus("Campus A")
                .location("Building 1")
                .room("101")
                .capacity(30)
                .build();
        ClassroomResponseDTO dto2 = ClassroomResponseDTO.builder()
                .id(2L)
                .classroomTypeId(5L)
                .campus("Campus B")
                .location("Building 2")
                .room("202")
                .capacity(35)
                .build();

        when(classroomRepository.findByClassroomTypeId(5L)).thenReturn(Flux.fromIterable(classrooms));
        when(modelMapper.map(c1, ClassroomResponseDTO.class)).thenReturn(dto1);
        when(modelMapper.map(c2, ClassroomResponseDTO.class)).thenReturn(dto2);

        List<ClassroomResponseDTO> result = classroomService.getClassroomsByType(5L).block();

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(5L, result.get(0).getClassroomTypeId());
        assertEquals(5L, result.get(1).getClassroomTypeId());
        verify(classroomRepository).findByClassroomTypeId(5L);
        verify(modelMapper).map(c1, ClassroomResponseDTO.class);
        verify(modelMapper).map(c2, ClassroomResponseDTO.class);
    }
}