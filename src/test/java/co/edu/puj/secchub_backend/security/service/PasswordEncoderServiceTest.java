package co.edu.puj.secchub_backend.security.service;

import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.lenient;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;


/**
 * Unit test for PasswordEncoderService
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("PasswordEncoderService Unit Test")
class PasswordEncoderServiceTest {
    
    @Mock
    private PasswordEncoder passwordEncoder;
    
    private PasswordEncoderService passwordEncoderService;
    
    // Use a real BCrypt encoder for realistic encoding in tests
    private final BCryptPasswordEncoder realEncoder = new BCryptPasswordEncoder();
    
    @BeforeEach
    void setUp() {
        passwordEncoderService = new PasswordEncoderService(passwordEncoder);
        
        // Configure mock to use real BCrypt for realistic behavior
        lenient().when(passwordEncoder.encode(anyString())).thenAnswer(invocation -> 
            realEncoder.encode(invocation.getArgument(0, String.class))
        );
        lenient().when(passwordEncoder.matches(anyString(), anyString())).thenAnswer(invocation -> 
            realEncoder.matches(invocation.getArgument(0, String.class), invocation.getArgument(1, String.class))
        );
    }

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
        String encoded = passwordEncoderService.encode(input);
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
        String encoded = passwordEncoderService.encode(differentInput);
        // When
        boolean result = passwordEncoderService.matches(input, encoded);
        // Then
        assertFalse(result, "Input should not match a different encoded value");
    }
}
