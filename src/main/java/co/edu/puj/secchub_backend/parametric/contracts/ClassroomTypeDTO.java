package co.edu.puj.secchub_backend.parametric.contracts;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for ClassroomType entity representation.
 * Used for transferring classroom type information between layers.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClassroomTypeDTO {
    
    /**
     * Unique identifier of the classroom type.
     */
    private Long id;
    
    /**
     * Name of the classroom type.
     * Example: "COMPUTER_LAB", "LECTURE_HALL", "LABORATORY".
     */
    private String name;
}