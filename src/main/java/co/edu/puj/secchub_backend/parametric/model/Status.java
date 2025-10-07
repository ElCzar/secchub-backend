package co.edu.puj.secchub_backend.parametric.model;

import jakarta.persistence.*;
import lombok.*;

/**
 * Entity representing a status in the system.
 * Statuses are used to track the state of various entities like users, courses, 
 * academic requests, classes, and teacher assignments.
 * This is a parametric lookup table that contains predefined status values.
 */
@Entity
@Table(name = "status")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Status {

    /**
     * Unique identifier for the status.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Name of the status.
     * This field is unique and cannot be null.
     * Examples: "ACTIVE", "INACTIVE", "PENDING", "APPROVED", "REJECTED"
     */
    @Column(name = "name", nullable = false, unique = true, length = 100)
    private String name;

    @Override
    public String toString() {
        return "Status{" +
                "id=" + id +
                ", name='" + name + '\'' +
                '}';
    }
}