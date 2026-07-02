package com.sm.leave.service;

import com.sm.leave.dto.response.AttendanceResponse;

public interface AttendanceService {

	AttendanceResponse clockIn(Long employeeId);
	AttendanceResponse clockOut(Long employeeId);
}
