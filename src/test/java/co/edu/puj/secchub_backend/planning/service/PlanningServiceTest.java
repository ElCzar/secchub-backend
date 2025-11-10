package co.edu.puj.secchub_backend.planning.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.modelmapper.ModelMapper;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.transaction.reactive.TransactionalOperator;

import co.edu.puj.secchub_backend.admin.contract.AdminModuleSemesterContract;
import co.edu.puj.secchub_backend.admin.dto.SemesterResponseDTO;
import co.edu.puj.secchub_backend.admin.service.CourseService;
import co.edu.puj.secchub_backend.admin.service.SectionService;
import co.edu.puj.secchub_backend.admin.service.TeacherService;
import co.edu.puj.secchub_backend.planning.dto.ClassCreateRequestDTO;
import co.edu.puj.secchub_backend.planning.dto.ClassResponseDTO;
import co.edu.puj.secchub_backend.planning.dto.ClassScheduleRequestDTO;
import co.edu.puj.secchub_backend.planning.dto.ClassScheduleResponseDTO;
import co.edu.puj.secchub_backend.planning.dto.ClassroomResponseDTO;
import co.edu.puj.secchub_backend.planning.dto.ClassroomScheduleConflictResponseDTO;
import co.edu.puj.secchub_backend.planning.exception.ClassCreationException;
import co.edu.puj.secchub_backend.planning.exception.ClassNotFoundException;
import co.edu.puj.secchub_backend.planning.exception.ClassScheduleNotFoundException;
import co.edu.puj.secchub_backend.planning.exception.PlanningBadRequestException;
import co.edu.puj.secchub_backend.planning.exception.PlanningServerErrorException;
import co.edu.puj.secchub_backend.planning.model.Class;
import co.edu.puj.secchub_backend.planning.model.ClassSchedule;
import co.edu.puj.secchub_backend.planning.repository.ClassRepository;
import co.edu.puj.secchub_backend.planning.repository.ClassScheduleRepository;
import co.edu.puj.secchub_backend.security.service.UserService;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@ExtendWith(MockitoExtension.class)
@DisplayName("PlanningService Unit Test")
class PlanningServiceTest {

    @Mock
    private ModelMapper modelMapper;

    @Mock
    private ClassRepository classRepository;
    @Mock
    private ClassScheduleRepository classScheduleRepository;
    @Mock
    private UserService userService;
    @Mock
    private SectionService sectionService;
    @Mock
    private CourseService courseService;
    @Mock
    private TeacherService teacherService;

    @Mock
    private AdminModuleSemesterContract semesterService;

    @Mock
    private ClassroomService classroomService;

    @Mock
    private TransactionalOperator transactionalOperator;

    @InjectMocks
    private PlanningService planningService;

    private MockedStatic<ReactiveSecurityContextHolder> mockedReactiveSecurityContextHolder;

    private Class testClass;
    private ClassResponseDTO testClassResponseDTO;
    private ClassCreateRequestDTO testClassCreateRequestDTO;
    private ClassSchedule testSchedule;
    private ClassScheduleResponseDTO testScheduleResponseDTO;
    private ClassScheduleRequestDTO testScheduleRequestDTO;

    @BeforeEach
    void setUp() {
        testClass = Class.builder()
                .id(1L)
                .section(1L)
                .courseId(100L)
                .semesterId(1L)
                .startDate(LocalDate.of(2025, 1, 15))
                .endDate(LocalDate.of(2025, 5, 15))
                .observation("Test class")
                .capacity(30)
                .statusId(1L)
                .build();

        testClassResponseDTO = ClassResponseDTO.builder()
                .id(1L)
                .section(1L)
                .courseId(100L)
                .semesterId(1L)
                .startDate(LocalDate.of(2025, 1, 15))
                .endDate(LocalDate.of(2025, 5, 15))
                .observation("Test class")
                .capacity(30)
                .statusId(1L)
                .build();

        testClassCreateRequestDTO = ClassCreateRequestDTO.builder()
                .section(1L)
                .courseId(100L)
                .startDate(LocalDate.of(2025, 1, 15))
                .endDate(LocalDate.of(2025, 5, 15))
                .observation("Test class")
                .capacity(30)
                .statusId(1L)
                .build();

        testSchedule = ClassSchedule.builder()
                .id(1L)
                .classId(1L)
                .classroomId(10L)
                .day("Monday")
                .startTime(LocalTime.of(9, 0))
                .endTime(LocalTime.of(11, 0))
                .modalityId(1L)
                .disability(false)
                .build();

        testScheduleResponseDTO = ClassScheduleResponseDTO.builder()
                .id(1L)
                .classId(1L)
                .classroomId(10L)
                .day("Monday")
                .startTime(LocalTime.of(9, 0))
                .endTime(LocalTime.of(11, 0))
                .modalityId(1L)
                .disability(false)
                .build();

        testScheduleRequestDTO = ClassScheduleRequestDTO.builder()
                .classroomId(10L)
                .day("Monday")
                .startTime(LocalTime.of(9, 0))
                .endTime(LocalTime.of(11, 0))
                .modalityId(1L)
                .disability(false)
                .build();

        // Mock transactional operator for both Mono and Flux
        lenient().when(transactionalOperator.transactional(ArgumentMatchers.<Mono<Object>>any()))
            .thenAnswer(invocation -> invocation.getArgument(0));
        lenient().when(transactionalOperator.transactional(ArgumentMatchers.<Flux<Object>>any()))
            .thenAnswer(invocation -> invocation.getArgument(0));
    }

    @AfterEach
    void tearDown() {
        if (mockedReactiveSecurityContextHolder != null) {
            mockedReactiveSecurityContextHolder.close();
        }
    }

    // ==================== CREATE CLASS TESTS ====================

    @Test
    @DisplayName("createClass - Should create class without schedules successfully")
    void testCreateClass_WithoutSchedules_ReturnsDTO() {
        ClassCreateRequestDTO requestWithoutSchedules = ClassCreateRequestDTO.builder()
                .section(1L)
                .courseId(100L)
                .capacity(30)
                .statusId(1L)
                .build();

        Class mappedClass = Class.builder()
                .section(1L)
                .courseId(100L)
                .capacity(30)
                .statusId(1L)
                .build();

        Class savedClass = Class.builder()
                .id(1L)
                .section(1L)
                .courseId(100L)
                .semesterId(1L)
                .capacity(30)
                .statusId(1L)
                .build();

        when(semesterService.getCurrentSemesterId()).thenReturn(Mono.just(1L));
        when(modelMapper.map(requestWithoutSchedules, Class.class)).thenReturn(mappedClass);
        when(classRepository.save(any(Class.class))).thenReturn(Mono.just(savedClass));
        when(modelMapper.map(savedClass, ClassResponseDTO.class)).thenReturn(testClassResponseDTO);

        ClassResponseDTO result = planningService.createClass(requestWithoutSchedules).block();

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals(1L, result.getSection());
        verify(semesterService).getCurrentSemesterId();
        verify(classRepository).save(any(Class.class));
        verify(classScheduleRepository, never()).saveAll(anyList());
    }

    @Test
    @DisplayName("createClass - Should create class with schedules successfully")
    void testCreateClass_WithSchedules_ReturnsDTOWithSchedules() {
        List<ClassScheduleRequestDTO> scheduleRequests = Arrays.asList(testScheduleRequestDTO);
        testClassCreateRequestDTO.setSchedules(scheduleRequests);

        Class mappedClass = Class.builder()
                .section(1L)
                .courseId(100L)
                .capacity(30)
                .build();

        Class savedClass = Class.builder()
                .id(1L)
                .section(1L)
                .courseId(100L)
                .semesterId(1L)
                .capacity(30)
                .build();

        ClassSchedule mappedSchedule = ClassSchedule.builder()
                .classroomId(10L)
                .day("Monday")
                .startTime(LocalTime.of(9, 0))
                .endTime(LocalTime.of(11, 0))
                .build();

        ClassSchedule savedSchedule = ClassSchedule.builder()
                .id(1L)
                .classId(1L)
                .classroomId(10L)
                .day("Monday")
                .startTime(LocalTime.of(9, 0))
                .endTime(LocalTime.of(11, 0))
                .build();

        when(semesterService.getCurrentSemesterId()).thenReturn(Mono.just(1L));
        when(modelMapper.map(testClassCreateRequestDTO, Class.class)).thenReturn(mappedClass);
        when(classRepository.save(any(Class.class))).thenReturn(Mono.just(savedClass));
        when(modelMapper.map(savedClass, ClassResponseDTO.class)).thenReturn(testClassResponseDTO);
        when(modelMapper.map(testScheduleRequestDTO, ClassSchedule.class)).thenReturn(mappedSchedule);
        when(classScheduleRepository.saveAll(anyList())).thenReturn(Flux.just(savedSchedule));
        when(modelMapper.map(savedSchedule, ClassScheduleResponseDTO.class)).thenReturn(testScheduleResponseDTO);

        ClassResponseDTO result = planningService.createClass(testClassCreateRequestDTO).block();

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertNotNull(result.getSchedules());
        assertEquals(1, result.getSchedules().size());
        verify(classScheduleRepository).saveAll(anyList());
    }

    @Test
    @DisplayName("createClass - When semester service fails throws ClassCreationException")
    void testCreateClass_SemesterServiceFails_ThrowsException() {
        when(semesterService.getCurrentSemesterId()).thenReturn(Mono.error(new RuntimeException("Semester service error")));

        Mono<ClassResponseDTO> result = planningService.createClass(testClassCreateRequestDTO);

        assertThrows(ClassCreationException.class, result::block);
        verify(semesterService).getCurrentSemesterId();
    }

    @Test
    @DisplayName("createClass - When mapping fails throws ClassCreationException")
    void testCreateClass_MappingFails_ThrowsException() {
        when(semesterService.getCurrentSemesterId()).thenReturn(Mono.just(1L));
        when(modelMapper.map(any(), eq(Class.class))).thenThrow(new RuntimeException("Mapping error"));

        Mono<ClassResponseDTO> result = planningService.createClass(testClassCreateRequestDTO);

        assertThrows(ClassCreationException.class, result::block);
        verify(semesterService).getCurrentSemesterId();
    }

    @Test
    @DisplayName("createClass - When error occurs throws ClassCreationException")
    void testCreateClass_ErrorOccurs_ThrowsException() {
        when(semesterService.getCurrentSemesterId()).thenReturn(Mono.just(1L));
        when(modelMapper.map(any(), eq(Class.class))).thenReturn(testClass);
        when(classRepository.save(any())).thenReturn(Mono.error(new RuntimeException("Database error")));

        Mono<ClassResponseDTO> result = planningService.createClass(testClassCreateRequestDTO);

        assertThrows(ClassCreationException.class, result::block);
        verify(classRepository).save(any(Class.class));
    }

    // ==================== FIND CLASSES TESTS ====================

    @ParameterizedTest(name = "findCurrentSemesterClasses - When user has section {0} returns classes")
    @MethodSource("userSectionProvider")
    @DisplayName("findCurrentSemesterClasses - Should return classes based on user section")
    void testFindCurrentSemesterClasses_BasedOnUserSection_ReturnsClasses(Long userSection) {
        setupSecurityContext("ROLE_USER");
        List<Class> userClasses = setUpUserMocking(userSection);
        when(modelMapper.map(any(Class.class), eq(ClassResponseDTO.class))).thenAnswer(invocation -> {
            Class cls = invocation.getArgument(0);
            return ClassResponseDTO.builder()
                    .id(cls.getId())
                    .section(cls.getSection())
                    .courseId(cls.getCourseId())
                    .build();
        });


        when(semesterService.getCurrentSemesterId()).thenReturn(Mono.just(1L));
        when(classRepository.findBySemesterId(1L)).thenReturn(Flux.fromIterable(userClasses));
        when(classScheduleRepository.findByClassId(anyLong())).thenReturn(Flux.just(testSchedule));
        when(modelMapper.map(testSchedule, ClassScheduleResponseDTO.class)).thenReturn(testScheduleResponseDTO);

        List<ClassResponseDTO> result = planningService.findCurrentSemesterClasses().collectList().block();

        assertNotNull(result, "Result should not be null");
        assertEquals(1, result.size(), "Result size should be 1");
        assertEquals(userSection, result.get(0).getSection(), "Returned class section should match user section");
        verify(semesterService).getCurrentSemesterId();
        verify(classRepository).findBySemesterId(1L);
    }

    @Test
    @DisplayName("findCurrentSemesterClasses - Should return for Admin all classes for current semester")
    void testFindCurrentSemesterClasses_ReturnsClasses() {
        setupSecurityContext("ROLE_ADMIN");
        when(semesterService.getCurrentSemesterId()).thenReturn(Mono.just(1L));
        when(classRepository.findBySemesterId(1L)).thenReturn(Flux.just(testClass));
        when(classScheduleRepository.findByClassId(1L)).thenReturn(Flux.just(testSchedule));
        when(modelMapper.map(testClass, ClassResponseDTO.class)).thenReturn(testClassResponseDTO);
        when(modelMapper.map(testSchedule, ClassScheduleResponseDTO.class)).thenReturn(testScheduleResponseDTO);

        List<ClassResponseDTO> result = planningService.findCurrentSemesterClasses().collectList().block();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(1L, result.get(0).getId());
        verify(semesterService).getCurrentSemesterId();
        verify(classRepository).findBySemesterId(1L);
    }

    @Test
    @DisplayName("findCurrentSemesterClasses - When no classes exist returns empty list")
    void testFindCurrentSemesterClasses_NoClassesExist_ReturnsEmptyList() {
        when(semesterService.getCurrentSemesterId()).thenReturn(Mono.just(1L));
        when(classRepository.findBySemesterId(1L)).thenReturn(Flux.empty());

        List<ClassResponseDTO> result = planningService.findCurrentSemesterClasses().collectList().block();

        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(semesterService).getCurrentSemesterId();
        verify(classRepository).findBySemesterId(1L);
    }

    @Test
    @DisplayName("findCurrentSemesterClasses - When semester service fails throws PlanningServerErrorException")
    void testFindCurrentSemesterClasses_SemesterServiceFails_ThrowsException() {
        when(semesterService.getCurrentSemesterId()).thenReturn(Mono.error(new RuntimeException("Semester service error")));

        Mono<List<ClassResponseDTO>> result = planningService.findCurrentSemesterClasses().collectList();

        assertThrows(PlanningServerErrorException.class, result::block);
        verify(semesterService).getCurrentSemesterId();
    }

    @ParameterizedTest(name = "findAllClasses - When user of section {0} returns classes")
    @DisplayName("findAllClasses - Should return classes based on user role and section")
    @MethodSource("userSectionProvider")
    void testFindAllClasses_BasedOnUserRoleAndSection_ReturnsClasses(Long userSection) {
        setupSecurityContext("ROLE_USER");
        List<Class> userClasses = setUpUserMocking(userSection);
        when(modelMapper.map(any(Class.class), eq(ClassResponseDTO.class))).thenAnswer(invocation -> {
            Class cls = invocation.getArgument(0);
            return ClassResponseDTO.builder()
                    .id(cls.getId())
                    .section(cls.getSection())
                    .courseId(cls.getCourseId())
                    .build();
        });

        when(classRepository.findAll()).thenReturn(Flux.fromIterable(userClasses));
        when(classScheduleRepository.findByClassId(anyLong())).thenReturn(Flux.just(testSchedule));
        when(modelMapper.map(testSchedule, ClassScheduleResponseDTO.class)).thenReturn(testScheduleResponseDTO);

        List<ClassResponseDTO> result = planningService.findAllClasses().collectList().block();

        assertNotNull(result, "Result should not be null");
        assertEquals(1, result.size(), "Result size should be 1");
        assertEquals(userSection, result.get(0).getId(), "Returned class section should match user section");
        verify(classRepository).findAll();
    }

    @Test
    @DisplayName("findAllClasses - When no classes exist returns empty list")
    void testFindAllClasses_NoClassesExist_ReturnsEmptyList() {
        when(classRepository.findAll()).thenReturn(Flux.empty());

        List<ClassResponseDTO> result = planningService.findAllClasses().collectList().block();

        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(classRepository).findAll();
    }

    @Test
    @DisplayName("findAllClasses - When class has no schedules returns DTO without schedules")
    void testFindAllClasses_ClassWithoutSchedules_ReturnsDTOWithoutSchedules() {
        setupSecurityContext("ROLE_ADMIN");
        when(classRepository.findAll()).thenReturn(Flux.just(testClass));
        when(classScheduleRepository.findByClassId(1L)).thenReturn(Flux.empty());
        when(modelMapper.map(testClass, ClassResponseDTO.class)).thenReturn(testClassResponseDTO);

        List<ClassResponseDTO> result = planningService.findAllClasses().collectList().block();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(1L, result.get(0).getId());
        assertTrue(result.get(0).getSchedules().isEmpty());
        verify(classRepository).findAll();
    }

    @Test
    @DisplayName("findAllClasses - Should return all classes")
    void testFindAllClasses_ReturnsAllClasses() {
        setupSecurityContext("ROLE_ADMIN");
        Class class2 = Class.builder().id(2L).section(2L).courseId(101L).build();
        ClassResponseDTO dto2 = ClassResponseDTO.builder().id(2L).section(2L).courseId(101L).build();

        when(classRepository.findAll()).thenReturn(Flux.just(testClass, class2));
        when(classScheduleRepository.findByClassId(1L)).thenReturn(Flux.just(testSchedule));
        when(classScheduleRepository.findByClassId(2L)).thenReturn(Flux.empty());
        when(modelMapper.map(testClass, ClassResponseDTO.class)).thenReturn(testClassResponseDTO);
        when(modelMapper.map(class2, ClassResponseDTO.class)).thenReturn(dto2);
        when(modelMapper.map(testSchedule, ClassScheduleResponseDTO.class)).thenReturn(testScheduleResponseDTO);

        List<ClassResponseDTO> result = planningService.findAllClasses().collectList().block();

        assertNotNull(result);
        assertEquals(2, result.size());
        verify(classRepository).findAll();
    }

    @Test
    @DisplayName("findAllClasses - When error occurs throws PlanningServerErrorException")
    void testFindAllClasses_ErrorOccurs_ThrowsException() {
        when(classRepository.findAll()).thenReturn(Flux.error(new RuntimeException("Database error")));

        Mono<List<ClassResponseDTO>> result = planningService.findAllClasses().collectList();

        assertThrows(PlanningServerErrorException.class, result::block);
        verify(classRepository).findAll();
    }

    @ParameterizedTest(name = "findClassById - When class ID {0} is of user section and exists returns DTO")
    @MethodSource("userSectionProvider")
    @DisplayName("findClassById - Should return class based on user role and section")
    void testFindClassById_BasedOnUserRoleAndSection_ReturnsDTO(Long userSection) {
        setupSecurityContext("ROLE_USER");
        List<Class> userClasses = setUpUserMocking(userSection);
        Class classById = userClasses.get(0 + (int)(long)(userSection - 1));
        when(modelMapper.map(any(Class.class), eq(ClassResponseDTO.class))).thenAnswer(invocation -> {
            Class cls = invocation.getArgument(0);
            return ClassResponseDTO.builder()
                    .id(cls.getId())
                    .section(cls.getSection())
                    .courseId(cls.getCourseId())
                    .build();
        });

        when(classRepository.findById(userSection)).thenReturn(Mono.just(classById));
        when(classScheduleRepository.findByClassId(userSection)).thenReturn(Flux.just(testSchedule));
        when(modelMapper.map(testSchedule, ClassScheduleResponseDTO.class)).thenReturn(testScheduleResponseDTO);

        ClassResponseDTO result = planningService.findClassById(userSection).block();

        assertNotNull(result, "Result should not be null");
        assertEquals(userSection, result.getId(), "Returned class ID should match requested ID");
        verify(classRepository).findById(userSection);
    }

    @ParameterizedTest(name = "findClassById - When class ID {0} is of different section throws ClassNotFoundException")
    @MethodSource("userSectionProvider")
    @DisplayName("findClassById - When class not in user section throws ClassNotFoundException")
    void testFindClassById_ClassNotInUserSection_ThrowsException(Long userSection) {
        setupSecurityContext("ROLE_USER");
        List<Class> userClasses = setUpUserMocking(userSection);
        Class classNotSame = userClasses.get(0);
        classNotSame.setCourseId(userSection == 1L ? 2L : 1L); // Ensure different section

        when(classRepository.findById(classNotSame.getId())).thenReturn(Mono.just(classNotSame));
        Mono<ClassResponseDTO> result = planningService.findClassById(classNotSame.getId());

        assertThrows(ClassNotFoundException.class, result::block);
    }

    @Test
    @DisplayName("findClassById - When class exists returns DTO")
    void testFindClassById_ClassExists_ReturnsDTO() {
        setupSecurityContext("ROLE_ADMIN");
        when(classRepository.findById(1L)).thenReturn(Mono.just(testClass));
        when(classScheduleRepository.findByClassId(1L)).thenReturn(Flux.just(testSchedule));
        when(modelMapper.map(testClass, ClassResponseDTO.class)).thenReturn(testClassResponseDTO);
        when(modelMapper.map(testSchedule, ClassScheduleResponseDTO.class)).thenReturn(testScheduleResponseDTO);

        ClassResponseDTO result = planningService.findClassById(1L).block();

        assertNotNull(result);
        assertEquals(1L, result.getId());
        verify(classRepository).findById(1L);
    }

    @Test
    @DisplayName("findClassById - When class not found throws ClassNotFoundException")
    void testFindClassById_ClassNotFound_ThrowsException() {
        when(classRepository.findById(99L)).thenReturn(Mono.empty());

        Mono<ClassResponseDTO> result = planningService.findClassById(99L);

        assertThrows(ClassNotFoundException.class, result::block);
        verify(classRepository).findById(99L);
    }

    @ParameterizedTest(name = "findClassesByCourse - When course ID {0} returns classes")
    @MethodSource("userSectionProvider")
    @DisplayName("findClassesByCourse - Should return classes based on user section")
    void testFindClassesByCourse_BasedOnUserRoleAndSection_ReturnsDTO(Long userSection){
        setupSecurityContext("ROLE_USER");
        List<Class> userClasses = setUpUserMocking(userSection);
        when(modelMapper.map(any(Class.class), eq(ClassResponseDTO.class))).thenAnswer(invocation -> {
            Class cls = invocation.getArgument(0);
            return ClassResponseDTO.builder()
                    .id(cls.getId())
                    .section(cls.getSection())
                    .courseId(cls.getCourseId())
                    .build();
        });

        when(classRepository.findByCourseId(100L)).thenReturn(Flux.fromIterable(userClasses));
        when(classScheduleRepository.findByClassId(anyLong())).thenReturn(Flux.just(testSchedule));
        when(modelMapper.map(testSchedule, ClassScheduleResponseDTO.class)).thenReturn(testScheduleResponseDTO);

        List<ClassResponseDTO> result = planningService.findClassesByCourse(100L).collectList().block();

        assertNotNull(result, "Result should not be null");
        assertEquals(1, result.size(), "Result size should be 1");
        assertEquals(userSection, result.get(0).getSection(), "Returned class section should match user section");
        verify(classRepository).findByCourseId(100L);
    }

    @Test
    @DisplayName("findClassesByCourse - Should return classes for specified course")
    void testFindClassesByCourse_ReturnsClasses() {
        setupSecurityContext("ROLE_ADMIN");
        when(classRepository.findByCourseId(100L)).thenReturn(Flux.just(testClass));
        when(classScheduleRepository.findByClassId(1L)).thenReturn(Flux.just(testSchedule));
        when(modelMapper.map(testClass, ClassResponseDTO.class)).thenReturn(testClassResponseDTO);
        when(modelMapper.map(testSchedule, ClassScheduleResponseDTO.class)).thenReturn(testScheduleResponseDTO);

        List<ClassResponseDTO> result = planningService.findClassesByCourse(100L).collectList().block();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(100L, result.get(0).getCourseId());
        verify(classRepository).findByCourseId(100L);
    }

    @Test
    @DisplayName("findClassesByCourse - When no classes exist returns empty list")
    void testFindClassesByCourse_NoClassesExist_ReturnsEmptyList() {
        when(classRepository.findByCourseId(100L)).thenReturn(Flux.empty());

        List<ClassResponseDTO> result = planningService.findClassesByCourse(100L).collectList().block();

        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(classRepository).findByCourseId(100L);
    }

    @Test
    @DisplayName("findClassesByCourse - When error occurs throws PlanningServerErrorException")
    void testFindClassesByCourse_ErrorOccurs_ThrowsException() {
        when(classRepository.findByCourseId(100L)).thenReturn(Flux.error(new RuntimeException("Database error")));

        Mono<List<ClassResponseDTO>> result = planningService.findClassesByCourse(100L).collectList();

        assertThrows(PlanningServerErrorException.class, result::block);
        verify(classRepository).findByCourseId(100L);
    }

    @ParameterizedTest(name = "findClassesBySection - Should only return own section {0} classes")
    @MethodSource("userSectionProvider")
    @DisplayName("findClassesBySection - Should return classes based on user section")
    void testFindClassesBySection_BasedOnUserRoleAndSection_ReturnsDTO(Long userSection){
        setupSecurityContext("ROLE_USER");
        List<Class> userClasses = setUpUserMocking(userSection);
        when(modelMapper.map(any(Class.class), eq(ClassResponseDTO.class))).thenAnswer(invocation -> {
            Class cls = invocation.getArgument(0);
            return ClassResponseDTO.builder()
                    .id(cls.getId())
                    .section(cls.getSection())
                    .courseId(cls.getCourseId())
                    .build();
        });

        when(classRepository.findBySection(userSection)).thenReturn(Flux.fromIterable(userClasses).filter(classEntity -> classEntity.getSection().equals(userSection)));
        when(classScheduleRepository.findByClassId(anyLong())).thenReturn(Flux.just(testSchedule));
        when(modelMapper.map(testSchedule, ClassScheduleResponseDTO.class)).thenReturn(testScheduleResponseDTO);

        List<ClassResponseDTO> resultOwnSection = planningService.findClassesBySection(userSection).collectList().block();

        assertNotNull(resultOwnSection, "Result should not be null");
        assertEquals(1, resultOwnSection.size(), "Result size should be 1");
        assertEquals(userSection, resultOwnSection.get(0).getSection(), "Returned class section should match user section");
        verify(classRepository).findBySection(userSection);

        Long otherSection = userSection == 1L ? 2L : 1L;
        when(classRepository.findBySection(otherSection)).thenReturn(Flux.fromIterable(userClasses).filter(classEntity -> classEntity.getSection().equals(otherSection)));
        List<ClassResponseDTO> resultOtherSection = planningService.findClassesBySection(otherSection).collectList().block();

        assertNotNull(resultOtherSection, "Result should not be null");
        assertEquals(0, resultOtherSection.size(), "Result should be empty");
    }

    @Test
    @DisplayName("findClassesBySection - Should return classes for specified section")
    void testFindClassesBySection_ReturnsClasses() {
        setupSecurityContext("ROLE_ADMIN");
        when(classRepository.findBySection(1L)).thenReturn(Flux.just(testClass));
        when(classScheduleRepository.findByClassId(1L)).thenReturn(Flux.just(testSchedule));
        when(modelMapper.map(testClass, ClassResponseDTO.class)).thenReturn(testClassResponseDTO);
        when(modelMapper.map(testSchedule, ClassScheduleResponseDTO.class)).thenReturn(testScheduleResponseDTO);

        List<ClassResponseDTO> result = planningService.findClassesBySection(1L).collectList().block();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(1L, result.get(0).getSection());
        verify(classRepository).findBySection(1L);
    }

    @Test
    @DisplayName("findClassesBySection - When no classes exist returns empty list")
    void testFindClassesBySection_NoClassesExist_ReturnsEmptyList() {
        when(classRepository.findBySection(1L)).thenReturn(Flux.empty());

        List<ClassResponseDTO> result = planningService.findClassesBySection(1L).collectList().block();

        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(classRepository).findBySection(1L);
    }

    @Test
    @DisplayName("findClassesBySection - When error occurs throws PlanningServerErrorException")
    void testFindClassesBySection_ErrorOccurs_ThrowsException() {
        when(classRepository.findBySection(1L)).thenReturn(Flux.error(new RuntimeException("Database error")));

        Mono<List<ClassResponseDTO>> result = planningService.findClassesBySection(1L).collectList();

        assertThrows(PlanningServerErrorException.class, result::block);
        verify(classRepository).findBySection(1L);
    }

    @ParameterizedTest(name = "findCurrentSemesterClassesByCourse - When course ID {0} returns classes based on user section")
    @MethodSource("userSectionProvider")
    @DisplayName("findCurrentSemesterClassesByCourse - Should return classes based on user section")
    void testFindCurrentSemesterClassesByCourse_BasedOnUserRoleAndSection_ReturnsDTO(Long userSection){
        setupSecurityContext("ROLE_USER");
        List<Class> userClasses = setUpUserMocking(userSection);
        when(modelMapper.map(any(Class.class), eq(ClassResponseDTO.class))).thenAnswer(invocation -> {
            Class cls = invocation.getArgument(0);
            return ClassResponseDTO.builder()
                    .id(cls.getId())
                    .section(cls.getSection())
                    .courseId(cls.getCourseId())
                    .build();
        });

        when(semesterService.getCurrentSemesterId()).thenReturn(Mono.just(1L));
        when(classRepository.findBySemesterIdAndCourseId(1L, 100L)).thenReturn(Flux.fromIterable(userClasses).filter(classEntity -> classEntity.getSection().equals(userSection)));
        when(classScheduleRepository.findByClassId(anyLong())).thenReturn(Flux.just(testSchedule));
        when(modelMapper.map(testSchedule, ClassScheduleResponseDTO.class)).thenReturn(testScheduleResponseDTO);

        List<ClassResponseDTO> result = planningService.findCurrentSemesterClassesByCourse(100L).collectList().block();

        assertNotNull(result, "Result should not be null");
        assertEquals(1, result.size(), "Result size should be 1");
        assertEquals(userSection, result.get(0).getSection(), "Returned class section should match user section");
        verify(semesterService).getCurrentSemesterId();
        verify(classRepository).findBySemesterIdAndCourseId(1L, 100L);
    }

    @Test
    @DisplayName("findCurrentSemesterClassesByCourse - Should return classes for current semester and course")
    void testFindCurrentSemesterClassesByCourse_ReturnsClasses() {
        setupSecurityContext("ROLE_ADMIN");
        when(semesterService.getCurrentSemesterId()).thenReturn(Mono.just(1L));
        when(classRepository.findBySemesterIdAndCourseId(1L, 100L)).thenReturn(Flux.just(testClass));
        when(classScheduleRepository.findByClassId(1L)).thenReturn(Flux.just(testSchedule));
        when(modelMapper.map(testClass, ClassResponseDTO.class)).thenReturn(testClassResponseDTO);
        when(modelMapper.map(testSchedule, ClassScheduleResponseDTO.class)).thenReturn(testScheduleResponseDTO);

        List<ClassResponseDTO> result = planningService.findCurrentSemesterClassesByCourse(100L).collectList().block();

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(semesterService).getCurrentSemesterId();
        verify(classRepository).findBySemesterIdAndCourseId(1L, 100L);
    }

    @Test
    @DisplayName("findCurrentSemesterClassesByCourse - When semester service fails throws PlanningServerErrorException")
    void testFindCurrentSemesterClassesByCourse_SemesterServiceFails_ThrowsException() {
        when(semesterService.getCurrentSemesterId()).thenReturn(Mono.error(new RuntimeException("Database error")));

        Mono<List<ClassResponseDTO>> result = planningService.findCurrentSemesterClassesByCourse(100L).collectList();

        assertThrows(PlanningServerErrorException.class, result::block);
        verify(semesterService).getCurrentSemesterId();
    }

    @ParameterizedTest(name = "findClassesBySemester - Should only return own section {0} classes")
    @MethodSource("userSectionProvider")
    @DisplayName("findClassesBySemester - Should return classes based on user section")
    void testFindClassesBySemester_BasedOnUserRoleAndSection_ReturnsDTO(Long userSection){
        setupSecurityContext("ROLE_USER");
        List<Class> userClasses = setUpUserMocking(userSection);
        when(modelMapper.map(any(Class.class), eq(ClassResponseDTO.class))).thenAnswer(invocation -> {
            Class cls = invocation.getArgument(0);
            return ClassResponseDTO.builder()
                    .id(cls.getId())
                    .section(cls.getSection())
                    .courseId(cls.getCourseId())
                    .build();
        });

        when(classRepository.findBySemesterId(1L)).thenReturn(Flux.fromIterable(userClasses).filter(classEntity -> classEntity.getSection().equals(userSection)));
        when(classScheduleRepository.findByClassId(anyLong())).thenReturn(Flux.just(testSchedule));
        when(modelMapper.map(testSchedule, ClassScheduleResponseDTO.class)).thenReturn(testScheduleResponseDTO);

        List<ClassResponseDTO> resultOwnSection = planningService.findClassesBySemester(1L).collectList().block();

        assertNotNull(resultOwnSection, "Result should not be null");
        assertEquals(1, resultOwnSection.size(), "Result size should be 1");
        assertEquals(userSection, resultOwnSection.get(0).getSection(), "Returned class section should match user section");
        verify(classRepository).findBySemesterId(1L);
    }

    @Test
    @DisplayName("findClassesBySemester - Should return classes for specified semester")
    void testFindClassesBySemester_ReturnsClasses() {
        setupSecurityContext("ROLE_ADMIN");
        when(classRepository.findBySemesterId(1L)).thenReturn(Flux.just(testClass));
        when(classScheduleRepository.findByClassId(1L)).thenReturn(Flux.just(testSchedule));
        when(modelMapper.map(testClass, ClassResponseDTO.class)).thenReturn(testClassResponseDTO);
        when(modelMapper.map(testSchedule, ClassScheduleResponseDTO.class)).thenReturn(testScheduleResponseDTO);

        List<ClassResponseDTO> result = planningService.findClassesBySemester(1L).collectList().block();

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(classRepository).findBySemesterId(1L);
    }

    @Test
    @DisplayName("findClassesBySemester - When error occurs throws PlanningServerErrorException")
    void testFindClassesBySemester_ErrorOccurs_ThrowsException() {
        when(classRepository.findBySemesterId(1L)).thenReturn(Flux.error(new RuntimeException("Database error")));

        Mono<List<ClassResponseDTO>> result = planningService.findClassesBySemester(1L).collectList();

        assertThrows(PlanningServerErrorException.class, result::block);
        verify(classRepository).findBySemesterId(1L);
    }

    // ==================== UPDATE CLASS TESTS ====================

    @ParameterizedTest(name = "updateClass - When class ID is of user section {0} updates and returns DTO")
    @MethodSource("userSectionProvider")
    @DisplayName("updateClass - Should update class based on user section")
    void testUpdateClass_BasedOnUserSection_UpdatesAndReturnsDTO(Long userSection) {
        setupSecurityContext("ROLE_USER");
        setUpUserMocking(userSection);
        Class existingClass = Class.builder()
                .id(userSection)
                .section(userSection)
                .courseId(userSection)
                .capacity(30)
                .build();

        Class updatedClass = Class.builder()
                .id(userSection)
                .section(userSection)
                .courseId(userSection)
                .capacity(40)
                .build();

        ClassCreateRequestDTO updateRequest = ClassCreateRequestDTO.builder()
                .capacity(40)
                .build();

        ClassResponseDTO updatedDTO = ClassResponseDTO.builder()
                .id(userSection)
                .section(userSection)
                .capacity(40)
                .build();

        // Mock ModelMapper configuration
        org.modelmapper.config.Configuration mockConfig = mock(org.modelmapper.config.Configuration.class);
        when(modelMapper.getConfiguration()).thenReturn(mockConfig);
        when(mockConfig.setPropertyCondition(any())).thenReturn(mockConfig);

        when(classRepository.findById(userSection)).thenReturn(Mono.just(existingClass));
        when(classRepository.save(any(Class.class))).thenReturn(Mono.just(updatedClass));
        
        // Mock both map calls: DTO->Entity (void) and Entity->DTO (return)
        doNothing().when(modelMapper).map(any(ClassCreateRequestDTO.class), any(Class.class));
        doReturn(updatedDTO).when(modelMapper).map(any(Class.class), eq(ClassResponseDTO.class));

        ClassResponseDTO result = planningService.updateClass(userSection, updateRequest).block();

        assertNotNull(result);
        assertEquals(userSection, result.getId());
        assertEquals(40, result.getCapacity());
        verify(classRepository).findById(userSection);
    }

    @ParameterizedTest(name = "updateClass - When class ID is of different section {0} throws ClassNotFoundException")
    @MethodSource("userSectionProvider")
    @DisplayName("updateClass - When class not in user section throws ClassNotFoundException")
    void testUpdateClass_ClassNotInUserSection_ThrowsException(Long userSection) {
        setupSecurityContext("ROLE_USER");
        setUpUserMocking(userSection);
        Class classNotSame = Class.builder()
                .id(userSection)
                .section(userSection == 1L? 2L : 1L) // Different section
                .courseId(100L)
                .capacity(30)
                .build();

        when(classRepository.findById(classNotSame.getId())).thenReturn(Mono.just(classNotSame));
        Mono<ClassResponseDTO> result = planningService.updateClass(classNotSame.getId(), new ClassCreateRequestDTO());

        assertThrows(ClassNotFoundException.class, result::block);
    }

    @Test
    @DisplayName("updateClass - When class exists updates and returns DTO")
    void testUpdateClass_ClassExists_UpdatesAndReturnsDTO() {
        setupSecurityContext("ROLE_ADMIN");
        Class existingClass = Class.builder()
                .id(1L)
                .section(1L)
                .courseId(100L)
                .capacity(30)
                .build();

        Class updatedClass = Class.builder()
                .id(1L)
                .section(2L)
                .courseId(100L)
                .capacity(40)
                .build();

        ClassCreateRequestDTO updateRequest = ClassCreateRequestDTO.builder()
                .section(2L)
                .capacity(40)
                .build();

        ClassResponseDTO updatedDTO = ClassResponseDTO.builder()
                .id(1L)
                .section(2L)
                .capacity(40)
                .build();

        // Mock ModelMapper configuration
        org.modelmapper.config.Configuration mockConfig = mock(org.modelmapper.config.Configuration.class);
        when(modelMapper.getConfiguration()).thenReturn(mockConfig);
        when(mockConfig.setPropertyCondition(any())).thenReturn(mockConfig);

        when(classRepository.findById(1L)).thenReturn(Mono.just(existingClass));
        when(classRepository.save(any(Class.class))).thenReturn(Mono.just(updatedClass));
        
        // Mock both map calls: DTO->Entity (void) and Entity->DTO (return)
        doNothing().when(modelMapper).map(any(ClassCreateRequestDTO.class), any(Class.class));
        doReturn(updatedDTO).when(modelMapper).map(any(Class.class), eq(ClassResponseDTO.class));

        ClassResponseDTO result = planningService.updateClass(1L, updateRequest).block();

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals(2L, result.getSection());
        verify(classRepository).findById(1L);
        verify(classRepository).save(any(Class.class));
    }

    @Test
    @DisplayName("updateClass - When class not found throws ClassNotFoundException")
    void testUpdateClass_ClassNotFound_ThrowsException() {
        when(classRepository.findById(99L)).thenReturn(Mono.empty());

        Mono<ClassResponseDTO> result = planningService.updateClass(99L, new ClassCreateRequestDTO());

        assertThrows(ClassNotFoundException.class, result::block);
    }

    @Test
    @DisplayName("updateClass - When error occurs throws PlanningServerErrorException")
    void testUpdateClass_ErrorOccurs_ThrowsException() {
        setupSecurityContext("ROLE_ADMIN");
        when(classRepository.findById(1L)).thenReturn(Mono.just(testClass));

        Mono<ClassResponseDTO> result = planningService.updateClass(1L, new ClassCreateRequestDTO());

        assertThrows(PlanningServerErrorException.class, result::block);
    }

    @Test
    @DisplayName("updateClass - When Null fields in request, existing values are not retained")
    void testUpdateClass_NullFieldsInRequest_ExistingValuesRetained() {
        setupSecurityContext("ROLE_ADMIN");
        Class existingClass = Class.builder()
                .id(1L)
                .section(1L)
                .courseId(100L)
                .capacity(30)
                .observation("Initial observation")
                .build();

        ClassCreateRequestDTO updateRequest = ClassCreateRequestDTO.builder()
                .section(null) // Intentionally null
                .capacity(40)
                .observation(null) // Intentionally null
                .build();

        Class updatedClass = Class.builder()
                .id(1L)
                .section(1L) // Should retain existing value
                .courseId(100L)
                .capacity(40)
                .observation("Initial observation") // Should retain existing value
                .build();

        ClassResponseDTO updatedDTO = ClassResponseDTO.builder()
                .id(1L)
                .section(1L)
                .capacity(40)
                .observation("Initial observation")
                .build();

        // Mock ModelMapper configuration
        org.modelmapper.config.Configuration mockConfig = mock(org.modelmapper.config.Configuration.class);
        when(modelMapper.getConfiguration()).thenReturn(mockConfig);
        when(mockConfig.setPropertyCondition(any())).thenReturn(mockConfig);

        when(classRepository.findById(1L)).thenReturn(Mono.just(existingClass));
        when(classRepository.save(any(Class.class))).thenReturn(Mono.just(updatedClass));
        
        // Mock both map calls: DTO->Entity (void) and Entity->DTO (return)
        doNothing().when(modelMapper).map(any(ClassCreateRequestDTO.class), any(Class.class));
        doReturn(updatedDTO).when(modelMapper).map(any(Class.class), eq(ClassResponseDTO.class));

        ClassResponseDTO result = planningService.updateClass(1L, updateRequest).block();

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals(1L, result.getSection()); // Existing value retained
        assertEquals(40, result.getCapacity());
        assertEquals("Initial observation", result.getObservation()); // Existing value retained
        verify(classRepository).findById(1L);
        verify(classRepository).save(any(Class.class));
    }

    // ==================== DELETE CLASS TESTS ====================

    @ParameterizedTest(name = "deleteClass - When class ID is of user section {0} deletes successfully")
    @MethodSource("userSectionProvider")
    @DisplayName("deleteClass - Should delete class based on user section")
    void testDeleteClass_BasedOnUserSection_DeletesSuccessfully(Long userSection) {
        setupSecurityContext("ROLE_USER");
        setUpUserMocking(userSection);
        Class existingClass = Class.builder().id(userSection).courseId(userSection).build();

        when(classRepository.findById(userSection)).thenReturn(Mono.just(existingClass));
        when(classRepository.deleteById(userSection)).thenReturn(Mono.empty());

        planningService.deleteClass(userSection).block();

        verify(classRepository).findById(userSection);
        verify(classRepository).deleteById(userSection);
    }

    @ParameterizedTest(name = "deleteClass - When class ID is of different section {0} throws ClassNotFoundException")
    @MethodSource("userSectionProvider")
    @DisplayName("deleteClass - When class not in user section throws ClassNotFoundException")
    void testDeleteClass_ClassNotInUserSection_ThrowsException(Long userSection) {
        setupSecurityContext("ROLE_USER");
        setUpUserMocking(userSection);
        Class classNotSame = Class.builder()
                .id(userSection)
                .courseId(userSection == 1L? 2L : 1L) // Different section
                .build();

        when(classRepository.findById(classNotSame.getId())).thenReturn(Mono.just(classNotSame));
        Mono<Void> result = planningService.deleteClass(classNotSame.getId());

        assertThrows(ClassNotFoundException.class, result::block);
    }

    @Test
    @DisplayName("deleteClass - When class exists deletes successfully")
    void testDeleteClass_ClassExists_DeletesSuccessfully() {
        setupSecurityContext("ROLE_ADMIN");
        Class existingClass = Class.builder().id(1L).build();

        when(classRepository.findById(1L)).thenReturn(Mono.just(existingClass));
        when(classRepository.deleteById(1L)).thenReturn(Mono.empty());

        planningService.deleteClass(1L).block();

        verify(classRepository).findById(1L);
        verify(classRepository).deleteById(1L);
    }

    @Test
    @DisplayName("deleteClass - When class not found throws ClassNotFoundException")
    void testDeleteClass_ClassNotFound_ThrowsException() {
        when(classRepository.findById(99L)).thenReturn(Mono.empty());

        Mono<Void> result = planningService.deleteClass(99L);

        assertThrows(ClassNotFoundException.class, result::block);
        verify(classRepository).findById(99L);
        verify(classRepository, never()).deleteById(any(Long.class));
    }


    // ==================== CREATE CLASS SCHEDULE TESTS ====================

    @ParameterizedTest(name = "addClassSchedule - When class ID is of user section {0} adds schedule successfully")
    @MethodSource("userSectionProvider")
    @DisplayName("addClassSchedule - Should add schedule based on user section")
    void testAddClassSchedule_BasedOnUserSection_AddsSchedule(Long userSection) {
        setupSecurityContext("ROLE_USER");
        setUpUserMocking(userSection);
        ClassSchedule mappedSchedule = ClassSchedule.builder()
                .classroomId(10L)
                .day("Monday")
                .build();

        ClassSchedule savedSchedule = ClassSchedule.builder()
                .id(1L)
                .classId(userSection)
                .classroomId(10L)
                .day("Monday")
                .build();

        ClassScheduleResponseDTO savedScheduleResponseDTO = ClassScheduleResponseDTO.builder()
                .id(1L)
                .classId(userSection)
                .classroomId(10L)
                .day("Monday")
                .build();

        Class test = Class.builder()
                .id(userSection)
                .section(userSection)
                .courseId(userSection)
                .build();
        
        when(classRepository.findById(userSection)).thenReturn(Mono.just(test));
        when(modelMapper.map(testScheduleRequestDTO, ClassSchedule.class)).thenReturn(mappedSchedule);
        when(classScheduleRepository.save(any(ClassSchedule.class))).thenReturn(Mono.just(savedSchedule));
        when(modelMapper.map(savedSchedule, ClassScheduleResponseDTO.class)).thenReturn(savedScheduleResponseDTO);

        ClassScheduleResponseDTO result = planningService.addClassSchedule(userSection, testScheduleRequestDTO).block();

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals(userSection, result.getClassId());
        verify(classRepository).findById(userSection);
        verify(classScheduleRepository).save(any(ClassSchedule.class));
    }

    @ParameterizedTest(name = "addClassSchedule - When class ID is of different section {0} throws ClassNotFoundException")
    @MethodSource("userSectionProvider")
    @DisplayName("addClassSchedule - When class not in user section throws ClassNotFoundException")
    void testAddClassSchedule_ClassNotInUserSection_ThrowsException(Long userSection) {
        setupSecurityContext("ROLE_USER");
        setUpUserMocking(userSection);
        Class classNotSame = Class.builder()
                .id(userSection)
                .section(userSection == 1L? 2L : 1L) // Different section
                .courseId(100L)
                .build();

        when(classRepository.findById(classNotSame.getId())).thenReturn(Mono.just(classNotSame));
        Mono<ClassScheduleResponseDTO> result = planningService.addClassSchedule(classNotSame.getId(), testScheduleRequestDTO);

        assertThrows(ClassNotFoundException.class, result::block);
        verify(classScheduleRepository, never()).save(any());
    }

    @Test
    @DisplayName("addClassSchedule - When class exists adds schedule successfully")
    void testAddClassSchedule_ClassExists_AddsSchedule() {
        setupSecurityContext("ROLE_ADMIN");
        ClassSchedule mappedSchedule = ClassSchedule.builder()
                .classroomId(10L)
                .day("Monday")
                .build();

        ClassSchedule savedSchedule = ClassSchedule.builder()
                .id(1L)
                .classId(1L)
                .classroomId(10L)
                .day("Monday")
                .build();

        Class test = Class.builder()
                .id(1L)
                .section(1L)
                .courseId(100L)
                .build();

        when(classRepository.findById(1L)).thenReturn(Mono.just(test));
        when(modelMapper.map(testScheduleRequestDTO, ClassSchedule.class)).thenReturn(mappedSchedule);
        when(classScheduleRepository.save(any(ClassSchedule.class))).thenReturn(Mono.just(savedSchedule));
        when(modelMapper.map(savedSchedule, ClassScheduleResponseDTO.class)).thenReturn(testScheduleResponseDTO);

        ClassScheduleResponseDTO result = planningService.addClassSchedule(1L, testScheduleRequestDTO).block();

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals(1L, result.getClassId());
        verify(classRepository).findById(1L);
        verify(classScheduleRepository).save(any(ClassSchedule.class));
    }

    @Test
    @DisplayName("addClassSchedule - When class not found throws ClassNotFoundException")
    void testAddClassSchedule_ClassNotFound_ThrowsException() {
        when(classRepository.findById(99L)).thenReturn(Mono.empty());

        Mono<ClassScheduleResponseDTO> result = planningService.addClassSchedule(99L, testScheduleRequestDTO);

        assertThrows(ClassNotFoundException.class, result::block);
        verify(classRepository).findById(99L);
        verify(classScheduleRepository, never()).save(any());
    }

    // ==================== FIND CLASS SCHEDULES TESTS ====================

    @ParameterizedTest(name = "findClassSchedulesByClassId - Should only return schedules for own section {0}")
    @MethodSource("userSectionProvider")
    @DisplayName("findClassSchedulesByClassId - Should return schedules based on user section")
    void testFindClassSchedulesByClassId_BasedOnUserRoleAndSection_ReturnsDTO(Long userSection){
        setupSecurityContext("ROLE_USER");
        setUpUserMocking(userSection);
        when(modelMapper.map(any(ClassSchedule.class), eq(ClassScheduleResponseDTO.class))).thenAnswer(invocation -> {
            ClassSchedule schedule = invocation.getArgument(0);
            return ClassScheduleResponseDTO.builder()
                    .id(schedule.getId())
                    .classId(schedule.getClassId())
                    .day(schedule.getDay())
                    .startTime(schedule.getStartTime())
                    .build();
        });

        Class test = Class.builder()
                .id(userSection)
                .section(userSection)
                .courseId(userSection)
                .build();

        ClassSchedule testClassSchedule = ClassSchedule.builder()
                .id(1L)
                .classId(userSection)
                .day("Monday")
                .startTime(LocalTime.of(9, 0))
                .build();

        ClassScheduleResponseDTO testClassScheduleResponseDTO = ClassScheduleResponseDTO.builder()
                .id(1L)
                .classId(userSection)
                .day("Monday")
                .startTime(LocalTime.of(9, 0))
                .build();
        
        when(classRepository.findById(userSection)).thenReturn(Mono.just(test));
        when(classScheduleRepository.findByClassId(userSection)).thenReturn(Flux.just(testClassSchedule));
        when(modelMapper.map(testClassSchedule, ClassScheduleResponseDTO.class)).thenReturn(testClassScheduleResponseDTO);

        List<ClassScheduleResponseDTO> result = planningService.findClassSchedulesByClassId(userSection).collectList().block();
        assertNotNull(result, "Result should not be null");
        assertEquals(1, result.size(), "Result size should be 1");
        assertEquals(userSection, result.get(0).getClassId(), "Returned schedule classId should match user section");
        verify(classScheduleRepository).findByClassId(userSection);
        verify(classRepository).findById(userSection);
    }

    @ParameterizedTest(name = "findClassSchedulesByClassId - When class ID is of different section {0} throws ClassNotFoundException")
    @MethodSource("userSectionProvider")
    @DisplayName("findClassSchedulesByClassId - When class not in user section throws ClassNotFoundException")
    void testFindClassSchedulesByClassId_ClassNotInUserSection_ThrowsException(Long userSection) {
        setupSecurityContext("ROLE_USER");
        setUpUserMocking(userSection);
        Class classNotSame = Class.builder()
                .id(userSection)
                .courseId(userSection == 1L? 2L : 1L) // Different section
                .build();

        when(classRepository.findById(userSection)).thenReturn(Mono.just(classNotSame));

        Flux<ClassScheduleResponseDTO> result = planningService.findClassSchedulesByClassId(userSection);
        assertThrows(ClassNotFoundException.class, result::blockFirst);
    }

    @Test
    @DisplayName("findClassSchedulesByClassId - Should return schedules for class")
    void testFindClassSchedulesByClassId_ReturnsSchedules() {
        setupSecurityContext("ROLE_ADMIN");

        Class test = Class.builder()
                .id(1L)
                .section(1L)
                .courseId(100L)
                .build();
        when(classRepository.findById(1L)).thenReturn(Mono.just(test));
        when(classScheduleRepository.findByClassId(1L)).thenReturn(Flux.just(testSchedule));
        when(modelMapper.map(testSchedule, ClassScheduleResponseDTO.class)).thenReturn(testScheduleResponseDTO);

        List<ClassScheduleResponseDTO> result = planningService.findClassSchedulesByClassId(1L).collectList().block();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(1L, result.get(0).getClassId());
        verify(classScheduleRepository).findByClassId(1L);
        verify(classRepository).findById(1L);
    }

    @ParameterizedTest(name = "findClassScheduleById - When class ID is of same section {0} returns schedule DTO")
    @MethodSource("userSectionProvider")
    @DisplayName("findClassScheduleById - When class not in user section returns schedule DTO")
    void testFindClassScheduleById_ClassInUserSection_ReturnsDTO(Long userSection) {
        setupSecurityContext("ROLE_USER");
        setUpUserMocking(userSection);

        Class test = Class.builder()
                .id(userSection)
                .section(userSection)
                .courseId(userSection)
                .build();

        ClassSchedule testClassSchedule = ClassSchedule.builder()
                .id(1L)
                .classId(userSection)
                .day("Monday")
                .startTime(LocalTime.of(9, 0))
                .build();

        ClassScheduleResponseDTO testClassScheduleResponseDTO = ClassScheduleResponseDTO.builder()
                .id(1L)
                .classId(userSection)
                .day("Monday")
                .startTime(LocalTime.of(9, 0))
                .build();
        
        when(classRepository.findById(userSection)).thenReturn(Mono.just(test));
        when(classScheduleRepository.findById(1L)).thenReturn(Mono.just(testClassSchedule));
        when(modelMapper.map(testClassSchedule, ClassScheduleResponseDTO.class)).thenReturn(testClassScheduleResponseDTO);

        ClassScheduleResponseDTO result = planningService.findClassScheduleById(1L).block();
        assertNotNull(result, "Result should not be null");
        assertEquals(1L, result.getId(), "Returned schedule ID should match");
        assertEquals(userSection, result.getClassId(), "Returned schedule classId should match user section");
        verify(classScheduleRepository).findById(1L);
        verify(classRepository).findById(userSection);
    }

    @ParameterizedTest(name = "findClassScheduleById - When class ID is of different section {0} throws ClassNotFoundException")
    @MethodSource("userSectionProvider")
    @DisplayName("findClassScheduleById - When class not in user section throws ClassNotFoundException")
    void testFindClassScheduleById_ClassNotInUserSection_ThrowsException(Long userSection) {
        setupSecurityContext("ROLE_USER");
        setUpUserMocking(userSection);
        Class classNotSame = Class.builder()
                .id(userSection)
                .section(userSection == 1L? 2L : 1L) // Different section
                .courseId(100L)
                .build();

        when(classRepository.findById(anyLong())).thenReturn(Mono.just(classNotSame));
        when(classScheduleRepository.findById(1L)).thenReturn(Mono.just(testSchedule));

        Mono<ClassScheduleResponseDTO> result = planningService.findClassScheduleById(1L);
        assertThrows(ClassNotFoundException.class, result::block);
    }

    @Test
    @DisplayName("findClassScheduleById - Should return schedule by ID")
    void testFindClassScheduleById_ReturnsSchedule() {
        setupSecurityContext("ROLE_ADMIN");

        Class test = Class.builder()
                .id(1L)
                .section(1L)
                .courseId(100L)
                .build();
        when(classRepository.findById(1L)).thenReturn(Mono.just(test));
        when(classScheduleRepository.findById(1L)).thenReturn(Mono.just(testSchedule));
        when(modelMapper.map(testSchedule, ClassScheduleResponseDTO.class)).thenReturn(testScheduleResponseDTO);

        ClassScheduleResponseDTO result = planningService.findClassScheduleById(1L).block();

        assertNotNull(result);
        assertEquals(1L, result.getId());
        verify(classScheduleRepository).findById(1L);
        verify(classRepository).findById(1L);
    }

    @Test
    @DisplayName("findClassScheduleById - When schedule not found throws ClassScheduleNotFoundException")
    void testFindClassScheduleById_ScheduleNotFound_ThrowsException() {
        when(classScheduleRepository.findById(99L)).thenReturn(Mono.empty());

        Mono<ClassScheduleResponseDTO> result = planningService.findClassScheduleById(99L);

        assertThrows(ClassScheduleNotFoundException.class, result::block);
    }

    @ParameterizedTest(name = "findClassSchedulesByClassroom - Should only return schedules for own section {0}")
    @MethodSource("userSectionProvider")
    @DisplayName("findClassSchedulesByClassroom - Should return schedules based on user section")
    void testFindClassSchedulesByClassroom_BasedOnUserRoleAndSection_ReturnsDTO(Long userSection){
        setupSecurityContext("ROLE_USER");
        setUpUserMocking(userSection);
        when(modelMapper.map(any(ClassSchedule.class), eq(ClassScheduleResponseDTO.class))).thenAnswer(invocation -> {
            ClassSchedule schedule = invocation.getArgument(0);
            return ClassScheduleResponseDTO.builder()
                    .id(schedule.getId())
                    .classId(schedule.getClassId())
                    .day(schedule.getDay())
                    .startTime(schedule.getStartTime())
                    .build();
        });

        Class test = Class.builder()
                .id(userSection)
                .section(userSection)
                .courseId(userSection)
                .build();

        ClassSchedule testClassSchedule = ClassSchedule.builder()
                .id(1L)
                .classId(userSection)
                .day("Monday")
                .startTime(LocalTime.of(9, 0))
                .build();

        Class testNotSection = Class.builder()
                .id(userSection == 1L ? 2L : 1L) // Different section
                .section(userSection == 1L ? 2L : 1L)
                .courseId(userSection == 1L ? 2L : 1L)
                .build();
        
        ClassSchedule testClassScheduleNotSection = ClassSchedule.builder()
        .id(2L)
        .classId(userSection == 1L ? 2L : 1L) // Different section
        .day("Monday")
        .startTime(LocalTime.of(9, 0))
        .build();

        when(classRepository.findById(userSection)).thenReturn(Mono.just(test));
        when(classRepository.findById(userSection == 1L ? 2L : 1L)).thenReturn(Mono.just(testNotSection));
        when(classScheduleRepository.findByClassroomId(10L)).thenReturn(Flux.just(testClassSchedule, testClassScheduleNotSection));

        List<ClassScheduleResponseDTO> result = planningService.findClassSchedulesByClassroom(10L).collectList().block();

        assertNotNull(result, "Result should not be null");
        assertEquals(1, result.size(), "Result size should be 1");
        assertEquals(userSection, result.get(0).getClassId(), "Returned schedule classId should match user section");
        verify(classScheduleRepository).findByClassroomId(10L);
        verify(classRepository).findById(userSection);
    }

    @Test
    @DisplayName("findClassSchedulesByClassroom - Should return schedules for classroom")
    void testFindClassSchedulesByClassroom_ReturnsSchedules() {
        setupSecurityContext("ROLE_ADMIN");
        Class test = Class.builder()
                .id(1L)
                .section(1L)
                .courseId(100L)
                .build();
        when(classRepository.findById(1L)).thenReturn(Mono.just(test));
        when(classScheduleRepository.findByClassroomId(10L)).thenReturn(Flux.just(testSchedule));
        when(modelMapper.map(testSchedule, ClassScheduleResponseDTO.class)).thenReturn(testScheduleResponseDTO);

        List<ClassScheduleResponseDTO> result = planningService.findClassSchedulesByClassroom(10L).collectList().block();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(10L, result.get(0).getClassroomId());
        verify(classScheduleRepository).findByClassroomId(10L);
        verify(classRepository).findById(1L);
    }

    @ParameterizedTest(name = "findClassSchedulesByDay - Should only return schedules for own section {0}")
    @MethodSource("userSectionProvider")
    @DisplayName("findClassSchedulesByDay - Should return schedules based on user section")
    void testFindClassSchedulesByDay_BasedOnUserRoleAndSection_ReturnsDTO(Long userSection){
        setupSecurityContext("ROLE_USER");
        setUpUserMocking(userSection);
        when(modelMapper.map(any(ClassSchedule.class), eq(ClassScheduleResponseDTO.class))).thenAnswer(invocation -> {
            ClassSchedule schedule = invocation.getArgument(0);
            return ClassScheduleResponseDTO.builder()
                    .id(schedule.getId())
                    .classId(schedule.getClassId())
                    .day(schedule.getDay())
                    .startTime(schedule.getStartTime())
                    .build();
        });

        Class test = Class.builder()
                .id(userSection)
                .section(userSection)
                .courseId(userSection)
                .build();

        ClassSchedule testClassSchedule = ClassSchedule.builder()
                .id(1L)
                .classId(userSection)
                .day("Monday")
                .startTime(LocalTime.of(9, 0))
                .build();

        Class testNotSection = Class.builder()
                .id(userSection == 1L ? 2L : 1L) // Different section
                .section(userSection == 1L ? 2L : 1L)
                .courseId(userSection == 1L ? 2L : 1L)
                .build();

        ClassSchedule testClassScheduleNotSection = ClassSchedule.builder()
                .id(2L)
                .classId(userSection == 1L ? 2L : 1L) // Different section
                .day("Monday")
                .startTime(LocalTime.of(9, 0))
                .build();

        when(classRepository.findById(userSection)).thenReturn(Mono.just(test));
        when(classRepository.findById(userSection == 1L ? 2L : 1L)).thenReturn(Mono.just(testNotSection));
        when(classScheduleRepository.findByDay("Monday")).thenReturn(Flux.just(testClassSchedule, testClassScheduleNotSection));

        List<ClassScheduleResponseDTO> result = planningService.findClassSchedulesByDay("Monday").collectList().block();
        assertNotNull(result, "Result should not be null");
        assertEquals(1, result.size(), "Result size should be 1");
        assertEquals(userSection, result.get(0).getClassId(), "Returned schedule classId should match user section");
        verify(classScheduleRepository).findByDay("Monday");
        verify(classRepository).findById(userSection);
    }

    @Test
    @DisplayName("findClassSchedulesByDay - Should return schedules for day")
    void testFindClassSchedulesByDay_ReturnsSchedules() {
        setupSecurityContext("ROLE_ADMIN");
        Class test = Class.builder()
                .id(1L)
                .section(1L)
                .courseId(100L)
                .build();
        when(classRepository.findById(1L)).thenReturn(Mono.just(test));
        when(classScheduleRepository.findByDay("Monday")).thenReturn(Flux.just(testSchedule));
        when(modelMapper.map(testSchedule, ClassScheduleResponseDTO.class)).thenReturn(testScheduleResponseDTO);

        List<ClassScheduleResponseDTO> result = planningService.findClassSchedulesByDay("Monday").collectList().block();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Monday", result.get(0).getDay());
        verify(classScheduleRepository).findByDay("Monday");
        verify(classRepository).findById(1L);
    }

    @ParameterizedTest(name = "findClassSchedulesByDisability - Should only return schedules for own section {0}")
    @MethodSource("userSectionProvider")
    @DisplayName("findClassSchedulesByDisability - Should return schedules based on user section")
    void testFindClassSchedulesByDisability_BasedOnUserRoleAndSection_ReturnsDTO(Long userSection){
        setupSecurityContext("ROLE_USER");
        setUpUserMocking(userSection);
        when(modelMapper.map(any(ClassSchedule.class), eq(ClassScheduleResponseDTO.class))).thenAnswer(invocation -> {
            ClassSchedule schedule = invocation.getArgument(0);
            return ClassScheduleResponseDTO.builder()
                    .id(schedule.getId())
                    .classId(schedule.getClassId())
                    .day(schedule.getDay())
                    .startTime(schedule.getStartTime())
                    .build();
        });

        Class test = Class.builder()
                .id(userSection)
                .section(userSection)
                .courseId(userSection)
                .build();

        ClassSchedule testClassSchedule = ClassSchedule.builder()
                .id(1L)
                .classId(userSection)
                .day("Monday")
                .startTime(LocalTime.of(9, 0))
                .build();

        Class testNotSection = Class.builder()
                .id(userSection == 1L ? 2L : 1L) // Different section
                .section(userSection == 1L ? 2L : 1L)
                .courseId(userSection == 1L ? 2L : 1L)
                .build();
        
        ClassSchedule testClassScheduleNotSection = ClassSchedule.builder()
        .id(2L)
        .classId(userSection == 1L ? 2L : 1L) // Different section
        .day("Monday")
        .startTime(LocalTime.of(9, 0))
        .build();

        when(classRepository.findById(userSection)).thenReturn(Mono.just(test));
        when(classRepository.findById(userSection == 1L ? 2L : 1L)).thenReturn(Mono.just(testNotSection));
        when(classScheduleRepository.findByDisability(false)).thenReturn(Flux.just(testClassSchedule, testClassScheduleNotSection));

        List<ClassScheduleResponseDTO> result = planningService.findClassSchedulesByDisability(false).collectList().block();

        assertNotNull(result, "Result should not be null");
        assertEquals(1, result.size(), "Result size should be 1");
        assertEquals(userSection, result.get(0).getClassId(), "Returned schedule classId should match user section");
        verify(classScheduleRepository).findByDisability(false);
        verify(classRepository).findById(userSection);
    }

    @Test
    @DisplayName("findClassSchedulesByDisability - Should return schedules with disability flag")
    void testFindClassSchedulesByDisability_ReturnsSchedules() {
        setupSecurityContext("ROLE_ADMIN");
        Class test = Class.builder()
                .id(1L)
                .section(1L)
                .courseId(100L)
                .build();
        when(classRepository.findById(1L)).thenReturn(Mono.just(test));
        when(classScheduleRepository.findByDisability(false)).thenReturn(Flux.just(testSchedule));
        when(modelMapper.map(testSchedule, ClassScheduleResponseDTO.class)).thenReturn(testScheduleResponseDTO);

        List<ClassScheduleResponseDTO> result = planningService.findClassSchedulesByDisability(false).collectList().block();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(false, result.get(0).getDisability());
        verify(classScheduleRepository).findByDisability(false);
        verify(classRepository).findById(1L);
    }

    // ==================== UPDATE CLASS SCHEDULE TESTS ====================

    @ParameterizedTest(name = "updateClassSchedule - When class ID is of user section {0} updates successfully")
    @MethodSource("userSectionProvider")
    @DisplayName("updateClassSchedule - Should update schedule based on user section")
    void testUpdateClassSchedule_BasedOnUserSection_UpdatesSuccessfully(Long userSection) {
        setupSecurityContext("ROLE_USER");
        setUpUserMocking(userSection);
        ClassSchedule existingSchedule = ClassSchedule.builder()
                .id(1L)
                .classId(userSection)
                .day("Monday")
                .startTime(LocalTime.of(9, 0))
                .build();

        ClassSchedule updatedSchedule = ClassSchedule.builder()
                .id(1L)
                .classId(userSection)
                .day("Tuesday")
                .startTime(LocalTime.of(10, 0))
                .build();

        ClassScheduleRequestDTO updateRequest = ClassScheduleRequestDTO.builder()
                .day("Tuesday")
                .startTime(LocalTime.of(10, 0))
                .build();

        ClassScheduleResponseDTO updatedDTO = ClassScheduleResponseDTO.builder()
                .id(1L)
                .day("Tuesday")
                .startTime(LocalTime.of(10, 0))
                .build();

        Class test = Class.builder()
                .id(userSection)
                .section(userSection)
                .courseId(userSection)
                .build();

        // Mock ModelMapper configuration
        org.modelmapper.config.Configuration mockConfig = mock(org.modelmapper.config.Configuration.class);
        when(modelMapper.getConfiguration()).thenReturn(mockConfig);
        when(mockConfig.setPropertyCondition(any())).thenReturn(mockConfig);

        when(classRepository.findById(userSection)).thenReturn(Mono.just(test));
        when(classScheduleRepository.findById(1L)).thenReturn(Mono.just(existingSchedule));
        when(classScheduleRepository.save(any(ClassSchedule.class))).thenReturn(Mono.just(updatedSchedule));

        // Mock both map calls: DTO->Entity and Entity->DTO
        doNothing().when(modelMapper).map(any(ClassScheduleRequestDTO.class), any(ClassSchedule.class));
        doReturn(updatedDTO).when(modelMapper).map(any(ClassSchedule.class), eq(ClassScheduleResponseDTO.class));

        ClassScheduleResponseDTO result = planningService.updateClassSchedule(1L, updateRequest).block();

        assertNotNull(result);
        assertEquals("Tuesday", result.getDay());
        verify(classScheduleRepository).findById(1L);
        verify(classRepository).findById(userSection);
        verify(classScheduleRepository).save(any(ClassSchedule.class));
    }

    @ParameterizedTest(name = "updateClassSchedule - When class ID is of different section {0} throws ClassNotFoundException")
    @MethodSource("userSectionProvider")
    @DisplayName("updateClassSchedule - When class not in user section throws ClassNotFoundException")
    void testUpdateClassSchedule_ClassNotInUserSection_ThrowsException(Long userSection) {
        setupSecurityContext("ROLE_USER");
        setUpUserMocking(userSection);
        Class classNotSame = Class.builder()
                .id(userSection)
                .section(userSection == 1L? 2L : 1L) // Different section
                .courseId(100L)
                .build();

        when(classRepository.findById(anyLong())).thenReturn(Mono.just(classNotSame));
        when(classScheduleRepository.findById(1L)).thenReturn(Mono.just(testSchedule));

        Mono<ClassScheduleResponseDTO> result = planningService.updateClassSchedule(1L, new ClassScheduleRequestDTO());
        assertThrows(ClassNotFoundException.class, result::block);
    }

    @Test
    @DisplayName("updateClassSchedule - When schedule exists updates successfully")
    void testUpdateClassSchedule_ScheduleExists_UpdatesSuccessfully() {
        setupSecurityContext("ROLE_ADMIN");
        ClassSchedule existingSchedule = ClassSchedule.builder()
                .id(1L)
                .classId(1L)
                .day("Monday")
                .startTime(LocalTime.of(9, 0))
                .build();

        ClassSchedule updatedSchedule = ClassSchedule.builder()
                .id(1L)
                .classId(1L)
                .day("Tuesday")
                .startTime(LocalTime.of(10, 0))
                .build();

        ClassScheduleRequestDTO updateRequest = ClassScheduleRequestDTO.builder()
                .day("Tuesday")
                .startTime(LocalTime.of(10, 0))
                .build();

        ClassScheduleResponseDTO updatedDTO = ClassScheduleResponseDTO.builder()
                .id(1L)
                .day("Tuesday")
                .startTime(LocalTime.of(10, 0))
                .build();

        Class test = Class.builder()
                .id(1L)
                .section(1L)
                .courseId(100L)
                .build();

        // Mock ModelMapper configuration
        org.modelmapper.config.Configuration mockConfig = mock(org.modelmapper.config.Configuration.class);
        when(modelMapper.getConfiguration()).thenReturn(mockConfig);
        when(mockConfig.setPropertyCondition(any())).thenReturn(mockConfig);

        when(classRepository.findById(1L)).thenReturn(Mono.just(test));
        when(classScheduleRepository.findById(1L)).thenReturn(Mono.just(existingSchedule));
        when(classScheduleRepository.save(any(ClassSchedule.class))).thenReturn(Mono.just(updatedSchedule));
        
        // Mock both map calls: DTO->Entity and Entity->DTO
        doNothing().when(modelMapper).map(any(ClassScheduleRequestDTO.class), any(ClassSchedule.class));
        doReturn(updatedDTO).when(modelMapper).map(any(ClassSchedule.class), eq(ClassScheduleResponseDTO.class));

        ClassScheduleResponseDTO result = planningService.updateClassSchedule(1L, updateRequest).block();

        assertNotNull(result);
        assertEquals("Tuesday", result.getDay());
        verify(classScheduleRepository).findById(1L);
        verify(classRepository).findById(1L);
        verify(classScheduleRepository).save(any(ClassSchedule.class));
    }

    @Test
    @DisplayName("updateClassSchedule - When schedule not found throws ClassScheduleNotFoundException")
    void testUpdateClassSchedule_ScheduleNotFound_ThrowsException() {
        when(classScheduleRepository.findById(99L)).thenReturn(Mono.empty());

        Mono<ClassScheduleResponseDTO> result = planningService.updateClassSchedule(99L, new ClassScheduleRequestDTO());

        assertThrows(ClassScheduleNotFoundException.class, result::block);
    }

    @Test
    @DisplayName("updateClassSchedule - When error occurs throws PlanningServerErrorException")
    void testUpdateClassSchedule_ErrorOccurs_ThrowsException() {
        when(classScheduleRepository.findById(1L)).thenReturn(Mono.just(testSchedule));

        Mono<ClassScheduleResponseDTO> result = planningService.updateClassSchedule(1L, new ClassScheduleRequestDTO());

        assertThrows(PlanningServerErrorException.class, result::block);
    }

    @Test
    @DisplayName("updateClassSchedule - When Null fields in request, existing values are retained")
    void testUpdateClassSchedule_NullFieldsInRequest_ExistingValuesRetained() {
        setupSecurityContext("ROLE_ADMIN");
        ClassSchedule existingSchedule = ClassSchedule.builder()
                .id(1L)
                .classId(1L)
                .day("Monday")
                .startTime(LocalTime.of(9, 0))
                .endTime(LocalTime.of(11, 0))
                .build();

        ClassScheduleRequestDTO updateRequest = ClassScheduleRequestDTO.builder()
                .day(null) // Intentionally null
                .startTime(LocalTime.of(10, 0))
                .endTime(null) // Intentionally null
                .build();

        ClassSchedule updatedSchedule = ClassSchedule.builder()
                .id(1L)
                .classId(1L)
                .day("Monday") // Should retain existing value
                .startTime(LocalTime.of(10, 0))
                .endTime(LocalTime.of(11, 0)) // Should retain existing value
                .build();

        ClassScheduleResponseDTO updatedDTO = ClassScheduleResponseDTO.builder()
                .id(1L)
                .day("Monday")
                .startTime(LocalTime.of(10, 0))
                .endTime(LocalTime.of(11, 0))
                .build();

        // Mock ModelMapper configuration
        org.modelmapper.config.Configuration mockConfig = mock(org.modelmapper.config.Configuration.class);
        when(modelMapper.getConfiguration()).thenReturn(mockConfig);
        when(mockConfig.setPropertyCondition(any())).thenReturn(mockConfig);
        when(classScheduleRepository.findById(1L)).thenReturn(Mono.just(existingSchedule));
        when(classScheduleRepository.save(any(ClassSchedule.class))).thenReturn(Mono.just(updatedSchedule));
        when(classRepository.findById(1L)).thenReturn(Mono.just(Class.builder().id(1L).build()));

        // Mock both map calls: DTO->Entity and Entity->DTO
        doNothing().when(modelMapper).map(any(ClassScheduleRequestDTO.class), any(ClassSchedule.class));
        doReturn(updatedDTO).when(modelMapper).map(any(ClassSchedule.class), eq(ClassScheduleResponseDTO.class));

        ClassScheduleResponseDTO result = planningService.updateClassSchedule(1L, updateRequest).block();
        assertNotNull(result);
        assertEquals("Monday", result.getDay()); // Existing value retained
        assertEquals(LocalTime.of(10, 0), result.getStartTime());
        assertEquals(LocalTime.of(11, 0), result.getEndTime()); // Existing
        verify(classScheduleRepository).findById(1L);
        verify(classRepository).findById(1L);
        verify(classScheduleRepository).save(any(ClassSchedule.class));
    }

    // ==================== DELETE CLASS SCHEDULE TESTS ====================

    @Test
    @DisplayName("deleteClassSchedule - When class ID is of user section deletes successfully")
    void testDeleteClassSchedule_BasedOnUserSection_DeletesSuccessfully() {
        setupSecurityContext("ROLE_USER");
        setUpUserMocking(1L);
        when(classRepository.findById(1L)).thenReturn(Mono.just(Class.builder().id(1L).courseId(1L).build()));
        when(classScheduleRepository.findById(1L)).thenReturn(Mono.just(ClassSchedule.builder().id(1L).classId(1L).build()));
        when(classScheduleRepository.deleteById(1L)).thenReturn(Mono.empty());

        planningService.deleteClassSchedule(1L).block();
        verify(classScheduleRepository).findById(1L);
        verify(classRepository).findById(1L);
        verify(classScheduleRepository).deleteById(1L);
    }

    @Test
    @DisplayName("deleteClassSchedule - When class ID is of different section throws ClassNotFoundException")
    void testDeleteClassSchedule_ClassNotInUserSection_ThrowsException() {
        setupSecurityContext("ROLE_USER");
        setUpUserMocking(1L);
        when(classRepository.findById(1L)).thenReturn(Mono.just(Class.builder().id(1L).courseId(2L).build()));
        when(classScheduleRepository.findById(1L)).thenReturn(Mono.just(ClassSchedule.builder().id(1L).classId(1L).build()));

        Mono<Void> result = planningService.deleteClassSchedule(1L);
        assertThrows(ClassNotFoundException.class, result::block);
        verify(classScheduleRepository).findById(1L);
        verify(classRepository).findById(1L);
        verify(classScheduleRepository, never()).deleteById(any(Long.class));
    }

    @Test
    @DisplayName("deleteClassSchedule - When schedule exists deletes successfully")
    void testDeleteClassSchedule_ScheduleExists_DeletesSuccessfully() {
        setupSecurityContext("ROLE_ADMIN");
        when(classRepository.findById(1L)).thenReturn(Mono.just(Class.builder().id(1L).build()));
        when(classScheduleRepository.findById(1L)).thenReturn(Mono.just(ClassSchedule.builder().id(1L).classId(1L).build()));
        when(classScheduleRepository.deleteById(1L)).thenReturn(Mono.empty());

        planningService.deleteClassSchedule(1L).block();

        verify(classScheduleRepository).findById(1L);
        verify(classRepository).findById(1L);
        verify(classScheduleRepository).deleteById(1L);
    }

    @Test
    @DisplayName("deleteClassSchedule - When schedule not found throws ClassScheduleNotFoundException")
    void testDeleteClassSchedule_ScheduleNotFound_ThrowsException() {
        when(classScheduleRepository.findById(99L)).thenReturn(Mono.empty());

        Mono<Void> result = planningService.deleteClassSchedule(99L);

        assertThrows(ClassScheduleNotFoundException.class, result::block);
        verify(classScheduleRepository).findById(99L);
        verify(classScheduleRepository, never()).deleteById(any(Long.class));
    }

    // ==================== PATCH CLASS SCHEDULE TESTS ====================

    @ParameterizedTest(name = "patchClassSchedule - When class ID is of user section {0} updates partially")
    @MethodSource("userSectionProvider")
    @DisplayName("patchClassSchedule - Should partially update schedule based on user section")
    void testPatchClassSchedule_BasedOnUserSection_UpdatesPartially(Long userSection) {
        setupSecurityContext("ROLE_USER");
        setUpUserMocking(userSection);
        ClassSchedule existingSchedule = ClassSchedule.builder()
                .id(1L)
                .classId(userSection)
                .day("Monday")
                .startTime(LocalTime.of(9, 0))
                .endTime(LocalTime.of(11, 0))
                .classroomId(10L)
                .build();

        ClassSchedule updatedSchedule = ClassSchedule.builder()
                .id(1L)
                .classId(userSection)
                .day("Tuesday")
                .startTime(LocalTime.of(9, 0))
                .endTime(LocalTime.of(11, 0))
                .classroomId(10L)
                .build();

        Map<String, Object> updates = new HashMap<>();
        updates.put("day", "Tuesday");

        ClassScheduleResponseDTO responseDTO = ClassScheduleResponseDTO.builder()
                .id(1L)
                .day("Tuesday")
                .build();

        Class test = Class.builder()
                .id(userSection)
                .section(userSection)
                .courseId(userSection)
                .build();

        when(classRepository.findById(userSection)).thenReturn(Mono.just(test));
        when(classScheduleRepository.findById(1L)).thenReturn(Mono.just(existingSchedule));
        when(classScheduleRepository.save(any(ClassSchedule.class))).thenReturn(Mono.just(updatedSchedule));
        when(modelMapper.map(updatedSchedule, ClassScheduleResponseDTO.class)).thenReturn(responseDTO);

        ClassScheduleResponseDTO result = planningService.patchClassSchedule(1L, updates).block();

        assertNotNull(result);
        assertEquals("Tuesday", result.getDay());
        verify(classScheduleRepository).findById(1L);
        verify(classRepository).findById(userSection);
        verify(classScheduleRepository).save(any(ClassSchedule.class));
    }

    @ParameterizedTest(name = "patchClassSchedule - When class ID is of different section {0} throws ClassNotFoundException")
    @MethodSource("userSectionProvider")
    @DisplayName("patchClassSchedule - When class not in user section throws ClassNotFoundException")
    void testPatchClassSchedule_ClassNotInUserSection_ThrowsException(Long userSection) {
        setupSecurityContext("ROLE_USER");
        setUpUserMocking(userSection);
        Class classNotSame = Class.builder()
                .id(userSection)
                .section(userSection == 1L? 2L : 1L) // Different section
                .courseId(100L)
                .build();

        when(classRepository.findById(anyLong())).thenReturn(Mono.just(classNotSame));
        when(classScheduleRepository.findById(1L)).thenReturn(Mono.just(testSchedule));

        Map<String, Object> updates = new HashMap<>();
        updates.put("day", "Tuesday");

        Mono<ClassScheduleResponseDTO> result = planningService.patchClassSchedule(1L, updates);
        assertThrows(ClassNotFoundException.class, result::block);
    }

    @Test
    @DisplayName("patchClassSchedule - Should partially update schedule")
    void testPatchClassSchedule_UpdatesPartially() {
        setupSecurityContext("ROLE_ADMIN");
        ClassSchedule existingSchedule = ClassSchedule.builder()
                .id(1L)
                .classId(1L)
                .day("Monday")
                .startTime(LocalTime.of(9, 0))
                .endTime(LocalTime.of(11, 0))
                .classroomId(10L)
                .build();

        ClassSchedule updatedSchedule = ClassSchedule.builder()
                .id(1L)
                .classId(1L)
                .day("Tuesday")
                .startTime(LocalTime.of(9, 0))
                .endTime(LocalTime.of(11, 0))
                .classroomId(10L)
                .build();

        Map<String, Object> updates = new HashMap<>();
        updates.put("day", "Tuesday");

        ClassScheduleResponseDTO responseDTO = ClassScheduleResponseDTO.builder()
                .id(1L)
                .day("Tuesday")
                .build();

        Class test = Class.builder()
                .id(1L)
                .section(1L)
                .courseId(100L)
                .build();

        when(classRepository.findById(1L)).thenReturn(Mono.just(test));

        when(classScheduleRepository.findById(1L)).thenReturn(Mono.just(existingSchedule));
        when(classScheduleRepository.save(any(ClassSchedule.class))).thenReturn(Mono.just(updatedSchedule));
        when(modelMapper.map(updatedSchedule, ClassScheduleResponseDTO.class)).thenReturn(responseDTO);

        ClassScheduleResponseDTO result = planningService.patchClassSchedule(1L, updates).block();

        assertNotNull(result);
        assertEquals("Tuesday", result.getDay());
        verify(classScheduleRepository).findById(1L);
        verify(classRepository).findById(1L);
        verify(classScheduleRepository).save(any(ClassSchedule.class));
    }

    @Test
    @DisplayName("patchClassSchedule - When schedule not found throws ClassScheduleNotFoundException")
    void testPatchClassSchedule_ScheduleNotFound_ThrowsException() {
        when(classScheduleRepository.findById(99L)).thenReturn(Mono.empty());

        Map<String, Object> updates = new HashMap<>();
        updates.put("day", "Tuesday");

        Mono<ClassScheduleResponseDTO> result = planningService.patchClassSchedule(99L, updates);

        assertThrows(ClassScheduleNotFoundException.class, result::block);
        verify(classScheduleRepository).findById(99L);
    }

    @Test
    @DisplayName("patchClassSchedule - When invalid field throws PlanningBadRequestException")
    void testPatchClassSchedule_InvalidField_ThrowsException() {
        setupSecurityContext("ROLE_ADMIN");
        ClassSchedule existingSchedule = ClassSchedule.builder()
                .id(1L)
                .classId(1L)
                .day("Monday")
                .build();

        Class test = Class.builder()
                .id(1L)
                .section(1L)
                .courseId(100L)
                .build();
        
        when(classRepository.findById(1L)).thenReturn(Mono.just(test));

        when(classScheduleRepository.findById(1L)).thenReturn(Mono.just(existingSchedule));

        Map<String, Object> updates = new HashMap<>();
        updates.put("invalidField", "value");

        Mono<ClassScheduleResponseDTO> result = planningService.patchClassSchedule(1L, updates);

        assertThrows(PlanningBadRequestException.class, result::block);
        verify(classScheduleRepository).findById(1L);
        verify(classRepository).findById(1L);
    }


    // ==================== DUPLICATE SEMESTER PLANNING TESTS ====================
    @ParameterizedTest(name = "duplicateSemesterPlanning - Should only duplicate classes and schedules for own section {0}")
    @MethodSource("userSectionProvider")
    @DisplayName("duplicateSemesterPlanning - Should duplicate based on user section")
    void testDuplicateSemesterPlanning_BasedOnUserSection_DuplicatesSuccessfully(Long userSection) {
        setupSecurityContext("ROLE_USER");
        List<Class> classes = setUpUserMocking(userSection);

        Class sourceClass = classes.get((int)(userSection - 1));
        Class copiedClass = Class.builder()
                .id(2L)
                .section(userSection)
                .courseId(100L)
                .semesterId(2L)
                .startDate(LocalDate.of(2025, 1, 10))
                .endDate(LocalDate.of(2025, 5, 10))
                .capacity(30)
                .build();

        ClassSchedule sourceSchedule = ClassSchedule.builder()
                .id(1L)
                .classId(sourceClass.getId())
                .day("Monday")
                .startTime(LocalTime.of(9, 0))
                .build();

        ClassSchedule copiedSchedule = ClassSchedule.builder()
                .id(2L)
                .classId(2L)
                .day("Monday")
                .startTime(LocalTime.of(9, 0))
                .build();

        ClassResponseDTO responseDTO = ClassResponseDTO.builder()
                .id(2L)
                .semesterId(2L)
                .startDate(LocalDate.of(2025, 1, 10))
                .endDate(LocalDate.of(2025, 5, 10))
                .build();

        SemesterResponseDTO targetSemester = SemesterResponseDTO.builder()
                .id(2L)
                .startDate(LocalDate.of(2025, 1, 10))
                .endDate(LocalDate.of(2025, 5, 10))
                .build();

        when(semesterService.getSemesterById(2L)).thenReturn(Mono.just(targetSemester));
        when(classRepository.findBySemesterId(1L)).thenReturn(Flux.fromIterable(classes));
        when(classRepository.save(any(Class.class))).thenReturn(Mono.just(copiedClass));
        when(classScheduleRepository.findByClassId(sourceClass.getId())).thenReturn(Flux.just(sourceSchedule));
        when(classScheduleRepository.save(any(ClassSchedule.class))).thenReturn(Mono.just(copiedSchedule));
        when(classScheduleRepository.findByClassId(2L)).thenReturn(Flux.just(copiedSchedule));

        // Add stubs for all map operations
        doNothing().when(modelMapper).map(any(Class.class), any(Class.class));
        doNothing().when(modelMapper).map(any(ClassSchedule.class), any(ClassSchedule.class));
        doReturn(responseDTO).when(modelMapper).map(any(Class.class), eq(ClassResponseDTO.class));
        doReturn(testScheduleResponseDTO).when(modelMapper).map(any(ClassSchedule.class), eq(ClassScheduleResponseDTO.class));

        List<ClassResponseDTO> result = planningService.duplicateSemesterPlanning(1L, 2L).collectList().block();

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(semesterService).getSemesterById(2L);
        verify(classRepository).findBySemesterId(1L);
        verify(classRepository, atLeastOnce()).save(any(Class.class));
        verify(classScheduleRepository, atLeastOnce()).save(any(ClassSchedule.class));
    }

    @Test
    @DisplayName("duplicateSemesterPlanning - Should duplicate classes and schedules to target semester")
    void testDuplicateSemesterPlanning_DuplicatesSuccessfully() {
        setupSecurityContext("ROLE_ADMIN");
        Class sourceClass = Class.builder()
                .id(1L)
                .section(1L)
                .courseId(100L)
                .semesterId(1L)
                .capacity(30)
                .build();
        Class copiedClass = Class.builder()
                .id(2L)
                .section(1L)
                .courseId(100L)
                .semesterId(2L)
                .startDate(LocalDate.of(2025, 1, 10))
                .endDate(LocalDate.of(2025, 5, 10))
                .capacity(30)
                .build();
        ClassSchedule sourceSchedule = ClassSchedule.builder()
                .id(1L)
                .classId(1L)
                .day("Monday")
                .startTime(LocalTime.of(9, 0))
                .build();
        ClassSchedule copiedSchedule = ClassSchedule.builder()
                .id(2L)
                .classId(2L)
                .day("Monday")
                .startTime(LocalTime.of(9, 0))
                .build();
        ClassResponseDTO responseDTO = ClassResponseDTO.builder()
                .id(2L)
                .semesterId(2L)
                .startDate(LocalDate.of(2025, 1, 10))
                .endDate(LocalDate.of(2025, 5, 10))
                .build();

        SemesterResponseDTO targetSemester = SemesterResponseDTO.builder()
                .id(2L)
                .startDate(LocalDate.of(2025, 1, 10))
                .endDate(LocalDate.of(2025, 5, 10))
                .build();
        
        when(semesterService.getSemesterById(2L)).thenReturn(Mono.just(targetSemester));
        when(classRepository.findBySemesterId(1L)).thenReturn(Flux.just(sourceClass));
        when(classRepository.save(any(Class.class))).thenReturn(Mono.just(copiedClass));
        when(classScheduleRepository.findByClassId(1L)).thenReturn(Flux.just(sourceSchedule));
        when(classScheduleRepository.save(any(ClassSchedule.class))).thenReturn(Mono.just(copiedSchedule));
        when(classScheduleRepository.findByClassId(2L)).thenReturn(Flux.just(copiedSchedule));
        
        // Add stubs for all map operations
        doNothing().when(modelMapper).map(any(Class.class), any(Class.class));
        doNothing().when(modelMapper).map(any(ClassSchedule.class), any(ClassSchedule.class));
        doReturn(responseDTO).when(modelMapper).map(any(Class.class), eq(ClassResponseDTO.class));
        doReturn(testScheduleResponseDTO).when(modelMapper).map(any(ClassSchedule.class), eq(ClassScheduleResponseDTO.class));
        
        List<ClassResponseDTO> result = planningService.duplicateSemesterPlanning(1L, 2L).collectList().block();
        
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(semesterService).getSemesterById(2L);
        verify(classRepository).findBySemesterId(1L);
        verify(classRepository, atLeastOnce()).save(any(Class.class));
        verify(classScheduleRepository, atLeastOnce()).save(any(ClassSchedule.class));
    }

    @Test
    @DisplayName("duplicateSemesterPlanning - When source has no schedules copies only class")
    void testDuplicateSemesterPlanning_NoSchedules_CopiesOnlyClass() {
        setupSecurityContext("ROLE_ADMIN");
        Class sourceClass = Class.builder()
                .id(1L)
                .section(1L)
                .courseId(100L)
                .semesterId(1L)
                .capacity(30)
                .build();

        Class copiedClass = Class.builder()
                .id(2L)
                .section(1L)
                .courseId(100L)
                .semesterId(2L)
                .startDate(LocalDate.of(2025, 1, 10))
                .endDate(LocalDate.of(2025, 5, 10))
                .capacity(30)
                .build();

        ClassResponseDTO responseDTO = ClassResponseDTO.builder()
                .id(2L)
                .semesterId(2L)
                .startDate(LocalDate.of(2025, 1, 10))
                .endDate(LocalDate.of(2025, 5, 10))
                .build();

        SemesterResponseDTO targetSemester = SemesterResponseDTO.builder()
                .id(2L)
                .startDate(LocalDate.of(2025, 1, 10))
                .endDate(LocalDate.of(2025, 5, 10))
                .build();

        when(semesterService.getSemesterById(2L)).thenReturn(Mono.just(targetSemester));
        when(classRepository.findBySemesterId(1L)).thenReturn(Flux.just(sourceClass));
        when(classRepository.save(any(Class.class))).thenReturn(Mono.just(copiedClass));
        when(classScheduleRepository.findByClassId(1L)).thenReturn(Flux.empty());
        when(classScheduleRepository.findByClassId(2L)).thenReturn(Flux.empty());

        doNothing().when(modelMapper).map(any(Class.class), any(Class.class));
        doReturn(responseDTO).when(modelMapper).map(any(Class.class), eq(ClassResponseDTO.class));

        List<ClassResponseDTO> result = planningService.duplicateSemesterPlanning(1L, 2L).collectList().block();

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(semesterService).getSemesterById(2L);
        verify(classRepository).findBySemesterId(1L);
        verify(classRepository).save(any(Class.class));
        verify(classScheduleRepository, never()).save(any(ClassSchedule.class));
    }

    @Test
    @DisplayName("duplicateSemesterPlanning - When there are no classes returns empty list and does nothing")
    void testDuplicateSemesterPlanning_NoClasses_ReturnsEmptyList() {
        SemesterResponseDTO targetSemester = SemesterResponseDTO.builder()
                .id(2L)
                .startDate(LocalDate.of(2025, 1, 10))
                .endDate(LocalDate.of(2025, 5, 10))
                .build();

        when(semesterService.getSemesterById(2L)).thenReturn(Mono.just(targetSemester));
        when(classRepository.findBySemesterId(1L)).thenReturn(Flux.empty());

        List<ClassResponseDTO> result = planningService.duplicateSemesterPlanning(1L, 2L).collectList().block();

        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(classRepository).findBySemesterId(1L);
        verify(classRepository, never()).save(any(Class.class));
        verify(classScheduleRepository, never()).save(any(ClassSchedule.class));
    }

    @Test
    @DisplayName("duplicateSemesterPlanning - When semester not found error occurs throws PlanningServerErrorException")
    void testDuplicateSemesterPlanning_ErrorOccurs_ThrowsException() {
        when(semesterService.getSemesterById(2L)).thenReturn(Mono.error(new RuntimeException("Semester not found")));

        Mono<List<ClassResponseDTO>> result = planningService.duplicateSemesterPlanning(1L, 2L).collectList();

        assertThrows(PlanningServerErrorException.class, result::block);
        verify(semesterService).getSemesterById(2L);
    }

    @ParameterizedTest(name = "applySemesterPlanningToCurrent - Should only apply classes for own section {0}")
    @MethodSource("userSectionProvider")
    @DisplayName("applySemesterPlanningToCurrent - Should apply based on user section")
    void testApplySemesterPlanningToCurrent_BasedOnUserSection_AppliesSuccessfully(Long userSection) {
        setupSecurityContext("ROLE_USER");
        List<Class> classes = setUpUserMocking(userSection);

        Class sourceClass = classes.get((int)(userSection - 1));
        Class copiedClass = Class.builder()
                .id(2L)
                .section(userSection)
                .courseId(userSection)
                .semesterId(2L)
                .startDate(LocalDate.of(2025, 1, 10))
                .endDate(LocalDate.of(2025, 5, 10))
                .capacity(30)
                .build();

        ClassResponseDTO responseDTO = ClassResponseDTO.builder()
                .id(2L)
                .semesterId(2L)
                .startDate(LocalDate.of(2025, 1, 10))
                .endDate(LocalDate.of(2025, 5, 10))
                .build();

        SemesterResponseDTO targetSemester = SemesterResponseDTO.builder()
                .id(2L)
                .startDate(LocalDate.of(2025, 1, 10))
                .endDate(LocalDate.of(2025, 5, 10))
                .build();

        when(semesterService.getCurrentSemesterId()).thenReturn(Mono.just(2L));
        when(semesterService.getSemesterById(2L)).thenReturn(Mono.just(targetSemester));
        when(classRepository.findBySemesterId(1L)).thenReturn(Flux.fromIterable(classes));
        when(classRepository.save(any(Class.class))).thenReturn(Mono.just(copiedClass));
        when(classScheduleRepository.findByClassId(sourceClass.getId())).thenReturn(Flux.empty());
        when(classScheduleRepository.findByClassId(2L)).thenReturn(Flux.empty());

        doNothing().when(modelMapper).map(any(Class.class), any(Class.class));
        doReturn(responseDTO).when(modelMapper).map(any(Class.class), eq(ClassResponseDTO.class));

        List<ClassResponseDTO> result = planningService.applySemesterPlanningToCurrent(1L).collectList().block();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(2L, result.get(0).getSemesterId());
        verify(semesterService).getCurrentSemesterId();
        verify(semesterService).getSemesterById(2L);
        verify(classRepository).findBySemesterId(1L);
    }

    @Test
    @DisplayName("applySemesterPlanningToCurrent - Should apply planning to current semester")
    void testApplySemesterPlanningToCurrent_AppliesSuccessfully() {
        setupSecurityContext("ROLE_ADMIN");
        Class sourceClass = Class.builder()
                .id(1L)
                .section(1L)
                .courseId(100L)
                .semesterId(1L)
                .capacity(30)
                .build();

        Class copiedClass = Class.builder()
                .id(2L)
                .section(1L)
                .courseId(100L)
                .semesterId(2L)
                .startDate(LocalDate.of(2025, 1, 10))
                .endDate(LocalDate.of(2025, 5, 10))
                .capacity(30)
                .build();

        ClassResponseDTO responseDTO = ClassResponseDTO.builder()
                .id(2L)
                .semesterId(2L)
                .startDate(LocalDate.of(2025, 1, 10))
                .endDate(LocalDate.of(2025, 5, 10))
                .build();

        SemesterResponseDTO targetSemester = SemesterResponseDTO.builder()
                .id(2L)
                .startDate(LocalDate.of(2025, 1, 10))
                .endDate(LocalDate.of(2025, 5, 10))
                .build();

        when(semesterService.getCurrentSemesterId()).thenReturn(Mono.just(2L));
        when(semesterService.getSemesterById(2L)).thenReturn(Mono.just(targetSemester));
        when(classRepository.findBySemesterId(1L)).thenReturn(Flux.just(sourceClass));
        when(classRepository.save(any(Class.class))).thenReturn(Mono.just(copiedClass));
        when(classScheduleRepository.findByClassId(1L)).thenReturn(Flux.empty());
        when(classScheduleRepository.findByClassId(2L)).thenReturn(Flux.empty());

        doNothing().when(modelMapper).map(any(Class.class), any(Class.class));
        doReturn(responseDTO).when(modelMapper).map(any(Class.class), eq(ClassResponseDTO.class));

        List<ClassResponseDTO> result = planningService.applySemesterPlanningToCurrent(1L).collectList().block();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(2L, result.get(0).getSemesterId());
        verify(semesterService).getCurrentSemesterId();
        verify(semesterService).getSemesterById(2L);
        verify(classRepository).findBySemesterId(1L);
    }

    @Test
    @DisplayName("applySemesterPlanningToCurrent - When no classes in source semester returns empty list")
    void testApplySemesterPlanningToCurrent_NoClasses_ReturnsEmptyList() {
        SemesterResponseDTO targetSemester = SemesterResponseDTO.builder()
                .id(2L)
                .startDate(LocalDate.of(2025, 1, 10))
                .endDate(LocalDate.of(2025, 5, 10))
                .build();

        when(semesterService.getCurrentSemesterId()).thenReturn(Mono.just(2L));
        when(semesterService.getSemesterById(2L)).thenReturn(Mono.just(targetSemester));
        when(classRepository.findBySemesterId(1L)).thenReturn(Flux.empty());

        List<ClassResponseDTO> result = planningService.applySemesterPlanningToCurrent(1L).collectList().block();

        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(semesterService).getCurrentSemesterId();
        verify(semesterService).getSemesterById(2L);
        verify(classRepository).findBySemesterId(1L);
    }

    @Test
    @DisplayName("applySemesterPlanningToCurrent - When error occurs throws PlanningServerErrorException")
    void testApplySemesterPlanningToCurrent_ErrorOccurs_ThrowsException() {
        when(semesterService.getCurrentSemesterId()).thenReturn(Mono.just(2L));
        when(semesterService.getSemesterById(2L)).thenReturn(Mono.error(new RuntimeException("Semester not found")));

        Mono<List<ClassResponseDTO>> result = planningService.applySemesterPlanningToCurrent(1L).collectList();

        assertThrows(PlanningServerErrorException.class, result::block);
        verify(semesterService).getCurrentSemesterId();
        verify(semesterService).getSemesterById(2L);
    }

    // ==================== PATCH CLASS SCHEDULE ADVANCED TESTS ====================

    @Test
    @DisplayName("patchClassSchedule - Should update startTime with String value")
    void testPatchClassSchedule_UpdatesStartTimeWithString() {
        setupSecurityContext("ROLE_ADMIN");
        ClassSchedule existingSchedule = ClassSchedule.builder()
                .id(1L)
                .classId(1L)
                .startTime(LocalTime.of(9, 0))
                .build();

        Map<String, Object> updates = new HashMap<>();
        updates.put("startTime", "10:30");

        Class test = Class.builder()
                .id(1L)
                .section(1L)
                .courseId(100L)
                .build();
        when(classRepository.findById(1L)).thenReturn(Mono.just(test));
        when(classScheduleRepository.findById(1L)).thenReturn(Mono.just(existingSchedule));
        when(classScheduleRepository.save(any(ClassSchedule.class))).thenAnswer(invocation -> {
            ClassSchedule saved = invocation.getArgument(0);
            return Mono.just(saved);
        });
        when(modelMapper.map(any(ClassSchedule.class), eq(ClassScheduleResponseDTO.class)))
                .thenReturn(testScheduleResponseDTO);

        ClassScheduleResponseDTO result = planningService.patchClassSchedule(1L, updates).block();

        assertNotNull(result);
        verify(classScheduleRepository).save(argThat(schedule ->
                schedule.getStartTime().equals(LocalTime.of(10, 30))
        ));
        verify(classRepository).findById(1L);
    }

    @Test
    @DisplayName("patchClassSchedule - Should update endTime with LocalTime value")
    void testPatchClassSchedule_UpdatesEndTimeWithLocalTime() {
        setupSecurityContext("ROLE_ADMIN");
        ClassSchedule existingSchedule = ClassSchedule.builder()
                .id(1L)
                .classId(1L)
                .endTime(LocalTime.of(11, 0))
                .build();

        Map<String, Object> updates = new HashMap<>();
        updates.put("endTime", LocalTime.of(12, 30));

        Class test = Class.builder()
                .id(1L)
                .section(1L)
                .courseId(100L)
                .build();
        when(classRepository.findById(1L)).thenReturn(Mono.just(test));
        when(classScheduleRepository.findById(1L)).thenReturn(Mono.just(existingSchedule));
        when(classScheduleRepository.save(any(ClassSchedule.class))).thenAnswer(invocation -> {
            ClassSchedule saved = invocation.getArgument(0);
            return Mono.just(saved);
        });
        when(modelMapper.map(any(ClassSchedule.class), eq(ClassScheduleResponseDTO.class)))
                .thenReturn(testScheduleResponseDTO);

        ClassScheduleResponseDTO result = planningService.patchClassSchedule(1L, updates).block();

        assertNotNull(result);
        verify(classScheduleRepository).save(argThat(schedule ->
                schedule.getEndTime().equals(LocalTime.of(12, 30))
        ));
        verify(classRepository).findById(1L);
    }

    @Test
    @DisplayName("patchClassSchedule - Should update multiple fields")
    void testPatchClassSchedule_UpdatesMultipleFields() {
        setupSecurityContext("ROLE_ADMIN");
        ClassSchedule existingSchedule = ClassSchedule.builder()
                .id(1L)
                .classId(1L)
                .day("Monday")
                .classroomId(10L)
                .modalityId(1L)
                .disability(false)
                .build();

        Map<String, Object> updates = new HashMap<>();
        updates.put("day", "Wednesday");
        updates.put("classroomId", 20L);
        updates.put("modalityId", 2L);
        updates.put("disability", true);

        Class test = Class.builder()
                .id(1L)
                .section(1L)
                .courseId(100L)
                .build();
        when(classRepository.findById(1L)).thenReturn(Mono.just(test));
        when(classScheduleRepository.findById(1L)).thenReturn(Mono.just(existingSchedule));
        when(classScheduleRepository.save(any(ClassSchedule.class))).thenAnswer(invocation -> {
            ClassSchedule saved = invocation.getArgument(0);
            return Mono.just(saved);
        });
        when(modelMapper.map(any(ClassSchedule.class), eq(ClassScheduleResponseDTO.class)))
                .thenReturn(testScheduleResponseDTO);

        ClassScheduleResponseDTO result = planningService.patchClassSchedule(1L, updates).block();

        assertNotNull(result);
        verify(classScheduleRepository).save(argThat(schedule ->
                schedule.getDay().equals("Wednesday") &&
                        schedule.getClassroomId().equals(20L) &&
                        schedule.getModalityId().equals(2L) &&
                        schedule.getDisability().equals(true)
        ));
        verify(classRepository).findById(1L);
    }

    // ==================== ADDITIONAL TESTS FOR COVERAGE ====================
    @Test
    @DisplayName("createClass - When semester service fails propagates error")
    void testCreateClass_SemesterServiceFails_PropagatesError() {
        when(semesterService.getCurrentSemesterId()).thenReturn(Mono.error(new RuntimeException("Semester service error")));

        Mono<ClassResponseDTO> result = planningService.createClass(testClassCreateRequestDTO);

        StepVerifier.create(result)
                .expectError(ClassCreationException.class)
                .verify();

        verify(semesterService).getCurrentSemesterId();
        verify(classRepository, never()).save(any());
    }

    @Test
    @DisplayName("findCurrentSemesterClasses - When semester service fails returns empty flux")
    void testFindCurrentSemesterClasses_SemesterServiceFails_ReturnsEmpty() {
        when(semesterService.getCurrentSemesterId()).thenReturn(Mono.error(new RuntimeException("Semester error")));

        Flux<ClassResponseDTO> result = planningService.findCurrentSemesterClasses();

        StepVerifier.create(result)
                .expectError()
                .verify();

        verify(semesterService).getCurrentSemesterId();
    }

    @Test
    @DisplayName("duplicateSemesterPlanning - When source semester has no classes returns empty flux")
    void testDuplicateSemesterPlanning_NoClasses_ReturnsEmpty() {
        SemesterResponseDTO targetSemester = SemesterResponseDTO.builder()
                .id(2L)
                .startDate(LocalDate.of(2025, 1, 10))
                .endDate(LocalDate.of(2025, 5, 10))
                .build();

        when(semesterService.getSemesterById(2L)).thenReturn(Mono.just(targetSemester));
        when(classRepository.findBySemesterId(1L)).thenReturn(Flux.empty());

        List<ClassResponseDTO> result = planningService.duplicateSemesterPlanning(1L, 2L).collectList().block();

        assertNotNull(result);
        assertEquals(0, result.size());
        verify(semesterService).getSemesterById(2L);
        verify(classRepository).findBySemesterId(1L);
        verify(classRepository, never()).save(any());
    }

    @Test
    @DisplayName("getClassSchedulesForClass - When class has multiple schedules returns all")
    void testGetClassSchedulesForClass_MultipleSchedules_ReturnsAll() {
        setupSecurityContext("ROLE_ADMIN");
        ClassSchedule schedule1 = ClassSchedule.builder()
                .id(1L)
                .classId(1L)
                .day("Monday")
                .build();

        ClassSchedule schedule2 = ClassSchedule.builder()
                .id(2L)
                .classId(1L)
                .day("Wednesday")
                .build();

        ClassScheduleResponseDTO dto1 = ClassScheduleResponseDTO.builder()
                .id(1L)
                .day("Monday")
                .build();

        ClassScheduleResponseDTO dto2 = ClassScheduleResponseDTO.builder()
                .id(2L)
                .day("Wednesday")
                .build();

        when(classRepository.findById(1L)).thenReturn(Mono.just(testClass));
        when(classScheduleRepository.findByClassId(1L)).thenReturn(Flux.just(schedule1, schedule2));
        when(modelMapper.map(testClass, ClassResponseDTO.class)).thenReturn(testClassResponseDTO);
        when(modelMapper.map(schedule1, ClassScheduleResponseDTO.class)).thenReturn(dto1);
        when(modelMapper.map(schedule2, ClassScheduleResponseDTO.class)).thenReturn(dto2);

        ClassResponseDTO result = planningService.findClassById(1L).block();

        assertNotNull(result);
        assertNotNull(result.getSchedules());
        assertEquals(2, result.getSchedules().size());
        verify(classScheduleRepository).findByClassId(1L);
    }

    // ==================== SECURITY CONTEXT ====================
    private void setupSecurityContext(String role) {
        List<SimpleGrantedAuthority> authorities = Collections.singletonList(new SimpleGrantedAuthority(role));
        Authentication authentication = new UsernamePasswordAuthenticationToken("testUser@example.com", "password", authorities);

        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);

        mockedReactiveSecurityContextHolder = mockStatic(ReactiveSecurityContextHolder.class);
        ((MockedStatic<?>) mockedReactiveSecurityContextHolder).when(ReactiveSecurityContextHolder::getContext)
                .thenReturn(Mono.just(securityContext));
    }

    private List<Class> setUpUserMocking(Long userSection) {
        Class class1 = Class.builder().id(1L).section(1L).courseId(1L).build();
        Class class2 = Class.builder().id(2L).section(2L).courseId(2L).build();
        Class class3 = Class.builder().id(3L).section(3L).courseId(3L).build();

        when(userService.getUserIdByEmail(anyString())).thenReturn(Mono.just(1L));
        when(sectionService.getSectionIdByUserId(1L)).thenReturn(Mono.just(userSection));
        
        when(courseService.getCourseSectionId(anyLong())).thenAnswer(invocation -> {
            Long classId = invocation.getArgument(0);
            if (classId.equals(1L)) return Mono.just(1L);
            else if (classId.equals(2L)) return Mono.just(2L);
            else if (classId.equals(3L)) return Mono.just(3L);
            else return Mono.empty();
        });

        return Arrays.asList(class1, class2, class3);
    }

    // ==================== DUPLICATE CLASS PLANNING TESTS ====================

    @Test
    @DisplayName("duplicateClassPlanning - Should duplicate multiple classes with schedules to current semester")
    void testDuplicateClassPlanning_MultipleClasses_DuplicatesSuccessfully() {
        setupSecurityContext("ROLE_ADMIN");
        
        // Given
        List<Long> sourceClassIds = Arrays.asList(1L, 2L);
        
        SemesterResponseDTO currentSemester = SemesterResponseDTO.builder()
                .id(3L)
                .startDate(LocalDate.of(2025, 8, 1))
                .endDate(LocalDate.of(2025, 12, 15))
                .build();
        
        Class sourceClass1 = Class.builder()
                .id(1L)
                .section(1L)
                .courseId(100L)
                .semesterId(1L)
                .capacity(30)
                .build();
                
        Class sourceClass2 = Class.builder()
                .id(2L)
                .section(2L)
                .courseId(101L)
                .semesterId(1L)
                .capacity(25)
                .build();
        
        Class copiedClass1 = Class.builder()
                .id(10L)
                .section(1L)
                .courseId(100L)
                .semesterId(3L)
                .startDate(LocalDate.of(2025, 8, 1))
                .endDate(LocalDate.of(2025, 12, 15))
                .capacity(30)
                .build();
                
        Class copiedClass2 = Class.builder()
                .id(11L)
                .section(2L)
                .courseId(101L)
                .semesterId(3L)
                .startDate(LocalDate.of(2025, 8, 1))
                .endDate(LocalDate.of(2025, 12, 15))
                .capacity(25)
                .build();
        
        ClassSchedule schedule1 = ClassSchedule.builder()
                .id(1L)
                .classId(1L)
                .day("Monday")
                .startTime(LocalTime.of(9, 0))
                .build();
                
        ClassSchedule schedule2 = ClassSchedule.builder()
                .id(2L)
                .classId(2L)
                .day("Tuesday")
                .startTime(LocalTime.of(10, 0))
                .build();
        
        ClassSchedule copiedSchedule1 = ClassSchedule.builder()
                .id(20L)
                .classId(10L)
                .day("Monday")
                .startTime(LocalTime.of(9, 0))
                .build();
                
        ClassSchedule copiedSchedule2 = ClassSchedule.builder()
                .id(21L)
                .classId(11L)
                .day("Tuesday")
                .startTime(LocalTime.of(10, 0))
                .build();
        
        ClassResponseDTO responseDTO1 = ClassResponseDTO.builder()
                .id(10L)
                .semesterId(3L)
                .build();
                
        ClassResponseDTO responseDTO2 = ClassResponseDTO.builder()
                .id(11L)
                .semesterId(3L)
                .build();
        
        // When
        when(semesterService.getCurrentSemester()).thenReturn(Mono.just(currentSemester));
        when(classRepository.findById(1L)).thenReturn(Mono.just(sourceClass1));
        when(classRepository.findById(2L)).thenReturn(Mono.just(sourceClass2));
        when(classRepository.save(any(Class.class)))
                .thenReturn(Mono.just(copiedClass1))
                .thenReturn(Mono.just(copiedClass2));
        when(classScheduleRepository.findByClassId(1L)).thenReturn(Flux.just(schedule1));
        when(classScheduleRepository.findByClassId(2L)).thenReturn(Flux.just(schedule2));
        when(classScheduleRepository.findByClassId(10L)).thenReturn(Flux.just(copiedSchedule1));
        when(classScheduleRepository.findByClassId(11L)).thenReturn(Flux.just(copiedSchedule2));
        when(classScheduleRepository.save(any(ClassSchedule.class)))
                .thenReturn(Mono.just(copiedSchedule1))
                .thenReturn(Mono.just(copiedSchedule2));
        
        doNothing().when(modelMapper).map(any(Class.class), any(Class.class));
        doNothing().when(modelMapper).map(any(ClassSchedule.class), any(ClassSchedule.class));
        doReturn(responseDTO1, responseDTO2).when(modelMapper).map(any(Class.class), eq(ClassResponseDTO.class));
        doReturn(testScheduleResponseDTO).when(modelMapper).map(any(ClassSchedule.class), eq(ClassScheduleResponseDTO.class));
        
        // Then
        List<ClassResponseDTO> result = planningService.duplicateClassPlanning(sourceClassIds).collectList().block();
        
        assertNotNull(result);
        assertEquals(2, result.size());
        verify(semesterService).getCurrentSemester();
        verify(classRepository).findById(1L);
        verify(classRepository).findById(2L);
        verify(classRepository, times(2)).save(any(Class.class));
        verify(classScheduleRepository, times(2)).save(any(ClassSchedule.class));
    }

    @Test
    @DisplayName("duplicateClassPlanning - Should duplicate single class with multiple schedules")
    void testDuplicateClassPlanning_SingleClassMultipleSchedules_DuplicatesSuccessfully() {
        setupSecurityContext("ROLE_ADMIN");
        
        // Given
        List<Long> sourceClassIds = Arrays.asList(1L);
        
        SemesterResponseDTO currentSemester = SemesterResponseDTO.builder()
                .id(2L)
                .startDate(LocalDate.of(2025, 1, 15))
                .endDate(LocalDate.of(2025, 5, 30))
                .build();
        
        Class sourceClass = Class.builder()
                .id(1L)
                .section(1L)
                .courseId(100L)
                .semesterId(1L)
                .capacity(30)
                .build();
        
        Class copiedClass = Class.builder()
                .id(5L)
                .section(1L)
                .courseId(100L)
                .semesterId(2L)
                .startDate(LocalDate.of(2025, 1, 15))
                .endDate(LocalDate.of(2025, 5, 30))
                .capacity(30)
                .build();
        
        ClassSchedule schedule1 = ClassSchedule.builder()
                .id(1L)
                .classId(1L)
                .day("Monday")
                .startTime(LocalTime.of(9, 0))
                .build();
                
        ClassSchedule schedule2 = ClassSchedule.builder()
                .id(2L)
                .classId(1L)
                .day("Wednesday")
                .startTime(LocalTime.of(11, 0))
                .build();
        
        ClassSchedule copiedSchedule1 = ClassSchedule.builder()
                .id(10L)
                .classId(5L)
                .day("Monday")
                .startTime(LocalTime.of(9, 0))
                .build();
                
        ClassSchedule copiedSchedule2 = ClassSchedule.builder()
                .id(11L)
                .classId(5L)
                .day("Wednesday")
                .startTime(LocalTime.of(11, 0))
                .build();
        
        ClassResponseDTO responseDTO = ClassResponseDTO.builder()
                .id(5L)
                .semesterId(2L)
                .build();
        
        // When
        when(semesterService.getCurrentSemester()).thenReturn(Mono.just(currentSemester));
        when(classRepository.findById(1L)).thenReturn(Mono.just(sourceClass));
        when(classRepository.save(any(Class.class))).thenReturn(Mono.just(copiedClass));
        when(classScheduleRepository.findByClassId(1L)).thenReturn(Flux.just(schedule1, schedule2));
        when(classScheduleRepository.findByClassId(5L)).thenReturn(Flux.just(copiedSchedule1, copiedSchedule2));
        when(classScheduleRepository.save(any(ClassSchedule.class)))
                .thenReturn(Mono.just(copiedSchedule1))
                .thenReturn(Mono.just(copiedSchedule2));
        
        doNothing().when(modelMapper).map(any(Class.class), any(Class.class));
        doNothing().when(modelMapper).map(any(ClassSchedule.class), any(ClassSchedule.class));
        doReturn(responseDTO).when(modelMapper).map(any(Class.class), eq(ClassResponseDTO.class));
        doReturn(testScheduleResponseDTO).when(modelMapper).map(any(ClassSchedule.class), eq(ClassScheduleResponseDTO.class));
        
        // Then
        List<ClassResponseDTO> result = planningService.duplicateClassPlanning(sourceClassIds).collectList().block();
        
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(semesterService).getCurrentSemester();
        verify(classRepository).findById(1L);
        verify(classRepository).save(any(Class.class));
        verify(classScheduleRepository, times(2)).save(any(ClassSchedule.class));
    }

    @Test
    @DisplayName("duplicateClassPlanning - When class has no schedules copies only class")
    void testDuplicateClassPlanning_NoSchedules_CopiesOnlyClass() {
        setupSecurityContext("ROLE_ADMIN");
        
        // Given
        List<Long> sourceClassIds = Arrays.asList(1L);
        
        SemesterResponseDTO currentSemester = SemesterResponseDTO.builder()
                .id(2L)
                .startDate(LocalDate.of(2025, 1, 15))
                .endDate(LocalDate.of(2025, 5, 30))
                .build();
        
        Class sourceClass = Class.builder()
                .id(1L)
                .section(1L)
                .courseId(100L)
                .semesterId(1L)
                .capacity(30)
                .build();
        
        Class copiedClass = Class.builder()
                .id(5L)
                .section(1L)
                .courseId(100L)
                .semesterId(2L)
                .startDate(LocalDate.of(2025, 1, 15))
                .endDate(LocalDate.of(2025, 5, 30))
                .capacity(30)
                .build();
        
        ClassResponseDTO responseDTO = ClassResponseDTO.builder()
                .id(5L)
                .semesterId(2L)
                .build();
        
        // When
        when(semesterService.getCurrentSemester()).thenReturn(Mono.just(currentSemester));
        when(classRepository.findById(1L)).thenReturn(Mono.just(sourceClass));
        when(classRepository.save(any(Class.class))).thenReturn(Mono.just(copiedClass));
        when(classScheduleRepository.findByClassId(1L)).thenReturn(Flux.empty());
        when(classScheduleRepository.findByClassId(5L)).thenReturn(Flux.empty());
        
        doNothing().when(modelMapper).map(any(Class.class), any(Class.class));
        doReturn(responseDTO).when(modelMapper).map(any(Class.class), eq(ClassResponseDTO.class));
        
        // Then
        List<ClassResponseDTO> result = planningService.duplicateClassPlanning(sourceClassIds).collectList().block();
        
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(5L, result.get(0).getId());
        assertEquals(2L, result.get(0).getSemesterId());
        verify(semesterService).getCurrentSemester();
        verify(classRepository).findById(1L);
        verify(classRepository).save(any(Class.class));
        verify(classScheduleRepository, never()).save(any(ClassSchedule.class));
    }

    @Test
    @DisplayName("duplicateClassPlanning - When class not found throws ClassNotFoundException")
    void testDuplicateClassPlanning_ClassNotFound_ThrowsException() {
        // Given
        List<Long> sourceClassIds = Arrays.asList(999L);
        
        SemesterResponseDTO currentSemester = SemesterResponseDTO.builder()
                .id(2L)
                .startDate(LocalDate.of(2025, 1, 15))
                .endDate(LocalDate.of(2025, 5, 30))
                .build();
        
        // When
        when(semesterService.getCurrentSemester()).thenReturn(Mono.just(currentSemester));
        when(classRepository.findById(999L)).thenReturn(Mono.empty());
        
        // Then
        StepVerifier.create(planningService.duplicateClassPlanning(sourceClassIds))
                .expectError(ClassNotFoundException.class)
                .verify();
        
        verify(semesterService).getCurrentSemester();
        verify(classRepository).findById(999L);
        verify(classRepository, never()).save(any(Class.class));
    }

    @Test
    @DisplayName("duplicateClassPlanning - When empty list provided returns empty flux")
    void testDuplicateClassPlanning_EmptyList_ReturnsEmpty() {
        // Given
        List<Long> sourceClassIds = Collections.emptyList();
        
        SemesterResponseDTO currentSemester = SemesterResponseDTO.builder()
                .id(2L)
                .startDate(LocalDate.of(2025, 1, 15))
                .endDate(LocalDate.of(2025, 5, 30))
                .build();
        
        // When
        when(semesterService.getCurrentSemester()).thenReturn(Mono.just(currentSemester));
        
        // Then
        List<ClassResponseDTO> result = planningService.duplicateClassPlanning(sourceClassIds).collectList().block();
        
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(semesterService).getCurrentSemester();
        verify(classRepository, never()).findById(anyLong());
    }

    @Test
    @DisplayName("duplicateClassPlanning - Should filter classes by user section and throw ClassNotFoundException for inaccessible classes")
    void testDuplicateClassPlanning_FiltersByUserSection_OnlyDuplicatesAccessibleClasses() {
        setupSecurityContext("ROLE_SECTION");
        
        // Given
        List<Long> sourceClassIds = Arrays.asList(1L, 2L);
        Long userSection = 1L;
        
        SemesterResponseDTO currentSemester = SemesterResponseDTO.builder()
                .id(2L)
                .startDate(LocalDate.of(2025, 1, 15))
                .endDate(LocalDate.of(2025, 5, 30))
                .build();
        
        Class sourceClass1 = Class.builder()
                .id(1L)
                .section(1L)
                .courseId(100L)
                .semesterId(1L)
                .capacity(30)
                .build();
                
        Class sourceClass2 = Class.builder()
                .id(2L)
                .section(2L) // Different section
                .courseId(101L)
                .semesterId(1L)
                .capacity(25)
                .build();
        
        Class copiedClass1 = Class.builder()
                .id(10L)
                .section(1L)
                .courseId(100L)
                .semesterId(2L)
                .startDate(LocalDate.of(2025, 1, 15))
                .endDate(LocalDate.of(2025, 5, 30))
                .capacity(30)
                .build();
        
        ClassResponseDTO responseDTO1 = ClassResponseDTO.builder()
                .id(10L)
                .semesterId(2L)
                .build();
        
        // When
        when(semesterService.getCurrentSemester()).thenReturn(Mono.just(currentSemester));
        when(classRepository.findById(1L)).thenReturn(Mono.just(sourceClass1));
        when(classRepository.findById(2L)).thenReturn(Mono.just(sourceClass2));
        when(classRepository.save(any(Class.class))).thenReturn(Mono.just(copiedClass1));
        when(classScheduleRepository.findByClassId(1L)).thenReturn(Flux.empty());
        when(classScheduleRepository.findByClassId(10L)).thenReturn(Flux.empty());
        
        when(userService.getUserIdByEmail(anyString())).thenReturn(Mono.just(1L));
        when(sectionService.getSectionIdByUserId(1L)).thenReturn(Mono.just(userSection));
        when(courseService.getCourseSectionId(100L)).thenReturn(Mono.just(1L));
        when(courseService.getCourseSectionId(101L)).thenReturn(Mono.just(2L));
        
        doNothing().when(modelMapper).map(any(Class.class), any(Class.class));
        doReturn(responseDTO1).when(modelMapper).map(any(Class.class), eq(ClassResponseDTO.class));
        
        // Then
        Flux<ClassResponseDTO> result = planningService.duplicateClassPlanning(sourceClassIds);

        assertThrows(ClassNotFoundException.class, result::blockLast);
    }

    @Test
    @DisplayName("duplicateClassPlanning - When semester service fails throws PlanningServerErrorException")
    void testDuplicateClassPlanning_SemesterServiceFails_ThrowsException() {
        // Given
        List<Long> sourceClassIds = Arrays.asList(1L);
        
        // When
        when(semesterService.getCurrentSemester()).thenReturn(Mono.error(new RuntimeException("Database error")));
        
        // Then
        StepVerifier.create(planningService.duplicateClassPlanning(sourceClassIds))
                .expectError(PlanningServerErrorException.class)
                .verify();
        
        verify(semesterService).getCurrentSemester();
        verify(classRepository, never()).findById(anyLong());
    }

    // ==================== IS CLASS IN SECTION TESTS ====================

    @Test
    @DisplayName("isClassInSection - When class belongs to section returns true")
    void testIsClassInSection_ClassBelongsToSection_ReturnsTrue() {
        Long classId = 1L;
        Long sectionId = 1L;

        when(courseService.getCourseSectionId(classId))
                .thenReturn(Mono.just(sectionId));

        Boolean result = planningService.isClassInSection(classId, sectionId).block();

        assertNotNull(result);
        assertTrue(result);
        verify(courseService).getCourseSectionId(classId);
    }

    @Test
    @DisplayName("isClassInSection - When class does not belong to section returns false")
    void testIsClassInSection_ClassDoesNotBelongToSection_ReturnsFalse() {
        Long classId = 1L;
        Long sectionId = 1L;
        Long differentSectionId = 2L;

        when(courseService.getCourseSectionId(classId))
                .thenReturn(Mono.just(differentSectionId));

        Boolean result = planningService.isClassInSection(classId, sectionId).block();

        assertNotNull(result);
        assertFalse(result);
        verify(courseService).getCourseSectionId(classId);
    }

    @Test
    @DisplayName("isClassInSection - When course service returns empty returns false")
    void testIsClassInSection_CourseServiceReturnsEmpty_ReturnsFalse() {
        Long classId = 999L;
        Long sectionId = 1L;

        when(courseService.getCourseSectionId(classId))
                .thenReturn(Mono.empty());

        Boolean result = planningService.isClassInSection(classId, sectionId).block();

        assertNull(result);
        verify(courseService).getCourseSectionId(classId);
    }

    // ==================== FIND CLASSES WITHOUT CLASSROOM ASSIGNED TESTS ====================

    @Test
    @DisplayName("findClassesWithoutClassroomAssigned - When classes without classroom exist returns them")
    void testFindClassesWithoutClassroomAssigned_ClassesExist_ReturnsClasses() {
        // Arrange
        Long currentSemesterId = 1L;
        Class classWithoutClassroom1 = Class.builder()
                .id(1L)
                .section(1L)
                .courseId(100L)
                .semesterId(currentSemesterId)
                .capacity(30)
                .build();
        
        Class classWithoutClassroom2 = Class.builder()
                .id(2L)
                .section(1L)
                .courseId(101L)
                .semesterId(currentSemesterId)
                .capacity(25)
                .build();

        ClassResponseDTO responseDTO1 = new ClassResponseDTO();
        responseDTO1.setId(1L);
        responseDTO1.setSchedules(Collections.emptyList());

        ClassResponseDTO responseDTO2 = new ClassResponseDTO();
        responseDTO2.setId(2L);
        responseDTO2.setSchedules(Collections.emptyList());

        setupSecurityContext("ROLE_ADMIN");

        when(semesterService.getCurrentSemesterId()).thenReturn(Mono.just(currentSemesterId));
        when(classRepository.findBySemesterIdAndNoClassroomAssigned(currentSemesterId))
                .thenReturn(Flux.just(classWithoutClassroom1, classWithoutClassroom2));
        when(classScheduleRepository.findByClassId(1L)).thenReturn(Flux.empty());
        when(classScheduleRepository.findByClassId(2L)).thenReturn(Flux.empty());
        when(modelMapper.map(classWithoutClassroom1, ClassResponseDTO.class)).thenReturn(responseDTO1);
        when(modelMapper.map(classWithoutClassroom2, ClassResponseDTO.class)).thenReturn(responseDTO2);

        // Act & Assert
        StepVerifier.create(planningService.findClassesWithoutClassroomAssigned())
                .expectNext(responseDTO1)
                .expectNext(responseDTO2)
                .verifyComplete();

        verify(semesterService).getCurrentSemesterId();
        verify(classRepository).findBySemesterIdAndNoClassroomAssigned(currentSemesterId);
    }

    @Test
    @DisplayName("findClassesWithoutClassroomAssigned - When no classes without classroom exist returns empty")
    void testFindClassesWithoutClassroomAssigned_NoClasses_ReturnsEmpty() {
        // Arrange
        Long currentSemesterId = 1L;

        when(semesterService.getCurrentSemesterId()).thenReturn(Mono.just(currentSemesterId));
        when(classRepository.findBySemesterIdAndNoClassroomAssigned(currentSemesterId))
                .thenReturn(Flux.empty());

        // Act & Assert
        StepVerifier.create(planningService.findClassesWithoutClassroomAssigned())
                .verifyComplete();

        verify(semesterService).getCurrentSemesterId();
        verify(classRepository).findBySemesterIdAndNoClassroomAssigned(currentSemesterId);
    }

    @Test
    @DisplayName("findClassesWithoutClassroomAssigned - When semester service fails returns error")
    void testFindClassesWithoutClassroomAssigned_SemesterServiceFails_ReturnsError() {
        when(semesterService.getCurrentSemesterId())
                .thenReturn(Mono.error(new RuntimeException("Semester service error")));

        // Act & Assert
        StepVerifier.create(planningService.findClassesWithoutClassroomAssigned())
                .expectError(PlanningServerErrorException.class)
                .verify();

        verify(semesterService).getCurrentSemesterId();
        verify(classRepository, never()).findBySemesterIdAndNoClassroomAssigned(anyLong());
    }

    @Test
    @DisplayName("findClassesWithoutClassroomAssigned - When user has ROLE_SECTION filters by section")
    void testFindClassesWithoutClassroomAssigned_RoleSection_FiltersClasses() {
        // Arrange
        Long currentSemesterId = 1L;
        Long userSectionId = 1L;
        Long courseSectionId = 1L;
        
        Class classInSection = Class.builder()
                .id(1L)
                .section(1L)
                .courseId(100L)
                .semesterId(currentSemesterId)
                .capacity(30)
                .build();

        ClassResponseDTO responseDTO = new ClassResponseDTO();
        responseDTO.setId(1L);
        responseDTO.setSchedules(Collections.emptyList());

        setupSecurityContext("ROLE_SECTION");

        when(semesterService.getCurrentSemesterId()).thenReturn(Mono.just(currentSemesterId));
        when(classRepository.findBySemesterIdAndNoClassroomAssigned(currentSemesterId))
                .thenReturn(Flux.just(classInSection));
        when(userService.getUserIdByEmail("testUser@example.com")).thenReturn(Mono.just(10L));
        when(sectionService.getSectionIdByUserId(10L)).thenReturn(Mono.just(userSectionId));
        when(courseService.getCourseSectionId(100L)).thenReturn(Mono.just(courseSectionId));
        when(classScheduleRepository.findByClassId(1L)).thenReturn(Flux.empty());
        when(modelMapper.map(classInSection, ClassResponseDTO.class)).thenReturn(responseDTO);

        // Act & Assert
        StepVerifier.create(planningService.findClassesWithoutClassroomAssigned())
                .expectNext(responseDTO)
                .verifyComplete();

        verify(semesterService).getCurrentSemesterId();
        verify(classRepository).findBySemesterIdAndNoClassroomAssigned(currentSemesterId);
        verify(userService).getUserIdByEmail("testUser@example.com");
        verify(sectionService).getSectionIdByUserId(10L);
        verify(courseService).getCourseSectionId(100L);
    }

    // ==================== FIND CLASSES WITHOUT TEACHER ASSIGNED TESTS ====================

    @Test
    @DisplayName("findClassesWithoutTeacherAssigned - When classes without teacher exist returns them")
    void testFindClassesWithoutTeacherAssigned_ClassesExist_ReturnsClasses() {
        // Arrange
        Long currentSemesterId = 1L;
        Class classWithoutTeacher1 = Class.builder()
                .id(1L)
                .section(1L)
                .courseId(100L)
                .semesterId(currentSemesterId)
                .capacity(30)
                .build();
        
        Class classWithoutTeacher2 = Class.builder()
                .id(2L)
                .section(1L)
                .courseId(101L)
                .semesterId(currentSemesterId)
                .capacity(25)
                .build();

        ClassResponseDTO responseDTO1 = new ClassResponseDTO();
        responseDTO1.setId(1L);
        responseDTO1.setSchedules(Collections.emptyList());

        ClassResponseDTO responseDTO2 = new ClassResponseDTO();
        responseDTO2.setId(2L);
        responseDTO2.setSchedules(Collections.emptyList());

        setupSecurityContext("ROLE_ADMIN");

        when(semesterService.getCurrentSemesterId()).thenReturn(Mono.just(currentSemesterId));
        when(classRepository.findBySemesterIdAndNoConfirmedTeacherAssigned(currentSemesterId))
                .thenReturn(Flux.just(classWithoutTeacher1, classWithoutTeacher2));
        when(classScheduleRepository.findByClassId(1L)).thenReturn(Flux.empty());
        when(classScheduleRepository.findByClassId(2L)).thenReturn(Flux.empty());
        when(modelMapper.map(classWithoutTeacher1, ClassResponseDTO.class)).thenReturn(responseDTO1);
        when(modelMapper.map(classWithoutTeacher2, ClassResponseDTO.class)).thenReturn(responseDTO2);

        // Act & Assert
        StepVerifier.create(planningService.findClassesWithoutTeacherAssigned())
                .expectNext(responseDTO1)
                .expectNext(responseDTO2)
                .verifyComplete();

        verify(semesterService).getCurrentSemesterId();
        verify(classRepository).findBySemesterIdAndNoConfirmedTeacherAssigned(currentSemesterId);
    }

    @Test
    @DisplayName("findClassesWithoutTeacherAssigned - When no classes without teacher exist returns empty")
    void testFindClassesWithoutTeacherAssigned_NoClasses_ReturnsEmpty() {
        // Arrange
        Long currentSemesterId = 1L;

        when(semesterService.getCurrentSemesterId()).thenReturn(Mono.just(currentSemesterId));
        when(classRepository.findBySemesterIdAndNoConfirmedTeacherAssigned(currentSemesterId))
                .thenReturn(Flux.empty());

        // Act & Assert
        StepVerifier.create(planningService.findClassesWithoutTeacherAssigned())
                .verifyComplete();

        verify(semesterService).getCurrentSemesterId();
        verify(classRepository).findBySemesterIdAndNoConfirmedTeacherAssigned(currentSemesterId);
    }

    @Test
    @DisplayName("findClassesWithoutTeacherAssigned - When semester service fails returns error")
    void testFindClassesWithoutTeacherAssigned_SemesterServiceFails_ReturnsError() {
        // Arrange
        when(semesterService.getCurrentSemesterId())
                .thenReturn(Mono.error(new RuntimeException("Semester service error")));

        // Act & Assert
        StepVerifier.create(planningService.findClassesWithoutTeacherAssigned())
                .expectError(PlanningServerErrorException.class)
                .verify();

        verify(semesterService).getCurrentSemesterId();
        verify(classRepository, never()).findBySemesterIdAndNoConfirmedTeacherAssigned(anyLong());
    }

    @Test
    @DisplayName("findClassesWithoutTeacherAssigned - When user has ROLE_SECTION filters by section")
    void testFindClassesWithoutTeacherAssigned_RoleSection_FiltersClasses() {
        // Arrange
        Long currentSemesterId = 1L;
        Long userSectionId = 1L;
        Long courseSectionId = 1L;
        
        Class classInSection = Class.builder()
                .id(1L)
                .section(1L)
                .courseId(100L)
                .semesterId(currentSemesterId)
                .capacity(30)
                .build();

        ClassResponseDTO responseDTO = new ClassResponseDTO();
        responseDTO.setId(1L);
        responseDTO.setSchedules(Collections.emptyList());

        setupSecurityContext("ROLE_SECTION");

        when(semesterService.getCurrentSemesterId()).thenReturn(Mono.just(currentSemesterId));
        when(classRepository.findBySemesterIdAndNoConfirmedTeacherAssigned(currentSemesterId))
                .thenReturn(Flux.just(classInSection));
        when(userService.getUserIdByEmail("testUser@example.com")).thenReturn(Mono.just(10L));
        when(sectionService.getSectionIdByUserId(10L)).thenReturn(Mono.just(userSectionId));
        when(courseService.getCourseSectionId(100L)).thenReturn(Mono.just(courseSectionId));
        when(classScheduleRepository.findByClassId(1L)).thenReturn(Flux.empty());
        when(modelMapper.map(classInSection, ClassResponseDTO.class)).thenReturn(responseDTO);

        // Act & Assert
        StepVerifier.create(planningService.findClassesWithoutTeacherAssigned())
                .expectNext(responseDTO)
                .verifyComplete();

        verify(semesterService).getCurrentSemesterId();
        verify(classRepository).findBySemesterIdAndNoConfirmedTeacherAssigned(currentSemesterId);
        verify(userService).getUserIdByEmail("testUser@example.com");
        verify(sectionService).getSectionIdByUserId(10L);
        verify(courseService).getCourseSectionId(100L);
    }

    @Test
    @DisplayName("findClassesWithoutTeacherAssigned - When classes have schedules returns them with schedules")
    void testFindClassesWithoutTeacherAssigned_ClassesHaveSchedules_ReturnsWithSchedules() {
        // Arrange
        Long currentSemesterId = 1L;
        Class classWithoutTeacher = Class.builder()
                .id(1L)
                .section(1L)
                .courseId(100L)
                .semesterId(currentSemesterId)
                .capacity(30)
                .build();

        ClassSchedule schedule1 = ClassSchedule.builder()
                .id(10L)
                .classId(1L)
                .day("Monday")
                .startTime(LocalTime.of(8, 0))
                .endTime(LocalTime.of(10, 0))
                .build();

        ClassSchedule schedule2 = ClassSchedule.builder()
                .id(11L)
                .classId(1L)
                .day("Wednesday")
                .startTime(LocalTime.of(8, 0))
                .endTime(LocalTime.of(10, 0))
                .build();

        ClassScheduleResponseDTO scheduleDTO1 = new ClassScheduleResponseDTO();
        scheduleDTO1.setId(10L);
        
        ClassScheduleResponseDTO scheduleDTO2 = new ClassScheduleResponseDTO();
        scheduleDTO2.setId(11L);

        ClassResponseDTO responseDTO = new ClassResponseDTO();
        responseDTO.setId(1L);
        responseDTO.setSchedules(Arrays.asList(scheduleDTO1, scheduleDTO2));

        setupSecurityContext("ROLE_ADMIN");

        when(semesterService.getCurrentSemesterId()).thenReturn(Mono.just(currentSemesterId));
        when(classRepository.findBySemesterIdAndNoConfirmedTeacherAssigned(currentSemesterId))
                .thenReturn(Flux.just(classWithoutTeacher));
        when(classScheduleRepository.findByClassId(1L)).thenReturn(Flux.just(schedule1, schedule2));
        when(modelMapper.map(classWithoutTeacher, ClassResponseDTO.class)).thenReturn(responseDTO);
        when(modelMapper.map(schedule1, ClassScheduleResponseDTO.class)).thenReturn(scheduleDTO1);
        when(modelMapper.map(schedule2, ClassScheduleResponseDTO.class)).thenReturn(scheduleDTO2);

        // Act & Assert
        StepVerifier.create(planningService.findClassesWithoutTeacherAssigned())
                .assertNext(result -> {
                    assertEquals(1L, result.getId());
                    assertNotNull(result.getSchedules());
                    assertEquals(2, result.getSchedules().size());
                })
                .verifyComplete();

        verify(semesterService).getCurrentSemesterId();
        verify(classRepository).findBySemesterIdAndNoConfirmedTeacherAssigned(currentSemesterId);
        verify(classScheduleRepository).findByClassId(1L);
    }

    // ========================================================================
    // getClassroomScheduleConflicts Tests
    // ========================================================================

    @Test
    @DisplayName("getClassroomScheduleConflicts - Complex overlapping scenario creates correct clusters")
    void testGetClassroomScheduleConflicts_ComplexOverlaps_CreatesCorrectClusters() {
        // Arrange
        Long currentSemesterId = 1L;
        Long classroomId = 100L;
        
        ClassroomResponseDTO classroom = ClassroomResponseDTO.builder()
                .id(classroomId)
                .room("A-101")
                .campus("Main Campus")
                .capacity(30)
                .build();

        // s1 = 11-13
        ClassSchedule s1 = ClassSchedule.builder()
                .id(1L)
                .classId(10L)
                .classroomId(classroomId)
                .day("Monday")
                .startTime(LocalTime.of(11, 0))
                .endTime(LocalTime.of(13, 0))
                .build();

        // s2 = 10-12 (overlaps with s1 and s5)
        ClassSchedule s2 = ClassSchedule.builder()
                .id(2L)
                .classId(20L)
                .classroomId(classroomId)
                .day("Monday")
                .startTime(LocalTime.of(10, 0))
                .endTime(LocalTime.of(12, 0))
                .build();

        // s3 = 12-15 (overlaps with s1 and s4)
        ClassSchedule s3 = ClassSchedule.builder()
                .id(3L)
                .classId(30L)
                .classroomId(classroomId)
                .day("Monday")
                .startTime(LocalTime.of(12, 0))
                .endTime(LocalTime.of(15, 0))
                .build();

        // s4 = 14-17 (overlaps with s3)
        ClassSchedule s4 = ClassSchedule.builder()
                .id(4L)
                .classId(40L)
                .classroomId(classroomId)
                .day("Monday")
                .startTime(LocalTime.of(14, 0))
                .endTime(LocalTime.of(17, 0))
                .build();

        // s5 = 9-12 (overlaps with s1 and s2)
        ClassSchedule s5 = ClassSchedule.builder()
                .id(5L)
                .classId(50L)
                .classroomId(classroomId)
                .day("Monday")
                .startTime(LocalTime.of(9, 0))
                .endTime(LocalTime.of(12, 0))
                .build();

        Class class1 = Class.builder().id(10L).section(1L).courseId(100L).semesterId(currentSemesterId).build();
        Class class2 = Class.builder().id(20L).section(1L).courseId(101L).semesterId(currentSemesterId).build();
        Class class3 = Class.builder().id(30L).section(1L).courseId(102L).semesterId(currentSemesterId).build();
        Class class4 = Class.builder().id(40L).section(1L).courseId(103L).semesterId(currentSemesterId).build();
        Class class5 = Class.builder().id(50L).section(1L).courseId(104L).semesterId(currentSemesterId).build();

        setupSecurityContext("ROLE_ADMIN");

        when(semesterService.getCurrentSemesterId()).thenReturn(Mono.just(currentSemesterId));
        when(classroomService.getAllClassrooms()).thenReturn(Flux.just(classroom));
        when(classScheduleRepository.findClassesWithOverlappingSchedulesInSameClassroom(currentSemesterId, classroomId))
                .thenReturn(Flux.just(s1, s2, s3, s4, s5));
        lenient().when(classRepository.findById(10L)).thenReturn(Mono.just(class1));
        lenient().when(classRepository.findById(20L)).thenReturn(Mono.just(class2));
        lenient().when(classRepository.findById(30L)).thenReturn(Mono.just(class3));
        lenient().when(classRepository.findById(40L)).thenReturn(Mono.just(class4));
        lenient().when(classRepository.findById(50L)).thenReturn(Mono.just(class5));


        // Act & Assert
        // Expected clusters:
        // Cluster 1: s1-s2-s5 (classes 10, 20, 50) - all three overlap with each other
        // Cluster 2: s1-s3 (classes 10, 30) - both overlap
        // Cluster 3: s3-s4 (classes 30, 40) - both overlap
        List<ClassroomScheduleConflictResponseDTO> conflicts = planningService.getClassroomScheduleConflicts()
                .collectList()
                .block();

        assertNotNull(conflicts);
        assertEquals(3, conflicts.size(), "Should have exactly 3 conflict clusters");

        // Verify each cluster
        List<List<Long>> clusterIds = conflicts.stream()
                .map(ClassroomScheduleConflictResponseDTO::getConflictingClassesIds)
                .toList();
        
        // Find cluster with 3 classes (s1-s2-s5)
        ClassroomScheduleConflictResponseDTO cluster3Classes = conflicts.stream()
                .filter(conflict -> conflict.getConflictingClassesIds().containsAll(Arrays.asList(10L, 20L, 50L)))
                .findFirst()
                .orElse(null);
        assertNotNull(cluster3Classes, "Should have a cluster with 3 classes");
        assertTrue(cluster3Classes.getConflictingClassesIds().containsAll(Arrays.asList(10L, 20L, 50L)),
                "Cluster should contain classes 10, 20, and 50");

        assertNotNull(cluster3Classes, "s1-s2-s5 conflict should exist");
        assertEquals(LocalTime.of(9, 0), cluster3Classes.getConflictStartTime(), "Min start time should be 09:00");
        assertEquals(LocalTime.of(13, 0), cluster3Classes.getConflictEndTime(), "Max end time should be 13:00");

        // Find clusters with 2 classes
        List<List<Long>> cluster2Classes = clusterIds.stream()
                .filter(ids -> ids.size() == 2)
                .toList();
        assertEquals(2, cluster2Classes.size(), "Should have 2 clusters with 2 classes each");
        
        // Verify s1-s3 cluster (classes 10, 30)
        boolean hasS1S3Cluster = cluster2Classes.stream()
                .anyMatch(ids -> ids.containsAll(Arrays.asList(10L, 30L)));
        assertTrue(hasS1S3Cluster, "Should have cluster with classes 10 and 30");

        // Verify min and max hours are correct for s1-s3 cluster
        ClassroomScheduleConflictResponseDTO s1s3Conflict = conflicts.stream()
                .filter(conflict -> conflict.getConflictingClassesIds().containsAll(Arrays.asList(10L, 30L)))
                .findFirst()
                .orElse(null);
        assertNotNull(s1s3Conflict, "s1-s3 conflict should exist");
        assertEquals(LocalTime.of(11, 0), s1s3Conflict.getConflictStartTime(), "Min start time should be 11:00");
        assertEquals(LocalTime.of(15, 0), s1s3Conflict.getConflictEndTime(), "Max end time should be 15:00");
        
        // Verify s3-s4 cluster (classes 30, 40)
        boolean hasS3S4Cluster = cluster2Classes.stream()
                .anyMatch(ids -> ids.containsAll(Arrays.asList(30L, 40L)));
        assertTrue(hasS3S4Cluster, "Should have cluster with classes 30 and 40");

        // Verify min and max hours are correct for s3-s4 cluster
        ClassroomScheduleConflictResponseDTO s3s4Conflict = conflicts.stream()
                .filter(conflict -> conflict.getConflictingClassesIds().containsAll(Arrays.asList(30L, 40L)))
                .findFirst()
                .orElse(null);
        assertNotNull(s3s4Conflict, "s3-s4 conflict should exist");
        assertEquals(LocalTime.of(12, 0), s3s4Conflict.getConflictStartTime(), "Min start time should be 12:00");
        assertEquals(LocalTime.of(17, 0), s3s4Conflict.getConflictEndTime(), "Max end time should be 17:00");

        // Verify all conflicts have correct classroom info
        conflicts.forEach(conflict -> {
            assertEquals(classroomId, conflict.getClassroomId());
            assertEquals("A-101", conflict.getClassroomName());
        });

        // Verify all have Monday as the day
        conflicts.forEach(conflict -> {
            assertEquals("Monday", conflict.getDay());
        });

        verify(semesterService).getCurrentSemesterId();
        verify(classroomService).getAllClassrooms();
        verify(classScheduleRepository).findClassesWithOverlappingSchedulesInSameClassroom(currentSemesterId, classroomId);
    }

    @Test
    @DisplayName("getClassroomScheduleConflicts - When schedules don't overlap but database returns them, then service returns empty")
    void testGetClassroomScheduleConflicts_NoOverlaps_ReturnsEmpty() {
        // Arrange
        Long currentSemesterId = 1L;
        Long classroomId = 100L;
        
        ClassroomResponseDTO classroom = ClassroomResponseDTO.builder()
                .id(classroomId)
                .room("A-101")
                .build();

        // Non-overlapping schedules
        ClassSchedule s1 = ClassSchedule.builder()
                .id(1L)
                .classId(10L)
                .classroomId(classroomId)
                .day("Monday")
                .startTime(LocalTime.of(9, 0))
                .endTime(LocalTime.of(10, 0))
                .build();

        ClassSchedule s2 = ClassSchedule.builder()
                .id(2L)
                .classId(20L)
                .classroomId(classroomId)
                .day("Monday")
                .startTime(LocalTime.of(10, 0))
                .endTime(LocalTime.of(11, 0))
                .build();


        when(semesterService.getCurrentSemesterId()).thenReturn(Mono.just(currentSemesterId));
        when(classroomService.getAllClassrooms()).thenReturn(Flux.just(classroom));
        when(classScheduleRepository.findClassesWithOverlappingSchedulesInSameClassroom(currentSemesterId, classroomId))
                .thenReturn(Flux.just(s1, s2));

        
        // Act & Assert - Should return empty because schedules don't actually overlap
        StepVerifier.create(planningService.getClassroomScheduleConflicts())
                .verifyComplete();

        verify(semesterService).getCurrentSemesterId();
        verify(classroomService).getAllClassrooms();
    }

    @Test
    @DisplayName("getClassroomScheduleConflicts - Different days no overlap")
    void testGetClassroomScheduleConflicts_DifferentDays_NoConflict() {
        // Arrange
        Long currentSemesterId = 1L;
        Long classroomId = 100L;
        
        ClassroomResponseDTO classroom = ClassroomResponseDTO.builder()
                .id(classroomId)
                .room("A-101")
                .build();

        ClassSchedule s1 = ClassSchedule.builder()
                .id(1L)
                .classId(10L)
                .classroomId(classroomId)
                .day("Monday")
                .startTime(LocalTime.of(9, 0))
                .endTime(LocalTime.of(11, 0))
                .build();

        ClassSchedule s2 = ClassSchedule.builder()
                .id(2L)
                .classId(20L)
                .classroomId(classroomId)
                .day("Tuesday")
                .startTime(LocalTime.of(9, 0))
                .endTime(LocalTime.of(11, 0))
                .build();

        when(semesterService.getCurrentSemesterId()).thenReturn(Mono.just(currentSemesterId));
        when(classroomService.getAllClassrooms()).thenReturn(Flux.just(classroom));
        when(classScheduleRepository.findClassesWithOverlappingSchedulesInSameClassroom(currentSemesterId, classroomId))
                .thenReturn(Flux.just(s1, s2));
        
        // Act & Assert - Different days should not create conflicts
        StepVerifier.create(planningService.getClassroomScheduleConflicts())
                .verifyComplete();

        verify(semesterService).getCurrentSemesterId();
        verify(classroomService).getAllClassrooms();
    }

    @Test
    @DisplayName("getClassroomScheduleConflicts - ROLE_SECTION filters conflicts by user section")
    void testGetClassroomScheduleConflicts_RoleSection_FiltersConflicts() {
        // Arrange
        Long currentSemesterId = 1L;
        Long classroomId = 100L;
        Long userSectionId = 1L;
        Long differentSectionId = 2L;
        
        ClassroomResponseDTO classroom = ClassroomResponseDTO.builder()
                .id(classroomId)
                .room("A-101")
                .build();

        // s1 = 11-13 (section 2 - not accessible)
        ClassSchedule s1 = ClassSchedule.builder()
                .id(1L)
                .classId(10L)
                .classroomId(classroomId)
                .day("Monday")
                .startTime(LocalTime.of(11, 0))
                .endTime(LocalTime.of(13, 0))
                .build();

        // s2 = 10-12 (section 1 - accessible, overlaps with s1 and s5)
        ClassSchedule s2 = ClassSchedule.builder()
                .id(2L)
                .classId(20L)
                .classroomId(classroomId)
                .day("Monday")
                .startTime(LocalTime.of(10, 0))
                .endTime(LocalTime.of(12, 0))
                .build();

        // s3 = 12-15 (section 2 - not accessible, overlaps with s1 and s4)
        ClassSchedule s3 = ClassSchedule.builder()
                .id(3L)
                .classId(30L)
                .classroomId(classroomId)
                .day("Monday")
                .startTime(LocalTime.of(12, 0))
                .endTime(LocalTime.of(15, 0))
                .build();

        // s4 = 14-17 (section 1 - accessible, overlaps with s3)
        ClassSchedule s4 = ClassSchedule.builder()
                .id(4L)
                .classId(40L)
                .classroomId(classroomId)
                .day("Monday")
                .startTime(LocalTime.of(14, 0))
                .endTime(LocalTime.of(17, 0))
                .build();

        // s5 = 9-12 (section 1 - accessible, overlaps with s1 and s2)
        ClassSchedule s5 = ClassSchedule.builder()
                .id(5L)
                .classId(50L)
                .classroomId(classroomId)
                .day("Monday")
                .startTime(LocalTime.of(9, 0))
                .endTime(LocalTime.of(12, 0))
                .build();

        // Class 10 and 30 belong to section 2 (not accessible)
        Class class1 = Class.builder()
                .id(10L)
                .section(2L)
                .courseId(100L)
                .semesterId(currentSemesterId)
                .build();

        // Classes 20, 40, 50 belong to section 1 (accessible)
        Class class2 = Class.builder()
                .id(20L)
                .section(1L)
                .courseId(101L)
                .semesterId(currentSemesterId)
                .build();

        Class class3 = Class.builder()
                .id(30L)
                .section(2L)
                .courseId(102L)
                .semesterId(currentSemesterId)
                .build();

        Class class4 = Class.builder()
                .id(40L)
                .section(1L)
                .courseId(103L)
                .semesterId(currentSemesterId)
                .build();

        Class class5 = Class.builder()
                .id(50L)
                .section(1L)
                .courseId(104L)
                .semesterId(currentSemesterId)
                .build();

        setupSecurityContext("ROLE_SECTION");

        when(semesterService.getCurrentSemesterId()).thenReturn(Mono.just(currentSemesterId));
        when(classroomService.getAllClassrooms()).thenReturn(Flux.just(classroom));
        when(classScheduleRepository.findClassesWithOverlappingSchedulesInSameClassroom(currentSemesterId, classroomId))
                .thenReturn(Flux.just(s1, s2, s3, s4, s5));
        lenient().when(classRepository.findById(10L)).thenReturn(Mono.just(class1));
        lenient().when(classRepository.findById(20L)).thenReturn(Mono.just(class2));
        lenient().when(classRepository.findById(30L)).thenReturn(Mono.just(class3));
        lenient().when(classRepository.findById(40L)).thenReturn(Mono.just(class4));
        lenient().when(classRepository.findById(50L)).thenReturn(Mono.just(class5));
        when(userService.getUserIdByEmail("testUser@example.com")).thenReturn(Mono.just(50L));

        lenient().when(sectionService.getSectionIdByUserId(50L)).thenReturn(Mono.just(userSectionId));
        lenient().when(courseService.getCourseSectionId(100L)).thenReturn(Mono.just(differentSectionId)); // Class 10 - different section
        lenient().when(courseService.getCourseSectionId(101L)).thenReturn(Mono.just(userSectionId));      // Class 20 - user's section
        lenient().when(courseService.getCourseSectionId(102L)).thenReturn(Mono.just(differentSectionId)); // Class 30 - different section
        lenient().when(courseService.getCourseSectionId(103L)).thenReturn(Mono.just(userSectionId));      // Class 40 - user's section
        lenient().when(courseService.getCourseSectionId(104L)).thenReturn(Mono.just(userSectionId));      // Class 50 - user's section

        // Act
        List<ClassroomScheduleConflictResponseDTO> conflicts = planningService.getClassroomScheduleConflicts()
                .collectList()
                .block();

        // Assert
        // Expected 3 clusters (classes 10 and 30 should be filtered out):
        // 1. Should give s1-s2-s5 cluster because s2 and s5 are accessible
        // 2. Should not include s1-s3 cluster because both s1 (class 10) and s3 (class 30) are filtered out
        // 3. Should give s3-s4 cluster because s2 is accessible
        // Only s2-s5 cluster remains with accessible classes
        assertNotNull(conflicts);
        assertEquals(2, conflicts.size(), "Should have exactly 2 conflict cluster (only accessible classes)");
        
        // Checks for all clusters to not include the pair of classes 10 and 30
        conflicts.forEach(conflict -> {
            assertFalse(conflict.getConflictingClassesIds().contains(10L) && conflict.getConflictingClassesIds().contains(30L), "Cluster should not contain class 10 (different section)");
        });

        verify(semesterService).getCurrentSemesterId();
        verify(classroomService).getAllClassrooms();
        verify(userService, atLeastOnce()).getUserIdByEmail("testUser@example.com");
    }

    // ========================================================================
    // getTeacherScheduleConflicts Tests
    // ========================================================================

    @Test
    @DisplayName("getTeacherScheduleConflicts - Complex overlapping scenario creates correct clusters")
    void testGetTeacherScheduleConflicts_ComplexOverlaps_CreatesCorrectClusters() {
        // Arrange
        Long currentSemesterId = 1L;
        Long teacherId = 100L;
        Long userId = 500L;
        
        co.edu.puj.secchub_backend.admin.contract.TeacherResponseDTO teacher = co.edu.puj.secchub_backend.admin.contract.TeacherResponseDTO.builder()
                .id(teacherId)
                .userId(userId)
                .maxHours(40)
                .build();

        co.edu.puj.secchub_backend.security.contract.UserInformationResponseDTO userInfo = 
            co.edu.puj.secchub_backend.security.contract.UserInformationResponseDTO.builder()
                .id(userId)
                .name("John")
                .lastName("Doe")
                .email("john.doe@example.com")
                .build();

        // Same 5 schedules as classroom conflict tests
        // s1 = 11-13
        ClassSchedule s1 = ClassSchedule.builder()
                .id(1L)
                .classId(10L)
                .day("Monday")
                .startTime(LocalTime.of(11, 0))
                .endTime(LocalTime.of(13, 0))
                .build();

        // s2 = 10-12 (overlaps with s1 and s5)
        ClassSchedule s2 = ClassSchedule.builder()
                .id(2L)
                .classId(20L)
                .day("Monday")
                .startTime(LocalTime.of(10, 0))
                .endTime(LocalTime.of(12, 0))
                .build();

        // s3 = 12-15 (overlaps with s1 and s4)
        ClassSchedule s3 = ClassSchedule.builder()
                .id(3L)
                .classId(30L)
                .day("Monday")
                .startTime(LocalTime.of(12, 0))
                .endTime(LocalTime.of(15, 0))
                .build();

        // s4 = 14-17 (overlaps with s3)
        ClassSchedule s4 = ClassSchedule.builder()
                .id(4L)
                .classId(40L)
                .day("Monday")
                .startTime(LocalTime.of(14, 0))
                .endTime(LocalTime.of(17, 0))
                .build();

        // s5 = 9-12 (overlaps with s1 and s2)
        ClassSchedule s5 = ClassSchedule.builder()
                .id(5L)
                .classId(50L)
                .day("Monday")
                .startTime(LocalTime.of(9, 0))
                .endTime(LocalTime.of(12, 0))
                .build();

        Class class1 = Class.builder().id(10L).section(1L).courseId(100L).semesterId(currentSemesterId).build();
        Class class2 = Class.builder().id(20L).section(1L).courseId(101L).semesterId(currentSemesterId).build();
        Class class3 = Class.builder().id(30L).section(1L).courseId(102L).semesterId(currentSemesterId).build();
        Class class4 = Class.builder().id(40L).section(1L).courseId(103L).semesterId(currentSemesterId).build();
        Class class5 = Class.builder().id(50L).section(1L).courseId(104L).semesterId(currentSemesterId).build();

        setupSecurityContext("ROLE_ADMIN");

        when(semesterService.getCurrentSemesterId()).thenReturn(Mono.just(currentSemesterId));
        when(teacherService.getAllTeachers()).thenReturn(Flux.just(teacher));
        when(classScheduleRepository.findTeacherScheduleConflicts(currentSemesterId, teacherId))
                .thenReturn(Flux.just(s1, s2, s3, s4, s5));
        when(userService.getUserInformationById(userId)).thenReturn(Mono.just(userInfo));
        lenient().when(classRepository.findById(10L)).thenReturn(Mono.just(class1));
        lenient().when(classRepository.findById(20L)).thenReturn(Mono.just(class2));
        lenient().when(classRepository.findById(30L)).thenReturn(Mono.just(class3));
        lenient().when(classRepository.findById(40L)).thenReturn(Mono.just(class4));
        lenient().when(classRepository.findById(50L)).thenReturn(Mono.just(class5));

        // Act & Assert
        // Expected clusters:
        // Cluster 1: s1-s2-s5 (classes 10, 20, 50) - all three overlap with each other
        // Cluster 2: s1-s3 (classes 10, 30) - both overlap
        // Cluster 3: s3-s4 (classes 30, 40) - both overlap
        List<co.edu.puj.secchub_backend.planning.dto.TeacherScheduleConflictResponseDTO> conflicts = 
                planningService.getTeacherScheduleConflicts()
                .collectList()
                .block();

        assertNotNull(conflicts);
        assertEquals(3, conflicts.size(), "Should have exactly 3 conflict clusters");

        // Verify each cluster
        List<List<Long>> clusterIds = conflicts.stream()
                .map(co.edu.puj.secchub_backend.planning.dto.TeacherScheduleConflictResponseDTO::getConflictingClassesIds)
                .toList();
        
        // Find cluster with 3 classes (s1-s2-s5)
        co.edu.puj.secchub_backend.planning.dto.TeacherScheduleConflictResponseDTO cluster3Classes = conflicts.stream()
                .filter(conflict -> conflict.getConflictingClassesIds().containsAll(Arrays.asList(10L, 20L, 50L)))
                .findFirst()
                .orElse(null);
        assertNotNull(cluster3Classes, "Should have a cluster with 3 classes");
        assertTrue(cluster3Classes.getConflictingClassesIds().containsAll(Arrays.asList(10L, 20L, 50L)),
                "Cluster should contain classes 10, 20, and 50");
        assertEquals(userId, cluster3Classes.getUserId());
        assertEquals("John Doe", cluster3Classes.getUserName());
        assertEquals("09:00", cluster3Classes.getConflictStartTime(), "Min start time should be 09:00");
        assertEquals("13:00", cluster3Classes.getConflictEndTime(), "Max end time should be 13:00");
        assertEquals("Monday", cluster3Classes.getConflictDay());

        // Find clusters with 2 classes
        List<List<Long>> cluster2Classes = clusterIds.stream()
                .filter(ids -> ids.size() == 2)
                .toList();
        assertEquals(2, cluster2Classes.size(), "Should have 2 clusters with 2 classes each");
        
        // Verify s1-s3 cluster (classes 10, 30)
        boolean hasS1S3Cluster = cluster2Classes.stream()
                .anyMatch(ids -> ids.containsAll(Arrays.asList(10L, 30L)));
        assertTrue(hasS1S3Cluster, "Should have cluster with classes 10 and 30");

        // Verify min and max hours are correct for s1-s3 cluster
        co.edu.puj.secchub_backend.planning.dto.TeacherScheduleConflictResponseDTO s1s3Conflict = conflicts.stream()
                .filter(conflict -> conflict.getConflictingClassesIds().containsAll(Arrays.asList(10L, 30L)))
                .findFirst()
                .orElse(null);
        assertNotNull(s1s3Conflict, "s1-s3 conflict should exist");
        assertEquals("11:00", s1s3Conflict.getConflictStartTime(), "Min start time should be 11:00");
        assertEquals("15:00", s1s3Conflict.getConflictEndTime(), "Max end time should be 15:00");
        
        // Verify s3-s4 cluster (classes 30, 40)
        boolean hasS3S4Cluster = cluster2Classes.stream()
                .anyMatch(ids -> ids.containsAll(Arrays.asList(30L, 40L)));
        assertTrue(hasS3S4Cluster, "Should have cluster with classes 30 and 40");

        // Verify min and max hours are correct for s3-s4 cluster
        co.edu.puj.secchub_backend.planning.dto.TeacherScheduleConflictResponseDTO s3s4Conflict = conflicts.stream()
                .filter(conflict -> conflict.getConflictingClassesIds().containsAll(Arrays.asList(30L, 40L)))
                .findFirst()
                .orElse(null);
        assertNotNull(s3s4Conflict, "s3-s4 conflict should exist");
        assertEquals("12:00", s3s4Conflict.getConflictStartTime(), "Min start time should be 12:00");
        assertEquals("17:00", s3s4Conflict.getConflictEndTime(), "Max end time should be 17:00");

        // Verify all have Monday as the day
        conflicts.forEach(conflict -> {
            assertEquals("Monday", conflict.getConflictDay());
            assertEquals(userId, conflict.getUserId());
            assertEquals("John Doe", conflict.getUserName());
        });

        verify(semesterService).getCurrentSemesterId();
        verify(userService, atLeast(1)).getUserInformationById(userId);
    }

    @Test
    @DisplayName("getTeacherScheduleConflicts - When schedules don't overlap, returns empty")
    void testGetTeacherScheduleConflicts_NoOverlaps_ReturnsEmpty() {
        // Arrange
        Long currentSemesterId = 1L;
        Long teacherId = 100L;
        
        co.edu.puj.secchub_backend.admin.contract.TeacherResponseDTO teacher = co.edu.puj.secchub_backend.admin.contract.TeacherResponseDTO.builder()
                .id(teacherId)
                .userId(500L)
                .build();

        // Non-overlapping schedules
        ClassSchedule s1 = ClassSchedule.builder()
                .id(1L)
                .classId(10L)
                .day("Monday")
                .startTime(LocalTime.of(9, 0))
                .endTime(LocalTime.of(10, 0))
                .build();

        ClassSchedule s2 = ClassSchedule.builder()
                .id(2L)
                .classId(20L)
                .day("Monday")
                .startTime(LocalTime.of(10, 0))
                .endTime(LocalTime.of(11, 0))
                .build();

        when(semesterService.getCurrentSemesterId()).thenReturn(Mono.just(currentSemesterId));
        when(teacherService.getAllTeachers()).thenReturn(Flux.just(teacher));
        when(classScheduleRepository.findTeacherScheduleConflicts(currentSemesterId, teacherId))
                .thenReturn(Flux.just(s1, s2));

        // Act & Assert
        StepVerifier.create(planningService.getTeacherScheduleConflicts())
                .expectNextCount(0)
                .verifyComplete();

        verify(semesterService).getCurrentSemesterId();
    }

    @Test
    @DisplayName("getTeacherScheduleConflicts - Different days no overlap")
    void testGetTeacherScheduleConflicts_DifferentDays_NoConflict() {
        // Arrange
        Long currentSemesterId = 1L;
        Long teacherId = 100L;
        
        co.edu.puj.secchub_backend.admin.contract.TeacherResponseDTO teacher = co.edu.puj.secchub_backend.admin.contract.TeacherResponseDTO.builder()
                .id(teacherId)
                .userId(500L)
                .build();

        // Same time but different days
        ClassSchedule s1 = ClassSchedule.builder()
                .id(1L)
                .classId(10L)
                .day("Monday")
                .startTime(LocalTime.of(10, 0))
                .endTime(LocalTime.of(12, 0))
                .build();

        ClassSchedule s2 = ClassSchedule.builder()
                .id(2L)
                .classId(20L)
                .day("Tuesday")
                .startTime(LocalTime.of(10, 0))
                .endTime(LocalTime.of(12, 0))
                .build();

        when(semesterService.getCurrentSemesterId()).thenReturn(Mono.just(currentSemesterId));
        when(teacherService.getAllTeachers()).thenReturn(Flux.just(teacher));
        when(classScheduleRepository.findTeacherScheduleConflicts(currentSemesterId, teacherId))
                .thenReturn(Flux.just(s1, s2));

        // Act & Assert
        StepVerifier.create(planningService.getTeacherScheduleConflicts())
                .expectNextCount(0)
                .verifyComplete();

        verify(semesterService).getCurrentSemesterId();
    }

    @Test
    @DisplayName("getTeacherScheduleConflicts - ROLE_SECTION filters conflicts by user section")
    void testGetTeacherScheduleConflicts_RoleSection_FiltersConflicts() {
        // Arrange
        Long currentSemesterId = 1L;
        Long teacherId = 100L;
        Long userId = 500L;
        Long userSectionId = 1L;
        Long differentSectionId = 2L;
        
        co.edu.puj.secchub_backend.admin.contract.TeacherResponseDTO teacher = co.edu.puj.secchub_backend.admin.contract.TeacherResponseDTO.builder()
                .id(teacherId)
                .userId(userId)
                .build();

        co.edu.puj.secchub_backend.security.contract.UserInformationResponseDTO userInfo = 
            co.edu.puj.secchub_backend.security.contract.UserInformationResponseDTO.builder()
                .id(userId)
                .name("John")
                .lastName("Doe")
                .build();

        // Same 5 schedules
        ClassSchedule s1 = ClassSchedule.builder()
                .id(1L)
                .classId(10L)
                .day("Monday")
                .startTime(LocalTime.of(11, 0))
                .endTime(LocalTime.of(13, 0))
                .build();

        ClassSchedule s2 = ClassSchedule.builder()
                .id(2L)
                .classId(20L)
                .day("Monday")
                .startTime(LocalTime.of(10, 0))
                .endTime(LocalTime.of(12, 0))
                .build();

        ClassSchedule s3 = ClassSchedule.builder()
                .id(3L)
                .classId(30L)
                .day("Monday")
                .startTime(LocalTime.of(12, 0))
                .endTime(LocalTime.of(15, 0))
                .build();

        ClassSchedule s4 = ClassSchedule.builder()
                .id(4L)
                .classId(40L)
                .day("Monday")
                .startTime(LocalTime.of(14, 0))
                .endTime(LocalTime.of(17, 0))
                .build();

        ClassSchedule s5 = ClassSchedule.builder()
                .id(5L)
                .classId(50L)
                .day("Monday")
                .startTime(LocalTime.of(9, 0))
                .endTime(LocalTime.of(12, 0))
                .build();

        // Classes: 10 and 30 are in different section, 20, 40, 50 are in user's section
        Class class1 = Class.builder().id(10L).section(differentSectionId).courseId(100L).semesterId(currentSemesterId).build();
        Class class2 = Class.builder().id(20L).section(userSectionId).courseId(101L).semesterId(currentSemesterId).build();
        Class class3 = Class.builder().id(30L).section(differentSectionId).courseId(102L).semesterId(currentSemesterId).build();
        Class class4 = Class.builder().id(40L).section(userSectionId).courseId(103L).semesterId(currentSemesterId).build();
        Class class5 = Class.builder().id(50L).section(userSectionId).courseId(104L).semesterId(currentSemesterId).build();

        setupSecurityContext("ROLE_SECTION");

        when(semesterService.getCurrentSemesterId()).thenReturn(Mono.just(currentSemesterId));
        when(teacherService.getAllTeachers()).thenReturn(Flux.just(teacher));
        when(classScheduleRepository.findTeacherScheduleConflicts(currentSemesterId, teacherId))
                .thenReturn(Flux.just(s1, s2, s3, s4, s5));
        when(userService.getUserInformationById(userId)).thenReturn(Mono.just(userInfo));
        lenient().when(classRepository.findById(10L)).thenReturn(Mono.just(class1));
        lenient().when(classRepository.findById(20L)).thenReturn(Mono.just(class2));
        lenient().when(classRepository.findById(30L)).thenReturn(Mono.just(class3));
        lenient().when(classRepository.findById(40L)).thenReturn(Mono.just(class4));
        lenient().when(classRepository.findById(50L)).thenReturn(Mono.just(class5));
        when(userService.getUserIdByEmail("testUser@example.com")).thenReturn(Mono.just(50L));

        lenient().when(sectionService.getSectionIdByUserId(50L)).thenReturn(Mono.just(userSectionId));
        lenient().when(courseService.getCourseSectionId(100L)).thenReturn(Mono.just(differentSectionId)); // Class 10 - different section
        lenient().when(courseService.getCourseSectionId(101L)).thenReturn(Mono.just(userSectionId));      // Class 20 - user's section
        lenient().when(courseService.getCourseSectionId(102L)).thenReturn(Mono.just(differentSectionId)); // Class 30 - different section
        lenient().when(courseService.getCourseSectionId(103L)).thenReturn(Mono.just(userSectionId));      // Class 40 - user's section
        lenient().when(courseService.getCourseSectionId(104L)).thenReturn(Mono.just(userSectionId));      // Class 50 - user's section

        // Act
        List<co.edu.puj.secchub_backend.planning.dto.TeacherScheduleConflictResponseDTO> conflicts = 
                planningService.getTeacherScheduleConflicts()
                .collectList()
                .block();

        // Assert
        // Expected 2 clusters (classes 10 and 30 should be filtered out):
        // 1. s2-s5 cluster (classes 20, 50) because both are accessible
        // 2. s3-s4 cluster (class 40) because s4 is accessible
        assertNotNull(conflicts);
        assertEquals(2, conflicts.size(), "Should have exactly 2 conflict clusters (only accessible classes)");
        
        // Verify no cluster contains both classes 10 and 30
        conflicts.forEach(conflict -> {
            assertFalse(conflict.getConflictingClassesIds().contains(10L) && conflict.getConflictingClassesIds().contains(30L), 
                    "Cluster should not contain class 10 (different section)");
        });

        verify(semesterService).getCurrentSemesterId();
        verify(userService, atLeastOnce()).getUserIdByEmail("testUser@example.com");
    }

    // ==================== PARAMETER PROVIDERS ====================
    private static List<Long> userSectionProvider() {
        return Arrays.asList(1L, 2L, 3L);
    }
}