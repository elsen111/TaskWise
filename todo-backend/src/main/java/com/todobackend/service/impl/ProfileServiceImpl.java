package com.todobackend.service.impl;

import com.todobackend.dto.request.ChangePasswordRequest;
import com.todobackend.dto.request.UpdateFullNameRequest;
import com.todobackend.dto.response.UserResponse;
import com.todobackend.entity.UserEntity;
import com.todobackend.exception.BusinessException;
import com.todobackend.exception.InvalidCredentialsException;
import com.todobackend.exception.NotFoundException;
import com.todobackend.mapper.UserMapper;
import com.todobackend.repository.UserRepository;
import com.todobackend.security.principal.UserPrincipal;
import com.todobackend.service.EmailService;
import com.todobackend.service.ProfileService;
import com.todobackend.service.RefreshTokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ProfileServiceImpl implements ProfileService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final RefreshTokenService refreshTokenService;
    private final EmailService emailService;

    @Transactional
    @Override
    public UserResponse updateFullName(
            UserPrincipal authenticatedUser,
            UpdateFullNameRequest request
    ) {

        UserEntity currentUser = userRepository.findById(authenticatedUser.getId())
                .orElseThrow(
                        () -> new NotFoundException("User not found")
                );

        currentUser.setFullName(request.fullName());

        return userMapper.toResponse(userRepository.save(currentUser));

    }

    @Transactional
    @Override
    public void changePassword(
            UserPrincipal authenticatedUser,
            ChangePasswordRequest request
    ) {

        UserEntity currentUser = userRepository.findById(authenticatedUser.getId())
                .orElseThrow(
                        () -> new NotFoundException("User not found with id " + authenticatedUser.getId())
                );

        if(!passwordEncoder.matches(request.currentPassword(), currentUser.getPasswordHash())) {
            throw new InvalidCredentialsException("Current password is incorrect.");
        }

        if(passwordEncoder.matches(request.newPassword(), currentUser.getPasswordHash())) {
            throw new BusinessException("New password must be different from the current password.");
        }

        currentUser.setPasswordHash(passwordEncoder.encode(request.newPassword()));

        refreshTokenService.revokeAllUserRefreshTokens(currentUser.getId());

        emailService.sendPasswordUpdateNotification(currentUser.getEmail());
    }
}
