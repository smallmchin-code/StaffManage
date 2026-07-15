package com.sm.leave.controller;

import com.sm.leave.dto.response.ApiResponse;
import com.sm.leave.dto.response.AttendanceHistoryResponse;
import com.sm.leave.dto.response.AttendanceResponse;
import com.sm.leave.entity.Employee;
import com.sm.leave.exception.LeaveException;
import com.sm.leave.repository.EmployeeRepository;
import com.sm.leave.service.AttendanceService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.time.YearMonth;
import java.util.List;

@RestController
@RequestMapping("/api/attendance")
@RequiredArgsConstructor
public class AttendanceController {

    private final AttendanceService   attendanceService;
    private final EmployeeRepository  employeeRepository;

    /**
     * 上班打卡
     * 所有登入角色皆可使用
     * 員工身份從 Session 取得，不需要前端傳任何參數
     */
    @PostMapping("/clock-in")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<AttendanceResponse>> clockIn(
            @AuthenticationPrincipal UserDetails userDetails) {

        Employee employee = getEmployeeFromSession(userDetails);
        return ResponseEntity.ok(ApiResponse.success(attendanceService.clockIn(employee.getId())));
    }

    /**
     * 下班打卡
     * 所有登入角色皆可使用
     */
    @PostMapping("/clock-out")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<AttendanceResponse>> clockOut(
            @AuthenticationPrincipal UserDetails userDetails) {

        Employee employee = getEmployeeFromSession(userDetails);
        return ResponseEntity.ok(ApiResponse.success(attendanceService.clockOut(employee.getId())));
    }

    /**
     * 查詢自己的當月出勤紀錄
     * 所有登入角色皆可使用，只看得到自己的
     * 呼叫範例：GET /api/attendance/my-history?month=2026-07
     * month 不傳時預設為當月
     */
    @GetMapping("/my-history")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<List<AttendanceHistoryResponse>>> getMyAttendance(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(required = false) String month) {

        Employee employee = getEmployeeFromSession(userDetails);

        // month 參數若前端沒傳，預設查當月
        YearMonth yearMonth = (month != null) ? YearMonth.parse(month) : YearMonth.now();

        return ResponseEntity.ok(ApiResponse.success(
                attendanceService.getMyAttendance(employee.getId(), yearMonth)));
    }

    // ─────────────────────────────────────────────
    // 私有工具方法：從 Session 取出當前登入的 Employee
    // ─────────────────────────────────────────────
    private Employee getEmployeeFromSession(UserDetails userDetails) {
        return employeeRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new LeaveException("找不到該使用者"));
    }
}
