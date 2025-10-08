package co.edu.puj.secchub_backend.parametric.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Entity representing an employment type in the system.
 * Employment types define the nature of employment for teachers (e.g., full-time, part-time
 */
@Entity
@Table(name = "employment_type")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmploymentType {
    @Id
    @GeneratedValue(strategy = jakarta.persistence.GenerationType.IDENTITY)
    private Long id;

    @Column
    private String name;
}
