package co.edu.puj.secchub_backend.admin.service;

import org.springframework.stereotype.Service;

import co.edu.puj.secchub_backend.security.contract.SecurityModuleUserContract;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RegisterService {
    private final SecurityModuleUserContract userService;
}
