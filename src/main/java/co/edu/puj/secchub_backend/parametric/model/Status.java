package co.edu.puj.secchub_backend.parametric.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Entity representing a status in the system.
 * Statuses are used to track the state of various entities like users, courses, 
 * academic requests, classes, and teacher assignments.
 * This is a parametric lookup table that contains predefined status values.
 */
@Table("status")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Status {
    @Id
    private Long id;
    
    @Column("name")
    private String name;
}