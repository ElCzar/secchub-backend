package co.edu.puj.secchub_backend.security.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;

import co.edu.puj.secchub_backend.parametric.contracts.ParametricContract;
import co.edu.puj.secchub_backend.security.model.User;
import co.edu.puj.secchub_backend.security.repository.UserRepository;
import reactor.core.publisher.Mono;

/**
 * Unit tests for ReactiveUserDetailsServiceImpl.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("ReactiveUserDetailsServiceImpl Unit Test")
class ReactiveUserDetailsServiceImplTest {
    @Mock
    private UserRepository userRepository;
    @Mock
    private ParametricContract parametricService;

    @InjectMocks
    private ReactiveUserDetailsServiceImpl reactiveUserDetailsServiceImpl;

    @Test
    @DisplayName("FindByUsername - When user is found and active, then should return UserDetails")
    void testFindByUsername_UserFoundAndActive_ReturnsUserDetails() {
        // Arrange
        User user = User.builder()
                .username("testuser")
                .password("password")
                .statusId(1L)
                .roleId(2L)
                .build();
        
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
        when(parametricService.getStatusNameById(1L)).thenReturn("Active");
        when(parametricService.getRoleNameById(2L)).thenReturn("ROLE_ADMIN");

        // When & Then
        UserDetails result = reactiveUserDetailsServiceImpl.findByUsername("testuser").as(Mono::block);

        // Then
        assertEquals("testuser", result.getUsername(), "Username should match");
        assertEquals("password", result.getPassword(), "Password should match");
        assertTrue(result.getAuthorities().stream().anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN")), "User should have ROLE_ADMIN authority");

        // Verify
        verify(userRepository).findByUsername("testuser");
        verify(parametricService).getStatusNameById(1L);
        verify(parametricService).getRoleNameById(2L);
    }

    @Test
    @DisplayName("FindByUsername - When user is not found, then should return empty Mono")
    void testFindByUsername_UserNotFound_ReturnsEmptyMono() {
        // Arrange
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.empty());

        // When
        UserDetails result = reactiveUserDetailsServiceImpl.findByUsername("testuser").block();

        // Then
        assertNull(result, "Result should be null for non-existing user");

        // Verify
        verify(userRepository).findByUsername("testuser");
    }

    @Test
    @DisplayName("FindByUsername - When user is inactive, then should return empty Mono")
    void testFindByUsername_UserInactive_ReturnsEmptyMono() {
        // Arrange
        User user = mock(User.class);
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
        when(user.getStatusId()).thenReturn(1L);
        when(parametricService.getStatusNameById(1L)).thenReturn("Inactive");

        // When
        UserDetails result = reactiveUserDetailsServiceImpl.findByUsername("testuser").block();

        // Then
        assertNull(result, "Result should be null for inactive user");

        // Verify
        verify(userRepository).findByUsername("testuser");
        verify(parametricService).getStatusNameById(1L);
    }

    @Test
    @DisplayName("FindByUsername - When user has no status, then should return empty Mono")
    void testFindByUsername_UserNoStatus_ReturnsEmptyMono() {
        // Arrange
        User user = mock(User.class);
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
        when(user.getStatusId()).thenReturn(null);

        // When
        UserDetails result = reactiveUserDetailsServiceImpl.findByUsername("testuser").block();

        // Then
        assertNull(result, "Result should be null for user with no status");

        // Verify
        verify(userRepository).findByUsername("testuser");
    }

    @Test
    @DisplayName("FindByEmail - When user is found and active, then should return Authentication")
    void testFindByEmail_UserFoundAndActive_ReturnsAuthentication() {
        // Arrange
        User user = mock(User.class);
        when(user.getUsername()).thenReturn("testuser");
        when(user.getEmail()).thenReturn("test@example.com");
        when(user.getPassword()).thenReturn("password");
        when(user.getStatusId()).thenReturn(1L);
        when(user.getRoleId()).thenReturn(2L);

        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
        when(parametricService.getStatusNameById(1L)).thenReturn("Active");
        when(parametricService.getRoleNameById(2L)).thenReturn("ROLE_ADMIN");

        // When
        Authentication result = reactiveUserDetailsServiceImpl.findByEmail("test@example.com").block();

        // Then
        assertEquals("test@example.com", result.getName(), "Name should match");
        assertEquals("password", result.getCredentials(), "Credentials should match");
        assertTrue(result.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN")), "User should have ROLE_ADMIN authority");

        // Verify
        verify(userRepository).findByEmail("test@example.com");
        verify(parametricService).getStatusNameById(1L);
        verify(parametricService).getRoleNameById(2L);
    }

    @Test
    @DisplayName("FindByEmail - When user is found, but has no role, then should assign default ROLE_USER")
    void testFindByEmail_UserFoundNoRole_AssignsDefaultRoleUser() {
        // Arrange
        User user = mock(User.class);
        when(user.getUsername()).thenReturn("testuser");
        when(user.getEmail()).thenReturn("test@example.com");
        when(user.getPassword()).thenReturn("password");
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
        when(user.getStatusId()).thenReturn(1L);
        when(parametricService.getStatusNameById(1L)).thenReturn("Active");
        when(user.getRoleId()).thenReturn(null);

        // When
        Authentication result = reactiveUserDetailsServiceImpl.findByEmail("test@example.com").block();

        // Then
        assertEquals("test@example.com", result.getName(), "Name should match");
        assertEquals("password", result.getCredentials(), "Credentials should match");
        assertTrue(result.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_USER")), "User should have ROLE_USER authority");

        // Verify
        verify(userRepository).findByEmail("test@example.com");
        verify(parametricService).getStatusNameById(1L);
    }

    @Test
    @DisplayName("FindByEmail - When user is not found, then should return empty Mono")
    void testFindByEmail_UserNotFound_ReturnsEmptyMono() {
        // Arrange
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.empty());

        // When
        Authentication result = reactiveUserDetailsServiceImpl.findByEmail("test@example.com").block();
        
        // Then
        assertNull(result, "Result should be null for non-existing user");

        // Verify
        verify(userRepository).findByEmail("test@example.com");
    }

    @Test
    @DisplayName("FindByEmail - When user is inactive, then should return empty Mono")
    void testFindByEmail_UserInactive_ReturnsEmptyMono() {
        // Arrange
        User user = mock(User.class);
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
        when(user.getStatusId()).thenReturn(1L);
        when(parametricService.getStatusNameById(1L)).thenReturn("Inactive");

        // When
        Authentication result = reactiveUserDetailsServiceImpl.findByEmail("test@example.com").block();

        // Then
        assertNull(result, "Result should be null for inactive user");

        // Verify
        verify(userRepository).findByEmail("test@example.com");
    }

    @Test
    @DisplayName("FindByEmail - When user has no status, then should return empty Mono")
    void testFindByEmail_UserNoStatus_ReturnsEmptyMono() {
        // Arrange
        User user = mock(User.class);
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
        when(user.getStatusId()).thenReturn(null);

        // When
        Authentication result = reactiveUserDetailsServiceImpl.findByEmail("test@example.com").block();

        // Then
        assertNull(result, "Result should be null for user with no status");

        // Verify
        verify(userRepository).findByEmail("test@example.com");
    }
}
