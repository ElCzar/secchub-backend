package co.edu.puj.secchub_backend.security.model;

import java.time.LocalDateTime;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Entity representing a user in the system.
 * Contains personal and authentication details.
 */
@Table("users")
@Data
@NoArgsConstructor 
@AllArgsConstructor
@Builder
public class User {

    @Id
    private Long id;

    private String username;
    private String password;
    private String faculty;
    private String name;

    @Column("last_name")
    private String lastName;

    private String email;

    @Column("status_id")
    private Long statusId;

    @Column("last_access")
    private LocalDateTime lastAccess;

    @Column("role_id")
    private Long roleId;

    @Column("document_type_id")
    private Long documentTypeId;

    @Column("document_number")
    private String documentNumber;
}