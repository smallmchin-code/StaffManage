package com.sm.leave.dto.response;

import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record LeaveCancelResponse(Long leaveRequestId,
                                  String status,        // 固定回傳 CANCELLED
                                  String employeeNo,
                                  LocalDateTime cancelledAt,
                                  String message

) {
}
