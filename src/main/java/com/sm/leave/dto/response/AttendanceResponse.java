package com.sm.leave.dto.response;

import lombok.Builder;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Builder
public record AttendanceResponse(Long attendanceId,
                                 String employeeName,
                                 LocalDateTime checkInTime,
                                 String status,
                                 String message,
                                 LocalDateTime checkOutTime,
                                 BigDecimal workHours) {


}
