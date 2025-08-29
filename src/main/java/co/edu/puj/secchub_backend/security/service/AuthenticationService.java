package co.edu.puj.secchub_backend.security.service;

import java.util.Optional;

import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import co.edu.puj.secchub_backend.security.domain.User;
import co.edu.puj.secchub_backend.security.dto.AuthTokenDTO;
import co.edu.puj.secchub_backend.security.exception.JwtAuthenticationException;
import co.edu.puj.secchub_backend.security.jwt.JwtTokenProvider;
import co.edu.puj.secchub_backend.security.repository.UserRepository;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthenticationService {

    private final PasswordEncoderService passwordEncoderService;
    private final JwtTokenProvider jwtTokenProvider;
    private final UserRepository userRepository;

    /**
     * Authenticates a user based on email and password.
     * @param email the user's email
     * @param password the user's password
     * @return AuthTokenDTO containing the authentication token if successful
     * @throws JwtAuthenticationException if authentication fails
     */
    public AuthTokenDTO authenticate(String email, String password) throws JwtAuthenticationException {
        log.info("Attempting to authenticate user with email: {}", email);
        Optional<User> userOptional = userRepository.findByEmail(email);

        if (userOptional.isEmpty()) {
            log.warn("Authentication failed: User not found for email: {}", email);
            throw new JwtAuthenticationException("Invalid email or password");
        }

        User user = userOptional.get();

        if (!passwordEncoderService.matches(password, user.getPassword())) {
            log.warn("Authentication failed: Invalid password for email: {}", email);
            throw new JwtAuthenticationException("Invalid email or password");
        }

        // Update last access time
        userRepository.updateLastAccess(user.getEmail());

        // Generate JWT token
        String token = jwtTokenProvider.generateToken(user.getEmail(), user.getRole().getName());
        log.info("Authentication successful for email: {}", email);
        
        return new AuthTokenDTO("Login successful", email, token);
    }
}
