package co.edu.puj.secchub_backend.admin.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


/**
 * Entity representing a Teacher in the university system.
 * Teachers are associated with user accounts and have specific employment types and workload limits.
 */
@Table("teacher")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Teacher {
    @Id
    private Long id;
    
    @Column("user_id")
    private Long userId;

    @Column("employment_type_id")
    private Long employmentTypeId;
    
    @Column("max_hours")
    private Integer maxHours;
}
