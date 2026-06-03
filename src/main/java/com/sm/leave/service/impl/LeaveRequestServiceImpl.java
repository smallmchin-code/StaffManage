package com.sm.leave.service.impl;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.extern.slf4j.Slf4j;

import com.sm.leave.dto.request.LeaveApplyRequest;
import com.sm.leave.dto.response.LeaveApplyResponse;
import com.sm.leave.repository.EmployeeRepository;
import com.sm.leave.repository.LeaveRequestRepository;
import com.sm.leave.service.LeaveRequestService;
import com.sm.leave.exception.LeaveException;
import com.sm.leave.entity.Employee;
import com.sm.leave.entity.LeaveType;
import com.sm.leave.entity.LeaveRequest;
import com.sm.leave.repository.LeaveTypeRepository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import java.time.temporal.ChronoUnit;

@Slf4j
@Service
@Transactional(readOnly = true)
public class LeaveRequestServiceImpl implements LeaveRequestService {
	
	private final EmployeeRepository employeeRepository;
	private final LeaveRequestRepository leaveRequestRepository;
	private final LeaveTypeRepository leaveTypeRepository;
	
	public LeaveRequestServiceImpl(EmployeeRepository employeeRepository,
	                               LeaveRequestRepository leaveRequestRepository,
	                               LeaveTypeRepository leaveTypeRepository) {
		this.employeeRepository = employeeRepository;
		this.leaveRequestRepository = leaveRequestRepository;
		this.leaveTypeRepository = leaveTypeRepository;
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
                .appliedAt(request.getAppliedAt())
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
        													 request.getAppliedAt()        							
        		);
        return response;
		  
	 }
	
//	/**
//	 * 請假審核
//	 */
//	@Override
//    @Transactional
//	public void approveLeave() {
//		
//	}
	
	
	
	private void sendNotificationToManager(Employee employee, LeaveRequest request) {
        // 實作你的通知邏輯，例如發送 Email 或系統推播
        log.info("已發送請假通知給員工 {} 的直屬主管。假單ID: {}", employee.getName(), request.getId());
    }
}
