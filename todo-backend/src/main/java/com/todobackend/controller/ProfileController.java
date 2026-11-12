package com.todobackend.controller;

import com.todobackend.common.dto.ApiResponse;
import com.todobackend.dto.request.ChangePasswordRequest;
import com.todobackend.dto.request.ConfirmEmailChangeRequest;
import com.todobackend.dto.request.EmailChangeRequest;
import com.todobackend.dto.request.UpdateFullNameRequest;
import com.todobackend.dto.response.UserResponse;
import com.todobackend.security.principal.UserPrincipal;
import com.todobackend.service.EmailChangeService;
import com.todobackend.service.ProfileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Tag(
        name = "Profile",
        description = "Endpoints for profile management"
)
@RestController
@RequestMapping("api/v1/profile")
@RequiredArgsConstructor
public class ProfileController {

    private final ProfileService profileService;
    private final EmailChangeService emailChangeService;

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

    @Operation(
            summary = "Request an email change.",
            description = "Sends an email verification link to the new email."
    )
    @PostMapping("/email/change")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Void>> requestEmailChange(
            @AuthenticationPrincipal UserPrincipal authenticatedUser,
            @Valid @RequestBody EmailChangeRequest request
    ) {

        emailChangeService.requestEmailChange(
                authenticatedUser,
                request
        );

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Verification email sent",
                        null
                )
        );
    }

    @Operation(
            summary = "Confirms the new email.",
            description = "Open the confirmation page in frontend UI and confirms the new email."
    )
    @SecurityRequirements
    @PostMapping("/email/confirm")
    public ResponseEntity<ApiResponse<Void>> confirmEmailChange(
            @Valid @RequestBody ConfirmEmailChangeRequest request
    ) {

        emailChangeService.confirmEmailChange(request.token());

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Email changed successfully",
                        null
                )
        );
    }


    @Operation(
            summary = "Update the account password",
            description = "Replaces the old password with the new one."
    )
    @PreAuthorize("isAuthenticated()")
    @PatchMapping("/password")
    public ResponseEntity<ApiResponse<Void>> changePassword(
            @AuthenticationPrincipal UserPrincipal authenticatedUser,
            @RequestBody @Valid ChangePasswordRequest request
    ) {

        profileService.changePassword(authenticatedUser, request);

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Password changed successfully",
                        null
                )
        );

    }

}
