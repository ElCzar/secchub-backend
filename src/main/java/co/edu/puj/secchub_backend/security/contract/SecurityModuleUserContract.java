package co.edu.puj.secchub_backend.security.contract;

import reactor.core.publisher.Mono;

/**
 * Public API contract for user-related operations that can be used by other modules.
 * This interface defines the operations that the security module exposes to other modules
 * while maintaining proper modulith boundaries.
 */
public interface SecurityModuleUserContract {
    /**
     * Gets user id by email.
     * @param email user's email
     * @return user id
     */
    Mono<Long> getUserIdByEmail(String email);

    /**
     * Creates a new user with the given information.
     * @param userCreationRequestDTO DTO containing the user's creation information (e.g., email and password)
     * @return created user's id
     */
    Mono<Long> createUser(UserCreationRequestDTO userCreationRequestDTO);

    /**
     * Gets user information by email including name, lastName, etc.
     * @param email user's email
     * @return UserInformationResponseDTO with user details
     */
    Mono<UserInformationResponseDTO> getUserInformationByEmail(String email);
}
