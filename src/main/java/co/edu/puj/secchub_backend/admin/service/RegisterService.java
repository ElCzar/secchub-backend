package co.edu.puj.secchub_backend.admin.service;

import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import co.edu.puj.secchub_backend.admin.dto.SectionCreateRequestDTO;
import co.edu.puj.secchub_backend.admin.dto.SectionRegisterRequestDTO;
import co.edu.puj.secchub_backend.admin.dto.SectionResponseDTO;
import co.edu.puj.secchub_backend.admin.dto.TeacherCreateRequestDTO;
import co.edu.puj.secchub_backend.admin.dto.TeacherRegisterRequestDTO;
import co.edu.puj.secchub_backend.admin.dto.TeacherResponseDTO;
import co.edu.puj.secchub_backend.admin.dto.UserRegisterRequestDTO;
import co.edu.puj.secchub_backend.parametric.contracts.ParametricContract;
import co.edu.puj.secchub_backend.security.contract.SecurityModuleUserContract;
import co.edu.puj.secchub_backend.security.contract.UserCreationRequestDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@Service
@Slf4j
@RequiredArgsConstructor
public class RegisterService {
    private final SecurityModuleUserContract userService;
    private final ParametricContract parametricService;
    private final TeacherService teacherService;
    private final SectionService sectionService;
    private final ModelMapper modelMapper;

    private static final String STATUS = "Active";
    private static final String ROLE_STUDENT = "ROLE_STUDENT";
    private static final String ROLE_ADMIN = "ROLE_ADMIN";
    private static final String ROLE_TEACHER = "ROLE_TEACHER";
    private static final String ROLE_USER = "ROLE_USER";
    private static final String ROLE_PROGRAM = "ROLE_PROGRAM";

    /**
     * Private method to create a user in the security module.
     * @param userRegisterRequestDTO with user data
     * @param role to assign to the user
     * @return Created user ID
     */
    private Mono<Long> createUserInSecurityModule(UserRegisterRequestDTO userRegisterRequestDTO, String role) {
        log.debug("Creating user in security module with email: {}", userRegisterRequestDTO.getEmail());
        return parametricService.getRoleByName(role)
            .flatMap(roleEntity -> parametricService.getStatusByName(STATUS)
                .flatMap(statusEntity -> {
                    UserCreationRequestDTO userCreationRequestDTO = modelMapper.map(userRegisterRequestDTO, UserCreationRequestDTO.class);
                    userCreationRequestDTO.setRoleId(roleEntity.getId());
                    userCreationRequestDTO.setStatusId(statusEntity.getId());
                    return userService.createUser(userCreationRequestDTO);
                })
            );
    }

    /**
     * Creates a new student user.
     * @param userRegisterRequestDTO with user data
     * @return Created user ID
     */
    public Mono<Long> registerStudent(UserRegisterRequestDTO userRegisterRequestDTO) {
        return createUserInSecurityModule(userRegisterRequestDTO, ROLE_STUDENT);
    }

    /**
     * Creates a new admin user.
     * @param userRegisterRequestDTO with user data
     * @return Created user ID
     */
    public Mono<Long> registerAdmin(UserRegisterRequestDTO userRegisterRequestDTO) {
        return createUserInSecurityModule(userRegisterRequestDTO, ROLE_ADMIN);
    }

    /**
     * Creates a new program user.
     * @param userRegisterRequestDTO with user data
     * @return Created user ID
     */
    public Mono<Long> registerProgram(UserRegisterRequestDTO userRegisterRequestDTO) {
        return createUserInSecurityModule(userRegisterRequestDTO, ROLE_PROGRAM);
    }

    /**
     * Creates a new teacher user.
     * @param teacherRegisterRequestDTO with user data
     * @return Created user ID
     */
    public Mono<TeacherResponseDTO> registerTeacher(TeacherRegisterRequestDTO teacherRegisterRequestDTO) {
        return createUserInSecurityModule(teacherRegisterRequestDTO.getUser(), ROLE_TEACHER)
            .flatMap(userId -> {
                TeacherCreateRequestDTO teacherCreateRequestDTO = modelMapper.map(teacherRegisterRequestDTO, TeacherCreateRequestDTO.class);
                teacherCreateRequestDTO.setUserId(userId);
                return teacherService.createTeacher(teacherCreateRequestDTO);
            });
    }

    /**
     * Creates a new section.
     * @param sectionRegisterRequestDTO with section data
     * @return Created section ID
     */
    public Mono<SectionResponseDTO> registerSection(SectionRegisterRequestDTO sectionRegisterRequestDTO) {
        return createUserInSecurityModule(sectionRegisterRequestDTO.getUser(), ROLE_USER)
            .flatMap(teacher -> {
                SectionCreateRequestDTO sectionCreateRequestDTO = modelMapper.map(sectionRegisterRequestDTO, SectionCreateRequestDTO.class);
                return sectionService.createSection(sectionCreateRequestDTO);
            });
    }
}
