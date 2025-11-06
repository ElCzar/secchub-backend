package co.edu.puj.secchub_backend.parametric.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Entity representing an employment type in the system.
 * Employment types define the nature of employment for teachers (e.g., full-time, part-time, etc.)
 */
@Table("employment_type")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmploymentType {
    @Id
    private Long id;

    @Column
    private String name;
}
