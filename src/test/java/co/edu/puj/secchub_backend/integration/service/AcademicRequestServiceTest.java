package co.edu.puj.secchub_backend.integration.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
import co.edu.puj.secchub_backend.integration.dto.AcademicRequestBatchRequestDTO;
import co.edu.puj.secchub_backend.integration.dto.AcademicRequestRequestDTO;
import co.edu.puj.secchub_backend.integration.dto.AcademicRequestResponseDTO;
import co.edu.puj.secchub_backend.integration.dto.CombinedRequestDTO;
import co.edu.puj.secchub_backend.integration.dto.ProcessPlanningRequestDTO;
import co.edu.puj.secchub_backend.integration.dto.RequestScheduleRequestDTO;
import co.edu.puj.secchub_backend.integration.dto.RequestScheduleResponseDTO;
import co.edu.puj.secchub_backend.integration.exception.AcademicRequestNotFound;
import co.edu.puj.secchub_backend.integration.exception.AcademicRequestServerErrorException;
import co.edu.puj.secchub_backend.integration.exception.RequestScheduleNotFound;
import co.edu.puj.secchub_backend.integration.model.AcademicRequest;
import co.edu.puj.secchub_backend.integration.model.RequestSchedule;
import co.edu.puj.secchub_backend.integration.repository.AcademicRequestRepository;
import co.edu.puj.secchub_backend.integration.repository.RequestScheduleRepository;
import co.edu.puj.secchub_backend.security.contract.SecurityModuleUserContract;
import co.edu.puj.secchub_backend.security.contract.UserInformationResponseDTO;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@ExtendWith(MockitoExtension.class)
@DisplayName("AcademicRequestService Unit Test")
class AcademicRequestServiceTest {

    @Mock
    private ModelMapper modelMapper;
    @Mock
    private TransactionalOperator transactionalOperator;
    @Mock
    private AcademicRequestRepository academicRequestRepository;
    @Mock
    private RequestScheduleRepository requestScheduleRepository;
    @Mock
    private SecurityModuleUserContract userService;
    @Mock
    private AdminModuleSemesterContract semesterService;
    @Mock
    private AdminModuleCourseContract courseService;
    @Mock
    private AdminModuleSectionContract sectionService;

    @InjectMocks
    private AcademicRequestService academicRequestService;

    private MockedStatic<ReactiveSecurityContextHolder> mockedReactiveSecurityContextHolder;

    private AcademicRequest testRequest;
    private AcademicRequestResponseDTO testRequestResponseDTO;
    private AcademicRequestRequestDTO testRequestDTO;
    private RequestSchedule testSchedule;
    private RequestScheduleResponseDTO testScheduleResponseDTO;
    private RequestScheduleRequestDTO testScheduleRequestDTO;

    @BeforeEach
    void setUp() {
        // Set up test request
        testRequest = AcademicRequest.builder()
                .id(1L)
                .userId(100L)
                .semesterId(1L)
                .courseId(10L)
                .capacity(30)
                .observation("Test observation")
                .requestDate(LocalDate.now())
                .accepted(false)
                .combined(false)
                .build();

        testRequestResponseDTO = new AcademicRequestResponseDTO();
        testRequestResponseDTO.setId(1L);
        testRequestResponseDTO.setUserId(100L);
        testRequestResponseDTO.setSemesterId(1L);
        testRequestResponseDTO.setCourseId(10L);
        testRequestResponseDTO.setCapacity(30);

        testRequestDTO = new AcademicRequestRequestDTO();
        testRequestDTO.setCourseId(10L);
        testRequestDTO.setCapacity(30);
        testRequestDTO.setObservation("Test observation");

        // Set up test schedule
        testSchedule = RequestSchedule.builder()
                .id(1L)
                .academicRequestId(1L)
                .startTime(java.sql.Time.valueOf("08:00:00"))
                .endTime(java.sql.Time.valueOf("10:00:00"))
                .day("Monday")
                .classRoomTypeId(1L)
                .modalityId(1L)
                .disability(false)
                .build();

        testScheduleResponseDTO = new RequestScheduleResponseDTO();
        testScheduleResponseDTO.setId(1L);
        testScheduleResponseDTO.setAcademicRequestId(1L);
        testScheduleResponseDTO.setStartTime("08:00");
        testScheduleResponseDTO.setEndTime("10:00");

        testScheduleRequestDTO = new RequestScheduleRequestDTO();
        testScheduleRequestDTO.setStartTime("08:00");
        testScheduleRequestDTO.setEndTime("10:00");
        testScheduleRequestDTO.setDay("Monday");
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
        SecurityContext securityContext = mock(SecurityContext.class);
        Authentication auth;

        String email = sectionId == null ? "admin@test.com" : "section@test.com";

        if (sectionId == null) {
            // Mock ADMIN user
            auth = new UsernamePasswordAuthenticationToken(
                    email,
                    "password",
                    List.of(new SimpleGrantedAuthority("ROLE_ADMIN"))
            );
        } else {
            // Mock ROLE_SECTION user
            auth = new UsernamePasswordAuthenticationToken(
                    email,
                    "password",
                    List.of(new SimpleGrantedAuthority("ROLE_SECTION"))
            );
        }

        lenient().when(securityContext.getAuthentication()).thenReturn(auth);

        mockedReactiveSecurityContextHolder = mockStatic(ReactiveSecurityContextHolder.class);
        mockedReactiveSecurityContextHolder.when(ReactiveSecurityContextHolder::getContext)
                .thenReturn(Mono.just(securityContext));

        // Mock user service methods - always needed for getCurrentUserId()
        if (sectionId != null) {
            lenient().when(sectionService.getSectionIdByUserId(100L)).thenReturn(Mono.just(sectionId));
        }

        UserInformationResponseDTO userInfo = new UserInformationResponseDTO();
        userInfo.setId(100L);
        userInfo.setEmail(email);
        userInfo.setName("Test");
        userInfo.setLastName("User");

        lenient().when(userService.getUserInformationByEmail(email)).thenReturn(Mono.just(userInfo));
    }

    // ==================== CREATE BATCH TESTS ====================

    @Test
    @DisplayName("createAcademicRequestBatch - Should create requests with schedules successfully")
    void testCreateAcademicRequestBatch_WithSchedules_Success() {
        setUpUserMocking(null);

        when(transactionalOperator.transactional(ArgumentMatchers.<Flux<AcademicRequest>>any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(userService.getUserIdByEmail("admin@test.com")).thenReturn(Mono.just(100L));

        AcademicRequestBatchRequestDTO batchDTO = new AcademicRequestBatchRequestDTO();
        batchDTO.setRequests(List.of(testRequestDTO));
        testRequestDTO.setSchedules(List.of(testScheduleRequestDTO));

        when(semesterService.getCurrentSemesterId()).thenReturn(Mono.just(1L));
        when(modelMapper.map(testRequestDTO, AcademicRequest.class)).thenReturn(testRequest);
        when(academicRequestRepository.save(any(AcademicRequest.class))).thenReturn(Mono.just(testRequest));
        when(modelMapper.map(testScheduleRequestDTO, RequestSchedule.class)).thenReturn(testSchedule);
        when(requestScheduleRepository.save(any(RequestSchedule.class))).thenReturn(Mono.just(testSchedule));
        when(modelMapper.map(testSchedule, RequestScheduleResponseDTO.class)).thenReturn(testScheduleResponseDTO);
        when(modelMapper.map(testRequest, AcademicRequestResponseDTO.class)).thenReturn(testRequestResponseDTO);
        when(courseService.getCourseName(10L)).thenReturn(Mono.just("Test Course"));

        List<AcademicRequestResponseDTO> result = academicRequestService.createAcademicRequestBatch(batchDTO)
                .collectList()
                .block();

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(academicRequestRepository).save(any(AcademicRequest.class));
        verify(requestScheduleRepository).save(any(RequestSchedule.class));
    }

    @Test
    @DisplayName("createAcademicRequestBatch - Should create requests without schedules successfully")
    void testCreateAcademicRequestBatch_WithoutSchedules_Success() {
        setUpUserMocking(null);

        when(transactionalOperator.transactional(ArgumentMatchers.<Flux<AcademicRequest>>any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(userService.getUserIdByEmail("admin@test.com")).thenReturn(Mono.just(100L));

        AcademicRequestBatchRequestDTO batchDTO = new AcademicRequestBatchRequestDTO();
        batchDTO.setRequests(List.of(testRequestDTO));

        when(semesterService.getCurrentSemesterId()).thenReturn(Mono.just(1L));
        when(modelMapper.map(testRequestDTO, AcademicRequest.class)).thenReturn(testRequest);
        when(academicRequestRepository.save(any(AcademicRequest.class))).thenReturn(Mono.just(testRequest));
        when(modelMapper.map(testRequest, AcademicRequestResponseDTO.class)).thenReturn(testRequestResponseDTO);
        when(courseService.getCourseName(10L)).thenReturn(Mono.just("Test Course"));

        List<AcademicRequestResponseDTO> result = academicRequestService.createAcademicRequestBatch(batchDTO)
                .collectList()
                .block();

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(academicRequestRepository).save(any(AcademicRequest.class));
        verify(requestScheduleRepository, never()).saveAll(anyList());
    }

    @Test
    @DisplayName("createAcademicRequestBatch - When semester service fails throws exception")
    void testCreateAcademicRequestBatch_SemesterServiceFails_ThrowsException() {
        when(transactionalOperator.transactional(ArgumentMatchers.<Flux<AcademicRequest>>any())).thenAnswer(invocation -> invocation.getArgument(0));
        AcademicRequestBatchRequestDTO batchDTO = new AcademicRequestBatchRequestDTO();
        batchDTO.setRequests(List.of(testRequestDTO));

        when(semesterService.getCurrentSemesterId()).thenReturn(Mono.error(new RuntimeException("Semester error")));

        Flux<AcademicRequestResponseDTO> result = academicRequestService.createAcademicRequestBatch(batchDTO);

        assertThrows(AcademicRequestServerErrorException.class, result::blockFirst);
    }

    // ==================== FIND REQUESTS TESTS ====================

    @Test
    @DisplayName("findCurrentSemesterAcademicRequests - Should return requests for Admin")
    void testFindCurrentSemesterAcademicRequests_Admin_ReturnsRequests() {
        setUpUserMocking(null);

        when(semesterService.getCurrentSemesterId()).thenReturn(Mono.just(1L));
        when(academicRequestRepository.findBySemesterId(1L)).thenReturn(Flux.just(testRequest));
        when(requestScheduleRepository.findByAcademicRequestId(1L)).thenReturn(Flux.just(testSchedule));
        when(modelMapper.map(testSchedule, RequestScheduleResponseDTO.class)).thenReturn(testScheduleResponseDTO);
        when(modelMapper.map(testRequest, AcademicRequestResponseDTO.class)).thenReturn(testRequestResponseDTO);
        when(courseService.getCourseName(10L)).thenReturn(Mono.just("Test Course"));

        List<AcademicRequestResponseDTO> result = academicRequestService.findCurrentSemesterAcademicRequests()
                .collectList()
                .block();

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(academicRequestRepository).findBySemesterId(1L);
    }

    @Test
    @DisplayName("findCurrentSemesterAcademicRequests - Should return requests for Section user")
    void testFindCurrentSemesterAcademicRequests_SectionUser_ReturnsRequests() {
        setUpUserMocking(1L);

        when(userService.getUserIdByEmail("section@test.com")).thenReturn(Mono.just(100L));
        when(semesterService.getCurrentSemesterId()).thenReturn(Mono.just(1L));
        when(academicRequestRepository.findBySemesterId(1L)).thenReturn(Flux.just(testRequest));
        when(courseService.getCourseSectionId(10L)).thenReturn(Mono.just(1L));
        when(requestScheduleRepository.findByAcademicRequestId(1L)).thenReturn(Flux.just(testSchedule));
        when(modelMapper.map(testSchedule, RequestScheduleResponseDTO.class)).thenReturn(testScheduleResponseDTO);
        when(modelMapper.map(testRequest, AcademicRequestResponseDTO.class)).thenReturn(testRequestResponseDTO);
        when(courseService.getCourseName(10L)).thenReturn(Mono.just("Test Course"));

        List<AcademicRequestResponseDTO> result = academicRequestService.findCurrentSemesterAcademicRequests()
                .collectList()
                .block();

        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    @DisplayName("findCurrentSemesterAcademicRequests - Should filter out requests from different section")
    void testFindCurrentSemesterAcademicRequests_DifferentSection_FiltersOut() {
        setUpUserMocking(2L);

        when(userService.getUserIdByEmail("section@test.com")).thenReturn(Mono.just(100L));
        when(semesterService.getCurrentSemesterId()).thenReturn(Mono.just(1L));
        when(academicRequestRepository.findBySemesterId(1L)).thenReturn(Flux.just(testRequest));
        when(courseService.getCourseSectionId(10L)).thenReturn(Mono.just(1L));

        List<AcademicRequestResponseDTO> result = academicRequestService.findCurrentSemesterAcademicRequests()
                .collectList()
                .block();

        assertNotNull(result);
        assertEquals(0, result.size());
    }

    @Test
    @DisplayName("findAllAcademicRequests - Should return all requests for Admin")
    void testFindAllAcademicRequests_Admin_ReturnsRequests() {
        setUpUserMocking(null);

        when(academicRequestRepository.findAll()).thenReturn(Flux.just(testRequest));
        when(requestScheduleRepository.findByAcademicRequestId(1L)).thenReturn(Flux.just(testSchedule));
        when(modelMapper.map(testSchedule, RequestScheduleResponseDTO.class)).thenReturn(testScheduleResponseDTO);
        when(modelMapper.map(testRequest, AcademicRequestResponseDTO.class)).thenReturn(testRequestResponseDTO);
        when(courseService.getCourseName(10L)).thenReturn(Mono.just("Test Course"));

        List<AcademicRequestResponseDTO> result = academicRequestService.findAllAcademicRequests()
                .collectList()
                .block();

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(academicRequestRepository).findAll();
    }

    @Test
    @DisplayName("findAcademicRequestById - Should return request for Admin")
    void testFindAcademicRequestById_Admin_ReturnsRequest() {
        setUpUserMocking(null);

        when(academicRequestRepository.findById(1L)).thenReturn(Mono.just(testRequest));
        when(requestScheduleRepository.findByAcademicRequestId(1L)).thenReturn(Flux.just(testSchedule));
        when(modelMapper.map(testSchedule, RequestScheduleResponseDTO.class)).thenReturn(testScheduleResponseDTO);
        when(modelMapper.map(testRequest, AcademicRequestResponseDTO.class)).thenReturn(testRequestResponseDTO);
        when(courseService.getCourseName(10L)).thenReturn(Mono.just("Test Course"));

        AcademicRequestResponseDTO result = academicRequestService.findAcademicRequestById(1L).block();
        assertNotNull(result);
        verify(academicRequestRepository).findById(1L);
    }

    @Test
    @DisplayName("findAcademicRequestById - When request not found throws exception")
    void testFindAcademicRequestById_NotFound_ThrowsException() {
        setUpUserMocking(null);

        when(academicRequestRepository.findById(1L)).thenReturn(Mono.empty());

        Mono<AcademicRequestResponseDTO> result = academicRequestService.findAcademicRequestById(1L);
        assertThrows(AcademicRequestNotFound.class, result::block);
    }

    // ==================== DELETE REQUEST TESTS ====================

    @Test
    @DisplayName("deleteAcademicRequest - Should delete successfully")
    void testDeleteAcademicRequest_Success() {
        when(academicRequestRepository.findById(1L)).thenReturn(Mono.just(testRequest));
        when(academicRequestRepository.deleteById(1L)).thenReturn(Mono.empty());

        academicRequestService.deleteAcademicRequest(1L).block();

        verify(academicRequestRepository).deleteById(1L);
    }

    @Test
    @DisplayName("deleteAcademicRequest - When request not found throws exception")
    void testDeleteAcademicRequest_NotFound_ThrowsException() {
        when(academicRequestRepository.findById(1L)).thenReturn(Mono.empty());

        Mono<Void> result = academicRequestService.deleteAcademicRequest(1L);
        assertThrows(AcademicRequestNotFound.class, result::block);
    }

    // ==================== UPDATE REQUEST TESTS ====================

    @Test
    @DisplayName("updateAcademicRequest - Should update successfully")
    void testUpdateAcademicRequest_Success() {
        setUpUserMocking(null);
        
        org.modelmapper.config.Configuration modelMapperConfig = mock(org.modelmapper.config.Configuration.class);
        when(modelMapper.getConfiguration()).thenReturn(modelMapperConfig);
        when(modelMapperConfig.setPropertyCondition(any())).thenReturn(modelMapperConfig);
        
        when(academicRequestRepository.findById(1L)).thenReturn(Mono.just(testRequest));
        // Mock the mapping from DTO to entity (this updates the entity in place, so we use doAnswer)
        doAnswer(invocation -> {
            AcademicRequestRequestDTO source = invocation.getArgument(0);
            AcademicRequest target = invocation.getArgument(1);
            target.setCourseId(source.getCourseId());
            target.setCapacity(source.getCapacity());
            target.setObservation(source.getObservation());
            return null;
        }).when(modelMapper).map(any(AcademicRequestRequestDTO.class), any(AcademicRequest.class));
        
        when(academicRequestRepository.save(any(AcademicRequest.class))).thenReturn(Mono.just(testRequest));
        when(modelMapper.map(testRequest, AcademicRequestResponseDTO.class)).thenReturn(testRequestResponseDTO);
        when(courseService.getCourseName(10L)).thenReturn(Mono.just("Test Course"));

        AcademicRequestResponseDTO result = academicRequestService.updateAcademicRequest(1L, testRequestDTO).block();
        
        assertNotNull(result);
        verify(academicRequestRepository).save(any(AcademicRequest.class));
    }

    @Test
    @DisplayName("updateAcademicRequest - When request not found throws exception")
    void testUpdateAcademicRequest_NotFound_ThrowsException() {
        when(academicRequestRepository.findById(1L)).thenReturn(Mono.empty());

        Mono<AcademicRequestResponseDTO> result = academicRequestService.updateAcademicRequest(1L, testRequestDTO);
        assertThrows(AcademicRequestNotFound.class, result::block);
    }

    // ==================== SCHEDULE TESTS ====================

    @Test
    @DisplayName("addRequestSchedule - Should add schedule successfully")
    void testAddRequestSchedule_Success() {
        when(academicRequestRepository.findById(1L)).thenReturn(Mono.just(testRequest));
        when(modelMapper.map(testScheduleRequestDTO, RequestSchedule.class)).thenReturn(testSchedule);
        when(requestScheduleRepository.save(any(RequestSchedule.class))).thenReturn(Mono.just(testSchedule));
        when(modelMapper.map(testSchedule, RequestScheduleResponseDTO.class)).thenReturn(testScheduleResponseDTO);

        RequestScheduleResponseDTO result = academicRequestService.addRequestSchedule(1L, testScheduleRequestDTO).block();

        assertNotNull(result);
        verify(requestScheduleRepository).save(any(RequestSchedule.class));
    }

    @Test
    @DisplayName("addRequestSchedule - When request not found throws exception")
    void testAddRequestSchedule_RequestNotFound_ThrowsException() {
        when(academicRequestRepository.findById(1L)).thenReturn(Mono.empty());

        Mono<RequestScheduleResponseDTO> result = academicRequestService.addRequestSchedule(1L, testScheduleRequestDTO);
        assertThrows(AcademicRequestNotFound.class, result::block);
    }

    @Test
    @DisplayName("findRequestSchedulesByAcademicRequestId - Should return schedules")
    void testFindRequestSchedulesByAcademicRequestId_ReturnsSchedules() {
        when(academicRequestRepository.findById(1L)).thenReturn(Mono.just(testRequest));
        when(requestScheduleRepository.findByAcademicRequestId(1L)).thenReturn(Flux.just(testSchedule));
        when(modelMapper.map(testSchedule, RequestScheduleResponseDTO.class)).thenReturn(testScheduleResponseDTO);

        List<RequestScheduleResponseDTO> result = academicRequestService
                .findRequestSchedulesByAcademicRequestId(1L)
                .collectList()
                .block();

        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    @DisplayName("updateRequestSchedule - Should update successfully")
    void testUpdateRequestSchedule_Success() {
        org.modelmapper.config.Configuration modelMapperConfig = mock(org.modelmapper.config.Configuration.class);
        when(modelMapper.getConfiguration()).thenReturn(modelMapperConfig);
        when(modelMapperConfig.setPropertyCondition(any())).thenReturn(modelMapperConfig);
        
        when(requestScheduleRepository.findById(1L)).thenReturn(Mono.just(testSchedule));
        // Mock the mapping from DTO to entity (this updates the entity in place, so we use doAnswer)
        doAnswer(invocation -> {
            RequestScheduleRequestDTO source = invocation.getArgument(0);
            RequestSchedule target = invocation.getArgument(1);
            target.setStartTime(java.sql.Time.valueOf(source.getStartTime() + ":00"));
            target.setEndTime(java.sql.Time.valueOf(source.getEndTime() + ":00"));
            target.setDay(source.getDay());
            return null;
        }).when(modelMapper).map(any(RequestScheduleRequestDTO.class), any(RequestSchedule.class));
        
        when(requestScheduleRepository.save(any(RequestSchedule.class))).thenReturn(Mono.just(testSchedule));
        when(modelMapper.map(testSchedule, RequestScheduleResponseDTO.class)).thenReturn(testScheduleResponseDTO);

        RequestScheduleResponseDTO result = academicRequestService.updateRequestSchedule(1L, testScheduleRequestDTO).block();

        assertNotNull(result);
        verify(requestScheduleRepository).save(any(RequestSchedule.class));
    }

    @Test
    @DisplayName("updateRequestSchedule - When schedule not found throws exception")
    void testUpdateRequestSchedule_NotFound_ThrowsException() {
        when(requestScheduleRepository.findById(1L)).thenReturn(Mono.empty());

        Mono<RequestScheduleResponseDTO> result = academicRequestService.updateRequestSchedule(1L, testScheduleRequestDTO);
        assertThrows(RequestScheduleNotFound.class, result::block);
    }

    @Test
    @DisplayName("deleteRequestSchedule - Should delete successfully")
    void testDeleteRequestSchedule_Success() {
        when(requestScheduleRepository.findById(1L)).thenReturn(Mono.just(testSchedule));
        when(requestScheduleRepository.deleteById(1L)).thenReturn(Mono.empty());

        academicRequestService.deleteRequestSchedule(1L).block();

        verify(requestScheduleRepository).deleteById(1L);
    }

    @Test
    @DisplayName("deleteRequestSchedule - When schedule not found throws exception")
    void testDeleteRequestSchedule_NotFound_ThrowsException() {
        when(requestScheduleRepository.findById(1L)).thenReturn(Mono.empty());

        Mono<Void> result = academicRequestService.deleteRequestSchedule(1L);

        assertThrows(RequestScheduleNotFound.class, result::block);
    }

    @Test
    @DisplayName("patchRequestSchedule - Should patch successfully")
    void testPatchRequestSchedule_Success() {
        Map<String, Object> updates = new HashMap<>();
        updates.put("startTime", "09:00");
        updates.put("endTime", "11:00");

        when(requestScheduleRepository.findById(1L)).thenReturn(Mono.just(testSchedule));
        // Mock the mapping from DTO to entity (this updates the entity in place)
        doAnswer(invocation -> {
            RequestScheduleResponseDTO source = invocation.getArgument(0);
            RequestSchedule target = invocation.getArgument(1);
            if (source.getStartTime() != null) {
                target.setStartTime(java.sql.Time.valueOf(source.getStartTime() + ":00"));
            }
            if (source.getEndTime() != null) {
                target.setEndTime(java.sql.Time.valueOf(source.getEndTime() + ":00"));
            }
            if (source.getDay() != null) {
                target.setDay(source.getDay());
            }
            return null;
        }).when(modelMapper).map(any(RequestScheduleResponseDTO.class), any(RequestSchedule.class));
        
        when(requestScheduleRepository.save(any(RequestSchedule.class))).thenReturn(Mono.just(testSchedule));
        when(modelMapper.map(testSchedule, RequestScheduleResponseDTO.class)).thenReturn(testScheduleResponseDTO);

        RequestScheduleResponseDTO result = academicRequestService.patchRequestSchedule(1L, updates).block();

        assertNotNull(result);
        verify(requestScheduleRepository).save(any(RequestSchedule.class));
    }

    // ==================== MARK TESTS ====================

    @Test
    @DisplayName("markAsAccepted - Should mark request as accepted")
    void testMarkAsAccepted_Success() {
        when(academicRequestRepository.findById(1L)).thenReturn(Mono.just(testRequest));
        when(academicRequestRepository.save(any(AcademicRequest.class))).thenReturn(Mono.just(testRequest));

        academicRequestService.markAsAccepted(1L).block();

        verify(academicRequestRepository).save(argThat(AcademicRequest::getAccepted));
    }

    @Test
    @DisplayName("markAsAccepted - When request not found throws exception")
    void testMarkAsAccepted_NotFound_ThrowsException() {
        when(academicRequestRepository.findById(1L)).thenReturn(Mono.empty());

        Mono<Void> result = academicRequestService.markAsAccepted(1L);

        assertThrows(AcademicRequestNotFound.class, result::block);
    }

    @Test
    @DisplayName("markAsCombined - Should mark request as combined")
    void testMarkAsCombined_Success() {
        when(academicRequestRepository.findById(1L)).thenReturn(Mono.just(testRequest));
        when(academicRequestRepository.save(any(AcademicRequest.class))).thenReturn(Mono.just(testRequest));

        academicRequestService.markAsCombined(1L).block();

        verify(academicRequestRepository).save(argThat(AcademicRequest::getCombined));
    }

    @Test
    @DisplayName("markMultipleAsAccepted - Should mark multiple requests as accepted")
    void testMarkMultipleAsAccepted_Success() {
        List<Long> ids = Arrays.asList(1L, 2L);
        AcademicRequest request2 = AcademicRequest.builder().id(2L).build();

        when(academicRequestRepository.findAllById(ids)).thenReturn(Flux.just(testRequest, request2));
        when(academicRequestRepository.saveAll(anyList())).thenReturn(Flux.just(testRequest, request2));

        academicRequestService.markMultipleAsAccepted(ids).block();

        verify(academicRequestRepository).saveAll(anyList());
    }

    @Test
    @DisplayName("markMultipleAsCombined - Should mark multiple requests as combined")
    void testMarkMultipleAsCombined_Success() {
        List<Long> ids = Arrays.asList(1L, 2L);
        AcademicRequest request2 = AcademicRequest.builder().id(2L).build();

        when(academicRequestRepository.findAllById(ids)).thenReturn(Flux.just(testRequest, request2));
        when(academicRequestRepository.saveAll(anyList())).thenReturn(Flux.just(testRequest, request2));

        academicRequestService.markMultipleAsCombined(ids).block();

        verify(academicRequestRepository).saveAll(anyList());
    }

    @Test
    @DisplayName("markMultipleAsAccepted - When empty list returns empty")
    void testMarkMultipleAsAccepted_EmptyList_ReturnsEmpty() {
        academicRequestService.markMultipleAsAccepted(List.of()).block();

        verify(academicRequestRepository, never()).findAllById(anyList());
    }

    // ==================== PROCESS PLANNING TESTS ====================

    @Test
    @DisplayName("processPlanningRequests - Should process successfully")
    void testProcessPlanningRequests_Success() {
        ProcessPlanningRequestDTO planningDTO = new ProcessPlanningRequestDTO();
        CombinedRequestDTO combinedRequest = new CombinedRequestDTO();
        combinedRequest.setSourceIds(Arrays.asList(1L, 2L));
        combinedRequest.setPrograms(Arrays.asList("Program1", "Program2"));
        combinedRequest.setMaterias(Arrays.asList("Math", "Physics"));
        combinedRequest.setCupos(50);
        planningDTO.setCombinedRequests(List.of(combinedRequest));

        when(academicRequestRepository.findById(anyLong())).thenReturn(Mono.just(testRequest));
        when(academicRequestRepository.save(any(AcademicRequest.class))).thenReturn(Mono.just(testRequest));

        Map<String, Object> result = academicRequestService.processPlanningRequests(planningDTO).block();

        assertNotNull(result);
        assertTrue((Boolean) result.get("success"));
        verify(academicRequestRepository, atLeastOnce()).save(any(AcademicRequest.class));
    }
}
