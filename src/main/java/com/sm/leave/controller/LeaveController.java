package com.sm.leave.controller;

import com.sm.leave.dto.request.LeaveApplyRequest;
import com.sm.leave.dto.request.LeaveApprovalRequest;
import com.sm.leave.dto.response.*;
import com.sm.leave.entity.Employee;
import com.sm.leave.service.LeaveRequestService;
import com.sm.leave.util.SecurityUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
    private static final Logger logger = LoggerFactory.getLogger(LeaveController.class);

    /**
     * 申請請假
     */
    @PreAuthorize("isAuthenticated()")
    @PostMapping("/apply")
    public ResponseEntity<ApiResponse<LeaveApplyResponse>> leaveApply(
            @Valid @RequestBody LeaveApplyRequest request) {
        Employee employee = securityUtils.getCurrentEmployee();

        logger.info("POST /api/leave/apply 開始 - 工號: {}", employee.getEmployeeNo());

        LeaveApplyResponse response = leaveRequestService.applyLeave(request, employee.getEmployeeNo());

        logger.info("POST /api/leave/apply 完成 - 工號: {}", employee.getEmployeeNo());
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * 審核假單（同意 / 駁回）
     */
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    @PostMapping("/approval")
    public ResponseEntity<ApiResponse<LeaveApprovalResponse>> leaveApproval(
            @Valid @RequestBody LeaveApprovalRequest request) {
        Employee manager = securityUtils.getCurrentEmployee();

        logger.info("POST /api/leave/approval 開始 - 審核者 ID: {}, 假單 ID: {}",
                manager.getId(), request.leaveRequestId());

        LeaveApprovalResponse response = leaveRequestService.approveLeave(request, manager.getId());

        logger.info("POST /api/leave/approval 完成 - 審核者 ID: {}, 假單 ID: {}", manager.getId(), request.leaveRequestId());
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * 取消自己的假單
     */
    @PatchMapping("/cancel/{leaveRequestId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<LeaveCancelResponse>> cancelLeave(
            @PathVariable Long leaveRequestId) {
        Employee employee = securityUtils.getCurrentEmployee();

        logger.info("PATCH /api/leave/cancel/{} 開始 - 工號: {}", leaveRequestId, employee.getEmployeeNo());

        LeaveCancelResponse response = leaveRequestService.cancelLeave(leaveRequestId, employee.getEmployeeNo());

        logger.info("PATCH /api/leave/cancel/{} 完成 - 工號: {}", leaveRequestId, employee.getEmployeeNo());
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * 查詢自己的請假紀錄
     */
    @GetMapping("/my-history")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<List<LeaveHistoryResponse>>> getMyLeaveHistory() {
        Employee employee = securityUtils.getCurrentEmployee();

        logger.info("GET /api/leave/my-history 開始 - 工號: {}", employee.getEmployeeNo());

        List<LeaveHistoryResponse> history = leaveRequestService.getMyLeaveHistory(employee.getEmployeeNo());

        logger.info("GET /api/leave/my-history 完成 - 工號: {}, 筆數: {}", employee.getEmployeeNo(), history.size());
        return ResponseEntity.ok(ApiResponse.success(history));
    }

    /**
     * 查詢待審核清單
     */
    @GetMapping("/pending")
//    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    public ResponseEntity<ApiResponse<List<PendingLeaveResponse>>> getPendingLeaves() {
        Employee manager = securityUtils.getCurrentEmployee();

        logger.info("GET /api/leave/pending 開始 - 主管 ID: {}", manager.getId());

        List<PendingLeaveResponse> pendingLeaves = leaveRequestService.getPendingLeaves(manager.getId());

        logger.info("GET /api/leave/pending 完成 - 主管 ID: {}, 待審核筆數: {}", manager.getId(), pendingLeaves.size());
        return ResponseEntity.ok(ApiResponse.success(pendingLeaves));

//        return ResponseEntity.ok(ApiResponse.success(pendingLeaves));
    }
}