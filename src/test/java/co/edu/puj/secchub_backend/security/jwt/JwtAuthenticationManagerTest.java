package co.edu.puj.secchub_backend.security.jwt;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.Mock;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import co.edu.puj.secchub_backend.security.service.ReactiveUserDetailsServiceImpl;
import reactor.core.publisher.Mono;

@ExtendWith(MockitoExtension.class)
@DisplayName("JwtAuthenticationManager Unit Test")
class JwtAuthenticationManagerTest {

    @Mock
    private JwtTokenProvider tokenProvider;

    @Mock
    private ReactiveUserDetailsServiceImpl userDetailsService;

    @Test
    @DisplayName("authenticate - When token is valid and user exists returns Authentication with expected principal, credentials and authorities")
    void authenticate_ValidToken_UserExists_ReturnsAuthentication() {
        String token = "validToken";
        String email = "user@example.com";

        when(tokenProvider.getEmailFromToken(token)).thenReturn(email);

        Authentication userAuth = new UsernamePasswordAuthenticationToken(
                "userPrincipal",
                "ignored",
                List.of(new SimpleGrantedAuthority("ROLE_USER"))
        );
        when(userDetailsService.findByEmail(email)).thenReturn(Mono.just(userAuth));

        JwtAuthenticationManager manager = new JwtAuthenticationManager(tokenProvider, userDetailsService);

        Authentication result = manager.authenticate(new UsernamePasswordAuthenticationToken(null, token)).block();

        assertNotNull(result);
        assertEquals("userPrincipal", result.getPrincipal());
        assertEquals(token, result.getCredentials());
        assertTrue(result.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_USER")));

        verify(tokenProvider).getEmailFromToken(token);
        verify(userDetailsService).findByEmail(email);
    }

    @Test
    @DisplayName("authenticate - When user not found returns empty Mono (null on block)")
    void authenticate_UserNotFound_ReturnsEmpty() {
        String token = "someToken";
        String email = "notfound@example.com";

        when(tokenProvider.getEmailFromToken(token)).thenReturn(email);
        when(userDetailsService.findByEmail(email)).thenReturn(Mono.empty());

        JwtAuthenticationManager manager = new JwtAuthenticationManager(tokenProvider, userDetailsService);

        Authentication result = manager.authenticate(new UsernamePasswordAuthenticationToken(null, token)).block();

        assertNull(result);

        verify(tokenProvider).getEmailFromToken(token);
        verify(userDetailsService).findByEmail(email);
    }

    @Test
    @DisplayName("authenticate - When userDetailsService errors the Mono should propagate error")
    void authenticate_UserServiceError_PropagatesError() {
        String token = "errToken";
        String email = "error@example.com";

        when(tokenProvider.getEmailFromToken(token)).thenReturn(email);
        when(userDetailsService.findByEmail(email)).thenReturn(Mono.error(new RuntimeException("backend failure")));

        JwtAuthenticationManager manager = new JwtAuthenticationManager(tokenProvider, userDetailsService);

        UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(null, token);
        Mono<Authentication> resultMono = manager.authenticate(authToken);
        assertThrows(RuntimeException.class, resultMono::block);

        verify(tokenProvider).getEmailFromToken(token);
        verify(userDetailsService).findByEmail(email);
    }
}
