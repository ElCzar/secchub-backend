package co.edu.puj.secchub_backend.integration.dto;


import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO that represents a batch of academic requests.
 * Is used when a user submits multiple academic requests at once.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AcademicRequestBatchRequestDTO {
    private List<AcademicRequestRequestDTO> requests;
}
