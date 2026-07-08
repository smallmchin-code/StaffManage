package com.sm.leave.repository;

import com.sm.leave.entity.AttendanceRecord;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
@Repository
public interface AttendanceRecordRepository extends JpaRepository<AttendanceRecord, Integer> {

	// 檢查在某個時間區間內，該員工是否已有打卡紀錄
    boolean existsByEmployeeIdAndCheckInTimeBetween(Long employeeId, LocalDateTime start, LocalDateTime end);
    
 // ✨ 下班打卡用的查詢：回傳 Optional，找不到就噴錯誤
    Optional<AttendanceRecord> findByEmployeeIdAndCheckInTimeBetween(Long employeeId, LocalDateTime start, LocalDateTime end);

    List<AttendanceRecord> findByEmployeeIdAndCheckInTimeBetweenOrderByCheckInTimeAsc(
            Long employeeId, LocalDateTime start, LocalDateTime end
    );
}
