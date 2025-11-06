package co.edu.puj.secchub_backend.parametric.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.modelmapper.ModelMapper;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import co.edu.puj.secchub_backend.parametric.contracts.ClassroomTypeDTO;
import co.edu.puj.secchub_backend.parametric.contracts.DocumentTypeDTO;
import co.edu.puj.secchub_backend.parametric.contracts.EmploymentTypeDTO;
import co.edu.puj.secchub_backend.parametric.contracts.ModalityDTO;
import co.edu.puj.secchub_backend.parametric.contracts.RoleDTO;
import co.edu.puj.secchub_backend.parametric.contracts.StatusDTO;
import co.edu.puj.secchub_backend.parametric.exception.ParametricValueNotFoundException;
import co.edu.puj.secchub_backend.parametric.model.*;
import co.edu.puj.secchub_backend.parametric.repository.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@ExtendWith(MockitoExtension.class)
@DisplayName("ParametricService Unit Test")
class ParametricServiceTest {

    @Mock
    private ModelMapper modelMapper;

    @Mock
    private StatusRepository statusRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private DocumentTypeRepository documentTypeRepository;

    @Mock
    private EmploymentTypeRepository employmentTypeRepository;

    @Mock
    private ModalityRepository modalityRepository;

    @Mock
    private ClassroomTypeRepository classroomTypeRepository;

    @InjectMocks
    private ParametricService parametricService;

    // ==========================================
    // STATUS Tests
    // ==========================================

    @Test
    @DisplayName("getAllStatuses - Should return mapped list of all statuses")
    void testGetAllStatuses_ReturnsMappedList() {
        Status s1 = Status.builder().id(1L).name("Active").build();
        Status s2 = Status.builder().id(2L).name("Inactive").build();
        List<Status> statuses = Arrays.asList(s1, s2);

        StatusDTO dto1 = StatusDTO.builder().id(1L).name("Active").build();
        StatusDTO dto2 = StatusDTO.builder().id(2L).name("Inactive").build();

        when(statusRepository.findAll()).thenReturn(Flux.fromIterable(statuses));
        when(modelMapper.map(s1, StatusDTO.class)).thenReturn(dto1);
        when(modelMapper.map(s2, StatusDTO.class)).thenReturn(dto2);

        List<StatusDTO> result = parametricService.getAllStatuses().collectList().block();

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("Active", result.get(0).getName());
        assertEquals("Inactive", result.get(1).getName());
        verify(statusRepository).findAll();
        verify(modelMapper).map(s1, StatusDTO.class);
        verify(modelMapper).map(s2, StatusDTO.class);
    }

    @Test
    @DisplayName("getStatusByName - When status exists returns DTO")
    void testGetStatusByName_StatusExists_ReturnsDTO() {
        Status status = Status.builder().id(1L).name("Active").build();
        StatusDTO dto = StatusDTO.builder().id(1L).name("Active").build();

        when(statusRepository.findByName("Active")).thenReturn(Mono.just(status));
        when(modelMapper.map(status, StatusDTO.class)).thenReturn(dto);

        StatusDTO result = parametricService.getStatusByName("Active").block();

        assertNotNull(result);
        assertEquals("Active", result.getName());
        verify(statusRepository).findByName("Active");
        verify(modelMapper).map(status, StatusDTO.class);
    }

    @Test
    @DisplayName("getStatusByName - When status not found throws ParametricValueNotFoundException")
    void testGetStatusByName_StatusNotFound_Throws() {
        when(statusRepository.findByName("Unknown")).thenReturn(Mono.empty());

        Mono<StatusDTO> statusMono = parametricService.getStatusByName("Unknown");
        assertThrows(ParametricValueNotFoundException.class, statusMono::block);
        verify(statusRepository).findByName("Unknown");
    }

    @Test
    @DisplayName("getStatusNameById - When status exists returns name")
    void testGetStatusNameById_StatusExists_ReturnsName() {
        Status status = Status.builder().id(1L).name("Active").build();

        when(statusRepository.findById(1L)).thenReturn(Mono.just(status));

        String result = parametricService.getStatusNameById(1L).block();

        assertEquals("Active", result);
        verify(statusRepository).findById(1L);
    }

    @Test
    @DisplayName("getStatusNameById - When status not found throws ParametricValueNotFoundException")
    void testGetStatusNameById_StatusNotFound_Throws() {
        when(statusRepository.findById(99L)).thenReturn(Mono.empty());

        Mono<String> nameMono = parametricService.getStatusNameById(99L);
        assertThrows(ParametricValueNotFoundException.class, nameMono::block);
        verify(statusRepository).findById(99L);
    }

    @Test
    @DisplayName("statusExists - When status exists returns true")
    void testStatusExists_StatusExists_ReturnsTrue() {
        when(statusRepository.existsByName("Active")).thenReturn(Mono.just(true));

        Boolean result = parametricService.statusExists("Active").block();

        assertTrue(result);
        verify(statusRepository).existsByName("Active");
    }

    // ==========================================
    // ROLE Tests
    // ==========================================

    @Test
    @DisplayName("getAllRoles - Should return mapped list of all roles")
    void testGetAllRoles_ReturnsMappedList() {
        Role r1 = Role.builder().id(1L).name("ADMIN").build();
        Role r2 = Role.builder().id(2L).name("USER").build();
        List<Role> roles = Arrays.asList(r1, r2);

        RoleDTO dto1 = RoleDTO.builder().id(1L).name("ADMIN").build();
        RoleDTO dto2 = RoleDTO.builder().id(2L).name("USER").build();

        when(roleRepository.findAll()).thenReturn(Flux.fromIterable(roles));
        when(modelMapper.map(r1, RoleDTO.class)).thenReturn(dto1);
        when(modelMapper.map(r2, RoleDTO.class)).thenReturn(dto2);

        List<RoleDTO> result = parametricService.getAllRoles().collectList().block();

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("ADMIN", result.get(0).getName());
        verify(roleRepository).findAll();
    }

    @Test
    @DisplayName("getRoleByName - When role exists returns DTO")
    void testGetRoleByName_RoleExists_ReturnsDTO() {
        Role role = Role.builder().id(1L).name("ADMIN").build();
        RoleDTO dto = RoleDTO.builder().id(1L).name("ADMIN").build();

        when(roleRepository.findByName("ADMIN")).thenReturn(Mono.just(role));
        when(modelMapper.map(role, RoleDTO.class)).thenReturn(dto);

        RoleDTO result = parametricService.getRoleByName("ADMIN").block();

        assertNotNull(result);
        assertEquals("ADMIN", result.getName());
        verify(roleRepository).findByName("ADMIN");
    }

    @Test
    @DisplayName("getRoleByName - When role not found throws ParametricValueNotFoundException")
    void testGetRoleByName_RoleNotFound_Throws() {
        when(roleRepository.findByName("INVALID")).thenReturn(Mono.empty());

        Mono<RoleDTO> roleMono = parametricService.getRoleByName("INVALID");
        assertThrows(ParametricValueNotFoundException.class, roleMono::block);
        verify(roleRepository).findByName("INVALID");
    }

    // ==========================================
    // DOCUMENT TYPE Tests
    // ==========================================

    @Test
    @DisplayName("getAllDocumentTypes - Should return mapped list of all document types")
    void testGetAllDocumentTypes_ReturnsMappedList() {
        DocumentType dt1 = DocumentType.builder().id(1L).name("CC").build();
        DocumentType dt2 = DocumentType.builder().id(2L).name("TI").build();

        DocumentTypeDTO dto1 = DocumentTypeDTO.builder().id(1L).name("CC").build();
        DocumentTypeDTO dto2 = DocumentTypeDTO.builder().id(2L).name("TI").build();

        when(documentTypeRepository.findAll()).thenReturn(Flux.just(dt1, dt2));
        when(modelMapper.map(dt1, DocumentTypeDTO.class)).thenReturn(dto1);
        when(modelMapper.map(dt2, DocumentTypeDTO.class)).thenReturn(dto2);

        List<DocumentTypeDTO> result = parametricService.getAllDocumentTypes().collectList().block();

        assertNotNull(result);
        assertEquals(2, result.size());
        verify(documentTypeRepository).findAll();
    }

    @Test
    @DisplayName("getDocumentTypeByName - When document type exists returns DTO")
    void testGetDocumentTypeByName_DocumentTypeExists_ReturnsDTO() {
        DocumentType docType = DocumentType.builder().id(1L).name("CC").build();
        DocumentTypeDTO dto = DocumentTypeDTO.builder().id(1L).name("CC").build();

        when(documentTypeRepository.findByName("CC")).thenReturn(Mono.just(docType));
        when(modelMapper.map(docType, DocumentTypeDTO.class)).thenReturn(dto);

        DocumentTypeDTO result = parametricService.getDocumentTypeByName("CC").block();

        assertNotNull(result);
        assertEquals("CC", result.getName());
        verify(documentTypeRepository).findByName("CC");
    }

    // ==========================================
    // EMPLOYMENT TYPE Tests
    // ==========================================

    @Test
    @DisplayName("getAllEmploymentTypes - Should return mapped list of all employment types")
    void testGetAllEmploymentTypes_ReturnsMappedList() {
        EmploymentType et1 = EmploymentType.builder().id(1L).name("Full-Time").build();
        EmploymentType et2 = EmploymentType.builder().id(2L).name("Part-Time").build();

        EmploymentTypeDTO dto1 = EmploymentTypeDTO.builder().id(1L).name("Full-Time").build();
        EmploymentTypeDTO dto2 = EmploymentTypeDTO.builder().id(2L).name("Part-Time").build();

        when(employmentTypeRepository.findAll()).thenReturn(Flux.just(et1, et2));
        when(modelMapper.map(et1, EmploymentTypeDTO.class)).thenReturn(dto1);
        when(modelMapper.map(et2, EmploymentTypeDTO.class)).thenReturn(dto2);

        List<EmploymentTypeDTO> result = parametricService.getAllEmploymentTypes().collectList().block();

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("Full-Time", result.get(0).getName());
        verify(employmentTypeRepository).findAll();
    }

    @Test
    @DisplayName("getEmploymentTypeNameById - When employment type exists returns name")
    void testGetEmploymentTypeNameById_EmploymentTypeExists_ReturnsName() {
        EmploymentType empType = EmploymentType.builder().id(1L).name("Full-Time").build();

        when(employmentTypeRepository.findById(1L)).thenReturn(Mono.just(empType));

        String result = parametricService.getEmploymentTypeNameById(1L).block();

        assertEquals("Full-Time", result);
        verify(employmentTypeRepository).findById(1L);
    }

    // ==========================================
    // MODALITY Tests
    // ==========================================

    @Test
    @DisplayName("getAllModalities - Should return mapped list of all modalities")
    void testGetAllModalities_ReturnsMappedList() {
        Modality m1 = Modality.builder().id(1L).name("Presencial").build();
        Modality m2 = Modality.builder().id(2L).name("Virtual").build();

        ModalityDTO dto1 = ModalityDTO.builder().id(1L).name("Presencial").build();
        ModalityDTO dto2 = ModalityDTO.builder().id(2L).name("Virtual").build();

        when(modalityRepository.findAll()).thenReturn(Flux.just(m1, m2));
        when(modelMapper.map(m1, ModalityDTO.class)).thenReturn(dto1);
        when(modelMapper.map(m2, ModalityDTO.class)).thenReturn(dto2);

        List<ModalityDTO> result = parametricService.getAllModalities().collectList().block();

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("Presencial", result.get(0).getName());
        verify(modalityRepository).findAll();
    }

    @Test
    @DisplayName("getModalityByName - When modality exists returns DTO")
    void testGetModalityByName_ModalityExists_ReturnsDTO() {
        Modality modality = Modality.builder().id(1L).name("Presencial").build();
        ModalityDTO dto = ModalityDTO.builder().id(1L).name("Presencial").build();

        when(modalityRepository.findByName("Presencial")).thenReturn(Mono.just(modality));
        when(modelMapper.map(modality, ModalityDTO.class)).thenReturn(dto);

        ModalityDTO result = parametricService.getModalityByName("Presencial").block();

        assertNotNull(result);
        assertEquals("Presencial", result.getName());
        verify(modalityRepository).findByName("Presencial");
    }

    // ==========================================
    // CLASSROOM TYPE Tests
    // ==========================================

    @Test
    @DisplayName("getAllClassroomTypes - Should return mapped list of all classroom types")
    void testGetAllClassroomTypes_ReturnsMappedList() {
        ClassroomType ct1 = ClassroomType.builder().id(1L).name("Aula").build();
        ClassroomType ct2 = ClassroomType.builder().id(2L).name("Laboratorio").build();

        ClassroomTypeDTO dto1 = ClassroomTypeDTO.builder().id(1L).name("Aula").build();
        ClassroomTypeDTO dto2 = ClassroomTypeDTO.builder().id(2L).name("Laboratorio").build();

        when(classroomTypeRepository.findAll()).thenReturn(Flux.just(ct1, ct2));
        when(modelMapper.map(ct1, ClassroomTypeDTO.class)).thenReturn(dto1);
        when(modelMapper.map(ct2, ClassroomTypeDTO.class)).thenReturn(dto2);

        List<ClassroomTypeDTO> result = parametricService.getAllClassroomTypes().collectList().block();

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("Aula", result.get(0).getName());
        assertEquals("Laboratorio", result.get(1).getName());
        verify(classroomTypeRepository).findAll();
    }

    @Test
    @DisplayName("getClassroomTypeByName - When classroom type exists returns DTO")
    void testGetClassroomTypeByName_ClassroomTypeExists_ReturnsDTO() {
        ClassroomType classroomType = ClassroomType.builder().id(1L).name("Aula").build();
        ClassroomTypeDTO dto = ClassroomTypeDTO.builder().id(1L).name("Aula").build();

        when(classroomTypeRepository.findByName("Aula")).thenReturn(Mono.just(classroomType));
        when(modelMapper.map(classroomType, ClassroomTypeDTO.class)).thenReturn(dto);

        ClassroomTypeDTO result = parametricService.getClassroomTypeByName("Aula").block();

        assertNotNull(result);
        assertEquals("Aula", result.getName());
        verify(classroomTypeRepository).findByName("Aula");
    }

    @Test
    @DisplayName("getClassroomTypeByName - When classroom type not found throws ParametricValueNotFoundException")
    void testGetClassroomTypeByName_ClassroomTypeNotFound_Throws() {
        when(classroomTypeRepository.findByName("Unknown")).thenReturn(Mono.empty());

        Mono<ClassroomTypeDTO> classroomTypeMono = parametricService.getClassroomTypeByName("Unknown");
        assertThrows(ParametricValueNotFoundException.class, classroomTypeMono::block);
        verify(classroomTypeRepository).findByName("Unknown");
    }

    @Test
    @DisplayName("getClassroomTypeNameById - When classroom type exists returns name")
    void testGetClassroomTypeNameById_ClassroomTypeExists_ReturnsName() {
        ClassroomType classroomType = ClassroomType.builder().id(1L).name("Aula").build();

        when(classroomTypeRepository.findById(1L)).thenReturn(Mono.just(classroomType));

        String result = parametricService.getClassroomTypeNameById(1L).block();

        assertEquals("Aula", result);
        verify(classroomTypeRepository).findById(1L);
    }

    @Test
    @DisplayName("classroomTypeExists - When classroom type exists returns true")
    void testClassroomTypeExists_ClassroomTypeExists_ReturnsTrue() {
        when(classroomTypeRepository.existsByName("Aula")).thenReturn(Mono.just(true));

        Boolean result = parametricService.classroomTypeExists("Aula").block();

        assertTrue(result);
        verify(classroomTypeRepository).existsByName("Aula");
    }
}