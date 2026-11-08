package com.todobackend.service.impl;

import com.todobackend.dto.request.LoginRequest;
import com.todobackend.dto.request.RefreshTokenRequest;
import com.todobackend.dto.request.RegisterRequest;
import com.todobackend.dto.response.AuthResponse;
import com.todobackend.dto.response.UserResponse;
import com.todobackend.entity.RefreshTokenEntity;
import com.todobackend.entity.UserEntity;
import com.todobackend.exception.ConflictException;
import com.todobackend.exception.InvalidTokenException;
import com.todobackend.exception.NotFoundException;
import com.todobackend.mapper.UserMapper;
import com.todobackend.service.AuthService;
import com.todobackend.repository.RefreshTokenRepository;
import com.todobackend.repository.UserRepository;
import com.todobackend.config.properties.JwtProperties;
import com.todobackend.security.jwt.JwtService;
import com.todobackend.security.principal.UserPrincipal;
import com.todobackend.security.util.TokenHashUtil;
import com.todobackend.service.RefreshTokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenGenerator;
    private final TokenHashUtil tokenHashUtil;
    private final JwtProperties jwtProperties;
    private final UserMapper userMapper;

    @Transactional
    @Override
    public AuthResponse register(RegisterRequest request) {

        UserEntity user = userMapper.toEntity(request);
        String normalizedEmail = normalizeEmail(request.email());

        if (userRepository.existsByEmailIgnoreCaseAndIsDeletedFalse(normalizedEmail)) {
            throw new ConflictException(
                    "Email is already in use"
            );
        }

        user.setEmail(normalizedEmail);
        user.setPasswordHash(passwordEncoder.encode(request.password()));

        UserEntity savedUser = userRepository.save(user);

        UserPrincipal principal = UserPrincipal.from(user);

        return createAuthResponse(principal, user);
    }

    @Override
    @Transactional
    public AuthResponse login(LoginRequest request) {

        String email = normalizeEmail(request.email());

        Authentication authentication =
                authenticationManager.authenticate(
                        new UsernamePasswordAuthenticationToken(
                                email,
                                request.password()
                        )
                );

        UserPrincipal principal =
                (UserPrincipal) authentication.getPrincipal();

        UserEntity user = userRepository.findById(principal.getId())
                .orElseThrow(() ->
                        new NotFoundException(
                                "User not found"
                        )
                );

        return createAuthResponse(principal, user);
    }

    @Override
    public UserResponse me(UserPrincipal userPrincipal) {

        UserEntity user = userRepository.findById(userPrincipal.getId())
                .orElseThrow(() -> new NotFoundException("User not found with id: " + userPrincipal.getId()));

        return userMapper.toResponse(user);

    }

    @Override
    @Transactional
    public AuthResponse refresh(RefreshTokenRequest request) {

        String tokenHash =
                tokenHashUtil.hash(request.refreshToken());

        RefreshTokenEntity currentToken =
                refreshTokenRepository.findByTokenHash(tokenHash)
                        .orElseThrow(() ->
                                new InvalidTokenException(
                                        "Invalid refresh token"
                                )
                        );

        if (!currentToken.isUsable()) {
            throw new InvalidTokenException(
                    "Refresh token is expired or revoked"
            );
        }

        UserEntity user = currentToken.getUser();

        currentToken.revoke();

        UserPrincipal principal =
                UserPrincipal.from(user);

        return createAuthResponse(principal, user);
    }

    @Override
    @Transactional
    public void logout(RefreshTokenRequest request) {

        String tokenHash =
                tokenHashUtil.hash(request.refreshToken());

        refreshTokenRepository.findByTokenHash(tokenHash)
                .ifPresent(RefreshTokenEntity::revoke);
    }


//    Helper methods
    private AuthResponse createAuthResponse(
            UserPrincipal principal,
            UserEntity user
    ) {

        String accessToken =
                jwtService.generateAccessToken(principal);

        String rawRefreshToken =
                refreshTokenGenerator.generate();

        String refreshTokenHash =
                tokenHashUtil.hash(rawRefreshToken);

        LocalDateTime expiresAt = LocalDateTime.now()
                .plus(
                        jwtProperties.refreshTokenExpiration(),
                        java.time.temporal.ChronoUnit.MILLIS
                );

        RefreshTokenEntity refreshToken =
                RefreshTokenEntity.builder()
                        .user(user)
                        .tokenHash(refreshTokenHash)
                        .expiresAt(expiresAt)
                        .build();

        refreshTokenRepository.save(refreshToken);

        return new AuthResponse(
                "Bearer",
                accessToken,
                rawRefreshToken,
                jwtService.getAccessTokenExpiration() / 1000,
                userMapper.toResponse(user)
        );
    }

    private String normalizeEmail(String email) {
        return email.trim()
                .toLowerCase(Locale.ROOT);
    }
}