package co.edu.puj.secchub_backend.security.jwt;

import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import co.edu.puj.secchub_backend.security.service.ReactiveUserDetailsServiceImpl;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationManager implements ReactiveAuthenticationManager {

    private final JwtTokenProvider tokenProvider;
    private final ReactiveUserDetailsServiceImpl userDetailsService;

    @Override
    public Mono<Authentication> authenticate(Authentication authentication) {
        String token = (String) authentication.getCredentials();
        String email = tokenProvider.getEmailFromToken(token);

        return userDetailsService.findByEmail(email)
                .map(userDetails -> createAuthenticationToken(userDetails, token));
    }

    private Authentication createAuthenticationToken(Authentication userDetails, String token) {
        return new UsernamePasswordAuthenticationToken(
                userDetails.getPrincipal(),
                token,
                userDetails.getAuthorities()
        );
    }
}