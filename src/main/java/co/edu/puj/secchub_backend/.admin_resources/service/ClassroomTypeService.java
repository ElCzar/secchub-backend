package co.edu.puj.secchub_backend.admin_resources.service;

import co.edu.puj.secchub_backend.admin_resources.dto.ClassroomTypeDTO;
import co.edu.puj.secchub_backend.admin_resources.exception.CustomException;
import co.edu.puj.secchub_backend.admin_resources.model.ClassroomType;
import co.edu.puj.secchub_backend.admin_resources.repository.ClassroomTypeRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Service class for managing classroom type operations.
 * 
 * <p>This service provides comprehensive functionality for classroom type management
 * including CRUD operations and validation. It serves as the business logic
 * layer for classroom type administration in the academic planning module.</p>
 * 
 * <p>Key functionalities include:
 * <ul>
 * <li>Classroom type CRUD operations with validation</li>
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
public class ClassroomTypeService {
    
    private final ClassroomTypeRepository classroomTypeRepository;
    private final ModelMapper modelMapper;
    
    /**
     * Get all classroom types.
     * 
     * @return list of all classroom types as DTOs
     */
    public List<ClassroomTypeDTO> getAllClassroomTypes() {
        log.info("Obteniendo todos los tipos de aula");
        List<ClassroomType> classroomTypes = classroomTypeRepository.findAllOrderedByName();
        return classroomTypes.stream()
            .map(this::convertToDTO)
            .collect(Collectors.toList());
    }
    
    /**
     * Get classroom type by ID.
     * 
     * @param id the classroom type ID
     * @return classroom type DTO
     * @throws CustomException if classroom type not found
     */
    public ClassroomTypeDTO getClassroomTypeById(Long id) {
        log.info("Obteniendo tipo de aula por ID: {}", id);
        ClassroomType classroomType = classroomTypeRepository.findById(id)
            .orElseThrow(() -> new CustomException("Tipo de aula no encontrado con ID: " + id));
        return convertToDTO(classroomType);
    }
    
    /**
     * Create a new classroom type.
     * 
     * @param classroomTypeDTO the classroom type data
     * @return created classroom type DTO
     * @throws CustomException if validation fails
     */
    @Transactional
    public ClassroomTypeDTO createClassroomType(ClassroomTypeDTO classroomTypeDTO) {
        log.info("Creando nuevo tipo de aula: {}", classroomTypeDTO.getName());
        
        validateClassroomTypeData(classroomTypeDTO);
        validateNoDuplicateClassroomType(classroomTypeDTO.getName());
        
        ClassroomType classroomType = convertToEntity(classroomTypeDTO);
        ClassroomType savedClassroomType = classroomTypeRepository.save(classroomType);
        
        log.info("Tipo de aula creado exitosamente con ID: {}", savedClassroomType.getId());
        return convertToDTO(savedClassroomType);
    }
    
    /**
     * Update an existing classroom type.
     * 
     * @param id the classroom type ID
     * @param classroomTypeDTO the updated classroom type data
     * @return updated classroom type DTO
     * @throws CustomException if classroom type not found or validation fails
     */
    @Transactional
    public ClassroomTypeDTO updateClassroomType(Long id, ClassroomTypeDTO classroomTypeDTO) {
        log.info("Actualizando tipo de aula con ID: {}", id);
        
        ClassroomType existingClassroomType = classroomTypeRepository.findById(id)
            .orElseThrow(() -> new CustomException("Tipo de aula no encontrado con ID: " + id));
        
        validateClassroomTypeData(classroomTypeDTO);
        
        // Only check for duplicates if the name is changing
        if (!existingClassroomType.getName().equalsIgnoreCase(classroomTypeDTO.getName())) {
            validateNoDuplicateClassroomType(classroomTypeDTO.getName());
        }
        
        existingClassroomType.setName(classroomTypeDTO.getName());
        ClassroomType updatedClassroomType = classroomTypeRepository.save(existingClassroomType);
        
        log.info("Tipo de aula actualizado exitosamente");
        return convertToDTO(updatedClassroomType);
    }
    
    /**
     * Delete a classroom type.
     * 
     * @param id the classroom type ID
     * @throws CustomException if classroom type not found or has dependencies
     */
    @Transactional
    public void deleteClassroomType(Long id) {
        log.info("Eliminando tipo de aula con ID: {}", id);
        
        if (!classroomTypeRepository.existsById(id)) {
            throw new CustomException("Tipo de aula no encontrado con ID: " + id);
        }
        
        // TODO: Verificar que no tenga aulas asignadas antes de eliminar
        
        classroomTypeRepository.deleteById(id);
        log.info("Tipo de aula eliminado exitosamente");
    }
    
    // ==========================================
    // MÉTODOS PRIVADOS DE VALIDACIÓN
    // ==========================================
    
    private void validateClassroomTypeData(ClassroomTypeDTO classroomTypeDTO) {
        if (classroomTypeDTO.getName() == null || classroomTypeDTO.getName().trim().isEmpty()) {
            throw new CustomException("El nombre del tipo de aula es obligatorio");
        }
        if (classroomTypeDTO.getName().length() > 100) {
            throw new CustomException("El nombre del tipo de aula no puede exceder 100 caracteres");
        }
    }
    
    private void validateNoDuplicateClassroomType(String name) {
        if (classroomTypeRepository.existsByNameIgnoreCase(name)) {
            throw new CustomException("Ya existe un tipo de aula con el nombre: " + name);
        }
    }
    
    private ClassroomTypeDTO convertToDTO(ClassroomType classroomType) {
        ClassroomTypeDTO dto = modelMapper.map(classroomType, ClassroomTypeDTO.class);
        
        // Agregar descripción basada en el nombre si no está establecida
        if (dto.getDescription() == null) {
            switch (classroomType.getName().toLowerCase()) {
                case "aulas":
                    dto.setDescription("Aulas tradicionales");
                    break;
                case "laboratorio":
                    dto.setDescription("Laboratorios");
                    break;
                case "aulas moviles":
                case "aulas móviles":
                    dto.setDescription("Aulas móviles");
                    break;
                case "aulas accesibles":
                    dto.setDescription("Aulas con accesibilidad");
                    break;
                default:
                    dto.setDescription("Tipo de aula especializada");
            }
        }
        
        return dto;
    }
    
    private ClassroomType convertToEntity(ClassroomTypeDTO dto) {
        return ClassroomType.builder()
            .name(dto.getName())
            .build();
    }
}
