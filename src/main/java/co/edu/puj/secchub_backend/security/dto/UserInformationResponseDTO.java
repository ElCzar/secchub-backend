package co.edu.puj.secchub_backend.security.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Data Transfer Object for user information response.
 * This DTO is used to send user details in responses, excluding sensitive information like passwords.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserInformationResponseDTO {
    private Long id;
    private String username;
    private String faculty;
    private String name;
    private String lastName;
    private String email;
    private Long statusId;
    private Long roleId;
    private Long documentType;
    private String documentNumber;
}
