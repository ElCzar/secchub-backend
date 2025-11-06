package co.edu.puj.secchub_backend.security.service;

import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

import co.edu.puj.secchub_backend.parametric.contracts.ParametricContract;
import co.edu.puj.secchub_backend.security.dto.AuthTokenResponseDTO;
import co.edu.puj.secchub_backend.security.dto.LoginRequestDTO;
import co.edu.puj.secchub_backend.security.dto.RefreshTokenRequestDTO;
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
    private final ParametricContract parametricService;

    /**
     * Authenticates a user reactively with email and password.
     * @param loginRequestDTO the login request containing email and password
     * @return Mono emitting AuthTokenResponseDTO upon successful authentication
     * @throws JwtAuthenticationException if authentication fails
     */
    public Mono<AuthTokenResponseDTO> authenticate(LoginRequestDTO loginRequestDTO) {
        if (loginRequestDTO == null || loginRequestDTO.getEmail() == null || loginRequestDTO.getPassword() == null) {
            log.warn("Authentication failed: Missing email or password");
            return Mono.error(new JwtAuthenticationException("Email and password must be provided"));
        }

        String email = loginRequestDTO.getEmail();
        String password = loginRequestDTO.getPassword();
        log.info("Attempting to authenticate user with email: {}", email);

        return Mono.defer(() -> userRepository.findByEmail(email))
                .switchIfEmpty(Mono.error(new JwtAuthenticationException("Invalid email or password")))
                .flatMap(user -> {
                    if (!passwordEncoderService.matches(password, user.getPassword())) {
                        log.warn("Authentication failed: Invalid password for email: {}", email);
                        return Mono.error(new JwtAuthenticationException("Invalid email or password"));
                    }

                    return Mono.fromRunnable(() -> userRepository.updateLastAccess(user.getEmail()))
                            .onErrorResume(e -> {
                                log.warn("User status invalid for email: {}: {}", email, e.getMessage());
                                return Mono.error(new JwtAuthenticationException("User account is not active"));
                            })
                            .thenReturn(user);
                })
                .flatMap(user -> 
                    parametricService.getRoleNameById(user.getRoleId())
                            .map(roleName -> {
                                String token = jwtTokenProvider.generateToken(user.getEmail(), roleName);
                                String refreshToken = jwtTokenProvider.generateRefreshToken(user.getEmail());
                                log.info("Authentication successful for email: {}", email);

                                return new AuthTokenResponseDTO(
                                        "Login successful",
                                        System.currentTimeMillis(),
                                        token,
                                        refreshToken,
                                        JwtTokenProvider.BEARER_PREFIX,
                                        roleName
                                );
                            })
                )
                .onErrorMap(e -> {
                    if (e instanceof JwtAuthenticationException) return e;
                    log.error("Unexpected error during authentication for email: {}", email, e);
                    return new JwtAuthenticationException("Authentication failed due to an internal error");
                });
    }


    /**
     * Refreshes the authentication token reactively using a refresh token.
     * @param refreshToken the refresh token request containing the refresh token
     * @return Mono emitting AuthTokenResponseDTO with new tokens
     * @throws JwtAuthenticationException if the refresh token is invalid
     */
    public Mono<AuthTokenResponseDTO> refreshToken(RefreshTokenRequestDTO refreshToken) {
        log.info("Attempting to refresh token");

        return Mono.defer(() -> Mono.fromCallable(() -> {
                    if (!jwtTokenProvider.validateRefreshToken(refreshToken.getRefreshToken())) {
                        throw new JwtAuthenticationException("Invalid refresh token");
                    }
                    return jwtTokenProvider.getEmailFromToken(refreshToken.getRefreshToken());
                }))
                .flatMap(email -> {
                    if (email == null) {
                        return Mono.error(new JwtAuthenticationException("Invalid email for refresh token"));
                    }

                    return userRepository.findByEmail(email)
                            .switchIfEmpty(Mono.error(new JwtAuthenticationException("Invalid email for refresh token")))
                            .flatMap(user -> 
                                parametricService.getRoleNameById(user.getRoleId())
                                        .map(roleName -> {
                                            String newToken = jwtTokenProvider.generateToken(email, roleName);
                                            String newRefreshToken = jwtTokenProvider.generateRefreshToken(email);
                                            log.info("Token refreshed successfully for email: {}", email);

                                            return new AuthTokenResponseDTO(
                                                    "Token refreshed successfully",
                                                    System.currentTimeMillis(),
                                                    newToken,
                                                    newRefreshToken,
                                                    JwtTokenProvider.BEARER_PREFIX,
                                                    roleName
                                            );
                                        })
                            );
                })
                .switchIfEmpty(Mono.error(new JwtAuthenticationException("Invalid refresh token")))
                .onErrorMap(e -> {
                    if (e instanceof JwtAuthenticationException) return e;
                    log.warn("Unexpected error during refresh: {}", e.getMessage(), e);
                    return new JwtAuthenticationException("Invalid refresh token");
                });
    }
}
