package co.edu.puj.secchub_backend.security.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import co.edu.puj.secchub_backend.security.dto.AuthTokenResponseDTO;
import co.edu.puj.secchub_backend.security.dto.LoginRequestDTO;
import co.edu.puj.secchub_backend.security.dto.RefreshTokenRequestDTO;
import co.edu.puj.secchub_backend.security.service.AuthenticationService;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

/**
 * REST controller for authentication operations.
 * Provides endpoints for user login and token refresh.
 */
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthenticationController {

	private final AuthenticationService authenticationService;

	/**
	 * Login endpoint for user authentication.
	 * @param authLoginDTO the login credentials
	 * @apiNote This endpoint allows users to log in and obtain an authentication token without requiring additional verification steps.
	 * @return a Mono containing the authentication token
	 */
	@PostMapping("/login")
	@PreAuthorize("permitAll()")
	public Mono<ResponseEntity<AuthTokenResponseDTO>> login(@RequestBody LoginRequestDTO loginRequestDTO) {
		return Mono.fromCallable(() -> authenticationService.authenticate(loginRequestDTO.getEmail(), loginRequestDTO.getPassword()))
				.map(ResponseEntity::ok);
	}

	/**
	 * Refresh token endpoint.
	 * @param refreshToken the refresh token
	 * @apiNote This endpoint allows users to obtain a new authentication token using a valid refresh token without requiring the user to log in again.
	 * @return a Mono containing the new authentication token
	 */
	@PostMapping("/refresh")
	@PreAuthorize("permitAll()")
	public Mono<ResponseEntity<AuthTokenResponseDTO>> refresh(@RequestBody RefreshTokenRequestDTO refreshToken) {
		return Mono.fromCallable(() -> authenticationService.refreshToken(refreshToken))
				.map(ResponseEntity::ok);
	}
}
