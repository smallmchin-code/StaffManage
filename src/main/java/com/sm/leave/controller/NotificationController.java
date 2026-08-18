package com.sm.leave.controller;

import com.sm.leave.dto.response.ApiResponse;
import com.sm.leave.dto.response.NotificationResponse;
import com.sm.leave.entity.Employee;
import com.sm.leave.service.NotificationService;
import com.sm.leave.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;
    private final SecurityUtils       securityUtils;

    /** 取得所有通知（最多 50 筆，最新在前） */
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<List<NotificationResponse>>> getNotifications() {
        Employee employee = securityUtils.getCurrentEmployee();

        return ResponseEntity.ok(ApiResponse.success(
                notificationService.getNotifications(employee.getId())));
    }

    /** 取得未讀通知數量 */
    @GetMapping("/unread-count")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Long>> getUnreadCount() {
        Employee employee = securityUtils.getCurrentEmployee();

        return ResponseEntity.ok(ApiResponse.success(
                notificationService.getUnreadCount(employee.getId())));
    }

    /** 標記單筆通知為已讀 */
    @PatchMapping("/{notificationId}/read")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Void>> markAsRead(@PathVariable Long notificationId) {
        Employee employee = securityUtils.getCurrentEmployee();

        notificationService.markAsRead(notificationId, employee.getId());
        return ResponseEntity.ok(ApiResponse.success("已標記為已讀"));
    }

    /** 一鍵全部已讀 */
    @PatchMapping("/read-all")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Void>> markAllAsRead() {
        Employee employee = securityUtils.getCurrentEmployee();

        notificationService.markAllAsRead(employee.getId());
        return ResponseEntity.ok(ApiResponse.success("已全部標記為已讀"));
    }
}