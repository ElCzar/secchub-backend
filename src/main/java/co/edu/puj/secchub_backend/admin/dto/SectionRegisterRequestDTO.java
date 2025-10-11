package co.edu.puj.secchub_backend.admin.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO that represents a section registration request.
 * It contains information uploaded by the user to create a section.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SectionRegisterRequestDTO {
    private String name;
    private UserRegisterRequestDTO user;
}
