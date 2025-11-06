package co.edu.puj.secchub_backend.admin.service;

import co.edu.puj.secchub_backend.admin.dto.*;
import co.edu.puj.secchub_backend.parametric.contracts.ParametricContract;
import co.edu.puj.secchub_backend.parametric.contracts.RoleDTO;
import co.edu.puj.secchub_backend.parametric.contracts.StatusDTO;
import co.edu.puj.secchub_backend.security.contract.SecurityModuleUserContract;
import co.edu.puj.secchub_backend.security.contract.UserCreationRequestDTO;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.modelmapper.ModelMapper;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("RegisterService Unit Tests")
class RegisterServiceTest {

    @Mock
    private SecurityModuleUserContract userService;

    @Mock
    private ParametricContract parametricService;

    @Mock
    private TeacherService teacherService;

    @Mock
    private SectionService sectionService;

    @Mock
    private ModelMapper modelMapper;

    @InjectMocks
    private RegisterService registerService;

    private UserRegisterRequestDTO userDTO;
    private TeacherRegisterRequestDTO teacherRegisterRequestDTO;
    private SectionRegisterRequestDTO sectionRegisterRequestDTO;

    @BeforeEach
    void setUp() {
        userDTO = new UserRegisterRequestDTO();
        userDTO.setEmail("test@uni.edu");
        userDTO.setPassword("secure123");

        teacherRegisterRequestDTO = new TeacherRegisterRequestDTO();
        teacherRegisterRequestDTO.setUser(userDTO);

        sectionRegisterRequestDTO = new SectionRegisterRequestDTO();
        sectionRegisterRequestDTO.setUser(userDTO);
    }

    @Test
    @DisplayName("registerStudent - Should create student user successfully")
    void testRegisterStudent_Success() {
        RoleDTO roleEntity = RoleDTO.builder().id(1L).name("ROLE_STUDENT").build();
        StatusDTO statusEntity = StatusDTO.builder().id(2L).name("Active").build();

        UserCreationRequestDTO creationDTO = new UserCreationRequestDTO();

        when(parametricService.getRoleByName("ROLE_STUDENT")).thenReturn(Mono.just(roleEntity));
        when(parametricService.getStatusByName("Active")).thenReturn(Mono.just(statusEntity));
        when(modelMapper.map(userDTO, UserCreationRequestDTO.class)).thenReturn(creationDTO);
        when(userService.createUser(creationDTO)).thenReturn(Mono.just(10L));

        StepVerifier.create(registerService.registerStudent(userDTO))
                .expectNext(10L)
                .verifyComplete();

        verify(parametricService).getRoleByName("ROLE_STUDENT");
        verify(parametricService).getStatusByName("Active");
        verify(userService).createUser(creationDTO);
    }

    @Test
    @DisplayName("registerAdmin - Should create admin user successfully")
    void testRegisterAdmin_Success() {
        RoleDTO roleEntity = RoleDTO.builder().id(11L).name("ROLE_ADMIN").build();
        StatusDTO statusEntity = StatusDTO.builder().id(22L).name("Active").build();
        UserCreationRequestDTO creationDTO = new UserCreationRequestDTO();

        when(parametricService.getRoleByName("ROLE_ADMIN")).thenReturn(Mono.just(roleEntity));
        when(parametricService.getStatusByName("Active")).thenReturn(Mono.just(statusEntity));
        when(modelMapper.map(userDTO, UserCreationRequestDTO.class)).thenReturn(creationDTO);
        when(userService.createUser(creationDTO)).thenReturn(Mono.just(5L));

        StepVerifier.create(registerService.registerAdmin(userDTO))
                .expectNext(5L)
                .verifyComplete();
    }

    @Test
    @DisplayName("registerProgram - Should create program user successfully")
    void testRegisterProgram_Success() {
        RoleDTO roleEntity = RoleDTO.builder().id(77L).name("ROLE_PROGRAM").build();
        StatusDTO statusEntity = StatusDTO.builder().id(88L).name("Active").build();
        UserCreationRequestDTO creationDTO = new UserCreationRequestDTO();

        when(parametricService.getRoleByName("ROLE_PROGRAM")).thenReturn(Mono.just(roleEntity));
        when(parametricService.getStatusByName("Active")).thenReturn(Mono.just(statusEntity));
        when(modelMapper.map(userDTO, UserCreationRequestDTO.class)).thenReturn(creationDTO);
        when(userService.createUser(creationDTO)).thenReturn(Mono.just(99L));

        StepVerifier.create(registerService.registerProgram(userDTO))
                .expectNext(99L)
                .verifyComplete();
    }

    @Test
    @DisplayName("registerTeacher - Should create teacher user and persist teacher")
    void testRegisterTeacher_Success() {
        RoleDTO roleEntity = RoleDTO.builder().id(3L).name("ROLE_TEACHER").build();
        StatusDTO statusEntity = StatusDTO.builder().id(4L).name("Active").build();
        UserCreationRequestDTO creationDTO = new UserCreationRequestDTO();
        TeacherCreateRequestDTO teacherCreateRequestDTO = new TeacherCreateRequestDTO();
        TeacherResponseDTO teacherResponseDTO = new TeacherResponseDTO();

        when(parametricService.getRoleByName("ROLE_TEACHER")).thenReturn(Mono.just(roleEntity));
        when(parametricService.getStatusByName("Active")).thenReturn(Mono.just(statusEntity));
        when(modelMapper.map(userDTO, UserCreationRequestDTO.class)).thenReturn(creationDTO);
        when(userService.createUser(creationDTO)).thenReturn(Mono.just(123L));

        when(modelMapper.map(teacherRegisterRequestDTO, TeacherCreateRequestDTO.class)).thenReturn(teacherCreateRequestDTO);
        when(teacherService.createTeacher(teacherCreateRequestDTO)).thenReturn(Mono.just(teacherResponseDTO));

        StepVerifier.create(registerService.registerTeacher(teacherRegisterRequestDTO))
                .expectNext(teacherResponseDTO)
                .verifyComplete();

        verify(parametricService).getRoleByName("ROLE_TEACHER");
        verify(teacherService).createTeacher(teacherCreateRequestDTO);
    }

    @Test
    @DisplayName("registerSection - Should create section user and persist section")
    void testRegisterSection_Success() {
        RoleDTO roleEntity = RoleDTO.builder().id(7L).name("ROLE_USER").build();
        StatusDTO statusEntity = StatusDTO.builder().id(8L).name("Active").build();
        UserCreationRequestDTO creationDTO = new UserCreationRequestDTO();
        SectionCreateRequestDTO sectionCreateRequestDTO = new SectionCreateRequestDTO();
        SectionResponseDTO sectionResponseDTO = new SectionResponseDTO();

        when(parametricService.getRoleByName("ROLE_USER")).thenReturn(Mono.just(roleEntity));
        when(parametricService.getStatusByName("Active")).thenReturn(Mono.just(statusEntity));
        when(modelMapper.map(userDTO, UserCreationRequestDTO.class)).thenReturn(creationDTO);
        when(userService.createUser(creationDTO)).thenReturn(Mono.just(321L));

        when(modelMapper.map(sectionRegisterRequestDTO, SectionCreateRequestDTO.class)).thenReturn(sectionCreateRequestDTO);
        when(sectionService.createSection(sectionCreateRequestDTO)).thenReturn(Mono.just(sectionResponseDTO));

        StepVerifier.create(registerService.registerSection(sectionRegisterRequestDTO))
                .expectNext(sectionResponseDTO)
                .verifyComplete();

        verify(parametricService).getRoleByName("ROLE_USER");
        verify(sectionService).createSection(sectionCreateRequestDTO);
    }

    @Test
    @DisplayName("createUserInSecurityModule - Should propagate error when parametric service fails")
    void testCreateUserInSecurityModule_Error() {
        when(parametricService.getRoleByName("ROLE_STUDENT")).thenReturn(Mono.error(new RuntimeException("DB error")));

        StepVerifier.create(registerService.registerStudent(userDTO))
                .expectErrorMatches(e -> e.getMessage().contains("DB error"))
                .verify();
    }
}
