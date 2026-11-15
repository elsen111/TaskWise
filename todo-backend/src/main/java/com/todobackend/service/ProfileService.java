package com.todobackend.service;

import com.todobackend.dto.request.ChangePasswordRequest;
import com.todobackend.dto.request.DeleteProfileRequest;
import com.todobackend.dto.request.UpdateFullNameRequest;
import com.todobackend.dto.response.UserResponse;
import com.todobackend.security.principal.UserPrincipal;

public interface ProfileService {

    UserResponse updateFullName(
            UserPrincipal authenticatedUser,
            UpdateFullNameRequest request
    );

    void changePassword(
            UserPrincipal authenticatedUser,
            ChangePasswordRequest request
    );

    void deleteProfile(
            UserPrincipal authenticatedUser,
            DeleteProfileRequest request
    );

}
