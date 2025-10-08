package co.edu.puj.secchub_backend.security.model;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Entity representing a user in the system.
 * Contains personal and authentication details.
 */
@Entity
@Table(name = "users")
@Data
@NoArgsConstructor 
@AllArgsConstructor
public class User {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String username;
    private String password;
    private String faculty;
    private String name;
    @Column(name = "last_name")
    private String lastName;
    private String email;

    @Column(name = "status_id")
    private Long statusId;

    @Column(name = "last_access")
    private LocalDateTime lastAccess;

    @Column(name = "role_id")
    private Long roleId;

    @Column(name = "document_type_id")
    private Long documentType;

    @Column(name = "document_number")
    private String documentNumber;
}