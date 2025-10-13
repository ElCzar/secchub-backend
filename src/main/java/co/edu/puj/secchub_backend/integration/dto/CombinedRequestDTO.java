package co.edu.puj.secchub_backend.integration.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * DTO for combined academic requests from multiple programs for the same subject.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CombinedRequestDTO {
    private String id; // Changed from Long to String to handle frontend-generated IDs
    private List<String> programs;
    private List<String> materias;
    private Integer cupos;
    private String startDate;
    private String endDate;
    private List<Long> sourceIds; // IDs of the original requests being combined
    private Boolean editable;
}