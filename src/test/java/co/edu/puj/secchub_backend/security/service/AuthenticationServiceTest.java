package co.edu.puj.secchub_backend.security.service;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.doThrow;

import java.util.Optional;
import java.util.stream.Stream;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import co.edu.puj.secchub_backend.parametric.service.ParametricService;
import co.edu.puj.secchub_backend.security.dto.AuthTokenResponseDTO;
import co.edu.puj.secchub_backend.security.dto.LoginRequestDTO;
import co.edu.puj.secchub_backend.security.dto.RefreshTokenRequestDTO;
import co.edu.puj.secchub_backend.security.exception.JwtAuthenticationException;
import co.edu.puj.secchub_backend.security.jwt.JwtTokenProvider;
import co.edu.puj.secchub_backend.security.model.User;
import co.edu.puj.secchub_backend.security.repository.UserRepository;

/**
 * Unit test class for AuthenticationService.
 * Tests business logic in isolation with all dependencies mocked.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("AuthenticationService Unit Test")
class AuthenticationServiceTest {
    @Mock
    private UserRepository userRepository;
    
    @Mock
    private PasswordEncoderService passwordEncoderService;
    
    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @Mock
    private ParametricService parametricService;
    
    @InjectMocks
    private AuthenticationService authenticationService;
    
    @DisplayName("Authenticate with valid credentials should succeed")
    @ParameterizedTest(name = "Authenticated user with email: {0}")
    @MethodSource("provideValidCredentials")
    void authenticateWithValidCredentials(String email, String password) {
        // Given - Setup user that exists in repository
        User mockUser = User.builder()
                .email(email)
                .password("encoded_" + password)
                .username("testuser")
                .roleId(1L)
                .statusId(1L)
                .build();
        
        // Stubbing repository and services
        when(userRepository.findByEmail(email))
            .thenReturn(Optional.of(mockUser));
        when(passwordEncoderService.matches(password, "encoded_" + password))
            .thenReturn(true);
        when(parametricService.getRoleNameById(1L))
            .thenReturn("ROLE_USER");
        when(jwtTokenProvider.generateToken(anyString(), anyString()))
            .thenReturn("mock-access-token");
        when(jwtTokenProvider.generateRefreshToken(anyString()))
            .thenReturn("mock-refresh-token");
        
        LoginRequestDTO loginRequestDTO = new LoginRequestDTO();
        loginRequestDTO.setEmail(email);
        loginRequestDTO.setPassword(password);
        
        // When
        AuthTokenResponseDTO authTokenResponseDTO = authenticationService.authenticate(loginRequestDTO);
        
        // Then
        assertNotNull(authTokenResponseDTO, "AuthTokenResponseDTO should not be null");
        assertNotNull(authTokenResponseDTO.getAccessToken(), "Access token should not be null");
        assertEquals("mock-access-token", authTokenResponseDTO.getAccessToken());
        
        // Verify interactions
        verify(userRepository).findByEmail(email);
        verify(passwordEncoderService).matches(password, "encoded_" + password);
        verify(jwtTokenProvider).generateToken(email, "ROLE_USER");
        verify(jwtTokenProvider).generateRefreshToken(email);
    }
    
    private static Stream<Arguments> provideValidCredentials() {
        return Stream.of(
                Arguments.of("testAdmin@example.com", "password"),
                Arguments.of("testUser@example.com", "password"),
                Arguments.of("testStudent@example.com", "password"),
                Arguments.of("testTeacher@example.com", "password"),
                Arguments.of("testProgram@example.com", "password")
        );
    }
    
    @DisplayName("Authenticate with invalid credentials should fail")
    @ParameterizedTest(name = "Authentication failed for email: {0}")
    @MethodSource("provideInvalidCredentials")
    void authenticateWithInvalidCredentials(String email, String password) {
        // Given - User exists but password is wrong
        User mockUser = User.builder()
                .email(email)
                .password("encoded_correct_password")
                .username("testuser")
                .roleId(1L)
                .statusId(1L)
                .build();
        
        when(userRepository.findByEmail(email))
            .thenReturn(Optional.of(mockUser));
        when(passwordEncoderService.matches(password, "encoded_correct_password"))
            .thenReturn(false);
        
        LoginRequestDTO loginRequestDTO = new LoginRequestDTO();
        loginRequestDTO.setEmail(email);
        loginRequestDTO.setPassword(password);
        
        // When & Then
        assertThrows(JwtAuthenticationException.class, 
            () -> authenticationService.authenticate(loginRequestDTO), 
            "JwtAuthenticationException should be thrown for invalid credentials");
        
        // Verify token was never generated for invalid credentials
        verify(jwtTokenProvider, never()).generateToken(any());
    }
    
    private static Stream<Arguments> provideInvalidCredentials() {
        return Stream.of(
                Arguments.of("testAdmin@example.com", "wrong-password"),
                Arguments.of("testUser@example.com", "wrong-password"),
                Arguments.of("testStudent@example.com", "wrong-password"),
                Arguments.of("testTeacher@example.com", "wrong-password"),
                Arguments.of("testProgram@example.com", "wrong-password")
        );
    }
    
    @Test
    @DisplayName("Authenticate with non-existent user should fail")
    void authenticateWithNonExistentUser() {
        // Given
        String email = "nonexistent@example.com";
        when(userRepository.findByEmail(email))
            .thenReturn(Optional.empty());
        
        LoginRequestDTO loginRequestDTO = new LoginRequestDTO();
        loginRequestDTO.setEmail(email);
        loginRequestDTO.setPassword("password");
        
        // When & Then
        assertThrows(JwtAuthenticationException.class,
            () -> authenticationService.authenticate(loginRequestDTO),
            "JwtAuthenticationException should be thrown for non-existent user");
        
        // Verify password was never checked
        verify(passwordEncoderService, never()).matches(anyString(), anyString());
        verify(jwtTokenProvider, never()).generateToken(any());
    }
    
    @Test
    @DisplayName("Authenticate with null email should fail")
    void authenticateWithNullEmail() {
        // Given
        LoginRequestDTO loginRequestDTO = new LoginRequestDTO();
        loginRequestDTO.setEmail(null);
        loginRequestDTO.setPassword("password");
        
        // When & Then
        assertThrows(JwtAuthenticationException.class,
            () -> authenticationService.authenticate(loginRequestDTO),
            "JwtAuthenticationException should be thrown for null email");
        
        // Verify no repository call was made
        verify(userRepository, never()).findByEmail(any());
        verify(passwordEncoderService, never()).matches(anyString(), anyString());
        verify(jwtTokenProvider, never()).generateToken(any());
    }

    @Test
    @DisplayName("Authenticate with null password should fail")
    void authenticateWithNullPassword() {
        // Given
        LoginRequestDTO loginRequestDTO = new LoginRequestDTO();
        loginRequestDTO.setEmail("test@example.com");
        loginRequestDTO.setPassword(null);

        // When & Then
        assertThrows(JwtAuthenticationException.class,
            () -> authenticationService.authenticate(loginRequestDTO),
            "JwtAuthenticationException should be thrown for null password");

        // Verify no repository call was made
        verify(userRepository, never()).findByEmail(any());
        verify(passwordEncoderService, never()).matches(anyString(), anyString());
        verify(jwtTokenProvider, never()).generateToken(any());
    }

    @Test
    @DisplayName("Authenticate with null request should fail")
    void authenticateWithNullRequest() {
        // When & Then
        assertThrows(JwtAuthenticationException.class,
            () -> authenticationService.authenticate(null),
            "JwtAuthenticationException should be thrown for null request");

        // Verify no repository call was made
        verify(userRepository, never()).findByEmail(any());
        verify(passwordEncoderService, never()).matches(anyString(), anyString());
        verify(jwtTokenProvider, never()).generateToken(any());
    }

    @Test
    @DisplayName("Authenticate with empty password should fail")
    void authenticateWithEmptyPassword() {
        // Given
        String email = "test@example.com";
        User mockUser = User.builder()
                .email(email)
                .password("encoded_password")
                .build();
        
        when(userRepository.findByEmail(email))
            .thenReturn(Optional.of(mockUser));
        when(passwordEncoderService.matches("", "encoded_password"))
            .thenReturn(false);
        
        LoginRequestDTO loginRequestDTO = new LoginRequestDTO();
        loginRequestDTO.setEmail(email);
        loginRequestDTO.setPassword("");
        
        // When & Then
        assertThrows(JwtAuthenticationException.class,
            () -> authenticationService.authenticate(loginRequestDTO),
            "JwtAuthenticationException should be thrown for empty password");
    }
    
    @Test
    @DisplayName("Authenticate should return token with correct structure")
    void authenticateShouldReturnCorrectTokenStructure() {
        // Given
        String email = "test@example.com";
        String password = "password";
        User mockUser = User.builder()
                .email(email)
                .password("encoded_password")
                .username("testuser")
                .roleId(1L)
                .build();
        
        when(userRepository.findByEmail(email))
            .thenReturn(Optional.of(mockUser));
        when(parametricService.getRoleNameById(1L))
            .thenReturn("ROLE_USER");
        when(passwordEncoderService.matches(password, "encoded_password"))
            .thenReturn(true);
        when(jwtTokenProvider.generateToken(email, "ROLE_USER"))
            .thenReturn("mock-access-token");
        when(jwtTokenProvider.generateRefreshToken(email))
            .thenReturn("mock-refresh-token");
        
        LoginRequestDTO loginRequestDTO = new LoginRequestDTO();
        loginRequestDTO.setEmail(email);
        loginRequestDTO.setPassword(password);
        
        // When
        AuthTokenResponseDTO result = authenticationService.authenticate(loginRequestDTO);
        
        // Then
        assertNotNull(result);
        assertNotNull(result.getAccessToken());
        assertEquals("mock-access-token", result.getAccessToken());
        assertEquals("mock-refresh-token", result.getRefreshToken());
        assertEquals("ROLE_USER", result.getRole());
    }

    @Test
    @DisplayName("Authenticate should handle exception from repository gracefully when searching for user")
    void authenticateShouldHandleRepositoryExceptionWhenSearchingForUser() {
        // Given
        String email = "test@example.com";
        String password = "password";

        when(userRepository.findByEmail(email))
            .thenThrow(new RuntimeException("Database error"));

        LoginRequestDTO loginRequestDTO = new LoginRequestDTO();
        loginRequestDTO.setEmail(email);
        loginRequestDTO.setPassword(password);

        // When & Then
        assertThrows(JwtAuthenticationException.class,
            () -> authenticationService.authenticate(loginRequestDTO),
            "JwtAuthenticationException should be thrown for repository error");

        // Verify no further calls were made
        verify(passwordEncoderService, never()).matches(anyString(), anyString());
        verify(jwtTokenProvider, never()).generateToken(any());
    }

    @Test
    @DisplayName("Authenticate should handle exception from repository gracefully when updating last access")
    void authenticateShouldHandleRepositoryExceptionWhenUpdatingLastAccess() {
        // Given
        String email = "test@example.com";
        String password = "password";
        User mockUser = User.builder()
                .email(email)
                .password("encoded_password")
                .username("testuser")
                .roleId(1L)
                .statusId(1L)
                .build();
        
        when(userRepository.findByEmail(email))
            .thenReturn(Optional.of(mockUser));
        when(passwordEncoderService.matches(password, "encoded_password"))
            .thenReturn(true);
        doThrow(new RuntimeException("Database error")).when(userRepository).updateLastAccess(email);

        // When
        LoginRequestDTO loginRequestDTO = new LoginRequestDTO();
        loginRequestDTO.setEmail(email);
        loginRequestDTO.setPassword(password);

        // Then
        assertThrows(JwtAuthenticationException.class,
            () -> authenticationService.authenticate(loginRequestDTO),
            "JwtAuthenticationException should be thrown for repository error when updating last access");

        // Verify no further calls were made
        verify(jwtTokenProvider, never()).generateToken(any());
    }

    @Test
    @DisplayName("Refresh token with valid token should succeed")
    void refreshTokenWithValidToken() {
        // Given
        User mockUser = User.builder()
                .email("test@example.com")
                .password("encoded_password")
                .username("testuser")
                .roleId(1L)
                .statusId(1L)
                .build();
        String refreshTokenString = "valid-refresh-token";

        when(jwtTokenProvider.validateRefreshToken(refreshTokenString)).thenReturn(true);
        when(jwtTokenProvider.getEmailFromToken(refreshTokenString)).thenReturn("test@example.com");
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(mockUser));
        when(parametricService.getRoleNameById(1L)).thenReturn("ROLE_USER");
        when(jwtTokenProvider.generateToken("test@example.com", "ROLE_USER")).thenReturn("mock-access-token");
        when(jwtTokenProvider.generateRefreshToken("test@example.com")).thenReturn("mock-refresh-token");

        RefreshTokenRequestDTO refreshTokenRequestDTO = new RefreshTokenRequestDTO();
        refreshTokenRequestDTO.setRefreshToken(refreshTokenString);

        // When
        AuthTokenResponseDTO result = authenticationService.refreshToken(refreshTokenRequestDTO);

        // Then
        assertNotNull(result);
        assertNotNull(result.getAccessToken());
        assertEquals("mock-access-token", result.getAccessToken());
        assertEquals("mock-refresh-token", result.getRefreshToken());
        assertEquals("ROLE_USER", result.getRole());
    }

    @Test
    @DisplayName("Refresh token with invalid token should fail")
    void refreshTokenWithInvalidToken() {
        // Given
        String refreshTokenString = "invalid-refresh-token";

        when(jwtTokenProvider.validateRefreshToken(refreshTokenString))
            .thenThrow(new RuntimeException("Invalid token"));
        
        RefreshTokenRequestDTO refreshTokenRequestDTO = new RefreshTokenRequestDTO();
        refreshTokenRequestDTO.setRefreshToken(refreshTokenString);

        // When
        assertThrows(JwtAuthenticationException.class,
            () -> authenticationService.refreshToken(refreshTokenRequestDTO),
            "JwtAuthenticationException should be thrown for invalid refresh token");

        // Verify no further calls were made
        verify(jwtTokenProvider, never()).generateToken(any());
    }

    @Test
    @DisplayName("Null refresh token request should fail")
    void nullRefreshTokenRequestShouldFail() {
        // Given
        RefreshTokenRequestDTO refreshTokenRequestDTO = null;

        // When
        assertThrows(JwtAuthenticationException.class,
            () -> authenticationService.refreshToken(refreshTokenRequestDTO),
            "JwtAuthenticationException should be thrown for null refresh token request");

        // Verify no calls were made
        verify(jwtTokenProvider, never()).validateRefreshToken(anyString());
    }

    @Test
    @DisplayName("Refresh token should handle exception from repository gracefully")
    void refreshTokenShouldHandleExceptionFromRepositoryGracefully() {
        // Given
        String refreshTokenString = "valid-refresh-token";

        when(jwtTokenProvider.validateRefreshToken(refreshTokenString)).thenReturn(true);
        when(jwtTokenProvider.getEmailFromToken(refreshTokenString)).thenReturn("test@example.com");
        when(userRepository.findByEmail("test@example.com")).thenThrow(new RuntimeException("Database error"));

        RefreshTokenRequestDTO refreshTokenRequestDTO = new RefreshTokenRequestDTO();
        refreshTokenRequestDTO.setRefreshToken(refreshTokenString);

        // When
        assertThrows(JwtAuthenticationException.class,
            () -> authenticationService.refreshToken(refreshTokenRequestDTO),
            "JwtAuthenticationException should be thrown for repository error");

        // Verify no further calls were made
        verify(jwtTokenProvider, never()).generateToken(any());
    }

    @Test
    @DisplayName("Refresh token that has no email should fail")
    void refreshTokenWithNoEmailShouldFail() {
        // Given
        String refreshTokenString = "not-valid-refresh-token";

        when(jwtTokenProvider.validateRefreshToken(refreshTokenString)).thenReturn(true);
        when(jwtTokenProvider.getEmailFromToken(refreshTokenString)).thenReturn(null);

        RefreshTokenRequestDTO refreshTokenRequestDTO = new RefreshTokenRequestDTO();
        refreshTokenRequestDTO.setRefreshToken(refreshTokenString);

        // When
        assertThrows(JwtAuthenticationException.class,
            () -> authenticationService.refreshToken(refreshTokenRequestDTO),
            "JwtAuthenticationException should be thrown for missing email in token");

        // Verify no further calls were made
        verify(jwtTokenProvider, never()).generateToken(any());
    }

    @Test
    @DisplayName("Refresh token with non-existent user should fail")
    void refreshTokenWithNonExistentUserShouldFail() {
        // Given
        String refreshTokenString = "valid-refresh-token";

        when(jwtTokenProvider.validateRefreshToken(refreshTokenString)).thenReturn(true);
        when(jwtTokenProvider.getEmailFromToken(refreshTokenString)).thenReturn("test@example.com");
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.empty());

        RefreshTokenRequestDTO refreshTokenRequestDTO = new RefreshTokenRequestDTO();
        refreshTokenRequestDTO.setRefreshToken(refreshTokenString);

        // When
        assertThrows(JwtAuthenticationException.class,
            () -> authenticationService.refreshToken(refreshTokenRequestDTO),
            "JwtAuthenticationException should be thrown for non-existent user");

        // Verify no further calls were made
        verify(jwtTokenProvider, never()).generateToken(any());
    }

    @Test
    @DisplayName("Access token provided instead of refresh token should fail")
    void accessTokenProvidedInsteadOfRefreshTokenShouldFail() {
        // Given
        String accessTokenString = "valid-access-token";

        when(jwtTokenProvider.validateRefreshToken(accessTokenString)).thenReturn(false);

        RefreshTokenRequestDTO refreshTokenRequestDTO = new RefreshTokenRequestDTO();
        refreshTokenRequestDTO.setRefreshToken(accessTokenString);

        // When
        assertThrows(JwtAuthenticationException.class,
            () -> authenticationService.refreshToken(refreshTokenRequestDTO),
            "JwtAuthenticationException should be thrown for access token used as refresh token");

        // Verify no further calls were made
        verify(userRepository, never()).findByEmail(anyString());
        verify(jwtTokenProvider, never()).generateToken(any());
    }
}