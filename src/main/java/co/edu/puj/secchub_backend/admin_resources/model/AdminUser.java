package co.edu.puj.secchub_backend.admin_resources.model;

import jakarta.persistence.*;
import lombok.*;
import java.util.Date;

@Entity(name = "AdminUser")
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdminUser {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 150)
    private String username;

    @Column(nullable = false, length = 255)
    private String password;

    @Column(length = 150)
    private String faculty;

    @Column(nullable = false, length = 150)
    private String name;

    @Column(nullable = false, length = 150)
    private String last_name;

    @Column(nullable = false, length = 255, unique = true)
    private String email;

    @Column
    private Long status_id;

    @Column
    private Date last_access;

    @Column
    private Long role_id;

    @Column
    private Long document_type_id;

    @Column(length = 50)
    private String document_number;
}
