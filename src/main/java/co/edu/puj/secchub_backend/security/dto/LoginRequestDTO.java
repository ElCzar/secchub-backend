package co.edu.puj.secchub_backend.security.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Data Transfer Object for login requests.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class LoginRequestDTO {
    private String email;
    private String password;
}
