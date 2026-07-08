package com.sm.leave.dto.request;

import java.time.LocalDate;
import java.time.LocalDateTime;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record LeaveApplyRequest(
        @NotBlank(message = "員工編號不能為空") String employeeNo,
        @NotNull(message = "假別不能為空") Long leaveTypeId,
        @NotNull(message = "開始日期不能為空") LocalDate startDate,
        @NotNull(message = "結束日期不能為空") LocalDate endDate,
        @NotBlank(message = "請假原因不能為空") String reason) {
}
