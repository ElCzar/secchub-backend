package co.edu.puj.secchub_backend.security.jwt;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.Mock;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.security.core.Authentication;

/**
 * Unit tests for JwtAuthenticationConverter.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("JwtAuthenticationConverter Unit Test")
class JwtAuthenticationConverterTest {

    @Mock
    private JwtTokenProvider tokenProvider;

    @Mock
    private ServerWebExchange exchange;

    @Mock
    private ServerHttpRequest request;

    @Test
    @DisplayName("convert - When Authorization header contains valid Bearer token returns Authentication")
    void convert_WithValidBearerToken_ReturnsAuthentication() {
        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.AUTHORIZATION, "Bearer token123");

        when(exchange.getRequest()).thenReturn(request);
        when(request.getHeaders()).thenReturn(headers);

        when(tokenProvider.validateAccessToken("token123")).thenReturn(true);
        when(tokenProvider.getEmailFromToken("token123")).thenReturn("user@example.com");

        JwtAuthenticationConverter converter = new JwtAuthenticationConverter(tokenProvider);

        Authentication auth = converter.convert(exchange).block();

        assertNotNull(auth);
        assertEquals("user@example.com", auth.getName());
        assertEquals("token123", auth.getCredentials());
    }

    @Test
    @DisplayName("convert - When Authorization header is missing returns empty Mono")
    void convert_MissingAuthorizationHeader_ReturnsEmpty() {
        HttpHeaders headers = new HttpHeaders();

        when(exchange.getRequest()).thenReturn(request);
        when(request.getHeaders()).thenReturn(headers);

        JwtAuthenticationConverter converter = new JwtAuthenticationConverter(tokenProvider);

        Authentication auth = converter.convert(exchange).block();

        assertNull(auth);
        verifyNoInteractions(tokenProvider);
    }

    @Test
    @DisplayName("convert - When Authorization header does not start with Bearer returns empty Mono")
    void convert_NonBearerAuthorizationHeader_ReturnsEmpty() {
        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.AUTHORIZATION, "Basic abcdef");

        when(exchange.getRequest()).thenReturn(request);
        when(request.getHeaders()).thenReturn(headers);

        JwtAuthenticationConverter converter = new JwtAuthenticationConverter(tokenProvider);

        Authentication auth = converter.convert(exchange).block();

        assertNull(auth);
        verifyNoInteractions(tokenProvider);
    }

    @Test
    @DisplayName("convert - When token is invalid according to provider returns empty Mono")
    void convert_InvalidToken_ReturnsEmpty() {
        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.AUTHORIZATION, "Bearer badtoken");

        when(exchange.getRequest()).thenReturn(request);
        when(request.getHeaders()).thenReturn(headers);

        when(tokenProvider.validateAccessToken("badtoken")).thenReturn(false);

        JwtAuthenticationConverter converter = new JwtAuthenticationConverter(tokenProvider);

        Authentication auth = converter.convert(exchange).block();

        assertNull(auth);
        verify(tokenProvider).validateAccessToken("badtoken");
        verify(tokenProvider, never()).getEmailFromToken(anyString());
    }
}
