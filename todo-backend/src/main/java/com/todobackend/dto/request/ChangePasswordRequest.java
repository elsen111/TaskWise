package com.todobackend.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record ChangePasswordRequest(
        @NotBlank(message = "Current password is required")
        String currentPassword,

        @NotBlank(message = "New password is required")
        @Pattern(
                regexp = "^(?=.*[A-Z])(?=.*[a-z])(?=.*\\d)(?=.*[^A-Za-z0-9]).+$",
                message = "New password must contain at least one uppercase letter, one lowercase letter, one digit, and one special character"
        )
        @Size(min = 8, message = "New password must be at least 8 characters long.")
        String newPassword
) {
}
