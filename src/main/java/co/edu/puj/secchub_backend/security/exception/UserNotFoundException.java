package co.edu.puj.secchub_backend.security.exception;

/**
 * Exception thrown when a user is not found in the system.
 * This exception should be used when searching for users by ID, email, or other identifiers
 * and the user does not exist in the database.
 */
public class UserNotFoundException extends RuntimeException {

    public UserNotFoundException(String message) {
        super(message);
    }
}