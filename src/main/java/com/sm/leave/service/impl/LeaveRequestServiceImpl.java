package com.sm.leave.service.impl;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.extern.slf4j.Slf4j;

import com.sm.leave.dto.request.*;
import com.sm.leave.dto.response.*;
import com.sm.leave.repository.EmployeeRepository;
import com.sm.leave.repository.LeaveRequestRepository;
import com.sm.leave.service.LeaveRequestService;
import com.sm.leave.exception.LeaveException;
import com.sm.leave.entity.ApprovalHistory;
import com.sm.leave.entity.Employee;
import com.sm.leave.entity.LeaveType;
import com.sm.leave.entity.LeaveRequest;
import com.sm.leave.repository.LeaveTypeRepository;
import com.sm.leave.repository.ApprovalHistoryRepository;

import java.math.BigDecimal;
import java.time.*;
import java.util.*;
import java.time.temporal.ChronoUnit;

@Slf4j
@Service
@Transactional(readOnly = true)
public class LeaveRequestServiceImpl implements LeaveRequestService {

    private final EmployeeRepository employeeRepository;
    private final LeaveRequestRepository leaveRequestRepository;
    private final LeaveTypeRepository leaveTypeRepository;
    private final ApprovalHistoryRepository approvalHistoryRepository;

    public LeaveRequestServiceImpl(EmployeeRepository employeeRepository,
                                   LeaveRequestRepository leaveRequestRepository,
                                   LeaveTypeRepository leaveTypeRepository,
                                   ApprovalHistoryRepository approvalHistoryRepository) {
        this.employeeRepository = employeeRepository;
        this.leaveRequestRepository = leaveRequestRepository;
        this.leaveTypeRepository = leaveTypeRepository;
        this.approvalHistoryRepository = approvalHistoryRepository;
    }

    /**
     * 申請假單
     */
    @Override
    @Transactional
    public LeaveApplyResponse applyLeave(LeaveApplyRequest request) {

        // 1. 驗證員工是否存在
        Employee employee = employeeRepository.findByEmployeeNo(request.getEmployeeNo())
                .orElseThrow(() -> new LeaveException("找不到工號為 " + request.getEmployeeNo() + " 的員工"));

        // 2. 檢查離職員工不能請假
        if ("INACTIVE".equalsIgnoreCase(employee.getStatus())) {
            throw new LeaveException("離職員工不能請假");
        }

        LocalDate startDate = request.getStartDate();
        LocalDate endDate = request.getEndDate();

        // 3. 檢查日期是否合法：結束日期不能 < 開始日期
        if (endDate.isBefore(startDate)) {
            throw new LeaveException("請假結束日期不能早於開始日期");
        }

        // 4. 檢查是否重複請假 (黃金交叉檢查)
        // 公式：新請假的開始時間 <= 已存在的結束時間 AND 新請假的結束時間 >= 已存在的開始時間
        // 同時，我們只檢查狀態不是 "REJECTED" 或 "CANCELLED" 的有效假單
        List<String> activeStatuses = List.of("PENDING", "APPROVED");
        boolean isOverlapped = leaveRequestRepository.existsByEmployeeAndStatusInAndStartDateLessThanEqualAndEndDateGreaterThanEqual(
                employee, activeStatuses, endDate, startDate
        );

        if (isOverlapped) {
            throw new LeaveException("申請的日期區間與已有假單重複");
        }

        // 5. 計算請假天數 (轉為 BigDecimal 符合 Entity 設計)
        long daysBetween = ChronoUnit.DAYS.between(startDate, endDate) + 1;
        BigDecimal totalDays = BigDecimal.valueOf(daysBetween);

        // 6. 撈取假別 (假設 request 有帶 leaveTypeId)
        LeaveType leaveType = leaveTypeRepository.findById(request.getLeaveTypeId())
                .orElseThrow(() -> new LeaveException("找不到指定的假別"));

        // 7. 建立請假單物件 (使用 Builder 模式，狀態 PENDING 在 PrePersist 或這裡設定皆可)
        LeaveRequest leaveRequest = LeaveRequest.builder()
                .employee(employee)
                .leaveType(leaveType)
                .startDate(startDate)
                .endDate(endDate)
                .totalDays(totalDays)
                .reason(request.getReason())
                .status("PENDING") // 雖然 PrePersist 有寫，但這裡明確指定更清晰
                .build();

        // 8. 儲存請假單
        LeaveRequest savedRequest = leaveRequestRepository.save(leaveRequest);

        // 9. 建立通知、通知主管
        sendNotificationToManager(employee, savedRequest);

        LeaveApplyResponse response = new LeaveApplyResponse(startDate,
                endDate,
                totalDays,
                request.getReason(),
                "PENDING",
                LocalDateTime.now()
        );
        return response;

    }

    /**
     * 請假審核
     */
    @Override
    @Transactional
    public LeaveApprovalResponse approveLeave(LeaveApprovalRequest LeaveApprovalRequest, Long managerId) {
        // 1. 驗證審核人（主管）是否存在
        Employee manager = employeeRepository.findById(managerId)
                .orElseThrow(() -> new LeaveException("找不到該審核人員"));

        // 2. 驗證是否為主管（這裡假設 Employee 有個 getRole() 或類似的屬性）
        if (!"MANAGER".equalsIgnoreCase(manager.getRole().getCode())) {
            throw new LeaveException("權限不足：只有主管階級可以審核假單");
        }

        // 3. 撈取假單
        LeaveRequest leaveRequest = leaveRequestRepository.findById(LeaveApprovalRequest.getLeaveRequestId())
                .orElseThrow(() -> new LeaveException("找不到該筆請假申請"));

        // 4. 驗證安全性：這個主管是否有權限審核這名員工？ (進階商務邏輯，可依需求加入)
        // if (!leaveRequest.getEmployee().getManager().getId().equals(managerId)) {
        //     throw new LeaveException("您不是該員工的直屬主管，無法審核此假單");
        // }

        // 5. 驗證是否能審核：只有 PENDING 狀態才能審核
        if (!"PENDING".equalsIgnoreCase(leaveRequest.getStatus())) {
            throw new LeaveException("該假單已被處理過，目前的狀態為: " + leaveRequest.getStatus());
        }

        // 6. 更新假單狀態與審核資訊
        Boolean approved = LeaveApprovalRequest.getApproved();
        if (approved == null) {
            throw new LeaveException("審核結果不能為空");
        }

        String action = approved ? "APPROVED" : "REJECTED";
        if (!approved && (LeaveApprovalRequest.getComment() == null || LeaveApprovalRequest.getComment().isBlank())) {
            throw new LeaveException("駁回必須填寫原因");
        }

        if (!approved) {
            leaveRequest.setRejectReason(LeaveApprovalRequest.getComment());
        }
        leaveRequest.setStatus(action);
        leaveRequest.setApprovedBy(manager);
        leaveRequest.setApprovedAt(LocalDateTime.now());
        // 因為有 @Transactional，JPA 在方法結束時會自動 flush (Dirty Checking)，不一定要手動 save
        LeaveRequest updatedRequest = leaveRequestRepository.save(leaveRequest);

        String comment = LeaveApprovalRequest.getComment() != null ? LeaveApprovalRequest.getComment() : "主管同意";

        // 7. 紀錄審核歷程 (因為 LeaveRequest 內部 有 List<ApprovalHistory>，我們直接建立並連動)
        ApprovalHistory history = ApprovalHistory.builder()
                .leaveRequest(updatedRequest)
                .approver(manager)
                .action("APPROVED")
                .comment(comment)
                .actionTime(LocalDateTime.now())
                .build();

        approvalHistoryRepository.save(history);

        // 8. 發送通知給員工
        sendNotificationToEmployee(updatedRequest.getEmployee(), updatedRequest);

        LeaveApprovalResponse response = LeaveApprovalResponse.builder()
                .leaveRequestId(updatedRequest.getId())
                .status("APPROVED")
                .approvedAt(LocalDateTime.now())
                .approvedByName(manager.getName())
                .approvedById(managerId)
                .comment(comment)
                .build();

        return response;

    }


    private void sendNotificationToManager(Employee employee, LeaveRequest request) {
        // 實作你的通知邏輯，例如發送 Email 或系統推播
        log.info("已發送請假通知給員工 {} 的直屬主管。假單ID: {}", employee.getName(), request.getId());
    }

    private void sendNotificationToEmployee(Employee employee, LeaveRequest request) {
        // 實作你的通知邏輯，例如發送 Email 或系統推播
        log.info("已發送審核結果通知給員工 {}。假單ID: {}", employee.getName(), request.getId());
    }

}
