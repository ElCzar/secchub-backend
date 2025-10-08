package co.edu.puj.secchub_backend.security.service;

import java.util.Optional;

import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import co.edu.puj.secchub_backend.parametric.contracts.ParametricContract;
import co.edu.puj.secchub_backend.security.dto.AuthTokenResponseDTO;
import co.edu.puj.secchub_backend.security.dto.RefreshTokenRequestDTO;
import co.edu.puj.secchub_backend.security.exception.JwtAuthenticationException;
import co.edu.puj.secchub_backend.security.jwt.JwtTokenProvider;
import co.edu.puj.secchub_backend.security.model.User;
import co.edu.puj.secchub_backend.security.repository.UserRepository;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthenticationService {

    private final PasswordEncoderService passwordEncoderService;
    private final JwtTokenProvider jwtTokenProvider;
    private final UserRepository userRepository;
    private final ParametricContract parametricService;

    /**
     * Authenticates a user based on email and password.
     * @param email the user's email
     * @param password the user's password
     * @return AuthTokenResponseDTO containing the authentication token if successful
     * @throws JwtAuthenticationException if authentication fails
     */
    public AuthTokenResponseDTO authenticate(String email, String password) throws JwtAuthenticationException {
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

        userRepository.updateLastAccess(user.getEmail());

        String token = jwtTokenProvider.generateToken(user.getEmail(), parametricService.getRoleNameById(user.getRoleId()));
        String refreshToken = jwtTokenProvider.generateRefreshToken(user.getEmail());
        log.info("Authentication successful for email: {}", email);

        return new AuthTokenResponseDTO("Login successful", System.currentTimeMillis(), token, refreshToken, JwtTokenProvider.BEARER_PREFIX, parametricService.getRoleNameById(user.getRoleId()));
    }

    /**
     * Refreshes the authentication token using the provided refresh token.
     * @param refreshToken the refresh token request DTO containing the refresh token
     * @return a new AuthTokenResponseDTO containing the refreshed authentication token
     */
    public AuthTokenResponseDTO refreshToken(RefreshTokenRequestDTO refreshToken) throws JwtAuthenticationException {
        log.info("Attempting to refresh token");
        String email;
        
        try {
            jwtTokenProvider.validateRefreshToken(refreshToken.getRefreshToken());
            email = jwtTokenProvider.getEmailFromToken(refreshToken.getRefreshToken());
        } catch (Exception e) {
            log.warn("Refresh token validation failed: {}", e.getMessage());
            throw new JwtAuthenticationException("Invalid refresh token");
        }

        if (email == null) {
            log.warn("Refresh token is invalid");
            throw new JwtAuthenticationException("Invalid refresh token");
        }

        Optional<User> userOptional = userRepository.findByEmail(email);

        if (userOptional.isEmpty()) {
            log.warn("Refresh token is invalid: User not found for email: {}", email);
            throw new JwtAuthenticationException("Invalid refresh token");
        }

        User user = userOptional.get();
        String newToken = jwtTokenProvider.generateToken(email, parametricService.getRoleNameById(user.getRoleId()));
        String newRefreshToken = jwtTokenProvider.generateRefreshToken(email);
        log.info("Token refreshed successfully for email: {}", email);

        return new AuthTokenResponseDTO("Token refreshed successfully", System.currentTimeMillis(), newToken, newRefreshToken, JwtTokenProvider.BEARER_PREFIX, parametricService.getRoleNameById(user.getRoleId()));
    }
}
