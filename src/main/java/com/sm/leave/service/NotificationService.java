package com.sm.leave.service;

import com.sm.leave.dto.response.NotificationResponse;
import com.sm.leave.entity.Employee;
import com.sm.leave.entity.LeaveRequest;

import java.util.List;

public interface NotificationService {

    /** 取得該員工所有通知（最新在前） */
    List<NotificationResponse> getNotifications(Long employeeId);

    /** 取得未讀通知數量（供前端 badge 顯示） */
    long getUnreadCount(Long employeeId);

    /** 標記單筆通知為已讀 */
    void markAsRead(Long notificationId, Long employeeId);

    /** 標記所有通知為已讀 */
    void markAllAsRead(Long employeeId);

    /** 發送通知給主管（請假申請觸發） */
    void sendLeaveApplyNotificationToManager(Employee employee, LeaveRequest leaveRequest);

    /** 發送審核結果通知給員工（核准/駁回觸發） */
    void sendLeaveResultNotificationToEmployee(Employee employee, LeaveRequest leaveRequest);
}
