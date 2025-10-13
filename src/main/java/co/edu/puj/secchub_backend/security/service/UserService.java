package co.edu.puj.secchub_backend.security.service;

import org.modelmapper.ModelMapper;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.stereotype.Service;

import co.edu.puj.secchub_backend.security.contract.SecurityModuleUserContract;
import co.edu.puj.secchub_backend.security.contract.UserCreationRequestDTO;
import co.edu.puj.secchub_backend.security.dto.UserInformationResponseDTO;
import co.edu.puj.secchub_backend.security.exception.UserNotFoundException;
import co.edu.puj.secchub_backend.security.model.User;
import co.edu.puj.secchub_backend.security.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserService implements SecurityModuleUserContract {
    
    private final UserRepository userRepository;
    private final ModelMapper modelMapper;
    private final PasswordEncoderService passwordEncoderService;

    @Override
    @Cacheable("user-id-by-email")
    public Long getUserIdByEmail(String email) {
        log.debug("Getting user id by email: {}", email);
        return userRepository.findByEmail(email)
                .map(User::getId)
                .orElseThrow(() -> new UserNotFoundException("User not found for email: " + email));
    }

    @Override
    public Long createUser(UserCreationRequestDTO userCreationRequestDTO) {
        log.debug("Creating user with email: {}", userCreationRequestDTO.getEmail());
        User user = modelMapper.map(userCreationRequestDTO, User.class);
        user.setPassword(passwordEncoderService.encode(user.getPassword()));
        User savedUser = userRepository.save(user);
        return savedUser.getId();
    }

    /**
     * Gets the userInformation by userId
     * @param userId the user ID
     * @return UserInformationResponseDTO with user details
     */
    public Mono<UserInformationResponseDTO> getUserInformationById(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found for id: " + userId));
        return Mono.just(modelMapper.map(user, UserInformationResponseDTO.class));
    }

    /**
     * Gets the userInformation by email
     * @param email the user email
     * @return UserInformationResponseDTO with user details
     */
    public Mono<UserInformationResponseDTO> getUserInformationByEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User not found for email: " + email));
        return Mono.just(modelMapper.map(user, UserInformationResponseDTO.class));
    }

    /**
     * Gets the logged-in user information
     * @return UserInformationResponseDTO with user details
     */
    public Mono<UserInformationResponseDTO> getUserInformation() {
        return ReactiveSecurityContextHolder.getContext()
                .flatMap(securityContext -> Mono.fromCallable(() ->{
                    String authenticatedUserId = securityContext.getAuthentication().getName();
                    User user = userRepository.findByEmail(authenticatedUserId)
                            .orElseThrow(() -> new UserNotFoundException("User not found for email: " + authenticatedUserId));
                    return modelMapper.map(user, UserInformationResponseDTO.class);
                }).subscribeOn(Schedulers.boundedElastic()));
    }
}
