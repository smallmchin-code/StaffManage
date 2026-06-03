package com.sm.leave.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class LeaveApplyResponse {

	LocalDate startDate;

	LocalDate endDate;

	BigDecimal totalDays;

	String reason;

	/** PENDING / APPROVED / REJECTED / CANCELLED */
	String status;

	LocalDateTime appliedAt;

	public LocalDate getStartDate() {
		return startDate;
	}

	public void setStartDate(
								LocalDate startDate) {
		this.startDate = startDate;
	}

	public LocalDate getEndDate() {
		return endDate;
	}

	public void setEndDate(
							LocalDate endDate) {
		this.endDate = endDate;
	}

	public BigDecimal getTotalDays() {
		return totalDays;
	}

	public void setTotalDays(
								BigDecimal totalDays) {
		this.totalDays = totalDays;
	}

	public String getReason() {
		return reason;
	}

	public void setReason(
							String reason) {
		this.reason = reason;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(
							String status) {
		this.status = status;
	}

	public LocalDateTime getAppliedAt() {
		return appliedAt;
	}

	public void setAppliedAt(
								LocalDateTime appliedAt) {
		this.appliedAt = appliedAt;
	}

	public LeaveApplyResponse(LocalDate startDate, LocalDate endDate, BigDecimal totalDays, String reason,
			String status, LocalDateTime appliedAt) {
		super();
		this.startDate = startDate;
		this.endDate = endDate;
		this.totalDays = totalDays;
		this.reason = reason;
		this.status = status;
		this.appliedAt = appliedAt;
	}

}
