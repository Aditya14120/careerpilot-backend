package com.careerpilot.careerpilot.auth.controller;

import com.careerpilot.careerpilot.auth.dto.LoginRequest;
import com.careerpilot.careerpilot.auth.dto.LoginResponse;
import com.careerpilot.careerpilot.auth.dto.RegisterRequest;
import com.careerpilot.careerpilot.auth.dto.RegisterResponse;
import com.careerpilot.careerpilot.auth.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public RegisterResponse register(@Valid @RequestBody RegisterRequest request) {
        return authService.register(request);
    }

    @PostMapping("/login")
    public LoginResponse login(@Valid @RequestBody LoginRequest request) {
        return authService.login(request);
    }
}
