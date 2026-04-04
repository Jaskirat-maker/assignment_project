package com.finance.controller;

import com.finance.dto.request.LoginRequest;
import com.finance.dto.request.RefreshTokenRequest;
import com.finance.dto.request.RegisterRequest;
import com.finance.dto.response.JwtResponse;
import com.finance.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<JwtResponse> register(@Valid @RequestBody RegisterRequest request) {
        log.info("Register request for user: {}", request.getUsername());
        JwtResponse response = authService.register(request);
        log.debug("Register response generated for user: {}", request.getUsername());
        return ResponseEntity.status(201).body(response);
    }

    @PostMapping("/login")
    public ResponseEntity<JwtResponse> login(@Valid @RequestBody LoginRequest request) {
        log.info("Login attempt for user: {}", request.getUsername());
        JwtResponse response = authService.login(request);
        log.debug("Login successful for user: {}", request.getUsername());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/refresh")
    public ResponseEntity<JwtResponse> refreshToken(@Valid @RequestBody RefreshTokenRequest request) {
        log.info("Refresh token request received");
        JwtResponse response = authService.refreshToken(request.getRefreshToken());
        log.debug("Refresh token rotated for user: {}", response.getUsername());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/logout")
    public ResponseEntity<String> logout(Authentication authentication) {
        String username = authentication.getName();
        log.info("Logout request for user: {}", username);
        authService.logout(username);
        return ResponseEntity.ok("Logged out successfully");
    }

}