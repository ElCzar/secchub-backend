package co.edu.puj.secchub_backend.integration.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Arrays;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
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

import co.edu.puj.secchub_backend.admin.contract.AdminModuleCourseContract;
import co.edu.puj.secchub_backend.admin.contract.AdminModuleSectionContract;
import co.edu.puj.secchub_backend.admin.contract.AdminModuleSemesterContract;
import co.edu.puj.secchub_backend.integration.dto.StudentApplicationRequestDTO;
import co.edu.puj.secchub_backend.integration.dto.StudentApplicationResponseDTO;
import co.edu.puj.secchub_backend.integration.dto.StudentApplicationScheduleRequestDTO;
import co.edu.puj.secchub_backend.integration.exception.StudentApplicationBadRequestException;
import co.edu.puj.secchub_backend.integration.exception.StudentApplicationNotFoundException;
import co.edu.puj.secchub_backend.integration.model.StudentApplication;
import co.edu.puj.secchub_backend.integration.model.StudentApplicationSchedule;
import co.edu.puj.secchub_backend.integration.repository.StudentApplicationRepository;
import co.edu.puj.secchub_backend.integration.repository.StudentApplicationScheduleRepository;
import co.edu.puj.secchub_backend.security.contract.SecurityModuleUserContract;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@ExtendWith(MockitoExtension.class)
@DisplayName("StudentApplicationService Unit Test")
class StudentApplicationServiceTest {

    @Mock
    private ModelMapper modelMapper;
    @Mock
    private TransactionalOperator transactionalOperator;
    @Mock
    private StudentApplicationRepository studentApplicationRepository;
    @Mock
    private StudentApplicationScheduleRepository requestScheduleRepository;
    @Mock
    private SecurityModuleUserContract userService;
    @Mock
    private AdminModuleSemesterContract semesterService;
    @Mock
    private AdminModuleSectionContract sectionService;
    @Mock
    private AdminModuleCourseContract courseService;

    @InjectMocks
    private StudentApplicationService studentApplicationService;

    private MockedStatic<ReactiveSecurityContextHolder> mockedReactiveSecurityContextHolder;

    private StudentApplication testStudentApplication;
    private StudentApplicationRequestDTO testStudentApplicationRequestDTO;
    private StudentApplicationSchedule testSchedule;
    private StudentApplicationScheduleRequestDTO testScheduleRequestDTO;

    private static final Long STATUS_PENDING_ID = 4L;
    private static final Long STATUS_APPROVED_ID = 8L;
    private static final Long STATUS_REJECTED_ID = 9L;

    @BeforeEach
    void setUp() {
        testStudentApplication = StudentApplication.builder()
                .id(1L)
                .userId(100L)
                .courseId(10L)
                .sectionId(1L)
                .semesterId(1L)
                .program("Computer Science")
                .studentSemester(5)
                .academicAverage(4.5)
                .phoneNumber("1234567890")
                .alternatePhoneNumber("0987654321")
                .address("123 Main St")
                .personalEmail("student@example.com")
                .wasTeachingAssistant(false)
                .courseAverage(4.3)
                .courseTeacher("Dr. Smith")
                .statusId(STATUS_PENDING_ID)
                .applicationDate(LocalDate.now())
                .build();

        testScheduleRequestDTO = StudentApplicationScheduleRequestDTO.builder()
                .day("Monday")
                .startTime("08:00:00")
                .endTime("10:00:00")
                .build();

        testSchedule = StudentApplicationSchedule.builder()
                .id(1L)
                .studentApplicationId(1L)
                .day("Monday")
                .startTime(LocalTime.parse("08:00:00"))
                .endTime(LocalTime.parse("10:00:00"))
                .build();

        testStudentApplicationRequestDTO = StudentApplicationRequestDTO.builder()
                .courseId(10L)
                .sectionId(1L)
                .program("Computer Science")
                .studentSemester(5)
                .academicAverage(4.5)
                .phoneNumber("1234567890")
                .alternatePhoneNumber("0987654321")
                .address("123 Main St")
                .personalEmail("student@example.com")
                .wasTeachingAssistant(false)
                .courseAverage(4.3)
                .courseTeacher("Dr. Smith")
                .schedules(Arrays.asList(testScheduleRequestDTO))
                .build();

        // Setup transactional operator to execute immediately
        lenient().when(transactionalOperator.transactional(ArgumentMatchers.<Mono<Object>>any()))
                .thenAnswer(invocation -> invocation.getArgument(0));
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

    // ==================== CREATE STUDENT APPLICATION TESTS ====================

    @Test
    @DisplayName("createStudentApplication - Should create application with schedules successfully")
    void testCreateStudentApplication_WithSchedules_Success() {
        SecurityContext securityContext = mock(SecurityContext.class);
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                "student@test.com",
                null,
                java.util.List.of(new SimpleGrantedAuthority("ROLE_STUDENT"))
        );
        when(securityContext.getAuthentication()).thenReturn(authentication);

        mockedReactiveSecurityContextHolder = mockStatic(ReactiveSecurityContextHolder.class);
        mockedReactiveSecurityContextHolder.when(ReactiveSecurityContextHolder::getContext)
                .thenReturn(Mono.just(securityContext));

        StudentApplication mappedApplication = StudentApplication.builder()
                .courseId(10L)
                .sectionId(1L)
                .program("Computer Science")
                .build();

        StudentApplication savedApplication = StudentApplication.builder()
                .id(1L)
                .userId(100L)
                .courseId(10L)
                .sectionId(1L)
                .semesterId(1L)
                .statusId(STATUS_PENDING_ID)
                .applicationDate(LocalDate.now())
                .program("Computer Science")
                .build();

        when(semesterService.getCurrentSemesterId()).thenReturn(Mono.just(1L));
        when(modelMapper.map(testStudentApplicationRequestDTO, StudentApplication.class)).thenReturn(mappedApplication);
        when(userService.getUserIdByEmail("student@test.com")).thenReturn(Mono.just(100L));
        when(studentApplicationRepository.findByUserIdAndSemesterId(100L, 1L)).thenReturn(Flux.empty());
        when(studentApplicationRepository.save(any(StudentApplication.class))).thenReturn(Mono.just(savedApplication));
        when(requestScheduleRepository.saveAll(anyList())).thenReturn(Flux.just(testSchedule));
        when(requestScheduleRepository.findByStudentApplicationId(1L)).thenReturn(Flux.just(testSchedule));

        StudentApplicationResponseDTO result = studentApplicationService.createStudentApplication(testStudentApplicationRequestDTO).block();

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals(100L, result.getUserId());
        assertEquals(STATUS_PENDING_ID, result.getStatusId());
        verify(studentApplicationRepository).save(any(StudentApplication.class));
        verify(requestScheduleRepository).saveAll(anyList());
    }

    @Test
    @DisplayName("createStudentApplication - Should create application without schedules successfully")
    void testCreateStudentApplication_WithoutSchedules_Success() {
        SecurityContext securityContext = mock(SecurityContext.class);
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                "student@test.com",
                null,
                java.util.List.of(new SimpleGrantedAuthority("ROLE_STUDENT"))
        );
        when(securityContext.getAuthentication()).thenReturn(authentication);

        mockedReactiveSecurityContextHolder = mockStatic(ReactiveSecurityContextHolder.class);
        mockedReactiveSecurityContextHolder.when(ReactiveSecurityContextHolder::getContext)
                .thenReturn(Mono.just(securityContext));

        StudentApplicationRequestDTO requestWithoutSchedules = StudentApplicationRequestDTO.builder()
                .courseId(10L)
                .sectionId(1L)
                .program("Computer Science")
                .schedules(null)
                .build();

        StudentApplication mappedApplication = StudentApplication.builder()
                .courseId(10L)
                .sectionId(1L)
                .build();

        StudentApplication savedApplication = StudentApplication.builder()
                .id(1L)
                .userId(100L)
                .courseId(10L)
                .sectionId(1L)
                .semesterId(1L)
                .statusId(STATUS_PENDING_ID)
                .applicationDate(LocalDate.now())
                .build();

        when(semesterService.getCurrentSemesterId()).thenReturn(Mono.just(1L));
        when(modelMapper.map(requestWithoutSchedules, StudentApplication.class)).thenReturn(mappedApplication);
        when(userService.getUserIdByEmail("student@test.com")).thenReturn(Mono.just(100L));
        when(studentApplicationRepository.findByUserIdAndSemesterId(100L, 1L)).thenReturn(Flux.empty());
        when(studentApplicationRepository.save(any(StudentApplication.class))).thenReturn(Mono.just(savedApplication));
        when(requestScheduleRepository.findByStudentApplicationId(1L)).thenReturn(Flux.empty());

        StudentApplicationResponseDTO result = studentApplicationService.createStudentApplication(requestWithoutSchedules).block();

        assertNotNull(result);
        assertEquals(1L, result.getId());
        verify(studentApplicationRepository).save(any(StudentApplication.class));
        verify(requestScheduleRepository, never()).saveAll(anyList());
    }

    @Test
    @DisplayName("createStudentApplication - When user already has application throws exception")
    void testCreateStudentApplication_UserAlreadyHasApplication_ThrowsException() {
        SecurityContext securityContext = mock(SecurityContext.class);
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                "student@test.com",
                null,
                java.util.List.of(new SimpleGrantedAuthority("ROLE_STUDENT"))
        );
        when(securityContext.getAuthentication()).thenReturn(authentication);

        mockedReactiveSecurityContextHolder = mockStatic(ReactiveSecurityContextHolder.class);
        mockedReactiveSecurityContextHolder.when(ReactiveSecurityContextHolder::getContext)
                .thenReturn(Mono.just(securityContext));

        StudentApplication resultApplicationResponseDTO = StudentApplication.builder()
                .id(2L)
                .userId(100L)
                .courseId(10L)
                .sectionId(1L)
                .semesterId(1L)
                .build();

        StudentApplication newStudentApplication = StudentApplication.builder()
                .courseId(10L)
                .sectionId(1L)
                .program("Computer Science")
                .build();

        when(semesterService.getCurrentSemesterId()).thenReturn(Mono.just(1L));
        when(modelMapper.map(testStudentApplicationRequestDTO, StudentApplication.class)).thenReturn(newStudentApplication);
        when(userService.getUserIdByEmail("student@test.com")).thenReturn(Mono.just(100L));
        when(studentApplicationRepository.findByUserIdAndSemesterId(100L, 1L))
                .thenReturn(Flux.just(resultApplicationResponseDTO));
        when(modelMapper.map(testStudentApplicationRequestDTO, StudentApplication.class)).thenReturn(newStudentApplication);

        Mono<StudentApplicationResponseDTO> result = studentApplicationService.createStudentApplication(testStudentApplicationRequestDTO);

        assertThrows(StudentApplicationBadRequestException.class, result::block);
        verify(studentApplicationRepository, never()).save(any());
    }

    @Test
    @DisplayName("createStudentApplication - When semester service fails throws exception")
    void testCreateStudentApplication_SemesterServiceFails_ThrowsException() {
        when(semesterService.getCurrentSemesterId()).thenReturn(Mono.error(new RuntimeException("Semester error")));

        Mono<StudentApplicationResponseDTO> result = studentApplicationService.createStudentApplication(testStudentApplicationRequestDTO);

        assertThrows(RuntimeException.class, result::block);
    }

    // ==================== LIST CURRENT SEMESTER APPLICATIONS TESTS ====================

    @Test
    @DisplayName("listCurrentSemesterStudentApplications - Should return applications for Admin")
    void testListCurrentSemesterStudentApplications_Admin_ReturnsApplications() {
        setUpUserMocking(null); // Admin

        when(semesterService.getCurrentSemesterId()).thenReturn(Mono.just(1L));
        when(studentApplicationRepository.findBySemesterId(1L)).thenReturn(Flux.just(testStudentApplication));
        when(requestScheduleRepository.findByStudentApplicationId(1L)).thenReturn(Flux.just(testSchedule));

        java.util.List<StudentApplicationResponseDTO> result = studentApplicationService.listCurrentSemesterStudentApplications()
                .collectList().block();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(1L, result.get(0).getId());
        verify(semesterService).getCurrentSemesterId();
        verify(studentApplicationRepository).findBySemesterId(1L);
    }

    @Test
    @DisplayName("listCurrentSemesterStudentApplications - Should return applications for Section user")
    void testListCurrentSemesterStudentApplications_SectionUser_ReturnsApplications() {
        setUpUserMocking(1L); // Section 1

        when(semesterService.getCurrentSemesterId()).thenReturn(Mono.just(1L));
        when(studentApplicationRepository.findBySemesterId(1L)).thenReturn(Flux.just(testStudentApplication));
        when(studentApplicationRepository.findById(1L)).thenReturn(Mono.just(testStudentApplication));
        when(requestScheduleRepository.findByStudentApplicationId(1L)).thenReturn(Flux.just(testSchedule));

        java.util.List<StudentApplicationResponseDTO> result = studentApplicationService.listCurrentSemesterStudentApplications()
                .collectList().block();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(1L, result.get(0).getId());
        verify(semesterService).getCurrentSemesterId();
        verify(studentApplicationRepository).findBySemesterId(1L);
    }

    @Test
    @DisplayName("listCurrentSemesterStudentApplications - Should filter out applications from different section")
    void testListCurrentSemesterStudentApplications_DifferentSection_FiltersOut() {
        setUpUserMocking(2L); // Section 2

        when(semesterService.getCurrentSemesterId()).thenReturn(Mono.just(1L));
        when(studentApplicationRepository.findBySemesterId(1L)).thenReturn(Flux.just(testStudentApplication));
        when(studentApplicationRepository.findById(1L)).thenReturn(Mono.just(testStudentApplication));

        java.util.List<StudentApplicationResponseDTO> result = studentApplicationService.listCurrentSemesterStudentApplications()
                .collectList().block();

        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(semesterService).getCurrentSemesterId();
        verify(studentApplicationRepository).findBySemesterId(1L);
    }

    @Test
    @DisplayName("listCurrentSemesterStudentApplications - When no applications exist returns empty")
    void testListCurrentSemesterStudentApplications_NoApplications_ReturnsEmpty() {

        when(semesterService.getCurrentSemesterId()).thenReturn(Mono.just(1L));
        when(studentApplicationRepository.findBySemesterId(1L)).thenReturn(Flux.empty());

        java.util.List<StudentApplicationResponseDTO> result = studentApplicationService.listCurrentSemesterStudentApplications()
                .collectList().block();

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    // ==================== LIST ALL APPLICATIONS TESTS ====================

    @Test
    @DisplayName("listAllStudentApplications - Should return all applications for Admin")
    void testListAllStudentApplications_Admin_ReturnsApplications() {
        setUpUserMocking(null); // Admin

        when(studentApplicationRepository.findAll()).thenReturn(Flux.just(testStudentApplication));
        when(requestScheduleRepository.findByStudentApplicationId(1L)).thenReturn(Flux.just(testSchedule));

        java.util.List<StudentApplicationResponseDTO> result = studentApplicationService.listAllStudentApplications()
                .collectList().block();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(1L, result.get(0).getId());
        verify(studentApplicationRepository).findAll();
    }

    @Test
    @DisplayName("listAllStudentApplications - Should return applications for Section user")
    void testListAllStudentApplications_SectionUser_ReturnsApplications() {
        setUpUserMocking(1L); // Section 1

        when(studentApplicationRepository.findAll()).thenReturn(Flux.just(testStudentApplication));
        when(studentApplicationRepository.findById(1L)).thenReturn(Mono.just(testStudentApplication));
        when(requestScheduleRepository.findByStudentApplicationId(1L)).thenReturn(Flux.just(testSchedule));

        java.util.List<StudentApplicationResponseDTO> result = studentApplicationService.listAllStudentApplications()
                .collectList().block();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(1L, result.get(0).getId());
        verify(studentApplicationRepository).findAll();
    }

    @Test
    @DisplayName("listAllStudentApplications - Should filter out applications from different section")
    void testListAllStudentApplications_DifferentSection_FiltersOut() {
        setUpUserMocking(2L); // Section 2

        when(studentApplicationRepository.findAll()).thenReturn(Flux.just(testStudentApplication));
        when(studentApplicationRepository.findById(1L)).thenReturn(Mono.just(testStudentApplication));

        java.util.List<StudentApplicationResponseDTO> result = studentApplicationService.listAllStudentApplications()
                .collectList().block();

        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(studentApplicationRepository).findAll();
    }

    @Test
    @DisplayName("listAllStudentApplications - When no applications exist returns empty list")
    void testListAllStudentApplications_NoApplications_ReturnsEmpty() {
        when(studentApplicationRepository.findAll()).thenReturn(Flux.empty());

        java.util.List<StudentApplicationResponseDTO> result = studentApplicationService.listAllStudentApplications()
                .collectList().block();

        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(studentApplicationRepository).findAll();
    }

    // ==================== FIND APPLICATION BY ID TESTS ====================

    @Test
    @DisplayName("findStudentApplicationById - Should return application for Admin")
    void testFindStudentApplicationById_Admin_ReturnsApplication() {
        setUpUserMocking(null); // Admin

        when(studentApplicationRepository.findById(1L)).thenReturn(Mono.just(testStudentApplication));
        when(requestScheduleRepository.findByStudentApplicationId(1L)).thenReturn(Flux.just(testSchedule));

        StudentApplicationResponseDTO result = studentApplicationService.findStudentApplicationById(1L).block();

        assertNotNull(result);
        assertEquals(1L, result.getId());
        verify(studentApplicationRepository).findById(1L);
    }

    @Test
    @DisplayName("findStudentApplicationById - Should return application for Section user")
    void testFindStudentApplicationById_SectionUser_ReturnsApplication() {
        setUpUserMocking(1L); // Section 1

        when(studentApplicationRepository.findById(1L)).thenReturn(Mono.just(testStudentApplication));
        when(requestScheduleRepository.findByStudentApplicationId(1L)).thenReturn(Flux.just(testSchedule));

        StudentApplicationResponseDTO result = studentApplicationService.findStudentApplicationById(1L).block();

        assertNotNull(result);
        assertEquals(1L, result.getId());
        verify(studentApplicationRepository, atLeast(1)).findById(1L);
    }

    @Test
    @DisplayName("findStudentApplicationById - When different section throws exception")
    void testFindStudentApplicationById_DifferentSection_ThrowsException() {
        setUpUserMocking(2L); // Section 2

        when(studentApplicationRepository.findById(1L)).thenReturn(Mono.just(testStudentApplication));

        Mono<StudentApplicationResponseDTO> result = studentApplicationService.findStudentApplicationById(1L);

        assertThrows(StudentApplicationNotFoundException.class, result::block);
        verify(studentApplicationRepository, atLeast(1)).findById(1L);
    }

    @Test
    @DisplayName("findStudentApplicationById - When application not found throws exception")
    void testFindStudentApplicationById_NotFound_ThrowsException() {
        when(studentApplicationRepository.findById(99L)).thenReturn(Mono.empty());

        Mono<StudentApplicationResponseDTO> result = studentApplicationService.findStudentApplicationById(99L);

        assertThrows(StudentApplicationNotFoundException.class, result::block);
        verify(studentApplicationRepository).findById(99L);
    }

    // ==================== LIST APPLICATIONS BY STATUS TESTS ====================

    @Test
    @DisplayName("listStudentApplicationsByStatus - Should return applications for Admin")
    void testListStudentApplicationsByStatus_Admin_ReturnsApplications() {
        setUpUserMocking(null); // Admin

        when(studentApplicationRepository.findByStatusId(STATUS_PENDING_ID)).thenReturn(Flux.just(testStudentApplication));
        when(requestScheduleRepository.findByStudentApplicationId(1L)).thenReturn(Flux.just(testSchedule));

        java.util.List<StudentApplicationResponseDTO> result = studentApplicationService.listStudentApplicationsByStatus(STATUS_PENDING_ID)
                .collectList().block();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(STATUS_PENDING_ID, result.get(0).getStatusId());
        verify(studentApplicationRepository).findByStatusId(STATUS_PENDING_ID);
    }

    @Test
    @DisplayName("listStudentApplicationsByStatus - Should return applications for Section user")
    void testListStudentApplicationsByStatus_SectionUser_ReturnsApplications() {
        setUpUserMocking(1L); // Section 1

        when(studentApplicationRepository.findByStatusId(STATUS_PENDING_ID)).thenReturn(Flux.just(testStudentApplication));
        when(studentApplicationRepository.findById(1L)).thenReturn(Mono.just(testStudentApplication));
        when(requestScheduleRepository.findByStudentApplicationId(1L)).thenReturn(Flux.just(testSchedule));

        java.util.List<StudentApplicationResponseDTO> result = studentApplicationService.listStudentApplicationsByStatus(STATUS_PENDING_ID)
                .collectList().block();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(STATUS_PENDING_ID, result.get(0).getStatusId());
        verify(studentApplicationRepository).findByStatusId(STATUS_PENDING_ID);
    }

    @Test
    @DisplayName("listStudentApplicationsByStatus - Should filter out applications from different section")
    void testListStudentApplicationsByStatus_DifferentSection_FiltersOut() {
        setUpUserMocking(2L); // Section 2

        when(studentApplicationRepository.findByStatusId(STATUS_PENDING_ID)).thenReturn(Flux.just(testStudentApplication));
        when(studentApplicationRepository.findById(1L)).thenReturn(Mono.just(testStudentApplication));

        java.util.List<StudentApplicationResponseDTO> result = studentApplicationService.listStudentApplicationsByStatus(STATUS_PENDING_ID)
                .collectList().block();

        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(studentApplicationRepository).findByStatusId(STATUS_PENDING_ID);
    }

    // ==================== LIST APPLICATIONS FOR SECTION TESTS ====================

    @Test
    @DisplayName("listStudentApplicationsForSection - Should return applications for Admin")
    void testListStudentApplicationsForSection_Admin_ReturnsApplications() {
        setUpUserMocking(null); // Admin

        when(studentApplicationRepository.findRequestsForSection(1L)).thenReturn(Flux.just(testStudentApplication));
        when(requestScheduleRepository.findByStudentApplicationId(1L)).thenReturn(Flux.just(testSchedule));

        java.util.List<StudentApplicationResponseDTO> result = studentApplicationService.listStudentApplicationsForSection(1L)
                .collectList().block();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(1L, result.get(0).getId());
        verify(studentApplicationRepository).findRequestsForSection(1L);
    }

    @Test
    @DisplayName("listStudentApplicationsForSection - Should return applications for Section user")
    void testListStudentApplicationsForSection_SectionUser_ReturnsApplications() {
        setUpUserMocking(1L); // Section 1

        when(studentApplicationRepository.findRequestsForSection(1L)).thenReturn(Flux.just(testStudentApplication));
        when(studentApplicationRepository.findById(1L)).thenReturn(Mono.just(testStudentApplication));
        when(requestScheduleRepository.findByStudentApplicationId(1L)).thenReturn(Flux.just(testSchedule));

        java.util.List<StudentApplicationResponseDTO> result = studentApplicationService.listStudentApplicationsForSection(1L)
                .collectList().block();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(1L, result.get(0).getId());
        verify(studentApplicationRepository).findRequestsForSection(1L);
    }

    @Test
    @DisplayName("listStudentApplicationsForSection - Should filter out applications from different section")
    void testListStudentApplicationsForSection_DifferentSection_FiltersOut() {
        setUpUserMocking(2L); // Section 2

        when(studentApplicationRepository.findRequestsForSection(1L)).thenReturn(Flux.just(testStudentApplication));
        when(studentApplicationRepository.findById(1L)).thenReturn(Mono.just(testStudentApplication));

        java.util.List<StudentApplicationResponseDTO> result = studentApplicationService.listStudentApplicationsForSection(1L)
                .collectList().block();

        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(studentApplicationRepository).findRequestsForSection(1L);
    }

    // ==================== APPROVE APPLICATION TESTS ====================

    @Test
    @DisplayName("approveStudentApplication - Should approve successfully for Admin")
    void testApproveStudentApplication_Admin_ApprovesSuccessfully() {
        setUpUserMocking(null); // Admin

        StudentApplication approvedApplication = StudentApplication.builder()
                .id(1L)
                .userId(100L)
                .courseId(10L)
                .sectionId(1L)
                .semesterId(1L)
                .statusId(STATUS_APPROVED_ID)
                .build();

        when(studentApplicationRepository.findById(1L)).thenReturn(Mono.just(testStudentApplication));
        when(studentApplicationRepository.save(any(StudentApplication.class))).thenReturn(Mono.just(approvedApplication));

        assertDoesNotThrow(() -> studentApplicationService.approveStudentApplication(1L).block());
        verify(studentApplicationRepository).findById(1L);
        verify(studentApplicationRepository).save(any(StudentApplication.class));
    }

    @Test
    @DisplayName("approveStudentApplication - Should approve successfully for Section user")
    void testApproveStudentApplication_SectionUser_ApprovesSuccessfully() {
        setUpUserMocking(1L); // Section 1

        StudentApplication approvedApplication = StudentApplication.builder()
                .id(1L)
                .userId(100L)
                .courseId(10L)
                .sectionId(1L)
                .semesterId(1L)
                .statusId(STATUS_APPROVED_ID)
                .build();

        when(studentApplicationRepository.findById(1L)).thenReturn(Mono.just(testStudentApplication));
        when(studentApplicationRepository.save(any(StudentApplication.class))).thenReturn(Mono.just(approvedApplication));

        assertDoesNotThrow(() -> studentApplicationService.approveStudentApplication(1L).block());
        verify(studentApplicationRepository, atLeast(1)).findById(1L);
        verify(studentApplicationRepository, times(1)).save(any(StudentApplication.class));
    }

    @Test
    @DisplayName("approveStudentApplication - When different section throws exception")
    void testApproveStudentApplication_DifferentSection_ThrowsException() {
        setUpUserMocking(2L); // Section 2

        when(studentApplicationRepository.findById(1L)).thenReturn(Mono.just(testStudentApplication));

        Mono<Void> result = studentApplicationService.approveStudentApplication(1L);

        assertThrows(StudentApplicationNotFoundException.class, result::block);
        verify(studentApplicationRepository, atLeast(1)).findById(1L);
        verify(studentApplicationRepository, never()).save(any());
    }

    @Test
    @DisplayName("approveStudentApplication - When application not found throws exception")
    void testApproveStudentApplication_NotFound_ThrowsException() {
        when(studentApplicationRepository.findById(99L)).thenReturn(Mono.empty());

        Mono<Void> result = studentApplicationService.approveStudentApplication(99L);

        assertThrows(StudentApplicationNotFoundException.class, result::block);
        verify(studentApplicationRepository).findById(99L);
        verify(studentApplicationRepository, never()).save(any());
    }

    // ==================== REJECT APPLICATION TESTS ====================

    @Test
    @DisplayName("rejectStudentApplication - Should reject successfully for Admin")
    void testRejectStudentApplication_Admin_RejectsSuccessfully() {
        setUpUserMocking(null); // Admin

        StudentApplication rejectedApplication = StudentApplication.builder()
                .id(1L)
                .userId(100L)
                .courseId(10L)
                .sectionId(1L)
                .semesterId(1L)
                .statusId(STATUS_REJECTED_ID)
                .build();

        when(studentApplicationRepository.findById(1L)).thenReturn(Mono.just(testStudentApplication));
        when(studentApplicationRepository.save(any(StudentApplication.class))).thenReturn(Mono.just(rejectedApplication));

        assertDoesNotThrow(() -> studentApplicationService.rejectStudentApplication(1L).block());
        verify(studentApplicationRepository).findById(1L);
        verify(studentApplicationRepository).save(any(StudentApplication.class));
    }

    @Test
    @DisplayName("rejectStudentApplication - Should reject successfully for Section user")
    void testRejectStudentApplication_SectionUser_RejectsSuccessfully() {
        setUpUserMocking(1L); // Section 1

        StudentApplication rejectedApplication = StudentApplication.builder()
                .id(1L)
                .userId(100L)
                .courseId(10L)
                .sectionId(1L)
                .semesterId(1L)
                .statusId(STATUS_REJECTED_ID)
                .build();

        when(studentApplicationRepository.findById(1L)).thenReturn(Mono.just(testStudentApplication));
        when(studentApplicationRepository.save(any(StudentApplication.class))).thenReturn(Mono.just(rejectedApplication));

        assertDoesNotThrow(() -> studentApplicationService.rejectStudentApplication(1L).block());
            verify(studentApplicationRepository, atLeast(1)).findById(1L);
            verify(studentApplicationRepository, times(1)).save(any(StudentApplication.class));
    }

    @Test
    @DisplayName("rejectStudentApplication - When different section throws exception")
    void testRejectStudentApplication_DifferentSection_ThrowsException() {
        setUpUserMocking(2L); // Section 2

        when(studentApplicationRepository.findById(1L)).thenReturn(Mono.just(testStudentApplication));
        Mono<Void> result = studentApplicationService.rejectStudentApplication(1L);

        assertThrows(StudentApplicationNotFoundException.class, result::block);
        verify(studentApplicationRepository, atLeast(1)).findById(1L);
        verify(studentApplicationRepository, never()).save(any());
    }

    @Test
    @DisplayName("rejectStudentApplication - When application not found throws exception")
    void testRejectStudentApplication_NotFound_ThrowsException() {
        when(studentApplicationRepository.findById(99L)).thenReturn(Mono.empty());

        Mono<Void> result = studentApplicationService.rejectStudentApplication(99L);

        assertThrows(StudentApplicationNotFoundException.class, result::block);
        verify(studentApplicationRepository).findById(99L);
        verify(studentApplicationRepository, never()).save(any());
    }

    // ==================== IS APPLICATION OF SECTION TESTS ====================

    @Test
    @DisplayName("isApplicationOfSection - When application has direct section ID returns true")
    void testIsApplicationOfSection_DirectSectionId_ReturnsTrue() {
        when(studentApplicationRepository.findById(1L)).thenReturn(Mono.just(testStudentApplication));

        Boolean result = studentApplicationService.isApplicationOfSection(1L, 1L).block();

        assertTrue(result);
    }

    @Test
    @DisplayName("isApplicationOfSection - When application has course with section returns true")
    void testIsApplicationOfSection_CourseSectionId_ReturnsTrue() {
        StudentApplication appWithCourse = StudentApplication.builder()
                .id(1L)
                .courseId(10L)
                .sectionId(null)
                .build();

        when(studentApplicationRepository.findById(1L)).thenReturn(Mono.just(appWithCourse));
        when(courseService.getCourseSectionId(10L)).thenReturn(Mono.just(1L));

        Boolean result = studentApplicationService.isApplicationOfSection(1L, 1L).block();

        assertTrue(result);
    }

    @Test
    @DisplayName("isApplicationOfSection - When application not in section returns false")
    void testIsApplicationOfSection_DifferentSection_ReturnsFalse() {
        when(studentApplicationRepository.findById(1L)).thenReturn(Mono.just(testStudentApplication));

        Boolean result = studentApplicationService.isApplicationOfSection(1L, 2L).block();

        assertFalse(result);
    }

    @Test
    @DisplayName("isApplicationOfSection - When application not found returns false")
    void testIsApplicationOfSection_NotFound_ReturnsFalse() {
        when(studentApplicationRepository.findById(99L)).thenReturn(Mono.empty());

        Boolean result = studentApplicationService.isApplicationOfSection(99L, 1L).block();

        assertFalse(result);
    }
}
