package co.edu.puj.secchub_backend.security.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import co.edu.puj.secchub_backend.security.contract.UserInformationResponseDTO;
import co.edu.puj.secchub_backend.security.service.UserService;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;


/**
 * REST controller for managing user-information related endpoints.
 * Provides endpoints to retrieve user details.
 */
@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    /**
     * Get logged-in user information.
     * @return ResponseEntity<UserInformationResponseDTO> with user details and code 200
     */
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public Mono<ResponseEntity<UserInformationResponseDTO>> getUserInformation() {
        return userService.getUserInformation()
                .map(ResponseEntity::ok);
    }

    /**
     * Get all users information.
     * @return ResponseEntity<List<UserInformationResponseDTO>> with list of users and code 200
     */
    @GetMapping("/all")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public Mono<ResponseEntity<java.util.List<UserInformationResponseDTO>>> getAllUsersInformation() {
        return userService.getAllUsersInformation()
                .collectList()
                .map(ResponseEntity::ok);
    }

    /**
     * Get user information by email.
     * @param email User email
     * @return ResponseEntity<UserInformationResponseDTO> with user details and code 200
     */
    @GetMapping("/email")
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_USER')")
    public Mono<ResponseEntity<UserInformationResponseDTO>> getUserInformationByEmail(@RequestParam String email) {
        return userService.getUserInformationByEmail(email)
                .cast(UserInformationResponseDTO.class)
                .map(ResponseEntity::ok);
    }

    /**
     * Get user information by user ID.
     * @param userId User ID
     * @return ResponseEntity<UserInformationResponseDTO> with user details and code 200
     */
    @GetMapping("/id/{userId}")
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_USER')")
    public Mono<ResponseEntity<UserInformationResponseDTO>> getUserInformationById(@PathVariable Long userId) {
        return userService.getUserInformationById(userId)
                .map(ResponseEntity::ok);
    }
}