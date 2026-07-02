package com.sm.leave.service;

import com.sm.leave.dto.request.*;
import com.sm.leave.dto.response.*;

public interface LeaveRequestService {
	
	 LeaveApplyResponse applyLeave(LeaveApplyRequest request);
	 
	 LeaveApprovalResponse approveLeave(LeaveApprovalRequest leaveApprovalRequest, Long managerId);
}
