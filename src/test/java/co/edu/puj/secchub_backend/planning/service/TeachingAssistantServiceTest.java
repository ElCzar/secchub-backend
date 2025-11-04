package co.edu.puj.secchub_backend.planning.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.sql.Time;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
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

import co.edu.puj.secchub_backend.admin.contract.AdminModuleSectionContract;
import co.edu.puj.secchub_backend.integration.contract.IntegrationModuleStudentApplicationContract;
import co.edu.puj.secchub_backend.planning.dto.TeachingAssistantRequestDTO;
import co.edu.puj.secchub_backend.planning.dto.TeachingAssistantResponseDTO;
import co.edu.puj.secchub_backend.planning.dto.TeachingAssistantScheduleRequestDTO;
import co.edu.puj.secchub_backend.planning.dto.TeachingAssistantScheduleResponseDTO;
import co.edu.puj.secchub_backend.planning.exception.TeachingAssistantBadRequestException;
import co.edu.puj.secchub_backend.planning.exception.TeachingAssistantNotFoundException;
import co.edu.puj.secchub_backend.planning.exception.TeachingAssistantServerErrorException;
import co.edu.puj.secchub_backend.planning.model.TeachingAssistant;
import co.edu.puj.secchub_backend.planning.model.TeachingAssistantSchedule;
import co.edu.puj.secchub_backend.planning.repository.TeachingAssistantRepository;
import co.edu.puj.secchub_backend.planning.repository.TeachingAssistantScheduleRepository;
import co.edu.puj.secchub_backend.security.contract.SecurityModuleUserContract;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@ExtendWith(MockitoExtension.class)
@DisplayName("TeachingAssistantService Unit Test")
class TeachingAssistantServiceTest {

    @Mock
    private TeachingAssistantRepository teachingAssistantRepository;

    @Mock
    private TeachingAssistantScheduleRepository scheduleRepository;

    @Mock
    private ModelMapper modelMapper;

    @Mock
    private SecurityModuleUserContract userService;

    @Mock
    private AdminModuleSectionContract sectionService;

    @Mock
    private IntegrationModuleStudentApplicationContract studentApplicationService;

    @Mock
    private TransactionalOperator transactionalOperator;

    @InjectMocks
    private TeachingAssistantService teachingAssistantService;

    private MockedStatic<ReactiveSecurityContextHolder> mockedReactiveSecurityContextHolder;

    private TeachingAssistant testTeachingAssistant;
    private TeachingAssistantResponseDTO testTeachingAssistantResponseDTO;
    private TeachingAssistantRequestDTO testTeachingAssistantRequestDTO;
    private TeachingAssistantSchedule testSchedule;
    private TeachingAssistantScheduleResponseDTO testScheduleResponseDTO;
    private TeachingAssistantScheduleRequestDTO testScheduleRequestDTO;

    @BeforeEach
    void setUp() {
        testTeachingAssistant = TeachingAssistant.builder()
                .id(1L)
                .classId(1L)
                .studentApplicationId(1L)
                .weeklyHours(10L)
                .weeks(15L)
                .totalHours(150L)
                .build();

        testTeachingAssistantResponseDTO = TeachingAssistantResponseDTO.builder()
                .id(1L)
                .classId(1L)
                .studentApplicationId(1L)
                .weeklyHours(10L)
                .weeks(15L)
                .totalHours(150L)
                .build();

        testTeachingAssistantRequestDTO = TeachingAssistantRequestDTO.builder()
                .classId(1L)
                .studentApplicationId(1L)
                .weeklyHours(10L)
                .weeks(15L)
                .totalHours(150L)
                .build();

        testSchedule = TeachingAssistantSchedule.builder()
                .id(1L)
                .teachingAssistantId(1L)
                .day("Monday")
                .startTime(Time.valueOf(LocalTime.of(9, 0)))
                .endTime(Time.valueOf(LocalTime.of(11, 0)))
                .build();

        testScheduleResponseDTO = TeachingAssistantScheduleResponseDTO.builder()
                .id(1L)
                .teachingAssistantId(1L)
                .day("Monday")
                .startTime(String.valueOf(Time.valueOf(LocalTime.of(9, 0))))
                .endTime(String.valueOf(Time.valueOf(LocalTime.of(11, 0))))
                .build();

        testScheduleRequestDTO = TeachingAssistantScheduleRequestDTO.builder()
                .day("Monday")
                .startTime(String.valueOf(Time.valueOf(LocalTime.of(9, 0))))
                .endTime(String.valueOf(Time.valueOf(LocalTime.of(11, 0))))
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
            ((MockedStatic<?>) mockedReactiveSecurityContextHolder).close();
        }
    }

    // =================== CREATE TEACHING ASSISTANT ====================

    @Test
    @DisplayName("createTeachingAssistant - Create Teaching Assistant Successfully with schedules")
    void createTeachingAssistant_SuccessfulWithSchedules() {
        List<TeachingAssistantScheduleRequestDTO> scheduleRequestDTOs = Arrays.asList(testScheduleRequestDTO);
        testTeachingAssistantRequestDTO.setSchedules(scheduleRequestDTOs);

        when(modelMapper.map(testTeachingAssistantRequestDTO, TeachingAssistant.class))
                .thenReturn(testTeachingAssistant);
        when(teachingAssistantRepository.save(testTeachingAssistant))
                .thenReturn(Mono.just(testTeachingAssistant));
        when(modelMapper.map(testTeachingAssistant, TeachingAssistantResponseDTO.class))
                .thenReturn(testTeachingAssistantResponseDTO);
        when(scheduleRepository.saveAll(ArgumentMatchers.<Flux<TeachingAssistantSchedule>>any()))
                .thenReturn(Flux.just(testSchedule));
        when(modelMapper.map(testSchedule, TeachingAssistantScheduleResponseDTO.class))
                .thenReturn(testScheduleResponseDTO);

        TeachingAssistantResponseDTO result = teachingAssistantService.createTeachingAssistant(testTeachingAssistantRequestDTO).block();

        assertEquals(testTeachingAssistantResponseDTO, result);
        assertEquals(1, result.getSchedules().size());
        assertEquals(testScheduleResponseDTO, result.getSchedules().get(0));

        verify(teachingAssistantRepository, times(1)).save(testTeachingAssistant);
        verify(scheduleRepository, times(1)).saveAll(ArgumentMatchers.<Flux<TeachingAssistantSchedule>>any());
    }

    @Test
    @DisplayName("createTeachingAssistant - Create Teaching Assistant Successfully without schedules")
    void createTeachingAssistant_SuccessfulWithoutSchedules() {
        when(modelMapper.map(testTeachingAssistantRequestDTO, TeachingAssistant.class))
                .thenReturn(testTeachingAssistant);
        when(teachingAssistantRepository.save(testTeachingAssistant))
                .thenReturn(Mono.just(testTeachingAssistant));
        when(modelMapper.map(testTeachingAssistant, TeachingAssistantResponseDTO.class))
                .thenReturn(testTeachingAssistantResponseDTO);

        TeachingAssistantResponseDTO result = teachingAssistantService.createTeachingAssistant(testTeachingAssistantRequestDTO).block();

        assertEquals(testTeachingAssistantResponseDTO, result);
        assertNull(result.getSchedules());
    }

    @Test
    @DisplayName("createTeachingAssistant - Handle error during Teaching Assistant creation")
    void createTeachingAssistant_HandleError() {
        when(modelMapper.map(testTeachingAssistantRequestDTO, TeachingAssistant.class))
                .thenReturn(testTeachingAssistant);
        when(teachingAssistantRepository.save(testTeachingAssistant))
                .thenReturn(Mono.error(new RuntimeException("Database error")));

        Mono<TeachingAssistantResponseDTO> resultMono = teachingAssistantService.createTeachingAssistant(testTeachingAssistantRequestDTO);

        assertThrows(TeachingAssistantBadRequestException.class, resultMono::block);
    }

    // ==================== UPDATE TEACHING ASSISTANT ====================

    @ParameterizedTest(name = "User with role {0} updates Teaching Assistant of its section successfully")
    @ValueSource(longs = {1L, 2L, 3L})
    @DisplayName("updateTeachingAssistant - Update Teaching Assistant Successfully with same user sections")
    void updateTeachingAssistant_Successful(Long userSection) {
        setupSecurityContext("ROLE_USER");
        List<TeachingAssistant> teachingAssistants = setUpUserMocking(userSection);
        Long teachingAssistantId = 1L;

        when(teachingAssistantRepository.findById(teachingAssistantId))
                .thenReturn(Mono.just(teachingAssistants.get(0)));
        when(teachingAssistantRepository.save(any(TeachingAssistant.class)))
                .thenReturn(Mono.just(teachingAssistants.get(0)));
        when(modelMapper.map(teachingAssistants.get(0), TeachingAssistantResponseDTO.class))
                .thenReturn(testTeachingAssistantResponseDTO);

        TeachingAssistantResponseDTO result = teachingAssistantService.updateTeachingAssistant(teachingAssistantId, testTeachingAssistantRequestDTO).block();

        assertEquals(testTeachingAssistantResponseDTO, result);
        verify(teachingAssistantRepository, times(1)).findById(teachingAssistantId);
        verify(teachingAssistantRepository, times(1)).save(any(TeachingAssistant.class));
    }

    @ParameterizedTest(name = "User with role {0} tries to update Teaching Assistant of different section and fails")
    @ValueSource(longs = {1L, 2L, 3L})
    @DisplayName("updateTeachingAssistant - Update Teaching Assistant Fails with different user sections")
    void updateTeachingAssistant_FailsDifferentSection(Long userSection) {
        setupSecurityContext("ROLE_USER");
        List<TeachingAssistant> teachingAssistants = setUpUserMocking(userSection);
        Long teachingAssistantId = 2L; // Different section

        when(teachingAssistantRepository.findById(teachingAssistantId))
                .thenReturn(Mono.just(teachingAssistants.get(1)));
        
        Mono<TeachingAssistantResponseDTO> resultMono = teachingAssistantService.updateTeachingAssistant(teachingAssistantId, testTeachingAssistantRequestDTO);

        assertThrows(TeachingAssistantNotFoundException.class, resultMono::block);
        verify(teachingAssistantRepository, times(1)).findById(teachingAssistantId);
        verify(teachingAssistantRepository, times(0)).save(any(TeachingAssistant.class));
    }

    @Test
    @DisplayName("updateTeachingAssistant - Update Teaching Assistant Successfully")
    void updateTeachingAssistant_Successful() {
        setupSecurityContext("ROLE_ADMIN");
        Long teachingAssistantId = 1L;

        when(teachingAssistantRepository.findById(teachingAssistantId))
                .thenReturn(Mono.just(testTeachingAssistant));
        when(teachingAssistantRepository.save(any(TeachingAssistant.class)))
                .thenReturn(Mono.just(testTeachingAssistant));
        when(modelMapper.map(testTeachingAssistant, TeachingAssistantResponseDTO.class))
                .thenReturn(testTeachingAssistantResponseDTO);

        TeachingAssistantResponseDTO result = teachingAssistantService.updateTeachingAssistant(teachingAssistantId, testTeachingAssistantRequestDTO).block();

        assertEquals(testTeachingAssistantResponseDTO, result);

        verify(teachingAssistantRepository, times(1)).findById(teachingAssistantId);
        verify(teachingAssistantRepository, times(1)).save(any(TeachingAssistant.class));
    }

    @Test
    @DisplayName("updateTeachingAssistant - Teaching Assistant Not Found")
    void updateTeachingAssistant_NotFound() {
        Long teachingAssistantId = 1L;

        when(teachingAssistantRepository.findById(teachingAssistantId))
                .thenReturn(Mono.empty());

        Mono<TeachingAssistantResponseDTO> resultMono = teachingAssistantService.updateTeachingAssistant(teachingAssistantId, testTeachingAssistantRequestDTO);

        assertThrows(TeachingAssistantNotFoundException.class, resultMono::block);
        verify(teachingAssistantRepository, times(1)).findById(teachingAssistantId);
        verify(teachingAssistantRepository, times(0)).save(any(TeachingAssistant.class));
    }

    @Test
    @DisplayName("updateTeachingAssistant - Handle error during Teaching Assistant update")
    void updateTeachingAssistant_HandleError() {
        setupSecurityContext("ROLE_ADMIN");
        Long teachingAssistantId = 1L;

        when(teachingAssistantRepository.findById(teachingAssistantId))
                .thenReturn(Mono.just(testTeachingAssistant));
        when(teachingAssistantRepository.save(any(TeachingAssistant.class)))
                .thenReturn(Mono.error(new RuntimeException("Database error")));

        Mono<TeachingAssistantResponseDTO> resultMono = teachingAssistantService.updateTeachingAssistant(teachingAssistantId, testTeachingAssistantRequestDTO);
        assertThrows(TeachingAssistantServerErrorException.class, resultMono::block);
    }

    // =================== DELETE TEACHING ASSISTANT ====================

    @ParameterizedTest(name = "User with role {0} deletes Teaching Assistant of its section successfully")
    @ValueSource(longs = {1L, 2L, 3L})
    @DisplayName("deleteTeachingAssistant - Delete Teaching Assistant Successfully with same user sections")
    void deleteTeachingAssistant_Successful(Long userSection) {
        setupSecurityContext("ROLE_USER");
        List<TeachingAssistant> teachingAssistants = setUpUserMocking(userSection);
        Long teachingAssistantId = 1L;

        when(teachingAssistantRepository.findById(teachingAssistantId))
                .thenReturn(Mono.just(teachingAssistants.get(0)));
        when(teachingAssistantRepository.delete(teachingAssistants.get(0)))
                .thenReturn(Mono.empty());
        when(scheduleRepository.findByTeachingAssistantId(teachingAssistantId))
                .thenReturn(Flux.empty());
        when(scheduleRepository.deleteAll(Flux.empty()))
                .thenReturn(Mono.empty());
        
        assertDoesNotThrow(() -> teachingAssistantService.deleteTeachingAssistant(teachingAssistantId).block());
        verify(teachingAssistantRepository, times(1)).findById(teachingAssistantId);
        verify(teachingAssistantRepository, times(1)).delete(teachingAssistants.get(0));
    }

    @ParameterizedTest(name = "User with role {0} tries to delete Teaching Assistant of different section and fails")
    @ValueSource(longs = {1L, 2L, 3L})
    @DisplayName("deleteTeachingAssistant - Delete Teaching Assistant Fails with different user sections")
    void deleteTeachingAssistant_FailsDifferentSection(Long userSection) {
        setupSecurityContext("ROLE_USER");
        List<TeachingAssistant> teachingAssistants = setUpUserMocking(userSection);
        Long teachingAssistantId = 2L; // Different section

        when(teachingAssistantRepository.findById(teachingAssistantId))
                .thenReturn(Mono.just(teachingAssistants.get(1)));
        Mono<Void> resultMono = teachingAssistantService.deleteTeachingAssistant(teachingAssistantId);
        assertThrows(TeachingAssistantNotFoundException.class, resultMono::block);
        verify(teachingAssistantRepository, times(1)).findById(teachingAssistantId);
        verify(teachingAssistantRepository, times(0)).delete(any(TeachingAssistant.class));
    }

    @Test
    @DisplayName("deleteTeachingAssistant - Delete Teaching Assistant Successfully")
    void deleteTeachingAssistant_Successful() {
        setupSecurityContext("ROLE_ADMIN");
        Long teachingAssistantId = 1L;

        when(teachingAssistantRepository.findById(teachingAssistantId))
                .thenReturn(Mono.just(testTeachingAssistant));
        when(teachingAssistantRepository.delete(testTeachingAssistant))
                .thenReturn(Mono.empty());
        when(scheduleRepository.findByTeachingAssistantId(teachingAssistantId))
                .thenReturn(Flux.empty());
        when(scheduleRepository.deleteAll(Flux.empty()))
                .thenReturn(Mono.empty());

        assertDoesNotThrow(() -> teachingAssistantService.deleteTeachingAssistant(teachingAssistantId).block());
        verify(teachingAssistantRepository, times(1)).findById(teachingAssistantId);
        verify(teachingAssistantRepository, times(1)).delete(testTeachingAssistant);
    }

    @Test
    @DisplayName("deleteTeachingAssistant - Teaching Assistant Not Found")
    void deleteTeachingAssistant_NotFound() {
        Long teachingAssistantId = 1L;

        when(teachingAssistantRepository.findById(teachingAssistantId))
                .thenReturn(Mono.empty());
        Mono<Void> resultMono = teachingAssistantService.deleteTeachingAssistant(teachingAssistantId);
        assertThrows(TeachingAssistantNotFoundException.class, resultMono::block);
        verify(teachingAssistantRepository, times(1)).findById(teachingAssistantId);
        verify(teachingAssistantRepository, times(0)).delete(any(TeachingAssistant.class));
    }

    @Test
    @DisplayName("deleteTeachingAssistant - Handle error during Teaching Assistant deletion")
    void deleteTeachingAssistant_HandleError() {
        setupSecurityContext("ROLE_ADMIN");
        Long teachingAssistantId = 1L;

        when(teachingAssistantRepository.findById(teachingAssistantId))
                .thenReturn(Mono.just(testTeachingAssistant));
        when(teachingAssistantRepository.delete(testTeachingAssistant))
                .thenReturn(Mono.error(new RuntimeException("Database error")));
        when(scheduleRepository.findByTeachingAssistantId(teachingAssistantId))
                .thenReturn(Flux.empty());
        when(scheduleRepository.deleteAll(Flux.empty()))
                .thenReturn(Mono.empty());

        Mono<Void> resultMono = teachingAssistantService.deleteTeachingAssistant(teachingAssistantId);
        assertThrows(TeachingAssistantServerErrorException.class, resultMono::block);
    }

    // ==================== GET TEACHING ASSISTANT ====================

    @ParameterizedTest(name = "findTeachingAssistantById - Gets teaching assistant by id for same section {0}")
    @ValueSource(longs = {1L, 2L, 3L})
    @DisplayName("findTeachingAssitantById - User should obtain teaching assistant by id for their same section")
    void findTeachingAssistantById_Successful_Section(Long userSection) {
        setupSecurityContext("ROLE_USER");
        List<TeachingAssistant> teachingAssistants = setUpUserMocking(userSection);
        Long teachingAssistantIdForTest = 1L;

        when(teachingAssistantRepository.findById(anyLong())).thenAnswer(invocation -> {
            Long teachingAssistantId = invocation.getArgument(0);
            if (teachingAssistantId.equals(1L)) return Mono.just(1L);
            else if (teachingAssistantId.equals(2L)) return Mono.just(2L);
            else if (teachingAssistantId.equals(3L)) return Mono.just(3L);
            else return Mono.empty();
        });
        when(teachingAssistantRepository.findById(teachingAssistantIdForTest))
                .thenReturn(Mono.just(teachingAssistants.get(0)));
        when(modelMapper.map(teachingAssistants.get(0), TeachingAssistantResponseDTO.class))
                .thenReturn(testTeachingAssistantResponseDTO);
        when(scheduleRepository.findByTeachingAssistantId(teachingAssistantIdForTest))
                .thenReturn(Flux.empty());

        TeachingAssistantResponseDTO result = teachingAssistantService.findTeachingAssistantById(teachingAssistantIdForTest).block();

        assertEquals(testTeachingAssistantResponseDTO, result);
        verify(teachingAssistantRepository, times(1)).findById(teachingAssistantIdForTest);
    }

    @ParameterizedTest(name = "findTeachingAssistantById - Fails to get teaching assistant by id for different section {0}")
    @ValueSource(longs = {1L, 2L, 3L})
    @DisplayName("findTeachingAssitantById - User should not obtain teaching assistant by id for different section")
    void findTeachingAssistantById_Fails_DifferentSection(Long userSection) {
        setupSecurityContext("ROLE_USER");
        List<TeachingAssistant> teachingAssistants = setUpUserMocking(userSection);
        Long teachingAssistantId = 2L; // Different section
        when(teachingAssistantRepository.findById(teachingAssistantId))
                .thenReturn(Mono.just(teachingAssistants.get(1)));
        Mono<TeachingAssistantResponseDTO> resultMono = teachingAssistantService.findTeachingAssistantById(teachingAssistantId);

        assertThrows(TeachingAssistantNotFoundException.class, resultMono::block);
        verify(teachingAssistantRepository, times(1)).findById(teachingAssistantId);
    }

    @Test
    @DisplayName("findTeachingAssistantById - Gets teaching assistant by id successfully with no schedule")
    void findTeachingAssistantById_Successful() {
        setupSecurityContext("ROLE_ADMIN");
        Long teachingAssistantIdForTest = 1L;

        when(teachingAssistantRepository.findById(anyLong())).thenAnswer(invocation -> {
            Long teachingAssistantId = invocation.getArgument(0);
            if (teachingAssistantId.equals(1L)) return Mono.just(1L);
            else if (teachingAssistantId.equals(2L)) return Mono.just(2L);
            else if (teachingAssistantId.equals(3L)) return Mono.just(3L);
            else return Mono.empty();
        });

        when(teachingAssistantRepository.findById(teachingAssistantIdForTest))
                .thenReturn(Mono.just(testTeachingAssistant));
        when(modelMapper.map(testTeachingAssistant, TeachingAssistantResponseDTO.class))
                .thenReturn(testTeachingAssistantResponseDTO);
        when(scheduleRepository.findByTeachingAssistantId(teachingAssistantIdForTest))
                .thenReturn(Flux.empty());

        TeachingAssistantResponseDTO result = teachingAssistantService.findTeachingAssistantById(teachingAssistantIdForTest).block();

        assertEquals(testTeachingAssistantResponseDTO, result);
        verify(teachingAssistantRepository, times(1)).findById(teachingAssistantIdForTest);
    }

    @Test
    @DisplayName("findTeachingAssistantById - Gets teaching assistant by id successfully with schedule")
    void findTeachingAssistantById_SuccessfulWithSchedule() {
        setupSecurityContext("ROLE_ADMIN");
        Long teachingAssistantId = 1L;

        when(teachingAssistantRepository.findById(teachingAssistantId))
                .thenReturn(Mono.just(testTeachingAssistant));
        when(modelMapper.map(testTeachingAssistant, TeachingAssistantResponseDTO.class))
                .thenReturn(testTeachingAssistantResponseDTO);
        when(scheduleRepository.findByTeachingAssistantId(teachingAssistantId))
                .thenReturn(Flux.just(testSchedule));
        when(modelMapper.map(testSchedule, TeachingAssistantScheduleResponseDTO.class))
                .thenReturn(testScheduleResponseDTO);

        TeachingAssistantResponseDTO result = teachingAssistantService.findTeachingAssistantById(teachingAssistantId).block();

        assertEquals(testTeachingAssistantResponseDTO, result);
        assertEquals(1, result.getSchedules().size());
        assertEquals(testScheduleResponseDTO, result.getSchedules().get(0));
        verify(teachingAssistantRepository, times(1)).findById(teachingAssistantId);
        verify(scheduleRepository, times(1)).findByTeachingAssistantId(teachingAssistantId);
    }

    @Test
    @DisplayName("findTeachingAssistantById - Teaching Assistant Not Found")
    void findTeachingAssistantById_NotFound() {
        Long teachingAssistantId = 1L;

        when(teachingAssistantRepository.findById(teachingAssistantId))
                .thenReturn(Mono.empty());

        Mono<TeachingAssistantResponseDTO> resultMono = teachingAssistantService.findTeachingAssistantById(teachingAssistantId);
        assertThrows(TeachingAssistantNotFoundException.class, resultMono::block);

        verify(teachingAssistantRepository, times(1)).findById(teachingAssistantId);
    }

    // ==================== FIND BY STUDENT APPLICATION ID ====================

    @ParameterizedTest(name = "findByStudentApplicationId - Gets teaching assistant for same section {0}")
    @ValueSource(longs = {1L, 2L, 3L})
    @DisplayName("findByStudentApplicationId - User should obtain teaching assistant for their same section")
    void findByStudentApplicationId_Successful_Section(Long userSection) {
        setupSecurityContext("ROLE_USER");
        List<TeachingAssistant> teachingAssistants = setUpUserMocking(userSection);
        Long studentApplicationId = userSection;

        when(teachingAssistantRepository.findByStudentApplicationId(studentApplicationId))
                .thenReturn(Mono.just(teachingAssistants.get(0)));
        when(modelMapper.map(teachingAssistants.get(0), TeachingAssistantResponseDTO.class))
                .thenReturn(testTeachingAssistantResponseDTO);
        when(scheduleRepository.findByTeachingAssistantId(teachingAssistants.get(0).getId()))
                .thenReturn(Flux.empty());

        TeachingAssistantResponseDTO result = teachingAssistantService.findByStudentApplicationId(studentApplicationId).block();

        assertEquals(testTeachingAssistantResponseDTO, result);
        verify(teachingAssistantRepository, times(1)).findByStudentApplicationId(studentApplicationId);
    }

    @ParameterizedTest(name = "findByStudentApplicationId - Fails for different section {0}")
    @ValueSource(longs = {1L, 2L, 3L})
    @DisplayName("findByStudentApplicationId - User should not obtain teaching assistant for different section")
    void findByStudentApplicationId_Fails_DifferentSection(Long userSection) {
        setupSecurityContext("ROLE_USER");
        List<TeachingAssistant> teachingAssistants = setUpUserMocking(userSection);
        Long differentSectionApplicationId = userSection == 1L ? 2L : 1L;

        when(teachingAssistantRepository.findByStudentApplicationId(differentSectionApplicationId))
                .thenReturn(Mono.just(teachingAssistants.get(1)));
        
        Mono<TeachingAssistantResponseDTO> resultMono = teachingAssistantService.findByStudentApplicationId(differentSectionApplicationId);

        assertThrows(TeachingAssistantNotFoundException.class, resultMono::block);
        verify(teachingAssistantRepository, times(1)).findByStudentApplicationId(differentSectionApplicationId);
    }

    @Test
    @DisplayName("findByStudentApplicationId - Gets teaching assistant successfully")
    void findByStudentApplicationId_Successful() {
        setupSecurityContext("ROLE_ADMIN");
        Long studentApplicationId = 1L;

        when(teachingAssistantRepository.findByStudentApplicationId(studentApplicationId))
                .thenReturn(Mono.just(testTeachingAssistant));
        when(modelMapper.map(testTeachingAssistant, TeachingAssistantResponseDTO.class))
                .thenReturn(testTeachingAssistantResponseDTO);
        when(scheduleRepository.findByTeachingAssistantId(testTeachingAssistant.getId()))
                .thenReturn(Flux.just(testSchedule));
        when(modelMapper.map(testSchedule, TeachingAssistantScheduleResponseDTO.class))
                .thenReturn(testScheduleResponseDTO);

        TeachingAssistantResponseDTO result = teachingAssistantService.findByStudentApplicationId(studentApplicationId).block();

        assertEquals(testTeachingAssistantResponseDTO, result);
        assertEquals(1, result.getSchedules().size());
        verify(teachingAssistantRepository, times(1)).findByStudentApplicationId(studentApplicationId);
    }

    @Test
    @DisplayName("findByStudentApplicationId - Teaching Assistant Not Found")
    void findByStudentApplicationId_NotFound() {
        Long studentApplicationId = 999L;

        when(teachingAssistantRepository.findByStudentApplicationId(studentApplicationId))
                .thenReturn(Mono.empty());

        Mono<TeachingAssistantResponseDTO> resultMono = teachingAssistantService.findByStudentApplicationId(studentApplicationId);
        assertThrows(TeachingAssistantNotFoundException.class, resultMono::block);

        verify(teachingAssistantRepository, times(1)).findByStudentApplicationId(studentApplicationId);
    }

    // ==================== LIST ALL TEACHING ASSISTANTS ====================

    @ParameterizedTest(name = "listAllTeachingAssistants - Returns teaching assistants for section {0}")
    @ValueSource(longs = {1L, 2L, 3L})
    @DisplayName("listAllTeachingAssistants - User should list teaching assistants for their section")
    void listAllTeachingAssistants_FilteredBySection(Long userSection) {
        setupSecurityContext("ROLE_USER");
        List<TeachingAssistant> teachingAssistants = setUpUserMocking(userSection);

        when(teachingAssistantRepository.findAll())
                .thenReturn(Flux.fromIterable(teachingAssistants));
        when(modelMapper.map(any(TeachingAssistant.class), eq(TeachingAssistantResponseDTO.class)))
                .thenReturn(testTeachingAssistantResponseDTO);

        List<TeachingAssistantResponseDTO> result = teachingAssistantService.listAllTeachingAssistants().collectList().block();

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(teachingAssistantRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("listAllTeachingAssistants - Admin gets all teaching assistants")
    void listAllTeachingAssistants_AdminGetsAll() {
        setupSecurityContext("ROLE_ADMIN");
        TeachingAssistant ta2 = TeachingAssistant.builder()
                .id(2L)
                .classId(2L)
                .studentApplicationId(2L)
                .weeklyHours(8L)
                .weeks(15L)
                .totalHours(120L)
                .build();

        when(teachingAssistantRepository.findAll())
                .thenReturn(Flux.just(testTeachingAssistant, ta2));
        when(modelMapper.map(any(TeachingAssistant.class), eq(TeachingAssistantResponseDTO.class)))
                .thenReturn(testTeachingAssistantResponseDTO);

        List<TeachingAssistantResponseDTO> result = teachingAssistantService.listAllTeachingAssistants().collectList().block();

        assertNotNull(result);
        assertEquals(2, result.size());
        verify(teachingAssistantRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("listAllTeachingAssistants - Returns empty list when none exist")
    void listAllTeachingAssistants_ReturnsEmpty() {
        when(teachingAssistantRepository.findAll())
                .thenReturn(Flux.empty());

        List<TeachingAssistantResponseDTO> result = teachingAssistantService.listAllTeachingAssistants().collectList().block();

        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(teachingAssistantRepository, times(1)).findAll();
    }

    // ==================== CREATE SCHEDULE ====================

    @ParameterizedTest(name = "createSchedule - Creates schedule for teaching assistant in section {0}")
    @ValueSource(longs = {1L, 2L, 3L})
    @DisplayName("createSchedule - User should create schedule for their section")
    void createSchedule_Successful_Section(Long userSection) {
        setupSecurityContext("ROLE_USER");
        List<TeachingAssistant> teachingAssistants = setUpUserMocking(userSection);
        Long teachingAssistantId = 1L;

        when(teachingAssistantRepository.findById(teachingAssistantId))
                .thenReturn(Mono.just(teachingAssistants.get(0)));
        when(scheduleRepository.save(any(TeachingAssistantSchedule.class)))
                .thenReturn(Mono.just(testSchedule));
        when(modelMapper.map(testSchedule, TeachingAssistantScheduleResponseDTO.class))
                .thenReturn(testScheduleResponseDTO);

        TeachingAssistantScheduleResponseDTO result = teachingAssistantService.createSchedule(teachingAssistantId, testScheduleRequestDTO).block();

        assertEquals(testScheduleResponseDTO, result);
        verify(teachingAssistantRepository, times(1)).findById(teachingAssistantId);
        verify(scheduleRepository, times(1)).save(any(TeachingAssistantSchedule.class));
    }

    @ParameterizedTest(name = "createSchedule - Fails for different section {0}")
    @ValueSource(longs = {1L, 2L, 3L})
    @DisplayName("createSchedule - User should not create schedule for different section")
    void createSchedule_Fails_DifferentSection(Long userSection) {
        setupSecurityContext("ROLE_USER");
        List<TeachingAssistant> teachingAssistants = setUpUserMocking(userSection);
        Long teachingAssistantId = 2L;

        when(teachingAssistantRepository.findById(teachingAssistantId))
                .thenReturn(Mono.just(teachingAssistants.get(1)));

        Mono<TeachingAssistantScheduleResponseDTO> resultMono = teachingAssistantService.createSchedule(teachingAssistantId, testScheduleRequestDTO);

        assertThrows(TeachingAssistantNotFoundException.class, resultMono::block);
        verify(teachingAssistantRepository, times(1)).findById(teachingAssistantId);
        verify(scheduleRepository, times(0)).save(any(TeachingAssistantSchedule.class));
    }

    @Test
    @DisplayName("createSchedule - Creates schedule successfully")
    void createSchedule_Successful() {
        setupSecurityContext("ROLE_ADMIN");
        Long teachingAssistantId = 1L;

        when(teachingAssistantRepository.findById(teachingAssistantId))
                .thenReturn(Mono.just(testTeachingAssistant));
        when(scheduleRepository.save(any(TeachingAssistantSchedule.class)))
                .thenReturn(Mono.just(testSchedule));
        when(modelMapper.map(testSchedule, TeachingAssistantScheduleResponseDTO.class))
                .thenReturn(testScheduleResponseDTO);

        TeachingAssistantScheduleResponseDTO result = teachingAssistantService.createSchedule(teachingAssistantId, testScheduleRequestDTO).block();

        assertEquals(testScheduleResponseDTO, result);
        verify(teachingAssistantRepository, times(1)).findById(teachingAssistantId);
        verify(scheduleRepository, times(1)).save(any(TeachingAssistantSchedule.class));
    }

    @Test
    @DisplayName("createSchedule - Teaching Assistant Not Found")
    void createSchedule_TeachingAssistantNotFound() {
        Long teachingAssistantId = 999L;

        when(teachingAssistantRepository.findById(teachingAssistantId))
                .thenReturn(Mono.empty());

        Mono<TeachingAssistantScheduleResponseDTO> resultMono = teachingAssistantService.createSchedule(teachingAssistantId, testScheduleRequestDTO);
        assertThrows(TeachingAssistantNotFoundException.class, resultMono::block);

        verify(teachingAssistantRepository, times(1)).findById(teachingAssistantId);
        verify(scheduleRepository, times(0)).save(any(TeachingAssistantSchedule.class));
    }

    // ==================== UPDATE SCHEDULE ====================

    @ParameterizedTest(name = "updateSchedule - Updates schedule for teaching assistant in section {0}")
    @ValueSource(longs = {1L, 2L, 3L})
    @DisplayName("updateSchedule - User should update schedule for their section")
    void updateSchedule_Successful_Section(Long userSection) {
        setupSecurityContext("ROLE_USER");
        List<TeachingAssistant> teachingAssistants = setUpUserMocking(userSection);
        Long scheduleId = 1L;

        TeachingAssistantSchedule existingSchedule = TeachingAssistantSchedule.builder()
                .id(scheduleId)
                .teachingAssistantId(teachingAssistants.get(0).getId())
                .day("Monday")
                .startTime(Time.valueOf(LocalTime.of(9, 0)))
                .endTime(Time.valueOf(LocalTime.of(11, 0)))
                .build();

        when(scheduleRepository.findById(scheduleId))
                .thenReturn(Mono.just(existingSchedule));
        when(teachingAssistantRepository.findById(existingSchedule.getTeachingAssistantId()))
                .thenReturn(Mono.just(teachingAssistants.get(0)));
        when(scheduleRepository.save(any(TeachingAssistantSchedule.class)))
                .thenReturn(Mono.just(testSchedule));
        when(modelMapper.map(testSchedule, TeachingAssistantScheduleResponseDTO.class))
                .thenReturn(testScheduleResponseDTO);

        TeachingAssistantScheduleResponseDTO result = teachingAssistantService.updateSchedule(scheduleId, testScheduleRequestDTO).block();

        assertEquals(testScheduleResponseDTO, result);
        verify(scheduleRepository, times(1)).findById(scheduleId);
        verify(scheduleRepository, times(1)).save(any(TeachingAssistantSchedule.class));
    }

    @ParameterizedTest(name = "updateSchedule - Fails for different section {0}")
    @ValueSource(longs = {1L, 2L, 3L})
    @DisplayName("updateSchedule - User should not update schedule for different section")
    void updateSchedule_Fails_DifferentSection(Long userSection) {
        setupSecurityContext("ROLE_USER");
        List<TeachingAssistant> teachingAssistants = setUpUserMocking(userSection);
        Long scheduleId = 1L;

        TeachingAssistantSchedule existingSchedule = TeachingAssistantSchedule.builder()
                .id(scheduleId)
                .teachingAssistantId(teachingAssistants.get(1).getId())
                .day("Monday")
                .startTime(Time.valueOf(LocalTime.of(9, 0)))
                .endTime(Time.valueOf(LocalTime.of(11, 0)))
                .build();

        when(scheduleRepository.findById(scheduleId))
                .thenReturn(Mono.just(existingSchedule));
        when(teachingAssistantRepository.findById(existingSchedule.getTeachingAssistantId()))
                .thenReturn(Mono.just(teachingAssistants.get(1)));

        Mono<TeachingAssistantScheduleResponseDTO> resultMono = teachingAssistantService.updateSchedule(scheduleId, testScheduleRequestDTO);

        assertThrows(TeachingAssistantNotFoundException.class, resultMono::block);
        verify(scheduleRepository, times(1)).findById(scheduleId);
        verify(scheduleRepository, times(0)).save(any(TeachingAssistantSchedule.class));
    }

    @Test
    @DisplayName("updateSchedule - Updates schedule successfully")
    void updateSchedule_Successful() {
        setupSecurityContext("ROLE_ADMIN");
        Long scheduleId = 1L;

        TeachingAssistantSchedule existingSchedule = TeachingAssistantSchedule.builder()
                .id(scheduleId)
                .teachingAssistantId(testTeachingAssistant.getId())
                .day("Monday")
                .startTime(Time.valueOf(LocalTime.of(9, 0)))
                .endTime(Time.valueOf(LocalTime.of(11, 0)))
                .build();

        when(scheduleRepository.findById(scheduleId))
                .thenReturn(Mono.just(existingSchedule));
        when(teachingAssistantRepository.findById(existingSchedule.getTeachingAssistantId()))
                .thenReturn(Mono.just(testTeachingAssistant));
        when(scheduleRepository.save(any(TeachingAssistantSchedule.class)))
                .thenReturn(Mono.just(testSchedule));
        when(modelMapper.map(testSchedule, TeachingAssistantScheduleResponseDTO.class))
                .thenReturn(testScheduleResponseDTO);

        TeachingAssistantScheduleResponseDTO result = teachingAssistantService.updateSchedule(scheduleId, testScheduleRequestDTO).block();

        assertEquals(testScheduleResponseDTO, result);
        verify(scheduleRepository, times(1)).findById(scheduleId);
        verify(scheduleRepository, times(1)).save(any(TeachingAssistantSchedule.class));
    }

    @Test
    @DisplayName("updateSchedule - Schedule Not Found")
    void updateSchedule_ScheduleNotFound() {
        Long scheduleId = 999L;

        when(scheduleRepository.findById(scheduleId))
                .thenReturn(Mono.empty());

        Mono<TeachingAssistantScheduleResponseDTO> resultMono = teachingAssistantService.updateSchedule(scheduleId, testScheduleRequestDTO);
        assertThrows(co.edu.puj.secchub_backend.planning.exception.TeachingAssistantScheduleNotFoundException.class, resultMono::block);

        verify(scheduleRepository, times(1)).findById(scheduleId);
        verify(scheduleRepository, times(0)).save(any(TeachingAssistantSchedule.class));
    }

    // ==================== DELETE SCHEDULE ====================

    @ParameterizedTest(name = "deleteSchedule - Deletes schedule for teaching assistant in section {0}")
    @ValueSource(longs = {1L, 2L, 3L})
    @DisplayName("deleteSchedule - User should delete schedule for their section")
    void deleteSchedule_Successful_Section(Long userSection) {
        setupSecurityContext("ROLE_USER");
        List<TeachingAssistant> teachingAssistants = setUpUserMocking(userSection);
        Long scheduleId = 1L;

        TeachingAssistantSchedule existingSchedule = TeachingAssistantSchedule.builder()
                .id(scheduleId)
                .teachingAssistantId(teachingAssistants.get(0).getId())
                .day("Monday")
                .startTime(Time.valueOf(LocalTime.of(9, 0)))
                .endTime(Time.valueOf(LocalTime.of(11, 0)))
                .build();

        when(scheduleRepository.findById(scheduleId))
                .thenReturn(Mono.just(existingSchedule));
        when(teachingAssistantRepository.findById(existingSchedule.getTeachingAssistantId()))
                .thenReturn(Mono.just(teachingAssistants.get(0)));
        when(scheduleRepository.delete(existingSchedule))
                .thenReturn(Mono.empty());

        assertDoesNotThrow(() -> teachingAssistantService.deleteSchedule(scheduleId).block());
        
        verify(scheduleRepository, times(1)).findById(scheduleId);
        verify(scheduleRepository, times(1)).delete(existingSchedule);
    }

    @ParameterizedTest(name = "deleteSchedule - Fails for different section {0}")
    @ValueSource(longs = {1L, 2L, 3L})
    @DisplayName("deleteSchedule - User should not delete schedule for different section")
    void deleteSchedule_Fails_DifferentSection(Long userSection) {
        setupSecurityContext("ROLE_USER");
        List<TeachingAssistant> teachingAssistants = setUpUserMocking(userSection);
        Long scheduleId = 1L;

        TeachingAssistantSchedule existingSchedule = TeachingAssistantSchedule.builder()
                .id(scheduleId)
                .teachingAssistantId(teachingAssistants.get(1).getId())
                .day("Monday")
                .startTime(Time.valueOf(LocalTime.of(9, 0)))
                .endTime(Time.valueOf(LocalTime.of(11, 0)))
                .build();

        when(scheduleRepository.findById(scheduleId))
                .thenReturn(Mono.just(existingSchedule));
        when(teachingAssistantRepository.findById(existingSchedule.getTeachingAssistantId()))
                .thenReturn(Mono.just(teachingAssistants.get(1)));

        Mono<Void> resultMono = teachingAssistantService.deleteSchedule(scheduleId);

        assertThrows(TeachingAssistantNotFoundException.class, resultMono::block);
        verify(scheduleRepository, times(1)).findById(scheduleId);
        verify(scheduleRepository, times(0)).delete(any(TeachingAssistantSchedule.class));
    }

    @Test
    @DisplayName("deleteSchedule - Deletes schedule successfully")
    void deleteSchedule_Successful() {
        setupSecurityContext("ROLE_ADMIN");
        Long scheduleId = 1L;

        TeachingAssistantSchedule existingSchedule = TeachingAssistantSchedule.builder()
                .id(scheduleId)
                .teachingAssistantId(testTeachingAssistant.getId())
                .day("Monday")
                .startTime(Time.valueOf(LocalTime.of(9, 0)))
                .endTime(Time.valueOf(LocalTime.of(11, 0)))
                .build();

        when(scheduleRepository.findById(scheduleId))
                .thenReturn(Mono.just(existingSchedule));
        when(teachingAssistantRepository.findById(existingSchedule.getTeachingAssistantId()))
                .thenReturn(Mono.just(testTeachingAssistant));
        when(scheduleRepository.delete(existingSchedule))
                .thenReturn(Mono.empty());

        assertDoesNotThrow(() -> teachingAssistantService.deleteSchedule(scheduleId).block());
        
        verify(scheduleRepository, times(1)).findById(scheduleId);
        verify(scheduleRepository, times(1)).delete(existingSchedule);
    }

    @Test
    @DisplayName("deleteSchedule - Schedule Not Found")
    void deleteSchedule_ScheduleNotFound() {
        Long scheduleId = 999L;

        when(scheduleRepository.findById(scheduleId))
                .thenReturn(Mono.empty());

        Mono<Void> resultMono = teachingAssistantService.deleteSchedule(scheduleId);
        assertThrows(co.edu.puj.secchub_backend.planning.exception.TeachingAssistantScheduleNotFoundException.class, resultMono::block);

        verify(scheduleRepository, times(1)).findById(scheduleId);
        verify(scheduleRepository, times(0)).delete(any(TeachingAssistantSchedule.class));
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

    private List<TeachingAssistant> setUpUserMocking(Long userSection) {
        TeachingAssistant testTeachingAssistant1 = TeachingAssistant.builder()
                .id(1L)
                .classId(userSection)
                .studentApplicationId(userSection)
                .weeklyHours(10L)
                .weeks(15L)
                .totalHours(150L)
                .build();

        Long teachingLong2 = userSection == 1L ? 2L : 1L;
        TeachingAssistant teachingAssistant2 = TeachingAssistant.builder()
                .id(2L)
                .classId(teachingLong2)
                .studentApplicationId(teachingLong2)
                .weeklyHours(8L)
                .weeks(15L)
                .totalHours(120L)
                .build();

        Long teachingLong3 = userSection == 1L ? 3L : 1L;
        TeachingAssistant teachingAssistant3 = TeachingAssistant.builder()
                .id(3L)
                .classId(teachingLong3)
                .studentApplicationId(teachingLong3)
                .weeklyHours(12L)
                .weeks(15L)
                .totalHours(180L)
                .build();
        
        when(userService.getUserIdByEmail(anyString())).thenReturn(Mono.just(1L));
        when(sectionService.getSectionIdByUserId(1L)).thenReturn(Mono.just(userSection));
        when(studentApplicationService.isApplicationOfSection(anyLong(), eq(userSection)))
                .thenAnswer(invocation -> {
                    Long applicationId = invocation.getArgument(0);
                    if (applicationId.equals(userSection)) return Mono.just(true);
                    else return Mono.just(false);
                });

        return Arrays.asList(testTeachingAssistant1, teachingAssistant2, teachingAssistant3);
    }
}