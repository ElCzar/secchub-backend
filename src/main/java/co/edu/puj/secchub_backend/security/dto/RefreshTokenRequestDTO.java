package co.edu.puj.secchub_backend.security.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Data Transfer Object for refresh token requests.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class RefreshTokenRequestDTO {
    private String refreshToken;
}
