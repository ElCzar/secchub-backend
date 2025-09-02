package co.edu.puj.secchub_backend.security.jwt;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.server.authentication.ServerAuthenticationConverter;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationConverter implements ServerAuthenticationConverter {

    private final JwtTokenProvider tokenProvider;

    /**
     * Converts the incoming request to an Authentication object.
     * @param exchange the server web exchange
     * @return a Mono emitting the Authentication object, or empty if no valid token is found
     */
    @Override
    public Mono<Authentication> convert(ServerWebExchange exchange) {
        return extractTokenFromRequest(exchange)
                .filter(tokenProvider::validateAccessToken)
                .map(token -> new UsernamePasswordAuthenticationToken(
                        tokenProvider.getEmailFromToken(token),
                        token
                ));
    }

    /**
     * Extracts the JWT token from the request headers.
     * @param exchange the server web exchange
     * @return a Mono emitting the JWT token, or empty if not found
     */
    private Mono<String> extractTokenFromRequest(ServerWebExchange exchange) {
        return Mono.justOrEmpty(exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION))
                .filter(authHeader -> authHeader.startsWith("Bearer "))
                .map(authHeader -> authHeader.substring(7));
    }
}