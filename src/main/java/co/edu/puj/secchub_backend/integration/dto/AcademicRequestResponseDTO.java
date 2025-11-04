package co.edu.puj.secchub_backend.integration.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

/**
 * DTO for academic request responses.
 * Contains complete information about an academic request including system-generated data.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AcademicRequestResponseDTO {
    private Long id;
    private Long userId;
    private Long courseId;
    private Long semesterId;
    private LocalDate startDate;
    private LocalDate endDate;
    private Integer capacity;
    private LocalDate requestDate;
    private String observation;
    private List<RequestScheduleResponseDTO> schedules;
    
    // Campos de estado
    private Boolean accepted;       // true si la solicitud fue aceptada y llevada a planificación
    private Boolean combined;       // true si la solicitud fue combinada con otras
    
    // Campos enriquecidos para el frontend
    private String userName;        // Nombre completo del usuario que hizo la solicitud
    private String courseName;      // Nombre del curso
    private String programName;     // Nombre del programa
}
