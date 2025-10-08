package co.edu.puj.secchub_backend.security.service;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import co.edu.puj.secchub_backend.security.contract.SecurityModuleUserContract;
import co.edu.puj.secchub_backend.security.exception.UserNotFoundException;
import co.edu.puj.secchub_backend.security.model.User;
import co.edu.puj.secchub_backend.security.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserService implements SecurityModuleUserContract {
    private final UserRepository userRepository;

    @Override
    @Cacheable("user-id-by-email")
    public Long getUserIdByEmail(String email) {
        log.debug("Getting user id by email: {}", email);
        return userRepository.findByEmail(email)
                .map(User::getId)
                .orElseThrow(() -> new UserNotFoundException("User not found for email: " + email));
    }
}
