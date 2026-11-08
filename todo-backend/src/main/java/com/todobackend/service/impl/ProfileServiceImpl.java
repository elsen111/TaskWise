package com.todobackend.service.impl;

import com.todobackend.dto.request.UpdateFullNameRequest;
import com.todobackend.dto.response.UserResponse;
import com.todobackend.entity.UserEntity;
import com.todobackend.exception.NotFoundException;
import com.todobackend.mapper.UserMapper;
import com.todobackend.repository.UserRepository;
import com.todobackend.security.principal.UserPrincipal;
import com.todobackend.service.ProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ProfileServiceImpl implements ProfileService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;

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
}
