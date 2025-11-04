package co.edu.puj.secchub_backend.admin.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Entity representing a Section.
 * A Section groups courses and belongs to a user (head of section).
 */
@Table("section")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Section {

    @Id
    private Long id;

    @Column("user_id")
    private Long userId;

    @Column
    private String name;

    @Column("planning_closed")
    private boolean planningClosed;
}
