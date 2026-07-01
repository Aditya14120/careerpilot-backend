package com.careerpilot.careerpilot.auth.service.impl;

import com.careerpilot.careerpilot.auth.dto.LoginRequest;
import com.careerpilot.careerpilot.auth.dto.LoginResponse;
import com.careerpilot.careerpilot.auth.dto.RegisterRequest;
import com.careerpilot.careerpilot.auth.dto.RegisterResponse;
import com.careerpilot.careerpilot.auth.entity.Role;
import com.careerpilot.careerpilot.auth.entity.User;
import com.careerpilot.careerpilot.auth.repository.UserRepository;
import com.careerpilot.careerpilot.auth.service.AuthService;
import com.careerpilot.careerpilot.exception.EmailAlreadyExistsException;
import com.careerpilot.careerpilot.security.jwt.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    @Override
    public RegisterResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new EmailAlreadyExistsException(request.getEmail());
        }

        User user = User.builder()
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .college(request.getCollege())
                .branch(request.getBranch())
                .graduationYear(request.getGraduationYear())
                .role(Role.USER)
                .build();

        User savedUser = userRepository.save(user);

        return RegisterResponse.builder()
                .id(savedUser.getId())
                .firstName(savedUser.getFirstName())
                .lastName(savedUser.getLastName())
                .email(savedUser.getEmail())
                .message("User registered successfully")
                .build();
    }
    
    @Override
    public LoginResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));

        String token = jwtService.generateToken(user);

        return LoginResponse.builder()
                .token(token)
                .message("Login successful")
                .build();
    }

}