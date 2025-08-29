package co.edu.puj.secchub_backend.security.service;

import lombok.RequiredArgsConstructor;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import co.edu.puj.secchub_backend.security.domain.User;
import co.edu.puj.secchub_backend.security.repository.UserRepository;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.Collections;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ReactiveUserDetailsServiceImpl implements ReactiveUserDetailsService {

    private final UserRepository userRepository;

    @Override
    public Mono<UserDetails> findByUsername(String username) {
        return Mono.fromCallable(() -> userRepository.findByUsername(username))
                .subscribeOn(Schedulers.boundedElastic())
                .filter(Optional::isPresent)
                .map(Optional::get)
                .filter(user -> user.getStatus() != null && "Active".equals(user.getStatus().getName()))
                .map(this::buildUserDetails);
    }

    private UserDetails buildUserDetails(User user) {
        String roleName = user.getRole() != null ? user.getRole().getName() : "ROLE_USER";

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

    public Mono<Authentication> findByEmail(String email) {
        return Mono.fromCallable(() -> userRepository.findByEmail(email))
                .subscribeOn(Schedulers.boundedElastic())
                .filter(Optional::isPresent)
                .map(Optional::get)
                .filter(user -> user.getStatus() != null && "Active".equals(user.getStatus().getName()))
                .map(this::buildUserDetails)
                .map(userDetails -> new UsernamePasswordAuthenticationToken(
                        userDetails.getUsername(),
                        userDetails.getPassword(),
                        userDetails.getAuthorities()
                ));
    }
}