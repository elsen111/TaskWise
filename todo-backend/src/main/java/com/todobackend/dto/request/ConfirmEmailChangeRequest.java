package com.todobackend.dto.request;

import jakarta.validation.constraints.NotBlank;

public record ConfirmEmailChangeRequest(
        @NotBlank(message = "Verification token is required")
        String token
) {
}
