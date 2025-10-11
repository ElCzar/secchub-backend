package co.edu.puj.secchub_backend.security.contract;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Data Transfer Object for user creation requests.
 * This DTO encapsulates the necessary information required to create a new user in the system.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserCreationRequestDTO {
    private String username;
    private String password;
    private String faculty;
    private String name;
    private String lastName;
    private String email;
    private Long statusId;
    private Long roleId;
    private Long documentTypeId;
    private String documentNumber;
}
