package co.edu.puj.secchub_backend.admin_resources.controller;

import co.edu.puj.secchub_backend.admin_resources.model.AdminUser;
import co.edu.puj.secchub_backend.admin_resources.repository.AdminUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/users")
@RequiredArgsConstructor
public class UserController {
    private final AdminUserRepository userRepository;

    @GetMapping
    public List<AdminUser> getAllUsers() {
        return userRepository.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<AdminUser> getUserById(@PathVariable Long id) {
        return userRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public AdminUser createUser(@RequestBody AdminUser user) {
        return userRepository.save(user);
    }

    @PutMapping("/{id}")
    public ResponseEntity<AdminUser> updateUser(@PathVariable Long id, @RequestBody AdminUser user) {
        return userRepository.findById(id)
                .map(existing -> {
                    user.setId(id);
                    return ResponseEntity.ok(userRepository.save(user));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        if (userRepository.existsById(id)) {
            userRepository.deleteById(id);
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }
}
