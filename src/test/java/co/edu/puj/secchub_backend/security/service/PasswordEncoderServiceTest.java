package co.edu.puj.secchub_backend.security.service;

import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;

import co.edu.puj.secchub_backend.DatabaseContainerIntegration;


/**
 * Test class for PasswordEncoderService
 */
@SpringBootTest
@DisplayName("PasswordEncoderService Integration Test")
class PasswordEncoderServiceTest extends DatabaseContainerIntegration {
    @Autowired
    private PasswordEncoderService passwordEncoderService;
    @Autowired
    private PasswordEncoder passwordEncoder;

    @DisplayName("Encode should delegate to PasswordEncoder and return encoded value")
    @ParameterizedTest(name = "{0} is different after encoding")
    @ValueSource(strings = {"password123", "admin!@#", "user_pass", "12345678"})
    void encode_delegatesToPasswordEncoder_andReturnsEncodedValue(String input) {
        // When
        String result = passwordEncoderService.encode(input);

        // Then
        assertNotEquals(input, result, "Encoded value should be different from the input");
    }

    @DisplayName("Matches should delegate to PasswordEncoder and return true when matching")
    @ParameterizedTest(name = "{0} matches its encoded value")
    @ValueSource(strings = {"password123", "admin!@#", "user_pass", "12345678"})
    void matches_delegatesToPasswordEncoder_andReturnsTrueWhenMatching(String input) {
        // Given
        String encoded = passwordEncoder.encode(input);
        // When
        boolean result = passwordEncoderService.matches(input, encoded);
        // Then
        assertTrue(result, "Input should match the encoded value");
    }

    @DisplayName("Matches should delegate to PasswordEncoder and return false when not matching")
    @ParameterizedTest(name = "{0} does not match a different encoded value")
    @ValueSource(strings = {"password123", "admin!@#", "user_pass", "12345678"})
    void matches_delegatesToPasswordEncoder_andReturnsFalseWhenNotMatching(String input) {
        // Given
        String differentInput = input + "diff";
        String encoded = passwordEncoder.encode(differentInput);
        // When
        boolean result = passwordEncoderService.matches(input, encoded);
        // Then
        assertFalse(result, "Input should not match a different encoded value");
    }
}
