package com.sm.leave.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AttendanceResponse {

	private Long     		attendanceId;
	private String 			employeeName;
	private LocalDateTime   checkInTime;
	private String			status;
	private String 			message;
	private LocalDateTime   checkOutTime;
	private BigDecimal      workHours;
}
