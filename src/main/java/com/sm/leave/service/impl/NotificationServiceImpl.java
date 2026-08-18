package com.sm.leave.service.impl;

import com.sm.leave.dto.response.NotificationResponse;
import com.sm.leave.entity.Employee;
import com.sm.leave.entity.LeaveRequest;
import com.sm.leave.entity.Notification;
import com.sm.leave.exception.LeaveException;
import com.sm.leave.repository.NotificationRepository;
import com.sm.leave.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;

    // 每次最多回傳 50 筆通知，避免全量查詢
    private static final int MAX_NOTIFICATION_SIZE = 50;

    // ── 查詢 ───────────────────────────────────────────

    @Override
    public List<NotificationResponse> getNotifications(Long employeeId) {
        log.debug("查詢員工(ID:{}) 的通知列表，最多回傳 {} 筆", employeeId, MAX_NOTIFICATION_SIZE);

        List<NotificationResponse> result = notificationRepository
                .findByEmployeeIdOrderByCreatedAtDesc(
                        employeeId, PageRequest.of(0, MAX_NOTIFICATION_SIZE))
                .stream()
                .map(this::toDto)
                .toList();

        log.debug("員工(ID:{}) 共查詢到 {} 筆通知", employeeId, result.size());
        return result;
    }

    @Override
    public long getUnreadCount(Long employeeId) {
        long count = notificationRepository.countByEmployeeIdAndIsReadFalse(employeeId);
        log.debug("員工(ID:{}) 未讀通知數量：{}", employeeId, count);
        return count;
    }

    // ── 標記已讀 ───────────────────────────────────────

    @Override
    @Transactional
    public void markAsRead(Long notificationId, Long employeeId) {
        log.debug("員工(ID:{}) 嘗試標記通知(ID:{}) 為已讀", employeeId, notificationId);

        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new LeaveException("找不到該通知"));

        if (!notification.getEmployee().getId().equals(employeeId)) {
            log.warn("安全性警訊：員工(ID:{}) 試圖標記不屬於自己的通知(ID:{})", employeeId, notificationId);
            throw new LeaveException("無權操作他人的通知");
        }

        notification.setIsRead(true);
        notificationRepository.save(notification);
        log.info("員工(ID:{}) 已讀通知(ID:{})", employeeId, notificationId);
    }

    @Override
    @Transactional
    public void markAllAsRead(Long employeeId) {
        log.debug("員工(ID:{}) 請求一鍵全部已讀", employeeId);

        // 批次 UPDATE，不需先 SELECT 再逐筆 save
        notificationRepository.markAllAsReadByEmployeeId(employeeId);

        log.info("員工(ID:{}) 已將所有通知標記為已讀", employeeId);
    }

    // ── 發送通知（由其他 Service 呼叫）──────────────────

    @Override
    @Transactional
    public void sendLeaveApplyNotificationToManager(Employee employee, LeaveRequest leaveRequest) {
        log.debug("準備發送請假通知，申請員工：{}，假單ID：{}", employee.getEmployeeNo(), leaveRequest.getId());

        Employee manager = employee.getManager();
        if (manager == null) {
            log.warn("員工 {}({}) 未設定直屬主管，跳過請假通知",
                    employee.getName(), employee.getEmployeeNo());
            return;
        }

        Notification notification = Notification.builder()
                .employee(manager)
                .title("新的請假申請待審核")
                .content(String.format("員工 %s 申請了 %s（%s ~ %s，共 %s 天），請盡速審核。",
                        employee.getName(),
                        leaveRequest.getLeaveType().getName(),
                        leaveRequest.getStartDate(),
                        leaveRequest.getEndDate(),
                        leaveRequest.getTotalDays()))
                .isRead(false)
                .build();

        notificationRepository.save(notification);
        log.info("請假通知已送出 → 主管：{}，申請人：{}，假單ID：{}",
                manager.getName(), employee.getName(), leaveRequest.getId());
    }

    @Override
    @Transactional
    public void sendLeaveResultNotificationToEmployee(Employee employee, LeaveRequest leaveRequest) {
        boolean approved = "APPROVED".equalsIgnoreCase(leaveRequest.getStatus());
        log.debug("準備發送審核結果通知，員工：{}，假單ID：{}，結果：{}",
                employee.getEmployeeNo(), leaveRequest.getId(), leaveRequest.getStatus());

        String title   = approved ? "您的請假申請已核准" : "您的請假申請已駁回";
        String content = approved
                ? String.format("您申請的 %s（%s ~ %s）已由 %s 核准。",
                leaveRequest.getLeaveType().getName(),
                leaveRequest.getStartDate(),
                leaveRequest.getEndDate(),
                leaveRequest.getApprovedBy().getName())
                : String.format("您申請的 %s（%s ~ %s）已被駁回，原因：%s",
                leaveRequest.getLeaveType().getName(),
                leaveRequest.getStartDate(),
                leaveRequest.getEndDate(),
                leaveRequest.getRejectReason());

        Notification notification = Notification.builder()
                .employee(employee)
                .title(title)
                .content(content)
                .isRead(false)
                .build();

        notificationRepository.save(notification);
        log.info("審核結果通知已送出 → 員工：{}，假單ID：{}，狀態：{}",
                employee.getName(), leaveRequest.getId(), leaveRequest.getStatus());
    }

    // ── 私有工具 ───────────────────────────────────────

    private NotificationResponse toDto(Notification n) {
        return NotificationResponse.builder()
                .notificationId(n.getId())
                .title(n.getTitle())
                .content(n.getContent())
                .isRead(n.getIsRead())
                .createdAt(n.getCreatedAt())
                .build();
    }
}