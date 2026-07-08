package com.sm.leave.dto.response;

import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Builder
public record LeaveHistoryResponse(Long leaveRequestId,
                                   String leaveTypeName,  // 顯示「特休」、「事假」，而不是丟一個假別 ID 給前,
                                   LocalDate startDate,
                                   LocalDate endDate,
                                   BigDecimal totalDays,
                                   String reason,
                                   String status,         // PENDING, APPROVED, REJECTED, CANCELL
                                   LocalDateTime appliedAt,

                                   // 審核相關資訊（選填，有被審核過才會有值）
                                   String approvedByName,
                                   LocalDateTime approvedAt,
                                   String rejectReason

) {
}
