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

import java.util.Collections;

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
        return userRepository.findByUsername(username)
                .filterWhen(user -> {
                    if (user.getStatusId() == null) {
                        return Mono.just(false);
                    }
                    return parametricService.getStatusNameById(user.getStatusId())
                            .map("Active"::equals)
                            .defaultIfEmpty(false);
                })
                .flatMap(this::buildUserDetailsReactive);
    }

    /**
     * Builds UserDetails from a User entity reactively.
     * @param user the User entity
     * @return Mono emitting UserDetails object
     */
    private Mono<UserDetails> buildUserDetailsReactive(User user) {
        Mono<String> roleNameMono = user.getRoleId() != null 
                ? parametricService.getRoleNameById(user.getRoleId())
                : Mono.just("ROLE_USER");

        return roleNameMono.map(roleName -> 
                org.springframework.security.core.userdetails.User.builder()
                        .username(user.getUsername())
                        .password(user.getPassword())
                        .authorities(Collections.singletonList(new SimpleGrantedAuthority(roleName)))
                        .accountExpired(false)
                        .accountLocked(false)
                        .credentialsExpired(false)
                        .disabled(false)
                        .build()
        );
    }

    /**
     * Finds a user by email and builds an Authentication token.
     * @param email the email to search for
     * @return Mono emitting Authentication token if found and active, empty otherwise
     */
    public Mono<Authentication> findByEmail(String email) {
        return userRepository.findByEmail(email)
                .filterWhen(user -> {
                    if (user.getStatusId() == null) {
                        return Mono.just(false);
                    }
                    return parametricService.getStatusNameById(user.getStatusId())
                            .map("Active"::equals)
                            .defaultIfEmpty(false);
                })
                .flatMap(user -> 
                    buildUserDetailsReactive(user)
                            .map(userDetails -> new UsernamePasswordAuthenticationToken(
                                    user.getEmail(),
                                    userDetails.getPassword(),
                                    userDetails.getAuthorities()
                            ))
                );
    }
}