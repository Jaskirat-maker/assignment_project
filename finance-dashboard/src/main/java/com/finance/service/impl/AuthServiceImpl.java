package com.finance.service.impl;

import com.finance.dto.request.LoginRequest;
import com.finance.dto.request.RegisterRequest;
import com.finance.dto.response.JwtResponse;
import com.finance.entity.RefreshToken;
import com.finance.entity.User;
import com.finance.entity.enums.Role;
import com.finance.exception.AuthenticationFailureException;
import com.finance.exception.BadRequestException;
import com.finance.exception.ConflictException;
import com.finance.repository.UserRepository;
import com.finance.security.JwtTokenProvider;
import com.finance.service.AuthService;
import com.finance.service.RefreshTokenService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
    private final RefreshTokenService refreshTokenService;

    @Override
    @Transactional
    public JwtResponse register(RegisterRequest request) {
        log.info("Registering user: {}", request.getUsername());
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new ConflictException("Username '" + request.getUsername() + "' is already taken");
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new ConflictException("Email '" + request.getEmail() + "' is already in use");
        }

        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(Role.ANALYST)
                .isActive(true)
                .build();

        userRepository.save(user);

        String token = jwtTokenProvider.generateToken(user.getUsername());
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(user.getId());

        return JwtResponse.builder()
                .token(token)
                .username(user.getUsername())
                .email(user.getEmail())
                .primaryRole(user.getRole().name())
                .refreshToken(refreshToken.getToken())
                .build();
    }

    @Override
    public JwtResponse login(LoginRequest request) {
        Authentication authentication;
        try {
            authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
            );
        } catch (BadCredentialsException exception) {
            throw new AuthenticationFailureException("Invalid username or password");
        }

        SecurityContextHolder.getContext().setAuthentication(authentication);

        String token = jwtTokenProvider.generateToken(request.getUsername());

        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new AuthenticationFailureException("User account not found"));

        RefreshToken refreshToken = refreshTokenService.createRefreshToken(user.getId());

        return JwtResponse.builder()
                .token(token)
                .username(user.getUsername())
                .email(user.getEmail())
                .primaryRole(user.getRole().name())
                .refreshToken(refreshToken.getToken())
                .build();
    }

    @Override
    public JwtResponse refreshToken(String refreshTokenInput) {
        RefreshToken refreshToken = refreshTokenService.verifyExpiration(
                refreshTokenService.findByToken(refreshTokenInput)
                        .orElseThrow(() -> new BadRequestException("Refresh token not found"))
        );

        User user = refreshToken.getUser();
        String accessToken = jwtTokenProvider.generateToken(user.getUsername());
        log.info("Refreshing token for user: {}", user.getUsername());
        RefreshToken newRefreshToken = refreshTokenService.createRefreshToken(user.getId());

        return JwtResponse.builder()
                .token(accessToken)
                .refreshToken(newRefreshToken.getToken())
                .username(user.getUsername())
                .email(user.getEmail())
                .primaryRole(user.getRole().name())
                .build();
    }

    @Override
    @Transactional
    public void logout(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new BadRequestException("User account not found"));
        refreshTokenService.deleteByUserId(user.getId());
    }

}