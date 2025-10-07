package co.edu.puj.secchub_backend.security.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Data Transfer Object for authentication tokens.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class AuthTokenResponseDTO {
    private String message;
    private long issuedAt;
    private String accessToken;
    private String refreshToken;
    private String tokenType;
    private String role;
}
