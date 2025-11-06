package co.edu.puj.secchub_backend.security.service;

import org.modelmapper.ModelMapper;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.stereotype.Service;

import co.edu.puj.secchub_backend.security.contract.SecurityModuleUserContract;
import co.edu.puj.secchub_backend.security.contract.UserCreationRequestDTO;
import co.edu.puj.secchub_backend.security.contract.UserInformationResponseDTO;
import co.edu.puj.secchub_backend.security.exception.UserNotFoundException;
import co.edu.puj.secchub_backend.security.model.User;
import co.edu.puj.secchub_backend.security.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
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
    public Mono<Long> getUserIdByEmail(String email) {
        log.debug("Getting user id by email: {}", email);

        return userRepository.findByEmail(email)
                .map(User::getId)
                .switchIfEmpty(Mono.error(new UserNotFoundException("User id not found for email: " + email)));
    }

    @Override
    public Mono<Long> createUser(UserCreationRequestDTO userCreationRequestDTO) {
        log.debug("Creating user with email: {}", userCreationRequestDTO.getEmail());

        return Mono.fromCallable(() -> {
            User user = modelMapper.map(userCreationRequestDTO, User.class);
            user.setPassword(passwordEncoderService.encode(user.getPassword()));
            return user;
        }).subscribeOn(Schedulers.boundedElastic())
        .flatMap(userRepository::save)
        .map(User::getId);
    }

    /**
     * Gets all users information
     * @return List<UserInformationResponseDTO> with list of users details
     */
    public Flux<UserInformationResponseDTO> getAllUsersInformation() {
        log.debug("Getting all users information");

        return userRepository.findAll()
                .map(user -> modelMapper.map(user, UserInformationResponseDTO.class));
    }

    /**
     * Gets the userInformation by userId
     * @param userId the user ID
     * @return UserInformationResponseDTO with user details
     */
    public Mono<UserInformationResponseDTO> getUserInformationById(Long userId) {
        log.debug("Getting user information by userId: {}", userId);

        return userRepository.findById(userId)
                .switchIfEmpty(Mono.error(new UserNotFoundException("User information not found for userId: " + userId)))
                .map(user -> modelMapper.map(user, UserInformationResponseDTO.class));
    }

    @Override
    public Mono<UserInformationResponseDTO> getUserInformationByEmail(String email) {
        log.debug("Getting user information by email: {}", email);
        
        return userRepository.findByEmail(email)
                .switchIfEmpty(Mono.error(new UserNotFoundException("User information not found for email: " + email)))
                .map(user -> modelMapper.map(user, UserInformationResponseDTO.class));
    }

    /**
     * Gets the userInformation by email (external version)
     * @param email the user email
     * @return UserInformationResponseDTO with user details
     */
    public Mono<UserInformationResponseDTO> getUserInformationByEmailExternal(String email) {
        log.debug("Getting user information by email (external): {}", email);

        return userRepository.findByEmail(email)
                .switchIfEmpty(Mono.error(new UserNotFoundException("User information externally not found for email: " + email)))
                .map(user -> modelMapper.map(user, UserInformationResponseDTO.class));
    }

    /**
     * Gets the logged-in user information
     * @return UserInformationResponseDTO with user details
     */
    public Mono<UserInformationResponseDTO> getUserInformation() {
        log.debug("Getting logged-in user information");
        
        return ReactiveSecurityContextHolder.getContext()
                .flatMap(securityContext -> {
                    String authenticatedUserEmail = securityContext.getAuthentication().getName();
                    return userRepository.findByEmail(authenticatedUserEmail)
                            .switchIfEmpty(Mono.error(
                                    new UserNotFoundException("User information not found for logged user with email: " + authenticatedUserEmail)))
                            .map(user -> modelMapper.map(user, UserInformationResponseDTO.class));
                });
    }
}
