package co.edu.puj.secchub_backend.security.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.modelmapper.ModelMapper;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContext;

import co.edu.puj.secchub_backend.security.contract.UserCreationRequestDTO;
import co.edu.puj.secchub_backend.security.contract.UserInformationResponseDTO;
import co.edu.puj.secchub_backend.security.exception.UserNotFoundException;
import co.edu.puj.secchub_backend.security.model.User;
import co.edu.puj.secchub_backend.security.repository.UserRepository;
import reactor.core.publisher.Mono;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserService Unit Test")
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private ModelMapper modelMapper;

    @Mock
    private PasswordEncoderService passwordEncoderService;

    @InjectMocks
    private UserService userService;

    @Test
    @DisplayName("getUserIdByEmail - When user exists returns id")
    void testGetUserIdByEmail_UserExists_ReturnsId() {
        User user = User.builder().id(42L).email("a@b.com").build();
        when(userRepository.findByEmail("a@b.com")).thenReturn(Optional.of(user));

        Long id = userService.getUserIdByEmail("a@b.com");

        assertEquals(42L, id);
        verify(userRepository).findByEmail("a@b.com");
    }

    @Test
    @DisplayName("getUserIdByEmail - When user not found throws UserNotFoundException")
    void testGetUserIdByEmail_UserNotFound_Throws() {
        when(userRepository.findByEmail("missing@x.com")).thenReturn(Optional.empty());
        assertThrows(UserNotFoundException.class, () -> userService.getUserIdByEmail("missing@x.com"));
        verify(userRepository).findByEmail("missing@x.com");
    }

    @Test
    @DisplayName("createUser - Maps, encodes password and saves returning id")
    void testCreateUser_MapsAndSaves_ReturnsId() {
        UserCreationRequestDTO req = new UserCreationRequestDTO();
        // modelMapper will be stubbed to return a User instance
        User mapped = User.builder().email("u@d.com").password("plain").build();
        User saved = User.builder().id(7L).email("u@d.com").password("encoded").build();

        when(modelMapper.map(req, User.class)).thenReturn(mapped);
        when(passwordEncoderService.encode("plain")).thenReturn("encoded");
        when(userRepository.save(mapped)).thenReturn(saved);

        Long resultId = userService.createUser(req);

        assertEquals(7L, resultId);
        verify(modelMapper).map(req, User.class);
        verify(passwordEncoderService).encode("plain");
        verify(userRepository).save(mapped);
    }

    @Test
    @DisplayName("getAllUsersInformation - Should return mapped list")
    void testGetAllUsersInformation_ReturnsMappedList() {
        User u1 = User.builder().id(1L).email("a@a.com").build();
        User u2 = User.builder().id(2L).email("b@b.com").build();
        List<User> users = Arrays.asList(u1, u2);

        UserInformationResponseDTO dto1 = new UserInformationResponseDTO();
        UserInformationResponseDTO dto2 = new UserInformationResponseDTO();

        when(userRepository.findAll()).thenReturn(users);
        when(modelMapper.map(u1, UserInformationResponseDTO.class)).thenReturn(dto1);
        when(modelMapper.map(u2, UserInformationResponseDTO.class)).thenReturn(dto2);

        List<UserInformationResponseDTO> result = userService.getAllUsersInformation().collectList().block();

        assertNotNull(result);
        assertEquals(2, result.size());
        verify(userRepository).findAll();
        verify(modelMapper).map(u1, UserInformationResponseDTO.class);
        verify(modelMapper).map(u2, UserInformationResponseDTO.class);
    }

    @Test
    @DisplayName("getUserInformationById - When user found returns DTO")
    void testGetUserInformationById_UserFound_ReturnsDTO() {
        User user = User.builder().id(10L).email("x@y.com").build();
        UserInformationResponseDTO dto = new UserInformationResponseDTO();

        when(userRepository.findById(10L)).thenReturn(Optional.of(user));
        when(modelMapper.map(user, UserInformationResponseDTO.class)).thenReturn(dto);

        UserInformationResponseDTO result = userService.getUserInformationById(10L).block();

        assertNotNull(result);
        verify(userRepository).findById(10L);
        verify(modelMapper).map(user, UserInformationResponseDTO.class);
    }

    @Test
    @DisplayName("getUserInformationById - When user not found throws UserNotFoundException")
    void testGetUserInformationById_UserNotFound_Throws() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(UserNotFoundException.class, () -> userService.getUserInformationById(99L));
        verify(userRepository).findById(99L);
    }

    @Test
    @DisplayName("getUserInformationByEmail - Should build contract DTO from user")
    void testGetUserInformationByEmail_ReturnsContractDTO() {
        User user = User.builder()
                .id(5L)
                .username("u")
                .faculty("F")
                .name("N")
                .lastName("L")
                .email("u@p.com")
                .statusId(1L)
                .roleId(2L)
                .documentTypeId(3L)
                .documentNumber("123")
                .build();

        when(userRepository.findByEmail("u@p.com")).thenReturn(Optional.of(user));

        UserInformationResponseDTO contractDTO =
                userService.getUserInformationByEmail("u@p.com").block();

        assertNotNull(contractDTO);
        assertEquals(5L, contractDTO.getId());
        assertEquals("u@p.com", contractDTO.getEmail());
        assertEquals("u", contractDTO.getUsername());
        verify(userRepository).findByEmail("u@p.com");
    }

    @Test
    @DisplayName("getUserInformationByEmailInternal - Should map using ModelMapper")
    void testGetUserInformationByEmailInternal_ReturnsDTO() {
        User user = User.builder().email("i@e.com").build();
        UserInformationResponseDTO dto = new UserInformationResponseDTO();

        when(userRepository.findByEmail("i@e.com")).thenReturn(Optional.of(user));
        when(modelMapper.map(user, UserInformationResponseDTO.class)).thenReturn(dto);

        UserInformationResponseDTO result = userService.getUserInformationByEmailInternal("i@e.com").block();

        assertNotNull(result);
        verify(userRepository).findByEmail("i@e.com");
        verify(modelMapper).map(user, UserInformationResponseDTO.class);
    }

    @Test
    @DisplayName("getUserInformation - Uses ReactiveSecurityContextHolder to resolve current user")
    void testGetUserInformation_UsesSecurityContext_ReturnsDTO() {
        SecurityContext securityContext = mock(SecurityContext.class);
        Authentication auth = mock(Authentication.class);
        when(auth.getName()).thenReturn("me@x.com");
        when(securityContext.getAuthentication()).thenReturn(auth);

        User user = User.builder().email("me@x.com").build();
        UserInformationResponseDTO dto = new UserInformationResponseDTO();

        when(userRepository.findByEmail("me@x.com")).thenReturn(Optional.of(user));
        when(modelMapper.map(user, UserInformationResponseDTO.class)).thenReturn(dto);

        try (MockedStatic<ReactiveSecurityContextHolder> mocked = mockStatic(ReactiveSecurityContextHolder.class)) {
            mocked.when(ReactiveSecurityContextHolder::getContext).thenReturn(Mono.just(securityContext));

            UserInformationResponseDTO result = userService.getUserInformation().block();

            assertNotNull(result);
            verify(userRepository).findByEmail("me@x.com");
            verify(modelMapper).map(user, UserInformationResponseDTO.class);
        }
    }
}


