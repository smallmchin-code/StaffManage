package com.sm.leave.dto.response;

import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Builder
public record PendingLeaveResponse(Long leaveRequestId,
                                   // 申請人基本資訊
                                   String employeeNo,
                                   String employeeName,

                                   // 假單資訊
                                   String leaveTypeName,
                                   LocalDate startDate,
                                   LocalDate endDate,
                                   BigDecimal totalDays,
                                   String reason,
                                   LocalDateTime appliedAt,
                                   String status) {
}
