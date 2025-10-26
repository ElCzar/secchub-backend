package co.edu.puj.secchub_backend.security.jwt;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Base64;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;

/**
 * Test class for JwtTokenProvider.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("JwtTokenProvider Unit Test")
class JwtTokenProviderTest {
    @InjectMocks
    private static JwtTokenProvider jwtTokenProvider;

    @BeforeAll
    static void setup() {
        jwtTokenProvider = new JwtTokenProvider(
            "VGhpc0lzQVNlY3JldEtleUZvckpXVE9rZW5Qcm92aWRlcg==",
            3600000L,
            86400000L,
            "secchub.javeriana.edu.co"
        );
    }

    @Test
    @DisplayName("Constructor throws when secret is too short")
    void constructor_throws_when_secret_too_short() {
        String shortBase64 = Base64.getEncoder().encodeToString(new byte[16]); // decodes to 16 bytes
        assertThrows(IllegalArgumentException.class, () -> 
            new JwtTokenProvider(shortBase64, 1000L, 2000L, "issuer")
        );
    }

    @Test
    @DisplayName("Generate access token and validate properties")
    void generate_access_token_and_validate_properties() {
        String secret = Base64.getEncoder().encodeToString(new byte[32]); // decodes to 32 bytes
        JwtTokenProvider provider = new JwtTokenProvider(secret, 100000L, 200000L, "issuer");

        String email = "user@example.com";
        String role = "ROLE_ADMIN";
        String token = provider.generateToken(email, role);

        assertTrue(provider.validateToken(token), "Generated token should be valid");
        assertTrue(provider.validateAccessToken(token), "Generated token should be a valid access token");
        assertEquals(email, provider.getEmailFromToken(token));
        assertEquals(role, provider.getRoleFromToken(token));
        assertEquals("access", provider.getTokenTypeFromToken(token));
        assertTrue(provider.isAccessToken(token));
        assertFalse(provider.isRefreshToken(token));
    }

    @Test
    @DisplayName("Generate refresh token and validate properties")
    void generate_refresh_token_and_validate_properties() {
        String secret = Base64.getEncoder().encodeToString(new byte[32]);
        JwtTokenProvider provider = new JwtTokenProvider(secret, 100000L, 200000L, "issuer");

        String email = "refresh@example.com";
        String refresh = provider.generateRefreshToken(email);

        assertTrue(provider.validateToken(refresh), "Refresh token should be structurally valid");
        assertTrue(provider.validateRefreshToken(refresh), "Should be valid refresh token");
        assertEquals(email, provider.getEmailFromToken(refresh));
        assertEquals("refresh", provider.getTokenTypeFromToken(refresh));
        assertTrue(provider.isRefreshToken(refresh));
        assertFalse(provider.isAccessToken(refresh));
    }

    @Test
    @DisplayName("Expired token is invalid")
    void expired_token_is_invalid() {
        String secret = Base64.getEncoder().encodeToString(new byte[32]);
        // negative expiration to create already-expired token
        JwtTokenProvider provider = new JwtTokenProvider(secret, -1000L, -1000L, "issuer");

        String token = provider.generateToken("a@b.com", "ROLE_X");
        assertFalse(provider.validateToken(token), "Token generated with negative expiration should be invalid");
        assertFalse(provider.validateAccessToken(token), "Access token check should be false for expired token");
    }

    @Test
    @DisplayName("Malformed token is invalid")
    void malformed_token_is_invalid() {
        String secret = Base64.getEncoder().encodeToString(new byte[32]);
        JwtTokenProvider provider = new JwtTokenProvider(secret, 100000L, 200000L, "issuer");

        String bad = "not.a.valid.jwt";
        assertFalse(provider.validateToken(bad), "Malformed token should not validate");
        assertFalse(provider.validateAccessToken(bad), "Malformed token should not be a valid access token");
        assertFalse(provider.validateRefreshToken(bad), "Malformed token should not be a valid refresh token");
    }

    @Test
    @DisplayName("Generate token with UserDetails sets expected claims and validates")
    void generateToken_with_UserDetails_sets_expected_claims_and_validates() {
        String secret = Base64.getEncoder().encodeToString(new byte[32]);
        JwtTokenProvider provider = new JwtTokenProvider(secret, 100000L, 200000L, "issuer");

        // Mock UserDetails
        UserDetails userDetails = mock(UserDetails.class);
        when(userDetails.getUsername()).thenReturn("user1@domain.com");

        String token = provider.generateToken(userDetails);

        assertTrue(provider.validateToken(token), "Token generated from UserDetails should be valid");
        assertTrue(provider.validateAccessToken(token), "Should be a valid access token");
        assertEquals("user1@domain.com", provider.getEmailFromToken(token));
        assertEquals("access", provider.getTokenTypeFromToken(token));
        assertTrue(provider.isAccessToken(token));
        assertFalse(provider.isRefreshToken(token));
    }

    @Test
    @DisplayName("Access token is identified correctly")
    void access_token_is_identified_correctly() {
        String secret = Base64.getEncoder().encodeToString(new byte[32]);
        JwtTokenProvider provider = new JwtTokenProvider(secret, 100000L, 200000L, "issuer");

        String token = provider.generateToken("user@example.com", "ROLE_USER");

        assertTrue(provider.isAccessToken(token), "Token should be identified as access token");
        assertFalse(provider.isRefreshToken(token), "Token should not be identified as refresh token");
    }

    @Test
    @DisplayName("Access token is not misidentified as refresh token")
    void access_token_is_not_misidentified_as_refresh_token() {
        String secret = Base64.getEncoder().encodeToString(new byte[32]);
        JwtTokenProvider provider = new JwtTokenProvider(secret, 100000L, 200000L, "issuer");

        String token = provider.generateToken("user@example.com", "ROLE_USER");

        assertFalse(provider.isRefreshToken(token), "Token should not be identified as refresh token");
    }

    @Test
    @DisplayName("Refresh token is identified correctly")
    void refresh_token_is_identified_correctly() {
        String secret = Base64.getEncoder().encodeToString(new byte[32]);
        JwtTokenProvider provider = new JwtTokenProvider(secret, 100000L, 200000L, "issuer");

        String token = provider.generateRefreshToken("user@example.com");

        assertTrue(provider.isRefreshToken(token), "Token should be identified as refresh token");
        assertFalse(provider.isAccessToken(token), "Token should not be identified as access token");
    }

    @Test
    @DisplayName("Refresh token is not misidentified as access token")
    void refresh_token_is_not_misidentified_as_access_token() {
        String secret = Base64.getEncoder().encodeToString(new byte[32]);
        JwtTokenProvider provider = new JwtTokenProvider(secret, 100000L, 200000L, "issuer");

        String token = provider.generateRefreshToken("user@example.com");

        assertFalse(provider.isAccessToken(token), "Token should not be identified as access token");
    }
}
