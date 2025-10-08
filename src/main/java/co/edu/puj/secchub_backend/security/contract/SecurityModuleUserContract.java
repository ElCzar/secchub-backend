package co.edu.puj.secchub_backend.security.contract;

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
    Long getUserIdByEmail(String email);

    /**
     * Creates a new user with the given information.
     * @param email user's email
     * @param password user's password
     * @return created user's id
     */
    Long createUser(UserCreationRequestDTO userCreationRequestDTO);
}
