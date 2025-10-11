package co.edu.puj.secchub_backend.admin.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import co.edu.puj.secchub_backend.admin.dto.SectionRegisterRequestDTO;
import co.edu.puj.secchub_backend.admin.dto.SectionResponseDTO;
import co.edu.puj.secchub_backend.admin.dto.TeacherRegisterRequestDTO;
import co.edu.puj.secchub_backend.admin.dto.TeacherResponseDTO;
import co.edu.puj.secchub_backend.admin.dto.UserRegisterRequestDTO;
import co.edu.puj.secchub_backend.admin.service.RegisterService;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

/**
 * REST controller in charge of user registration.
 * This includes:
 * - Register students
 * - Register teachers
 * - Register admins
 * - Register sections
 * - Register programs
 */
@RestController
@RequestMapping("/admin/register")
@RequiredArgsConstructor
public class RegisterController {
    private final RegisterService registerService;
    
    /**
     * Post a new student
     * @param UserRegisterRequestDTO with student data
     * @return Created student id with status 201
     */
    @PostMapping("/student")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public Mono<ResponseEntity<Long>> registerStudent(@RequestBody UserRegisterRequestDTO request) {
        return registerService.registerStudent(request)
                .map(createdUserId -> ResponseEntity.status(HttpStatus.CREATED).body(createdUserId));
    }

    /**
     * Post a new admin
     * @param UserRegisterRequestDTO with admin data
     * @return Created admin id with status 201
     */
    @PostMapping("/admin")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public Mono<ResponseEntity<Long>> registerAdmin(@RequestBody UserRegisterRequestDTO request) {
        return registerService.registerAdmin(request)
                .map(createdUserId -> ResponseEntity.status(HttpStatus.CREATED).body(createdUserId));
    }

    /**
     * Post a new program
     * @param UserRegisterRequestDTO with program data
     * @return Created program id with status 201
     */
    @PostMapping("/program")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public Mono<ResponseEntity<Long>> registerProgram(@RequestBody UserRegisterRequestDTO request) {
        return registerService.registerProgram(request)
                .map(createdUserId -> ResponseEntity.status(HttpStatus.CREATED).body(createdUserId));
    }

    /**
     * Post a new teacher
     * @param TeacherRegisterRequestDTO with teacher data
     * @return Created teacher id with status 201
     */
    @PostMapping("/teacher")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public Mono<ResponseEntity<TeacherResponseDTO>> registerTeacher(@RequestBody TeacherRegisterRequestDTO request) {
        return registerService.registerTeacher(request)
                .map(createdTeacher -> ResponseEntity.status(HttpStatus.CREATED).body(createdTeacher));
    }

    /**
     * Post a new section
     * @param SectionRegisterRequestDTO with section data
     * @return Created section id with status 201
     */
    @PostMapping("/section")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public Mono<ResponseEntity<SectionResponseDTO>> registerSection(@RequestBody SectionRegisterRequestDTO request) {
        return registerService.registerSection(request)
                .map(createdSection -> ResponseEntity.status(HttpStatus.CREATED).body(createdSection));
    }
}
