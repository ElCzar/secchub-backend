package co.edu.puj.secchub_backend.integration.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * DTO for individual academic requests that are being processed.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class IndividualRequestDTO {
    private Object id; // Changed to Object to handle both String and Long IDs
    private String program;
    private String materia;
    private Integer cupos;
    private String startDate;
    private String endDate;
    private String comments;
    private String comentarios;
    private Boolean selected;
    private String state; // 'new', 'existing', 'deleted'
    private List<Object> schedules; // Flexible schedule objects
}