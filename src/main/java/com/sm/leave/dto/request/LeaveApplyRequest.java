package com.sm.leave.dto.request;

import java.time.LocalDate;
import java.time.LocalDateTime;

import jakarta.validation.constraints.NotBlank;

public record LeaveApplyRequest(
        @NotBlank(message = "員工編號不能為空") String employeeNo,
        @NotBlank(message = "假別不能為空") Long leaveTypeId,
        @NotBlank(message = "開始日期不能為空") LocalDate startDate,
        @NotBlank(message = "結束日期不能為空") LocalDate endDate,
        @NotBlank(message = "請假原因不能為空") String reason) {
}
