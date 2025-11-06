package co.edu.puj.secchub_backend.integration.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import org.mockito.ArgumentMatchers;

import java.util.stream.Stream;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.modelmapper.ModelMapper;
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

import co.edu.puj.secchub_backend.admin.contract.AdminModuleSectionContract;
import co.edu.puj.secchub_backend.admin.contract.AdminModuleSemesterContract;
import co.edu.puj.secchub_backend.admin.contract.AdminModuleTeacherContract;
import co.edu.puj.secchub_backend.integration.dto.TeacherClassRequestDTO;
import co.edu.puj.secchub_backend.integration.dto.TeacherClassResponseDTO;
import co.edu.puj.secchub_backend.integration.exception.TeacherClassNotFoundException;
import co.edu.puj.secchub_backend.integration.exception.TeacherClassServerErrorException;
import co.edu.puj.secchub_backend.integration.model.TeacherClass;
import co.edu.puj.secchub_backend.integration.repository.TeacherClassRepository;
import co.edu.puj.secchub_backend.planning.contract.PlanningModuleClassContract;
import co.edu.puj.secchub_backend.security.contract.SecurityModuleUserContract;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@ExtendWith(MockitoExtension.class)
@DisplayName("TeacherClassService Unit Test")
class TeacherClassServiceTest {

    @Mock
    private SecurityModuleUserContract userService;
    @Mock
    private AdminModuleSectionContract sectionService;
    @Mock
    private AdminModuleSemesterContract semesterService;
    @Mock
    private PlanningModuleClassContract classService;
    @Mock
    private AdminModuleTeacherContract teacherService;
    @Mock
    private TeacherClassRepository repository;
    @Mock
    private ModelMapper modelMapper;
    @Mock
    private TransactionalOperator transactionalOperator;

    @InjectMocks
    private TeacherClassService teacherClassService;

    private MockedStatic<ReactiveSecurityContextHolder> mockedReactiveSecurityContextHolder;

    private TeacherClass testTeacherClass;
    private TeacherClassResponseDTO testTeacherClassResponseDTO;
    private TeacherClassRequestDTO testTeacherClassRequestDTO;

    private static final Long STATUS_PENDING_ID = 4L;
    private static final Long STATUS_ACCEPTED_ID = 8L;
    private static final Long STATUS_REJECTED_ID = 9L;

    @BeforeEach
    void setUp() {
        testTeacherClass = TeacherClass.builder()
                .id(1L)
                .teacherId(10L)
                .classId(100L)
                .semesterId(1L)
                .statusId(STATUS_PENDING_ID)
                .decision(null)
                .observation(null)
                .build();

        testTeacherClassResponseDTO = TeacherClassResponseDTO.builder()
                .id(1L)
                .teacherId(10L)
                .classId(100L)
                .semesterId(1L)
                .statusId(STATUS_PENDING_ID)
                .decision(null)
                .observation(null)
                .build();

        testTeacherClassRequestDTO = TeacherClassRequestDTO.builder()
                .teacherId(10L)
                .classId(100L)
                .build();

        // Setup transactional operator to execute immediately
        lenient().when(transactionalOperator.transactional(ArgumentMatchers.<Mono<Object>>any())).thenAnswer(invocation -> invocation.getArgument(0));
    }

    @AfterEach
    void tearDown() {
        if (mockedReactiveSecurityContextHolder != null) {
            mockedReactiveSecurityContextHolder.close();
        }
    }

    // ==================== HELPER METHODS ====================

    /**
     * Sets up user mocking for ADMIN or ROLE_SECTION based on sectionId parameter.
     * @param sectionId if null, sets up ADMIN role; otherwise ROLE_SECTION with that section
     */
    private void setUpUserMocking(Long sectionId) {
        // Close any existing mock to prevent leaks
        if (mockedReactiveSecurityContextHolder != null) {
            mockedReactiveSecurityContextHolder.close();
        }
        SecurityContext securityContext = mock(SecurityContext.class);
        Authentication authentication;

        if (sectionId == null) {
            // ADMIN role
            authentication = new UsernamePasswordAuthenticationToken(
                    "admin@test.com",
                    null,
                    java.util.List.of(new SimpleGrantedAuthority("ROLE_ADMIN"))
            );
        } else {
            // ROLE_SECTION
            authentication = new UsernamePasswordAuthenticationToken(
                    "section@test.com",
                    null,
                    java.util.List.of(new SimpleGrantedAuthority("ROLE_SECTION"))
            );
            when(userService.getUserIdByEmail("section@test.com")).thenReturn(Mono.just(50L));
            when(sectionService.getSectionIdByUserId(50L)).thenReturn(Mono.just(sectionId));
        }

        when(securityContext.getAuthentication()).thenReturn(authentication);

        mockedReactiveSecurityContextHolder = mockStatic(ReactiveSecurityContextHolder.class);
        mockedReactiveSecurityContextHolder.when(ReactiveSecurityContextHolder::getContext)
                .thenReturn(Mono.just(securityContext));
    }

    /**
     * Sets up user mocking for ROLE_TEACHER.
     * @param teacherId the teacher ID for the authenticated teacher
     */
    private void setUpTeacherMocking(Long teacherId) {
        SecurityContext securityContext = mock(SecurityContext.class);
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                "teacher@test.com",
                null,
                java.util.List.of(new SimpleGrantedAuthority("ROLE_TEACHER"))
        );

        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(userService.getUserIdByEmail("teacher@test.com")).thenReturn(Mono.just(100L));
        when(teacherService.getTeacherIdByUserId(100L)).thenReturn(Mono.just(teacherId));

        mockedReactiveSecurityContextHolder = mockStatic(ReactiveSecurityContextHolder.class);
        mockedReactiveSecurityContextHolder.when(ReactiveSecurityContextHolder::getContext)
                .thenReturn(Mono.just(securityContext));
    }

    /**
     * Provides test data for different user sections (admin and section users).
     * @return Stream of Long representing section IDs (null for admin, 1L and 2L for section users)
     */
    static Stream<Long> userSectionProvider() {
        return Stream.of(1L, 2L, 3L);
    }

    /**
     * Provides test data for different roles including teacher.
     * @return Stream of Arguments with role type and IDs
     */
    static Stream<Arguments> userRoleProvider() {
        return Stream.of(
            Arguments.of("ADMIN", null, null),
            Arguments.of("SECTION", 1L, null),
            Arguments.of("SECTION", 2L, null),
            Arguments.of("TEACHER", null, 10L)
        );
    }

    // ==================== CREATE TEACHER CLASS TESTS ====================

    @Test
    @DisplayName("createTeacherClass - Should create teacher class successfully")
    void testCreateTeacherClass_Success() {
        TeacherClass mappedTeacherClass = TeacherClass.builder()
                .teacherId(10L)
                .classId(100L)
                .build();

        TeacherClass savedTeacherClass = TeacherClass.builder()
                .id(1L)
                .teacherId(10L)
                .classId(100L)
                .semesterId(1L)
                .statusId(STATUS_PENDING_ID)
                .build();

        when(semesterService.getCurrentSemesterId()).thenReturn(Mono.just(1L));
        when(modelMapper.map(testTeacherClassRequestDTO, TeacherClass.class)).thenReturn(mappedTeacherClass);
        when(repository.save(any(TeacherClass.class))).thenReturn(Mono.just(savedTeacherClass));
        when(modelMapper.map(savedTeacherClass, TeacherClassResponseDTO.class)).thenReturn(testTeacherClassResponseDTO);

        TeacherClassResponseDTO result = teacherClassService.createTeacherClass(testTeacherClassRequestDTO).block();

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals(10L, result.getTeacherId());
        assertEquals(100L, result.getClassId());
        assertEquals(STATUS_PENDING_ID, result.getStatusId());
        verify(semesterService).getCurrentSemesterId();
        verify(repository).save(any(TeacherClass.class));
    }

    @Test
    @DisplayName("createTeacherClass - When semester service fails throws TeacherClassServerErrorException")
    void testCreateTeacherClass_SemesterServiceFails_ThrowsException() {
        when(semesterService.getCurrentSemesterId()).thenReturn(Mono.error(new RuntimeException("Semester error")));

        Mono<TeacherClassResponseDTO> result = teacherClassService.createTeacherClass(testTeacherClassRequestDTO);

        assertThrows(TeacherClassServerErrorException.class, result::block);
        verify(semesterService).getCurrentSemesterId();
    }

    // ==================== LIST CURRENT SEMESTER TEACHER CLASSES TESTS ====================

    @ParameterizedTest(name = "listCurrentSemesterTeacherClasses - When user has section {0} returns filtered classes")
    @MethodSource("userSectionProvider")
    @DisplayName("listCurrentSemesterTeacherClasses - Should return classes based on user section")
    void testListCurrentSemesterTeacherClasses_BasedOnUserSection_ReturnsClasses(Long userSection) {
        setUpUserMocking(userSection);

        when(semesterService.getCurrentSemesterId()).thenReturn(Mono.just(1L));
        when(repository.findBySemesterId(1L)).thenReturn(Flux.just(testTeacherClass));
        when(classService.isClassInSection(100L, userSection)).thenReturn(Mono.just(true));
        when(modelMapper.map(testTeacherClass, TeacherClassResponseDTO.class)).thenReturn(testTeacherClassResponseDTO);

        Flux<TeacherClassResponseDTO> result = teacherClassService.listCurrentSemesterTeacherClasses();

        StepVerifier.create(result)
                .expectNext(testTeacherClassResponseDTO)
                .verifyComplete();

        verify(semesterService).getCurrentSemesterId();
        verify(repository).findBySemesterId(1L);
    }

    @Test
    @DisplayName("listCurrentSemesterTeacherClasses - When teacher role returns only their classes")
    void testListCurrentSemesterTeacherClasses_TeacherRole_ReturnsOnlyTheirClasses() {
        setUpTeacherMocking(10L);

        when(semesterService.getCurrentSemesterId()).thenReturn(Mono.just(1L));
        when(repository.findBySemesterId(1L)).thenReturn(Flux.just(testTeacherClass));
        when(modelMapper.map(testTeacherClass, TeacherClassResponseDTO.class)).thenReturn(testTeacherClassResponseDTO);

        Flux<TeacherClassResponseDTO> result = teacherClassService.listCurrentSemesterTeacherClasses();

        StepVerifier.create(result)
                .expectNext(testTeacherClassResponseDTO)
                .verifyComplete();

        verify(userService).getUserIdByEmail("teacher@test.com");
        verify(teacherService).getTeacherIdByUserId(100L);
    }

    @Test
    @DisplayName("listCurrentSemesterTeacherClasses - When teacher role filters out other teacher classes")
    void testListCurrentSemesterTeacherClasses_TeacherRole_FiltersOtherTeachers() {
        setUpTeacherMocking(99L); // Different teacher ID

        when(semesterService.getCurrentSemesterId()).thenReturn(Mono.just(1L));
        when(repository.findBySemesterId(1L)).thenReturn(Flux.just(testTeacherClass)); // teacherId = 10L
        
        Flux<TeacherClassResponseDTO> result = teacherClassService.listCurrentSemesterTeacherClasses();

        StepVerifier.create(result)
                .verifyComplete(); // No results expected

        verify(userService).getUserIdByEmail("teacher@test.com");
        verify(teacherService).getTeacherIdByUserId(100L);
    }

    @Test
    @DisplayName("listCurrentSemesterTeacherClasses - When semester service fails throws TeacherClassServerErrorException")
    void testListCurrentSemesterTeacherClasses_SemesterServiceFails_ThrowsException() {
        when(semesterService.getCurrentSemesterId()).thenReturn(Mono.error(new RuntimeException("Semester error")));

        Flux<TeacherClassResponseDTO> result = teacherClassService.listCurrentSemesterTeacherClasses();

        StepVerifier.create(result)
                .expectError(TeacherClassServerErrorException.class)
                .verify();
    }

    // ==================== LIST CURRENT SEMESTER TEACHER CLASSES BY TEACHER TESTS ====================

    @ParameterizedTest(name = "listCurrentSemesterTeacherClassesByTeacher - When user has section {0} returns filtered classes")
    @MethodSource("userSectionProvider")
    @DisplayName("listCurrentSemesterTeacherClassesByTeacher - Should return classes based on user section")
    void testListCurrentSemesterTeacherClassesByTeacher_BasedOnUserSection_ReturnsClasses(Long userSection) {
        setUpUserMocking(userSection);

        when(semesterService.getCurrentSemesterId()).thenReturn(Mono.just(1L));
        when(repository.findBySemesterIdAndTeacherId(1L, 10L)).thenReturn(Flux.just(testTeacherClass));
        when(classService.isClassInSection(100L, userSection)).thenReturn(Mono.just(true));
        when(modelMapper.map(testTeacherClass, TeacherClassResponseDTO.class)).thenReturn(testTeacherClassResponseDTO);

        Flux<TeacherClassResponseDTO> result = teacherClassService.listCurrentSemesterTeacherClassesByTeacher(10L);

        StepVerifier.create(result)
                .expectNext(testTeacherClassResponseDTO)
                .verifyComplete();

        verify(semesterService).getCurrentSemesterId();
        verify(repository).findBySemesterIdAndTeacherId(1L, 10L);
    }

    @Test
    @DisplayName("listCurrentSemesterTeacherClassesByTeacher - When teacher role returns only their classes")
    void testListCurrentSemesterTeacherClassesByTeacher_TeacherRole_ReturnsOnlyTheirClasses() {
        setUpTeacherMocking(10L);

        when(semesterService.getCurrentSemesterId()).thenReturn(Mono.just(1L));
        when(repository.findBySemesterIdAndTeacherId(1L, 10L)).thenReturn(Flux.just(testTeacherClass));
        when(modelMapper.map(testTeacherClass, TeacherClassResponseDTO.class)).thenReturn(testTeacherClassResponseDTO);

        Flux<TeacherClassResponseDTO> result = teacherClassService.listCurrentSemesterTeacherClassesByTeacher(10L);

        StepVerifier.create(result)
                .expectNext(testTeacherClassResponseDTO)
                .verifyComplete();

        verify(userService).getUserIdByEmail("teacher@test.com");
        verify(teacherService).getTeacherIdByUserId(100L);
    }

    // ==================== LIST ALL TEACHER CLASS BY TEACHER TESTS ====================

    @ParameterizedTest(name = "listAllTeacherClassByTeacher - When user has section {0} returns filtered classes")
    @MethodSource("userSectionProvider")
    @DisplayName("listAllTeacherClassByTeacher - Should return classes based on user section")
    void testListAllTeacherClassByTeacher_BasedOnUserSection_ReturnsClasses(Long userSection) {
        setUpUserMocking(userSection);

        when(repository.findByTeacherId(10L)).thenReturn(Flux.just(testTeacherClass));
        when(classService.isClassInSection(100L, userSection)).thenReturn(Mono.just(true));
        when(modelMapper.map(testTeacherClass, TeacherClassResponseDTO.class)).thenReturn(testTeacherClassResponseDTO);

        Flux<TeacherClassResponseDTO> result = teacherClassService.listAllTeacherClassByTeacher(10L);

        StepVerifier.create(result)
                .expectNext(testTeacherClassResponseDTO)
                .verifyComplete();

        verify(repository).findByTeacherId(10L);
    }

    @Test
    @DisplayName("listAllTeacherClassByTeacher - When teacher role returns only their classes")
    void testListAllTeacherClassByTeacher_TeacherRole_ReturnsOnlyTheirClasses() {
        setUpTeacherMocking(10L);

        when(repository.findByTeacherId(10L)).thenReturn(Flux.just(testTeacherClass));
        when(modelMapper.map(testTeacherClass, TeacherClassResponseDTO.class)).thenReturn(testTeacherClassResponseDTO);

        Flux<TeacherClassResponseDTO> result = teacherClassService.listAllTeacherClassByTeacher(10L);

        StepVerifier.create(result)
                .expectNext(testTeacherClassResponseDTO)
                .verifyComplete();

        verify(userService).getUserIdByEmail("teacher@test.com");
        verify(teacherService).getTeacherIdByUserId(100L);
    }

    @Test
    @DisplayName("listAllTeacherClassByTeacher - When error occurs throws TeacherClassServerErrorException")
    void testListAllTeacherClassByTeacher_ErrorOccurs_ThrowsException() {
        when(repository.findByTeacherId(10L)).thenReturn(Flux.error(new RuntimeException("Database error")));

        Flux<TeacherClassResponseDTO> result = teacherClassService.listAllTeacherClassByTeacher(10L);

        StepVerifier.create(result)
                .expectError(TeacherClassServerErrorException.class)
                .verify();
    }

    // ==================== LIST TEACHER CLASS BY STATUS TESTS ====================

    @ParameterizedTest(name = "listTeacherClassByStatus - When user has section {0} returns filtered classes")
    @MethodSource("userSectionProvider")
    @DisplayName("listTeacherClassByStatus - Should return classes based on user section")
    void testListTeacherClassByStatus_BasedOnUserSection_ReturnsClasses(Long userSection) {
        setUpUserMocking(userSection);

        when(repository.findByTeacherIdAndStatusId(10L, STATUS_PENDING_ID)).thenReturn(Flux.just(testTeacherClass));
        when(classService.isClassInSection(100L, userSection)).thenReturn(Mono.just(true));
        when(modelMapper.map(testTeacherClass, TeacherClassResponseDTO.class)).thenReturn(testTeacherClassResponseDTO);

        Flux<TeacherClassResponseDTO> result = teacherClassService.listTeacherClassByStatus(10L, STATUS_PENDING_ID);

        StepVerifier.create(result)
                .expectNext(testTeacherClassResponseDTO)
                .verifyComplete();

        verify(repository).findByTeacherIdAndStatusId(10L, STATUS_PENDING_ID);
    }

    @Test
    @DisplayName("listTeacherClassByStatus - When teacher role returns only their classes")
    void testListTeacherClassByStatus_TeacherRole_ReturnsOnlyTheirClasses() {
        setUpTeacherMocking(10L);

        when(repository.findByTeacherIdAndStatusId(10L, STATUS_ACCEPTED_ID)).thenReturn(Flux.just(testTeacherClass));
        when(modelMapper.map(testTeacherClass, TeacherClassResponseDTO.class)).thenReturn(testTeacherClassResponseDTO);

        Flux<TeacherClassResponseDTO> result = teacherClassService.listTeacherClassByStatus(10L, STATUS_ACCEPTED_ID);

        StepVerifier.create(result)
                .expectNext(testTeacherClassResponseDTO)
                .verifyComplete();

        verify(userService).getUserIdByEmail("teacher@test.com");
        verify(teacherService).getTeacherIdByUserId(100L);
    }

    // ==================== LIST TEACHER CLASS BY CLASS ID TESTS ====================

    @ParameterizedTest(name = "listTeacherClassByClassId - When user has section {0} returns filtered classes")
    @MethodSource("userSectionProvider")
    @DisplayName("listTeacherClassByClassId - Should return classes based on user section")
    void testListTeacherClassByClassId_BasedOnUserSection_ReturnsClasses(Long userSection) {
        setUpUserMocking(userSection);

        when(repository.findByClassId(100L)).thenReturn(Flux.just(testTeacherClass));
        when(classService.isClassInSection(100L, userSection)).thenReturn(Mono.just(true));
        when(modelMapper.map(testTeacherClass, TeacherClassResponseDTO.class)).thenReturn(testTeacherClassResponseDTO);

        Flux<TeacherClassResponseDTO> result = teacherClassService.listTeacherClassByClassId(100L);

        StepVerifier.create(result)
                .expectNext(testTeacherClassResponseDTO)
                .verifyComplete();

        verify(repository).findByClassId(100L);
    }

    @Test
    @DisplayName("listTeacherClassByClassId - When teacher role returns only their classes")
    void testListTeacherClassByClassId_TeacherRole_ReturnsOnlyTheirClasses() {
        setUpTeacherMocking(10L);

        when(repository.findByClassId(100L)).thenReturn(Flux.just(testTeacherClass));
        when(modelMapper.map(testTeacherClass, TeacherClassResponseDTO.class)).thenReturn(testTeacherClassResponseDTO);

        Flux<TeacherClassResponseDTO> result = teacherClassService.listTeacherClassByClassId(100L);

        StepVerifier.create(result)
                .expectNext(testTeacherClassResponseDTO)
                .verifyComplete();

        verify(userService).getUserIdByEmail("teacher@test.com");
        verify(teacherService).getTeacherIdByUserId(100L);
    }

    @Test
    @DisplayName("listTeacherClassByClassId - When teacher role filters out other teacher classes")
    void testListTeacherClassByClassId_TeacherRole_FiltersOtherTeachers() {
        setUpTeacherMocking(99L); // Different teacher ID

        when(repository.findByClassId(100L)).thenReturn(Flux.just(testTeacherClass)); // teacherId = 10L

        Flux<TeacherClassResponseDTO> result = teacherClassService.listTeacherClassByClassId(100L);

        StepVerifier.create(result)
                .verifyComplete(); // No results expected

        verify(userService).getUserIdByEmail("teacher@test.com");
        verify(teacherService).getTeacherIdByUserId(100L);
    }

    // ==================== ACCEPT TEACHER CLASS TESTS ====================

    @ParameterizedTest(name = "acceptTeacherClass - When user has section {0} accepts successfully")
    @MethodSource("userSectionProvider")
    @DisplayName("acceptTeacherClass - Should accept class based on user section")
    void testAcceptTeacherClass_BasedOnUserSection_AcceptsSuccessfully(Long userSection) {
        setUpUserMocking(userSection);

        TeacherClass acceptedClass = TeacherClass.builder()
                .id(1L)
                .teacherId(10L)
                .classId(100L)
                .semesterId(1L)
                .statusId(STATUS_ACCEPTED_ID)
                .decision(true)
                .observation("Accepted")
                .build();

        TeacherClassResponseDTO acceptedDTO = TeacherClassResponseDTO.builder()
                .id(1L)
                .teacherId(10L)
                .classId(100L)
                .semesterId(1L)
                .statusId(STATUS_ACCEPTED_ID)
                .decision(true)
                .observation("Accepted")
                .build();

        when(repository.findById(1L)).thenReturn(Mono.just(testTeacherClass));
        when(classService.isClassInSection(100L, userSection)).thenReturn(Mono.just(true));
        when(repository.save(any(TeacherClass.class))).thenReturn(Mono.just(acceptedClass));
        when(modelMapper.map(acceptedClass, TeacherClassResponseDTO.class)).thenReturn(acceptedDTO);

        TeacherClassResponseDTO result = teacherClassService.acceptTeacherClass(1L, "Accepted").block();

        assertNotNull(result);
        assertEquals(STATUS_ACCEPTED_ID, result.getStatusId());
        assertTrue(result.getDecision());
        assertEquals("Accepted", result.getObservation());
        verify(repository).findById(1L);
        verify(repository).save(any(TeacherClass.class));
    }

    @Test
    @DisplayName("acceptTeacherClass - When teacher role accepts their class successfully")
    void testAcceptTeacherClass_TeacherRole_AcceptsSuccessfully() {
        setUpTeacherMocking(10L);

        TeacherClass acceptedClass = TeacherClass.builder()
                .id(1L)
                .teacherId(10L)
                .classId(100L)
                .semesterId(1L)
                .statusId(STATUS_ACCEPTED_ID)
                .decision(true)
                .observation("Accepted by teacher")
                .build();

        TeacherClassResponseDTO acceptedDTO = TeacherClassResponseDTO.builder()
                .id(1L)
                .teacherId(10L)
                .classId(100L)
                .semesterId(1L)
                .statusId(STATUS_ACCEPTED_ID)
                .decision(true)
                .observation("Accepted by teacher")
                .build();

        when(repository.findById(1L)).thenReturn(Mono.just(testTeacherClass));
        when(repository.save(any(TeacherClass.class))).thenReturn(Mono.just(acceptedClass));
        when(modelMapper.map(acceptedClass, TeacherClassResponseDTO.class)).thenReturn(acceptedDTO);

        TeacherClassResponseDTO result = teacherClassService.acceptTeacherClass(1L, "Accepted by teacher").block();

        assertNotNull(result);
        assertEquals(STATUS_ACCEPTED_ID, result.getStatusId());
        assertTrue(result.getDecision());
        assertEquals("Accepted by teacher", result.getObservation());
        verify(userService).getUserIdByEmail("teacher@test.com");
        verify(teacherService).getTeacherIdByUserId(100L);
    }

    @Test
    @DisplayName("acceptTeacherClass - When teacher role tries to accept other teacher class throws exception")
    void testAcceptTeacherClass_TeacherRole_OtherTeacherClass_ThrowsException() {
        setUpTeacherMocking(99L); // Different teacher ID

        when(repository.findById(1L)).thenReturn(Mono.just(testTeacherClass)); // teacherId = 10L

        Mono<TeacherClassResponseDTO> result = teacherClassService.acceptTeacherClass(1L, "Accepted");

        assertThrows(TeacherClassNotFoundException.class, result::block);
        verify(userService).getUserIdByEmail("teacher@test.com");
        verify(teacherService).getTeacherIdByUserId(100L);
        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("acceptTeacherClass - When teacher class not found throws TeacherClassNotFoundException")
    void testAcceptTeacherClass_NotFound_ThrowsException() {
        when(repository.findById(99L)).thenReturn(Mono.empty());

        Mono<TeacherClassResponseDTO> result = teacherClassService.acceptTeacherClass(99L, "Accepted");

        assertThrows(TeacherClassNotFoundException.class, result::block);
        verify(repository).findById(99L);
        verify(repository, never()).save(any());
    }

    // ==================== REJECT TEACHER CLASS TESTS ====================

    @ParameterizedTest(name = "rejectTeacherClass - When user has section {0} rejects successfully")
    @MethodSource("userSectionProvider")
    @DisplayName("rejectTeacherClass - Should reject class based on user section")
    void testRejectTeacherClass_BasedOnUserSection_RejectsSuccessfully(Long userSection) {
        setUpUserMocking(userSection);

        TeacherClass rejectedClass = TeacherClass.builder()
                .id(1L)
                .teacherId(10L)
                .classId(100L)
                .semesterId(1L)
                .statusId(STATUS_REJECTED_ID)
                .decision(false)
                .observation("Rejected")
                .build();

        TeacherClassResponseDTO rejectedDTO = TeacherClassResponseDTO.builder()
                .id(1L)
                .teacherId(10L)
                .classId(100L)
                .semesterId(1L)
                .statusId(STATUS_REJECTED_ID)
                .decision(false)
                .observation("Rejected")
                .build();

        when(repository.findById(1L)).thenReturn(Mono.just(testTeacherClass));
        when(classService.isClassInSection(100L, userSection)).thenReturn(Mono.just(true));
        when(repository.save(any(TeacherClass.class))).thenReturn(Mono.just(rejectedClass));
        when(modelMapper.map(rejectedClass, TeacherClassResponseDTO.class)).thenReturn(rejectedDTO);

        TeacherClassResponseDTO result = teacherClassService.rejectTeacherClass(1L, "Rejected").block();

        assertNotNull(result);
        assertEquals(STATUS_REJECTED_ID, result.getStatusId());
        assertFalse(result.getDecision());
        assertEquals("Rejected", result.getObservation());
        verify(repository).findById(1L);
        verify(repository).save(any(TeacherClass.class));
    }

    @Test
    @DisplayName("rejectTeacherClass - When teacher role rejects their class successfully")
    void testRejectTeacherClass_TeacherRole_RejectsSuccessfully() {
        setUpTeacherMocking(10L);

        TeacherClass rejectedClass = TeacherClass.builder()
                .id(1L)
                .teacherId(10L)
                .classId(100L)
                .semesterId(1L)
                .statusId(STATUS_REJECTED_ID)
                .decision(false)
                .observation("Rejected by teacher")
                .build();

        TeacherClassResponseDTO rejectedDTO = TeacherClassResponseDTO.builder()
                .id(1L)
                .teacherId(10L)
                .classId(100L)
                .semesterId(1L)
                .statusId(STATUS_REJECTED_ID)
                .decision(false)
                .observation("Rejected by teacher")
                .build();

        when(repository.findById(1L)).thenReturn(Mono.just(testTeacherClass));
        when(repository.save(any(TeacherClass.class))).thenReturn(Mono.just(rejectedClass));
        when(modelMapper.map(rejectedClass, TeacherClassResponseDTO.class)).thenReturn(rejectedDTO);

        TeacherClassResponseDTO result = teacherClassService.rejectTeacherClass(1L, "Rejected by teacher").block();

        assertNotNull(result);
        assertEquals(STATUS_REJECTED_ID, result.getStatusId());
        assertFalse(result.getDecision());
        assertEquals("Rejected by teacher", result.getObservation());
        verify(userService).getUserIdByEmail("teacher@test.com");
        verify(teacherService).getTeacherIdByUserId(100L);
    }

    @Test
    @DisplayName("rejectTeacherClass - When teacher role tries to reject other teacher class throws exception")
    void testRejectTeacherClass_TeacherRole_OtherTeacherClass_ThrowsException() {
        setUpTeacherMocking(99L); // Different teacher ID

        when(repository.findById(1L)).thenReturn(Mono.just(testTeacherClass)); // teacherId = 10L

        Mono<TeacherClassResponseDTO> result = teacherClassService.rejectTeacherClass(1L, "Rejected");

        assertThrows(TeacherClassNotFoundException.class, result::block);
        verify(userService).getUserIdByEmail("teacher@test.com");
        verify(teacherService).getTeacherIdByUserId(100L);
        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("rejectTeacherClass - When teacher class not found throws TeacherClassNotFoundException")
    void testRejectTeacherClass_NotFound_ThrowsException() {
        when(repository.findById(99L)).thenReturn(Mono.empty());

        Mono<TeacherClassResponseDTO> result = teacherClassService.rejectTeacherClass(99L, "Rejected");

        assertThrows(TeacherClassNotFoundException.class, result::block);
        verify(repository).findById(99L);
        verify(repository, never()).save(any());
    }

    // ==================== DELETE TEACHER CLASS TESTS ====================

    @ParameterizedTest(name = "deleteTeacherClassByTeacherAndClass - When user has section {0} deletes successfully")
    @MethodSource("userSectionProvider")
    @DisplayName("deleteTeacherClassByTeacherAndClass - Should delete based on user section")
    void testDeleteTeacherClassByTeacherAndClass_BasedOnUserSection_DeletesSuccessfully(Long userSection) {
        setUpUserMocking(userSection);

        when(repository.findByTeacherIdAndClassId(10L, 100L)).thenReturn(Mono.just(testTeacherClass));
        when(classService.isClassInSection(100L, userSection)).thenReturn(Mono.just(true));
        when(repository.deleteById(1L)).thenReturn(Mono.empty());

        Mono<Void> result = teacherClassService.deleteTeacherClassByTeacherAndClass(10L, 100L);

        StepVerifier.create(result)
                .verifyComplete();

        verify(repository).findByTeacherIdAndClassId(10L, 100L);
        verify(repository).deleteById(1L);
    }

    @Test
    @DisplayName("deleteTeacherClassByTeacherAndClass - When teacher role deletes their class successfully")
    void testDeleteTeacherClassByTeacherAndClass_TeacherRole_DeletesSuccessfully() {
        setUpTeacherMocking(10L);

        when(repository.findByTeacherIdAndClassId(10L, 100L)).thenReturn(Mono.just(testTeacherClass));
        when(repository.deleteById(1L)).thenReturn(Mono.empty());

        Mono<Void> result = teacherClassService.deleteTeacherClassByTeacherAndClass(10L, 100L);

        StepVerifier.create(result)
                .verifyComplete();

        verify(userService).getUserIdByEmail("teacher@test.com");
        verify(teacherService).getTeacherIdByUserId(100L);
        verify(repository).deleteById(1L);
    }

    @Test
    @DisplayName("deleteTeacherClassByTeacherAndClass - When teacher role tries to delete other teacher class throws exception")
    void testDeleteTeacherClassByTeacherAndClass_TeacherRole_OtherTeacherClass_ThrowsException() {
        setUpTeacherMocking(99L); // Different teacher ID

        when(repository.findByTeacherIdAndClassId(10L, 100L)).thenReturn(Mono.just(testTeacherClass)); // teacherId = 10L

        Mono<Void> result = teacherClassService.deleteTeacherClassByTeacherAndClass(10L, 100L);

        assertThrows(TeacherClassNotFoundException.class, result::block);
        verify(userService).getUserIdByEmail("teacher@test.com");
        verify(teacherService).getTeacherIdByUserId(100L);
        verify(repository, never()).deleteById(anyLong());
    }

    @Test
    @DisplayName("deleteTeacherClassByTeacherAndClass - When teacher class not found throws TeacherClassNotFoundException")
    void testDeleteTeacherClassByTeacherAndClass_NotFound_ThrowsException() {
        when(repository.findByTeacherIdAndClassId(10L, 100L)).thenReturn(Mono.empty());

        Mono<Void> result = teacherClassService.deleteTeacherClassByTeacherAndClass(10L, 100L);

        assertThrows(TeacherClassNotFoundException.class, result::block);
        verify(repository).findByTeacherIdAndClassId(10L, 100L);
        verify(repository, never()).deleteById(anyLong());
    }

    @Test
    @DisplayName("deleteTeacherClassByTeacherAndClass - When section user tries to delete different section class throws exception")
    void testDeleteTeacherClassByTeacherAndClass_DifferentSection_ThrowsException() {
        setUpUserMocking(2L); // Section 2

        when(repository.findByTeacherIdAndClassId(10L, 100L)).thenReturn(Mono.just(testTeacherClass));
        when(classService.isClassInSection(100L, 2L)).thenReturn(Mono.just(false)); // Class not in section 2

        Mono<Void> result = teacherClassService.deleteTeacherClassByTeacherAndClass(10L, 100L);

        assertThrows(TeacherClassNotFoundException.class, result::block);
        verify(repository).findByTeacherIdAndClassId(10L, 100L);
        verify(classService).isClassInSection(100L, 2L);
        verify(repository, never()).deleteById(anyLong());
    }
}
