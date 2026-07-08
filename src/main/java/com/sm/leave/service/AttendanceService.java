package com.sm.leave.service;

import com.sm.leave.dto.response.AttendanceHistoryResponse;
import com.sm.leave.dto.response.AttendanceResponse;

import java.time.YearMonth;
import java.util.List;

public interface AttendanceService {

	AttendanceResponse clockIn(Long employeeId);
	AttendanceResponse clockOut(Long employeeId);
	List<AttendanceHistoryResponse> getMyAttendance(Long employeeId, YearMonth month);
}
