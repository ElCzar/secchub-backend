package co.edu.puj.secchub_backend.integration.model;

import jakarta.persistence.*;
import lombok.*;

/**
 * Entity representing a Section.
 * A Section groups courses and belongs to a user (head of section).
 */
@Entity
@Table(name = "section")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Section {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id")
    private Long userId;

    @Column(nullable = false, length = 150)
    private String name;
}
