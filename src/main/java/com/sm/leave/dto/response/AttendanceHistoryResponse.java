package com.sm.leave.dto.response;

import lombok.Builder;

import java.math.BigDecimal;
import java.time.*;

@Builder
public record AttendanceHistoryResponse(Long attendanceId,
                                        LocalDate workDate,          // 工作日期 (例如：2026-07-07)
                                        LocalDateTime checkInTime,   // 上班打卡時間
                                        LocalDateTime checkOutTime,  // 下班打卡時間 (尚未下班則為 null)
                                        BigDecimal workHours,        // 當日總工時 (尚未下班則為 null)
                                        String status) {
}
