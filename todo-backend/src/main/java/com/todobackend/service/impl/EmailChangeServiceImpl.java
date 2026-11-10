package com.todobackend.service.impl;

import com.todobackend.dto.request.EmailChangeRequest;
import com.todobackend.entity.EmailChangeRequestEntity;
import com.todobackend.entity.UserEntity;
import com.todobackend.exception.BusinessException;
import com.todobackend.exception.InvalidCredentialsException;
import com.todobackend.exception.InvalidTokenException;
import com.todobackend.exception.NotFoundException;
import com.todobackend.repository.EmailChangeRequestRepository;
import com.todobackend.repository.UserRepository;
import com.todobackend.security.principal.UserPrincipal;
import com.todobackend.service.EmailChangeService;
import com.todobackend.service.EmailChangeTokenService;
import com.todobackend.service.EmailService;
import com.todobackend.service.RefreshTokenService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Locale;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class EmailChangeServiceImpl implements EmailChangeService {

    private final UserRepository userRepository;
    private final EmailChangeRequestRepository emailChangeRepository;
    private final EmailChangeTokenService tokenService;
    private final PasswordEncoder passwordEncoder;
    private final JavaMailSender mailSender;
    private final RefreshTokenService refreshTokenService;
    private final EmailService emailService;

    @Value("${app.frontend.url}")
    private String frontendUrl;

    @Value("${app.security.email-change.expiration}")
    private long expirationMinutes;

    @Override
    @Transactional
    public void requestEmailChange(
            UserPrincipal userPrincipal,
            EmailChangeRequest request
    ) {

        UserEntity user = userRepository.findById(userPrincipal.getId())
                .orElseThrow(() ->
                        new NotFoundException("User not found")
                );

        if (!passwordEncoder.matches(
                request.password(),
                user.getPasswordHash())) {

            throw new InvalidCredentialsException(
                    "Current password is incorrect"
            );
        }

        String newEmail = request.newEmail()
                .trim()
                .toLowerCase(Locale.ROOT);

        if (user.getEmail().equalsIgnoreCase(newEmail)) {
            throw new BusinessException(
                    "New email must be different from current email"
            );
        }

        if (userRepository.existsByEmailIgnoreCaseAndIsDeletedFalse(newEmail)) {
            throw new BusinessException(
                    "Email is already registered"
            );
        }

        emailChangeRepository.invalidatePreviousRequests(
                user.getId(),
                Instant.now()
        );

        String rawToken = tokenService.generateToken();

        String tokenHash = tokenService.hashToken(rawToken);

        EmailChangeRequestEntity entity =
                EmailChangeRequestEntity.builder()
                        .user(user)
                        .newEmail(newEmail)
                        .tokenHash(tokenHash)
                        .expiresAt(
                                Instant.now().plus(
                                        expirationMinutes,
                                        ChronoUnit.MINUTES
                                )
                        )
                        .build();

        emailChangeRepository.save(entity);

        emailService.sendEmailChangeVerification(newEmail, rawToken);
    }

    @Override
    @Transactional
    public void confirmEmailChange(String rawToken) {

        String tokenHash = tokenService.hashToken(rawToken);

        EmailChangeRequestEntity request =
                emailChangeRepository.findByTokenHashForUpdate(tokenHash)
                        .orElseThrow(() ->
                                new InvalidTokenException(
                                        "Invalid verification token"
                                )
                        );

        if (request.getUsedAt() != null) {
            throw new InvalidTokenException(
                    "Verification token has already been used"
            );
        }

        if (!request.getExpiresAt().isAfter(Instant.now())) {
            throw new InvalidTokenException(
                    "Verification token has expired"
            );
        }

        UserEntity user = userRepository.findById(request.getUser().getId())
                .orElseThrow(() ->
                        new NotFoundException("User not found")
                );

        if (userRepository.existsByEmailIgnoreCaseAndIsDeletedFalse(request.getNewEmail())) {
            throw new BusinessException(
                    "Email is already registered"
            );
        }

        String oldEmail = user.getEmail();

        user.setEmail(request.getNewEmail());

        userRepository.save(user);

        request.setUsedAt(Instant.now());

        emailChangeRepository.save(request);

        refreshTokenService.revokeAllUserRefreshTokens(user.getId());

        emailService.sendEmailChangeNotification(oldEmail, user.getEmail());
    }

}