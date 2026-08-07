package com.sm.leave.dto.request;

import jakarta.validation.constraints.NotBlank;

public record UpdatePasswordRequest(
        @NotBlank String oldPassword,
        @NotBlank String newPassword) {}
