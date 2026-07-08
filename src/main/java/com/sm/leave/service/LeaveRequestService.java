package com.sm.leave.service;

import com.sm.leave.dto.request.*;
import com.sm.leave.dto.response.*;

import java.util.List;

public interface LeaveRequestService {

    LeaveApplyResponse applyLeave(LeaveApplyRequest request);

    LeaveApprovalResponse approveLeave(LeaveApprovalRequest leaveApprovalRequest, Long managerId);

    LeaveCancelResponse cancelLeave(Long leaveRequestId, String employeeNo);

    List<LeaveHistoryResponse> getMyLeaveHistory(String employeeNo);

    List<PendingLeaveResponse> getPendingLeaves(Long managerId);

}
