package co.edu.puj.secchub_backend.integration.service;

import co.edu.puj.secchub_backend.admin.dto.SemesterResponseDTO;
import co.edu.puj.secchub_backend.admin.service.SemesterService;
import co.edu.puj.secchub_backend.integration.dto.ProgramContextDTO;
import co.edu.puj.secchub_backend.security.dto.UserInformationResponseDTO;
import co.edu.puj.secchub_backend.security.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

/**
 * Service for program context operations.
 * Provides context information for programs including career name and current semester.
 */
@Service
@RequiredArgsConstructor
public class ProgramContextService {
    
    private final UserService userService;
    private final SemesterService semesterService;

    /**
     * Gets the program context information for the authenticated user.
     * @return ProgramContextDTO containing career name and current semester
     */
    public Mono<ProgramContextDTO> getProgramContext() {
        // Get both user information and current semester in parallel
        Mono<UserInformationResponseDTO> userMono = userService.getUserInformation();
        Mono<SemesterResponseDTO> semesterMono = semesterService.getCurrentSemester();
        
        return Mono.zip(userMono, semesterMono)
                .map(tuple -> {
                    UserInformationResponseDTO user = tuple.getT1();
                    SemesterResponseDTO semester = tuple.getT2();
                    
                    // Format semester as "YYYY-{period}"
                    String formattedSemester = String.format("%d-%d", semester.getYear(), semester.getPeriod());
                    
                    return new ProgramContextDTO(user.getName(), formattedSemester);
                });
    }
}