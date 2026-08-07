package com.sm.leave.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ResignationRequest(
        @NotNull Long employeeId,
        @NotBlank String reason) {}
