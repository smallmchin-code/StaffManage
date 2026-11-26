package com.sm.leave.controller;

import com.sm.leave.dto.response.ApiResponse;
import com.sm.leave.dto.response.AttendanceHistoryResponse;
import com.sm.leave.dto.response.AttendanceResponse;
import com.sm.leave.entity.Employee;
import com.sm.leave.service.AttendanceService;
import com.sm.leave.util.SecurityUtils;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.YearMonth;
import java.util.List;

@RestController
@RequestMapping("/api/attendance")
@RequiredArgsConstructor
public class AttendanceController {

    private static final Logger logger = LoggerFactory.getLogger(AttendanceController.class);

    private final AttendanceService   attendanceService;
    private final SecurityUtils       securityUtils;

    /**
     * 上班打卡
     */
    @PostMapping("/clock-in")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<AttendanceResponse>> clockIn() {
        Employee employee = securityUtils.getCurrentEmployee();

        logger.info("POST /api/attendance/clock-in 開始 - 員工 ID: {}", employee.getId());

        AttendanceResponse response = attendanceService.clockIn(employee.getId());

        logger.info("POST /api/attendance/clock-in 完成 - 員工 ID: {}", employee.getId());
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * 下班打卡
     */
    @PostMapping("/clock-out")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<AttendanceResponse>> clockOut() {
        Employee employee = securityUtils.getCurrentEmployee();

        logger.info("POST /api/attendance/clock-out 開始 - 員工 ID: {}", employee.getId());

        AttendanceResponse response = attendanceService.clockOut(employee.getId());

        logger.info("POST /api/attendance/clock-out 完成 - 員工 ID: {}", employee.getId());
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * 查詢自己的當月出勤紀錄
     */
    @GetMapping("/my-history")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<List<AttendanceHistoryResponse>>> getMyAttendance(
            @RequestParam(required = false) String month) {
        Employee employee = securityUtils.getCurrentEmployee();

        YearMonth yearMonth = (month != null) ? YearMonth.parse(month) : YearMonth.now();

        logger.info("GET /api/attendance/my-history 開始 - 員工 ID: {}, 月份: {}", employee.getId(), yearMonth);

        List<AttendanceHistoryResponse> history = attendanceService.getMyAttendance(employee.getId(), yearMonth);

        logger.info("GET /api/attendance/my-history 完成 - 員工 ID: {}, 筆數: {}", employee.getId(), history.size());
        return ResponseEntity.ok(ApiResponse.success(history));
    }
}