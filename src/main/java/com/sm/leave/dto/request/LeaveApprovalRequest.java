package com.sm.leave.dto.request;

import jakarta.validation.constraints.NotNull;

public record LeaveApprovalRequest(Long leaveRequestId,

                                   Boolean approved,

                                   String comment) {


}