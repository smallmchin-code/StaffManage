package com.sm.leave.dto.request;

import java.time.LocalDate;
import java.time.LocalDateTime;

import jakarta.validation.constraints.NotNull;

public class LeaveApplyRequest {
	
	@NotNull(message = "員工編號不能為空")
	private String employeeNo;
	
	@NotNull(message = "假別不能為空")
	private Long leaveTypeId;

	@NotNull(message = "開始日期不能為空")
    private LocalDate startDate;

	@NotNull(message = "結束日期不能為空")
    private LocalDate endDate;

	@NotNull(message = "請假原因不能為空")
    private String reason;
	
	private LocalDateTime appliedAt;
	
	public LocalDateTime getAppliedAt() {
		return this.appliedAt;
	}
	public String getEmployeeNo() {
		return this.employeeNo;
	}
	
	public Long getLeaveTypeId() {
		return this.leaveTypeId;
	}
	
	public LocalDate getStartDate() {
		return this.startDate;
	}
	
	public LocalDate getEndDate() {
		return this.endDate;
	}
	
	public String getReason() {
		return this.reason;
	}
	
}
