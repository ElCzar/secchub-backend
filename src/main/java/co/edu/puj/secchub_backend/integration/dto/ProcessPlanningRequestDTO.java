package co.edu.puj.secchub_backend.integration.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * DTO for processing planning requests with combined and individual academic requests.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProcessPlanningRequestDTO {
    private List<CombinedRequestDTO> combinedRequests;
    private List<IndividualRequestDTO> individualRequests;
}