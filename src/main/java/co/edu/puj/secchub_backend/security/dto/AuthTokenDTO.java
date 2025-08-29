package co.edu.puj.secchub_backend.security.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AuthTokenDTO {
    private String message;
    private String email;
    private String token;
}
