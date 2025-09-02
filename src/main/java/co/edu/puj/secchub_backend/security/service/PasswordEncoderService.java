package co.edu.puj.secchub_backend.security.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PasswordEncoderService {

    private final PasswordEncoder passwordEncoder;

    /**
     * Encodes a raw password using BCrypt
     * @param rawPassword the raw password to encode
     * @return the encoded password
     */
    public String encode(String rawPassword) {
        return passwordEncoder.encode(rawPassword);
    }

    /**
     * Verifies a raw password against an encoded password
     * @param rawPassword the raw password to verify
     * @param encodedPassword the encoded password to verify against
     * @return true if the passwords match, false otherwise
     */
    public boolean matches(String rawPassword, String encodedPassword) {
        return passwordEncoder.matches(rawPassword, encodedPassword);
    }
}