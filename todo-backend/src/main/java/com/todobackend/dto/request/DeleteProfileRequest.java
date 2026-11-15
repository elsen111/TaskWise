package com.todobackend.dto.request;

import jakarta.validation.constraints.NotBlank;

public record DeleteProfileRequest(
        @NotBlank(message = "Current password is required for verification")
        String password
) {
}
