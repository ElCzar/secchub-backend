package co.edu.puj.secchub_backend.security.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import co.edu.puj.secchub_backend.security.dto.AuthLoginDTO;
import co.edu.puj.secchub_backend.security.dto.AuthTokenDTO;
import co.edu.puj.secchub_backend.security.service.AuthenticationService;
import reactor.core.publisher.Mono;

import java.util.Map;

@RestController
@RequestMapping("/auth")
public class AuthenticationController {

	private final AuthenticationService authenticationService;

	public AuthenticationController(AuthenticationService authenticationService) {
		this.authenticationService = authenticationService;
	}

	/**
	 * Login endpoint for user authentication.
	 * @param authLoginDTO the login credentials
	 * @return a Mono containing the authentication token
	 */
	@PostMapping("/login")
	@PreAuthorize("permitAll()")
	public Mono<ResponseEntity<AuthTokenDTO>> login(@RequestBody AuthLoginDTO authLoginDTO) {
		return Mono.fromCallable(() -> authenticationService.authenticate(authLoginDTO.getEmail(), authLoginDTO.getPassword()))
				.map(ResponseEntity::ok)
				.onErrorReturn(ResponseEntity.status(HttpStatus.UNAUTHORIZED)
						.body(new AuthTokenDTO("Login failed", authLoginDTO.getEmail(), null)));
	}

	@GetMapping("/example")
	@PreAuthorize("hasRole('ROLE_ADMIN')")
	public Mono<ResponseEntity<Map<String, Object>>> example() {
		return Mono.just(ResponseEntity.ok(Map.of("message", "This is an example endpoint.")));
	}
}
