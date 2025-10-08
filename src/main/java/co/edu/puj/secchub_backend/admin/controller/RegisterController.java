package co.edu.puj.secchub_backend.admin.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

/**
 * REST controller in charge of user registration.
 * This includes:
 * - Register students
 * - Register teachers
 * - Register admins
 * - Register sections
 * - Register programs
 */
@RestController
@RequestMapping("/admin/register")
@RequiredArgsConstructor
public class RegisterController {
    
}
