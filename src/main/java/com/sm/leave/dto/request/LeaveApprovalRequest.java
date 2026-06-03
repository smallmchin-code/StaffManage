package com.sm.leave.dto.request;

import jakarta.validation.constraints.NotNull;

public class LeaveApprovalRequest {

    @NotNull(message = "假單編號不能為空")
    private Long leaveRequestId;

    @NotNull(message = "審核編號不能為空")
    private String approverNo;

    private Boolean approved;

    private String comment;

    public Long getLeaveRequestId() {
        return leaveRequestId;
    }

    public String getApproverNo() {
        return approverNo;
    }

    public Boolean getApproved() {
        return approved;
    }

    public String getComment() {
        return comment;
    }

}