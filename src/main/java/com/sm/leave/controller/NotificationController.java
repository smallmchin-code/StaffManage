package com.sm.leave.controller;

import com.sm.leave.dto.response.ApiResponse;
import com.sm.leave.dto.response.NotificationResponse;
import com.sm.leave.entity.Employee;
import com.sm.leave.service.NotificationService;
import com.sm.leave.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private static final Logger logger = LoggerFactory.getLogger(NotificationController.class);

    private final NotificationService notificationService;
    private final SecurityUtils       securityUtils;

    /** 取得所有通知（最多 50 筆，最新在前） */
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<List<NotificationResponse>>> getNotifications() {
        Employee employee = securityUtils.getCurrentEmployee();

        logger.info("GET /api/notifications 開始 - 員工 ID: {}", employee.getId());

        List<NotificationResponse> notifications = notificationService.getNotifications(employee.getId());

        logger.info("GET /api/notifications 完成 - 員工 ID: {}, 筆數: {}", employee.getId(), notifications.size());
        return ResponseEntity.ok(ApiResponse.success(notifications));
    }

    /** 取得未讀通知數量 */
    @GetMapping("/unread-count")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Long>> getUnreadCount() {
        Employee employee = securityUtils.getCurrentEmployee();

        logger.info("GET /api/notifications/unread-count 開始 - 員工 ID: {}", employee.getId());

        Long unreadCount = notificationService.getUnreadCount(employee.getId());

        logger.info("GET /api/notifications/unread-count 完成 - 員工 ID: {}, 未讀數量: {}", employee.getId(), unreadCount);
        return ResponseEntity.ok(ApiResponse.success(unreadCount));
    }

    /** 標記單筆通知為已讀 */
    @PatchMapping("/{notificationId}/read")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Void>> markAsRead(@PathVariable Long notificationId) {
        Employee employee = securityUtils.getCurrentEmployee();

        logger.info("PATCH /api/notifications/{}/read 開始 - 員工 ID: {}", notificationId, employee.getId());

        notificationService.markAsRead(notificationId, employee.getId());

        logger.info("PATCH /api/notifications/{}/read 完成 - 員工 ID: {}", notificationId, employee.getId());
        return ResponseEntity.ok(ApiResponse.success("已標記為已讀"));
    }

    /** 一鍵全部已讀 */
    @PatchMapping("/read-all")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Void>> markAllAsRead() {
        Employee employee = securityUtils.getCurrentEmployee();

        logger.info("PATCH /api/notifications/read-all 開始 - 員工 ID: {}", employee.getId());

        notificationService.markAllAsRead(employee.getId());

        logger.info("PATCH /api/notifications/read-all 完成 - 員工 ID: {}", employee.getId());
        return ResponseEntity.ok(ApiResponse.success("已全部標記為已讀"));
    }
}