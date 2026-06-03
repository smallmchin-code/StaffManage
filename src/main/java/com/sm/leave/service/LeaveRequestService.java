package com.sm.leave.service;

import com.sm.leave.dto.request.LeaveApplyRequest;
import com.sm.leave.dto.response.LeaveApplyResponse;

public interface LeaveRequestService {
	
	 LeaveApplyResponse applyLeave(LeaveApplyRequest request);
}
