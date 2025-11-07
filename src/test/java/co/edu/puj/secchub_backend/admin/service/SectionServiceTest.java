package co.edu.puj.secchub_backend.admin.service;

import co.edu.puj.secchub_backend.admin.dto.SectionCreateRequestDTO;
import co.edu.puj.secchub_backend.admin.dto.SectionResponseDTO;
import co.edu.puj.secchub_backend.admin.exception.SectionNotFoundException;
import co.edu.puj.secchub_backend.admin.model.Section;
import co.edu.puj.secchub_backend.admin.repository.SectionRepository;
import co.edu.puj.secchub_backend.security.contract.SecurityModuleUserContract;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;

import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

@ExtendWith(MockitoExtension.class)
@DisplayName("SectionService Unit Tests")
class SectionServiceTest {

    @Mock
    private ModelMapper modelMapper;

    @Mock
    private DatabaseClient databaseClient;

    @Mock
    private SectionRepository sectionRepository;

    @Mock
    private SecurityModuleUserContract userService;

    @InjectMocks
    private SectionService sectionService;

    private Section section;
    private SectionResponseDTO responseDTO;

    @BeforeEach
    void setUp() {
        section = Section.builder()
                .id(1L)
                .userId(100L)
                .name("NEW SECTION")
                .planningClosed(false)
                .build();

        responseDTO = SectionResponseDTO.builder()
                .id(1L)
                .userId(100L)
                .name("NEW SECTION")
                .planningClosed(false)
                .build();
    }

    @Test
    @DisplayName("createSection - Should map, save and return DTO")
    void testCreateSection_Success() {
        SectionCreateRequestDTO requestDTO = SectionCreateRequestDTO.builder()
                .userId(100L)
                .name("NEW SECTION")
                .build();

        when(modelMapper.map(requestDTO, Section.class)).thenReturn(section);
        when(sectionRepository.save(section)).thenReturn(Mono.just(section));
        when(modelMapper.map(section, SectionResponseDTO.class)).thenReturn(responseDTO);

        StepVerifier.create(sectionService.createSection(requestDTO))
                .assertNext(result -> {
                    assertEquals(1L, result.getId());
                    assertEquals(100L, result.getUserId());
                    assertFalse(result.isPlanningClosed());
                })
                .verifyComplete();

        verify(modelMapper).map(requestDTO, Section.class);
        verify(sectionRepository).save(section);
        verify(modelMapper).map(section, SectionResponseDTO.class);
    }

    @Test
    @DisplayName("findAllSections - Should return mapped list of sections")
    void testFindAllSections_ReturnsMappedList() {
        Section s1 = Section.builder().id(1L).userId(101L).build();
        Section s2 = Section.builder().id(2L).userId(102L).build();
        SectionResponseDTO d1 = SectionResponseDTO.builder().id(1L).userId(101L).build();
        SectionResponseDTO d2 = SectionResponseDTO.builder().id(2L).userId(102L).build();

        when(sectionRepository.findAll()).thenReturn(Flux.just(s1, s2));
        when(modelMapper.map(s1, SectionResponseDTO.class)).thenReturn(d1);
        when(modelMapper.map(s2, SectionResponseDTO.class)).thenReturn(d2);

        StepVerifier.create(sectionService.findAllSections().collectList())
                .assertNext(list -> {
                    assertEquals(2, list.size());
                    assertEquals(List.of(101L, 102L),
                            list.stream().map(SectionResponseDTO::getUserId).toList());
                })
                .verifyComplete();

        verify(sectionRepository).findAll();
        verify(modelMapper).map(s1, SectionResponseDTO.class);
        verify(modelMapper).map(s2, SectionResponseDTO.class);
    }

    @Test
    @DisplayName("findSectionById - When found returns DTO")
    void testFindSectionById_Found() {
        when(sectionRepository.findById(1L)).thenReturn(Mono.just(section));
        when(modelMapper.map(section, SectionResponseDTO.class)).thenReturn(responseDTO);

        StepVerifier.create(sectionService.findSectionById(1L))
                .assertNext(result -> assertEquals(100L, result.getUserId()))
                .verifyComplete();

        verify(sectionRepository).findById(1L);
        verify(modelMapper).map(section, SectionResponseDTO.class);
    }

    @Test
    @DisplayName("findSectionById - When not found throws SectionNotFoundException")
    void testFindSectionById_NotFound() {
        when(sectionRepository.findById(99L)).thenReturn(Mono.empty());

        StepVerifier.create(sectionService.findSectionById(99L))
                .expectError(SectionNotFoundException.class)
                .verify();

        verify(sectionRepository).findById(99L);
    }

    @Test
    @DisplayName("findSectionsByUserId - When found returns DTO")
    void testFindSectionsByUserId_Found() {
        when(sectionRepository.findByUserId(100L)).thenReturn(Mono.just(section));
        when(modelMapper.map(section, SectionResponseDTO.class)).thenReturn(responseDTO);

        StepVerifier.create(sectionService.findSectionsByUserId(100L))
                .assertNext(result -> {
                    assertEquals(1L, result.getId());
                    assertEquals(100L, result.getUserId());
                })
                .verifyComplete();

        verify(sectionRepository).findByUserId(100L);
        verify(modelMapper).map(section, SectionResponseDTO.class);
    }

    @Test
    @DisplayName("findSectionsByUserId - When not found throws SectionNotFoundException")
    void testFindSectionsByUserId_NotFound() {
        when(sectionRepository.findByUserId(100L)).thenReturn(Mono.empty());

        StepVerifier.create(sectionService.findSectionsByUserId(100L))
                .expectError(SectionNotFoundException.class)
                .verify();

        verify(sectionRepository).findByUserId(100L);
    }

    @Test
    @DisplayName("closePlanningForCurrentUser - Should update planningClosed to true")
    void testClosePlanningForCurrentUser_Success() {
        String username = "teacher@uni.edu";
        Long userId = 100L;
        Section updatedSection = Section.builder().id(1L).userId(100L).planningClosed(true).build();
        SectionResponseDTO updatedDTO = SectionResponseDTO.builder().id(1L).userId(100L).planningClosed(true).build();

        var auth = new UsernamePasswordAuthenticationToken(username, null);
        var context = new SecurityContextImpl(auth);

        when(userService.getUserIdByEmail(username)).thenReturn(Mono.just(userId));
        when(sectionRepository.findByUserId(userId)).thenReturn(Mono.just(section));
        when(sectionRepository.save(any(Section.class))).thenReturn(Mono.just(updatedSection));
        when(modelMapper.map(updatedSection, SectionResponseDTO.class)).thenReturn(updatedDTO);

        StepVerifier.create(
                sectionService.closePlanningForCurrentUser()
                        .contextWrite(ReactiveSecurityContextHolder.withSecurityContext(Mono.just(context)))
        )
                .assertNext(result -> {
                    assertTrue(result.isPlanningClosed());
                    assertEquals(100L, result.getUserId());
                })
                .verifyComplete();

        verify(userService).getUserIdByEmail(username);
        verify(sectionRepository).findByUserId(userId);
        verify(sectionRepository).save(any(Section.class));
        verify(modelMapper).map(updatedSection, SectionResponseDTO.class);
    }

    @Test
    @DisplayName("isPlanningClosedForCurrentUser - Should return true or false based on section state")
    void testIsPlanningClosedForCurrentUser_ReturnsStatus() {
        String username = "professor@uni.edu";
        Long userId = 200L;
        Section sectionClosed = Section.builder().id(2L).userId(200L).planningClosed(true).build();

        var auth = new UsernamePasswordAuthenticationToken(username, null);
        var context = new SecurityContextImpl(auth);

        when(userService.getUserIdByEmail(username)).thenReturn(Mono.just(userId));
        when(sectionRepository.findByUserId(userId)).thenReturn(Mono.just(sectionClosed));

        StepVerifier.create(
                sectionService.isPlanningClosedForCurrentUser()
                        .contextWrite(ReactiveSecurityContextHolder.withSecurityContext(Mono.just(context)))
        )
                .expectNext(true)
                .verifyComplete();

        verify(userService).getUserIdByEmail(username);
        verify(sectionRepository).findByUserId(userId);
    }

    @Test
    @DisplayName("getSectionIdByUserId - Should return section ID for given user")
    void testGetSectionIdByUserId_Success() {
        when(sectionRepository.findByUserId(100L)).thenReturn(Mono.just(section));

        StepVerifier.create(sectionService.getSectionIdByUserId(100L))
                .expectNext(1L)
                .verifyComplete();

        verify(sectionRepository).findByUserId(100L);
    }

    @Test
    @DisplayName("openPlanningForAllSections - Should reset planningClosed for all sections")
    void testOpenPlanningForAllSections_Success() {
        Section s1 = Section.builder().id(1L).planningClosed(true).build();
        Section s2 = Section.builder().id(2L).planningClosed(true).build();

        when(sectionRepository.findAll()).thenReturn(Flux.just(s1, s2));
        when(sectionRepository.save(any(Section.class)))
                .thenAnswer(inv -> Mono.just(inv.getArgument(0)));

        sectionService.openPlanningForAllSections();

        verify(sectionRepository).findAll();
        verify(sectionRepository, times(2)).save(any(Section.class));
    }

    @Test
    @DisplayName("getPlanningStatusStats - Should return correct open, closed and total counts")
    void testGetPlanningStatusStats_ReturnsCorrectCounts() {
        Section openSection1 = Section.builder().id(1L).planningClosed(false).build();
        Section openSection2 = Section.builder().id(2L).planningClosed(false).build();
        Section closedSection = Section.builder().id(3L).planningClosed(true).build();

        when(sectionRepository.findAll()).thenReturn(Flux.just(openSection1, openSection2, closedSection));

        StepVerifier.create(sectionService.getPlanningStatusStats())
                .assertNext(stats -> {
                    assertEquals(2, stats.getOpenCount());
                    assertEquals(1, stats.getClosedCount());
                    assertEquals(3, stats.getTotalCount());
                })
                .verifyComplete();

        verify(sectionRepository).findAll();
    }

    @Test
    @DisplayName("getPlanningStatusStats - Should return zero counts when no sections exist")
    void testGetPlanningStatusStats_EmptySections() {
        when(sectionRepository.findAll()).thenReturn(Flux.empty());

        StepVerifier.create(sectionService.getPlanningStatusStats())
                .assertNext(stats -> {
                    assertEquals(0, stats.getOpenCount());
                    assertEquals(0, stats.getClosedCount());
                    assertEquals(0, stats.getTotalCount());
                })
                .verifyComplete();

        verify(sectionRepository).findAll();
    }

    @Test
    @DisplayName("getPlanningStatusStats - Should return all closed when all sections are closed")
    void testGetPlanningStatusStats_AllClosed() {
        Section closed1 = Section.builder().id(1L).planningClosed(true).build();
        Section closed2 = Section.builder().id(2L).planningClosed(true).build();

        when(sectionRepository.findAll()).thenReturn(Flux.just(closed1, closed2));

        StepVerifier.create(sectionService.getPlanningStatusStats())
                .assertNext(stats -> {
                    assertEquals(0, stats.getOpenCount());
                    assertEquals(2, stats.getClosedCount());
                    assertEquals(2, stats.getTotalCount());
                })
                .verifyComplete();

        verify(sectionRepository).findAll();
    }

    @Test
    @DisplayName("getPlanningStatusStats - Should return all open when all sections are open")
    void testGetPlanningStatusStats_AllOpen() {
        Section open1 = Section.builder().id(1L).planningClosed(false).build();
        Section open2 = Section.builder().id(2L).planningClosed(false).build();

        when(sectionRepository.findAll()).thenReturn(Flux.just(open1, open2));

        StepVerifier.create(sectionService.getPlanningStatusStats())
                .assertNext(stats -> {
                    assertEquals(2, stats.getOpenCount());
                    assertEquals(0, stats.getClosedCount());
                    assertEquals(2, stats.getTotalCount());
                })
                .verifyComplete();

        verify(sectionRepository).findAll();
    }
}
