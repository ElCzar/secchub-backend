package co.edu.puj.secchub_backend.security.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.SignatureException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Date;
import org.springframework.security.core.userdetails.UserDetails;

@Slf4j
@Component
public class JwtTokenProvider {
    private static final String ACCESS_TOKEN_TYPE = "access";
    private static final String REFRESH_TOKEN_TYPE = "refresh";
    public static final String BEARER_PREFIX = "Bearer ";

    private final SecretKey key;
    private final long jwtExpirationMs;
    private final long jwtRefreshExpirationMs;
    private final String jwtIssuer;

    public JwtTokenProvider(@Value("${jwt.secret}") String jwtSecret,
                            @Value("${jwt.expiration-ms}") long jwtExpirationMs,
                            @Value("${jwt.refresh-expiration-ms}") long jwtRefreshExpirationMs,
                            @Value("${jwt.issuer:secchub.javeriana.edu.co}") String jwtIssuer) {
        // Decode the Base64 secret
        byte[] keyBytes = Base64.getDecoder().decode(jwtSecret.getBytes(StandardCharsets.UTF_8));
        if (keyBytes.length < 32) {
            throw new IllegalArgumentException("JWT secret must be at least 256 bits (32 bytes) when decoded");
        }
        this.key = new SecretKeySpec(keyBytes, "HmacSHA256");
        this.jwtExpirationMs = jwtExpirationMs;
        this.jwtRefreshExpirationMs = jwtRefreshExpirationMs;
        this.jwtIssuer = jwtIssuer;
    }

    /**
     * Generates a JWT token for the given email and role.
     * @param email
     * @param role
     * @return A JWT token for authentication
     */
    public String generateToken(String email, String role) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtExpirationMs);
        log.debug("Generating token for email: {} with role: {}", email, role);

        return Jwts.builder()
                .subject(email)
                .claim("role", role)
                .claim("type", ACCESS_TOKEN_TYPE)
                .issuer(jwtIssuer)
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(key)
                .compact();
    }

    /**
     * Generates a JWT refresh token for the given user email.
     * @param email
     * @return A JWT refresh token for authentication
     */
    public String generateRefreshToken(String email) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtRefreshExpirationMs);
        log.debug("Generating refresh token for email: {}", email);

        return Jwts.builder()
                .subject(email)
                .claim("type", REFRESH_TOKEN_TYPE)
                .issuer(jwtIssuer)
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(key)
                .compact();
    }

    /**
     * Generates a JWT token for the given user details.
     * @param userDetails the user details
     * @return A JWT token for authentication
     */
    public String generateToken(UserDetails userDetails) {
        return Jwts.builder()
                .subject(userDetails.getUsername())
                .claim("type", ACCESS_TOKEN_TYPE)
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + jwtExpirationMs))
                .signWith(key)
                .compact();
    }

    /**
     * Extracts the claims from the JWT token.
     * @param token
     * @return The claims contained in the JWT token.
     */
    public Claims getClaims(String token) {
        return extractAllClaims(token);
    }

    /**
     * Extracts all claims from the JWT token.
     * @param token
     * @return The claims contained in the JWT token.
     */
    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /**
     * Extracts the email from the JWT token.
     * @param token
     * @return The email contained in the JWT token.
     */
    public String getEmailFromToken(String token) {
        return getClaims(token).getSubject();
    }

    /**
     * Extracts the role from the JWT token.
     * @param token
     * @return The role contained in the JWT token.
     */
    public String getRoleFromToken(String token) {
        return getClaims(token).get("role", String.class);
    }

    /**
     * Obtains the token type from the JWT token.
     * @param token
     * @return The token type contained in the JWT token.
     */
    public String getTokenTypeFromToken(String token) {
        return getClaims(token).get("type", String.class);
    }

    /**
     * Checks if token is an access token
     * @param token
     * @return true if the token is an access token, false otherwise.
     */
    public boolean isAccessToken(String token) {
        try {
            return ACCESS_TOKEN_TYPE.equals(getTokenTypeFromToken(token));
        } catch (Exception e) {
            log.error("Error checking if token is access token: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Checks if token is a refresh token
     * @param token
     * @return true if the token is a refresh token, false otherwise.
     */
    public boolean isRefreshToken(String token) {
        try {
            return REFRESH_TOKEN_TYPE.equals(getTokenTypeFromToken(token));
        } catch (Exception e) {
            log.error("Error checking if token is refresh token: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Validates the JWT token.
     * @param token
     * @return true if the token is valid, false otherwise.
     */
    public boolean validateToken(String token) {
        try {
            Jws<Claims> claims = Jwts.parser()
                    .verifyWith(key)
                    .requireIssuer(jwtIssuer)
                    .build()
                    .parseSignedClaims(token);

            // Verify token expiration
            return !claims.getPayload().getExpiration().before(new Date());
        } catch (SignatureException ex) {
            log.error("Invalid JWT signature");
        } catch (MalformedJwtException ex) {
            log.error("Invalid JWT token");
        } catch (ExpiredJwtException ex) {
            log.error("Expired JWT token");
        } catch (UnsupportedJwtException ex) {
            log.error("Unsupported JWT token");
        } catch (IllegalArgumentException ex) {
            log.error("JWT claims string is empty: {}", ex.getMessage());
        }
        return false;
    }

    /**
     * Validate an access token.
     * @param token
     * @return true if the token is a valid access token, false otherwise.
     */
    public boolean validateAccessToken(String token) {
        return validateToken(token) && isAccessToken(token);
    }

    /**
     * Validates a refresh token.
     * @param token
     * @return true if the token is a valid refresh token, false otherwise.
     */
    public boolean validateRefreshToken(String token) {
        return validateToken(token) && isRefreshToken(token);
    }

    /**
     * @return The expiration time of the JWT token.
     */
    public long getJwtExpirationMs() {
        return jwtExpirationMs;
    }

    /**
     * @return The expiration time of the JWT refresh token.
     */
    public long getJwtRefreshExpirationMs() {
        return jwtRefreshExpirationMs;
    }
}