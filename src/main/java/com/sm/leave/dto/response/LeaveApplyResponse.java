package com.sm.leave.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record LeaveApplyResponse(LocalDate startDate,

                                 LocalDate endDate,

                                 BigDecimal totalDays,

                                 String reason,

                                 /*
                                  * PENDING / APPROVED / REJECTED / CANCELLED
                                  * */
                                 String status,

                                 LocalDateTime appliedAt) {


}
