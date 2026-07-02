package com.sm.leave.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LeaveApprovalResponse {

    // 1. 讓前端確認是哪一筆假單被操作了
    private Long leaveRequestId;

    // 2. 最核心：更新後的最新狀態 (例如: APPROVED / REJECTED)
    private String status;

    // 3. 審核資訊 (讓前端可以立刻更新「審核人」與「審核時間」的區塊)
    private Long          approvedById;
    private String        approvedByName;
    private LocalDateTime approvedAt;

    // 4. 提示訊息 (非必要，但對前端彈出 Toast 提示很有幫助)
    private String comment;
}
