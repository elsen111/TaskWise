package com.todobackend.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record EmailChangeRequest(
        @Email(message = "Email must be valid")
        @NotBlank(message = "Email is required")
        @Size(min = 11, max = 255, message = "Email length must be between 11 and 255 characters long")
        String newEmail,

        @NotBlank(message = "Password is required")
        String password
) {
}
