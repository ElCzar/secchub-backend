package co.edu.puj.secchub_backend.security.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import co.edu.puj.secchub_backend.security.jwt.JwtTokenProvider;
import reactor.core.publisher.Mono;

import java.util.Map;

@RestController
@RequestMapping("/auth")
public class AuthenticationController {

	private final JwtTokenProvider jwtTokenProvider;

	public AuthenticationController(JwtTokenProvider jwtTokenProvider) {
		this.jwtTokenProvider = jwtTokenProvider;
	}

	@PostMapping("/login")
	@PreAuthorize("permitAll()")
	public Mono<ResponseEntity<Map<String, Object>>> login(@RequestBody Map<String, String> credentials) {
		String email = credentials.get("email");
		String password = credentials.get("password");
		System.out.println("Attempting login for user: " + email);

		// Validate credentials (this is just a placeholder, implement your own logic)
		if ("u".equals(email) && "p".equals(password)) {
			String token = jwtTokenProvider.generateToken(email, "ROLE_ADMIN");
			return Mono.just(ResponseEntity.ok(Map.of("message", "Login successful", "token", token)));
		} else {
			return Mono.just(ResponseEntity.status(HttpStatus.UNAUTHORIZED)
					.body(Map.of("message", "Invalid credentials")));
		}
	}

	@GetMapping("/example")
	@PreAuthorize("hasRole('ROLE_ADMIN')")
	public Mono<ResponseEntity<Map<String, Object>>> example() {
		return Mono.just(ResponseEntity.ok(Map.of("message", "This is an example endpoint.")));
	}
}
