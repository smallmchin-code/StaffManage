package com.sm.leave.repository;

import com.sm.leave.entity.Notification;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    // 修正：回傳 List，並支援分頁（避免全量查詢）
    List<Notification> findByEmployeeIdOrderByCreatedAtDesc(Long employeeId, Pageable pageable);

    Long countByEmployeeIdAndIsReadFalse(Long employeeId);

    List<Notification> findByEmployeeIdAndIsReadFalse(Long employeeId);

    // 批次更新已讀，取代逐筆 save
    @Modifying
    @Query("UPDATE Notification n SET n.isRead = true WHERE n.employee.id = :employeeId AND n.isRead = false")
    void markAllAsReadByEmployeeId(@Param("employeeId") Long employeeId);
}