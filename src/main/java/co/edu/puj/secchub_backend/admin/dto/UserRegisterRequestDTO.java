package co.edu.puj.secchub_backend.admin.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserRegisterRequestDTO {
    private String username;
    private String password;
    private String faculty;
    private String name;
    private String lastName;
    private String email;
    private String documentTypeId;
    private String documentNumber;
}
