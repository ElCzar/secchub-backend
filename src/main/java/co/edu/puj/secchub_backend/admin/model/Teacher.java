package co.edu.puj.secchub_backend.admin.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.persistence.*;

/**
 * Entity representing a Teacher in the university system.
 * Teachers are associated with user accounts and have specific employment types and workload limits.
 */
@Entity
@Table(name = "teacher")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Teacher {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "user_id")
    private Long userId;

    @Column(name = "employment_type_id")
    private Long employmentTypeId;
    
    @Column(name = "max_hours")
    private Integer maxHours;
}
