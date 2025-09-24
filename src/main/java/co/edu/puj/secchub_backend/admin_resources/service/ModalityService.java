package co.edu.puj.secchub_backend.admin_resources.service;

import co.edu.puj.secchub_backend.admin_resources.dto.ModalityDTO;
import co.edu.puj.secchub_backend.admin_resources.exception.CustomException;
import co.edu.puj.secchub_backend.admin_resources.model.Modality;
import co.edu.puj.secchub_backend.admin_resources.repository.ModalityRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Service class for managing modality operations.
 * 
 * <p>This service provides comprehensive functionality for modality management
 * including CRUD operations and validation. It serves as the business logic
 * layer for modality administration in the academic planning module.</p>
 * 
 * <p>Key functionalities include:
 * <ul>
 * <li>Modality CRUD operations with validation</li>
 * <li>Search by name</li>
 * <li>Business rule enforcement</li>
 * <li>Data transformation between entities and DTOs</li>
 * </ul></p>
 * 
 * @author SecHub Development Team
 * @version 1.0
 * @since 1.0
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ModalityService {
    
    private final ModalityRepository modalityRepository;
    private final ModelMapper modelMapper;
    
    /**
     * Get all modalities.
     * 
     * @return list of all modalities as DTOs
     */
    public List<ModalityDTO> getAllModalities() {
        log.info("Obteniendo todas las modalidades");
        List<Modality> modalities = modalityRepository.findAllOrderedByName();
        return modalities.stream()
            .map(this::convertToDTO)
            .collect(Collectors.toList());
    }
    
    /**
     * Get modality by ID.
     * 
     * @param id the modality ID
     * @return modality DTO
     * @throws CustomException if modality not found
     */
    public ModalityDTO getModalityById(Long id) {
        log.info("Obteniendo modalidad por ID: {}", id);
        Modality modality = modalityRepository.findById(id)
            .orElseThrow(() -> new CustomException("Modalidad no encontrada con ID: " + id));
        return convertToDTO(modality);
    }
    
    /**
     * Create a new modality.
     * 
     * @param modalityDTO the modality data
     * @return created modality DTO
     * @throws CustomException if validation fails
     */
    @Transactional
    public ModalityDTO createModality(ModalityDTO modalityDTO) {
        log.info("Creando nueva modalidad: {}", modalityDTO.getName());
        
        validateModalityData(modalityDTO);
        validateNoDuplicateModality(modalityDTO.getName());
        
        Modality modality = convertToEntity(modalityDTO);
        Modality savedModality = modalityRepository.save(modality);
        
        log.info("Modalidad creada exitosamente con ID: {}", savedModality.getId());
        return convertToDTO(savedModality);
    }
    
    /**
     * Update an existing modality.
     * 
     * @param id the modality ID
     * @param modalityDTO the updated modality data
     * @return updated modality DTO
     * @throws CustomException if modality not found or validation fails
     */
    @Transactional
    public ModalityDTO updateModality(Long id, ModalityDTO modalityDTO) {
        log.info("Actualizando modalidad con ID: {}", id);
        
        Modality existingModality = modalityRepository.findById(id)
            .orElseThrow(() -> new CustomException("Modalidad no encontrada con ID: " + id));
        
        validateModalityData(modalityDTO);
        
        // Only check for duplicates if the name is changing
        if (!existingModality.getName().equalsIgnoreCase(modalityDTO.getName())) {
            validateNoDuplicateModality(modalityDTO.getName());
        }
        
        existingModality.setName(modalityDTO.getName());
        Modality updatedModality = modalityRepository.save(existingModality);
        
        log.info("Modalidad actualizada exitosamente");
        return convertToDTO(updatedModality);
    }
    
    /**
     * Delete a modality.
     * 
     * @param id the modality ID
     * @throws CustomException if modality not found or has dependencies
     */
    @Transactional
    public void deleteModality(Long id) {
        log.info("Eliminando modalidad con ID: {}", id);
        
        if (!modalityRepository.existsById(id)) {
            throw new CustomException("Modalidad no encontrada con ID: " + id);
        }
        
        // TODO: Verificar que no tenga horarios asignados antes de eliminar
        
        modalityRepository.deleteById(id);
        log.info("Modalidad eliminada exitosamente");
    }
    
    // ==========================================
    // MÉTODOS PRIVADOS DE VALIDACIÓN
    // ==========================================
    
    private void validateModalityData(ModalityDTO modalityDTO) {
        if (modalityDTO.getName() == null || modalityDTO.getName().trim().isEmpty()) {
            throw new CustomException("El nombre de la modalidad es obligatorio");
        }
        if (modalityDTO.getName().length() > 100) {
            throw new CustomException("El nombre de la modalidad no puede exceder 100 caracteres");
        }
    }
    
    private void validateNoDuplicateModality(String name) {
        if (modalityRepository.existsByNameIgnoreCase(name)) {
            throw new CustomException("Ya existe una modalidad con el nombre: " + name);
        }
    }
    
    private ModalityDTO convertToDTO(Modality modality) {
        ModalityDTO dto = modelMapper.map(modality, ModalityDTO.class);
        
        // Agregar descripción basada en el nombre si no está establecida
        if (dto.getDescription() == null) {
            switch (modality.getName().toUpperCase()) {
                case "IN-PERSON":
                case "PRESENCIAL":
                    dto.setDescription("Clase presencial");
                    break;
                case "ONLINE":
                case "VIRTUAL":
                    dto.setDescription("Clase virtual");
                    break;
                case "HYBRID":
                case "HIBRIDO":
                case "HÍBRIDO":
                    dto.setDescription("Clase híbrida");
                    break;
                default:
                    dto.setDescription("Modalidad de enseñanza");
            }
        }
        
        return dto;
    }
    
    private Modality convertToEntity(ModalityDTO dto) {
        return Modality.builder()
            .name(dto.getName())
            .build();
    }
}
