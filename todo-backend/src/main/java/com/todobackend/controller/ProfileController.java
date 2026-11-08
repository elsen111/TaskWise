package com.todobackend.controller;

import com.todobackend.common.dto.ApiResponse;
import com.todobackend.dto.request.UpdateFullNameRequest;
import com.todobackend.dto.response.UserResponse;
import com.todobackend.security.principal.UserPrincipal;
import com.todobackend.service.ProfileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(
        name = "Profile",
        description = "Endpoints for profile management"
)
@RestController
@RequestMapping("/profile")
@RequiredArgsConstructor
public class ProfileController {

    private final ProfileService profileService;

    @Operation(
            summary = "Update the full name",
            description = "Replaces the old full name with the new one and returns the updated user response."
    )
    @PreAuthorize("isAuthenticated()")
    @PatchMapping
    public ResponseEntity<ApiResponse<UserResponse>> updateFullName(
            @AuthenticationPrincipal UserPrincipal authenticatedUser,
            @RequestBody @Valid UpdateFullNameRequest request
    ) {

        UserResponse response = profileService.updateFullName(authenticatedUser, request);

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Full name updated successfully",
                        response
                )
        );

    }

}
