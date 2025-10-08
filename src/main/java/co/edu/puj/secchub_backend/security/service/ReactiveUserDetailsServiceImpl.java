package co.edu.puj.secchub_backend.security.service;

import lombok.RequiredArgsConstructor;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import co.edu.puj.secchub_backend.parametric.contracts.ParametricContract;
import co.edu.puj.secchub_backend.security.model.User;
import co.edu.puj.secchub_backend.security.repository.UserRepository;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.Collections;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ReactiveUserDetailsServiceImpl implements ReactiveUserDetailsService {

    private final UserRepository userRepository;
    private final ParametricContract parametricService;

    /**
     * Finds a user by username and builds UserDetails for authentication.
     * @param username the username to search for
     * @return Mono emitting UserDetails if found and active, empty otherwise
     */
    @Override
    public Mono<UserDetails> findByUsername(String username) {
        return Mono.fromCallable(() -> userRepository.findByUsername(username))
                .subscribeOn(Schedulers.boundedElastic())
                .filter(Optional::isPresent)
                .map(Optional::get)
                .filter(user -> user.getStatusId() != null && "Active".equals(parametricService.getStatusNameById(user.getStatusId())))
                .map(this::buildUserDetails);
    }

    /**
     * Builds UserDetails from a User entity.
     * @param user the User entity
     * @return UserDetails object
     */
    private UserDetails buildUserDetails(User user) {
        String roleName = user.getRoleId() != null ? parametricService.getRoleNameById(user.getRoleId()) : "ROLE_USER";

        return org.springframework.security.core.userdetails.User.builder()
                .username(user.getUsername())
                .password(user.getPassword())
                .authorities(Collections.singletonList(new SimpleGrantedAuthority(roleName)))
                .accountExpired(false)
                .accountLocked(false)
                .credentialsExpired(false)
                .disabled(false)
                .build();
    }

    /**
     * Finds a user by email and builds an Authentication token.
     * @param email the email to search for
     * @return Mono emitting Authentication token if found and active, empty otherwise
     */
    public Mono<Authentication> findByEmail(String email) {
        return Mono.fromCallable(() -> userRepository.findByEmail(email))
                .subscribeOn(Schedulers.boundedElastic())
                .filter(Optional::isPresent)
                .map(Optional::get)
                .filter(user -> user.getStatusId() != null && "Active".equals(parametricService.getStatusNameById(user.getStatusId())))
                .map(this::buildUserDetails)
                .map(userDetails -> new UsernamePasswordAuthenticationToken(
                        userDetails.getUsername(),
                        userDetails.getPassword(),
                        userDetails.getAuthorities()
                ));
    }
}