package com.sm.leave.controller;

import com.sm.leave.dto.request.LeaveApplyRequest;
import com.sm.leave.dto.request.LeaveApprovalRequest;
import com.sm.leave.dto.response.*;
import com.sm.leave.entity.Employee;
import com.sm.leave.service.LeaveRequestService;
import com.sm.leave.util.SecurityUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/leave")
@RequiredArgsConstructor
public class LeaveController {

    private final LeaveRequestService leaveRequestService;
    private final SecurityUtils       securityUtils;

    /**
     * 申請請假
     * 所有登入角色皆可使用
     * 員工身份從 Session 取得，不信任前端傳入的 employeeNo
     */
    @PreAuthorize("isAuthenticated()")
    @PostMapping("/apply")
    public ResponseEntity<ApiResponse<LeaveApplyResponse>> leaveApply(
            @Valid @RequestBody LeaveApplyRequest request) {
        Employee employee = securityUtils.getCurrentEmployee();
        return ResponseEntity.ok(ApiResponse.success(leaveRequestService.applyLeave(request, employee.getEmployeeNo())));
    }

    /**
     * 審核假單（同意 / 駁回）
     * 只有 MANAGER 或 ADMIN 可以操作
     */
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    @PostMapping("/approval")
    public ResponseEntity<ApiResponse<LeaveApprovalResponse>> leaveApproval(
            @Valid @RequestBody LeaveApprovalRequest request) {

        Employee manager = securityUtils.getCurrentEmployee();
        return ResponseEntity.ok(ApiResponse.success(leaveRequestService.approveLeave(request, manager.getId())));
    }

    /**
     * 取消自己的假單
     * 所有登入角色皆可使用，但 service 層會驗證只能取消自己的假單
     */
    @PatchMapping("/cancel/{leaveRequestId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<LeaveCancelResponse>> cancelLeave(
            @PathVariable Long leaveRequestId) {

        Employee employee = securityUtils.getCurrentEmployee();
        return ResponseEntity.ok(ApiResponse.success(
                leaveRequestService.cancelLeave(leaveRequestId, employee.getEmployeeNo())));
    }

    /**
     * 查詢自己的請假紀錄
     * 所有登入角色皆可使用，只看得到自己的
     */
    @GetMapping("/my-history")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<List<LeaveHistoryResponse>>> getMyLeaveHistory() {

        Employee employee = securityUtils.getCurrentEmployee();
        return ResponseEntity.ok(ApiResponse.success(
                leaveRequestService.getMyLeaveHistory(employee.getEmployeeNo())));
    }

    /**
     * 查詢待審核清單
     * 只有 MANAGER 或 ADMIN 可以使用
     */
    @GetMapping("/pending")
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    public ResponseEntity<ApiResponse<List<PendingLeaveResponse>>> getPendingLeaves() {

        Employee manager = securityUtils.getCurrentEmployee();
        return ResponseEntity.ok(ApiResponse.success(
                leaveRequestService.getPendingLeaves(manager.getId())));
    }

}
