package com.sm.leave.service.impl;

import java.time.*;
import java.math.RoundingMode;
import java.math.BigDecimal;
import java.util.List;

import com.sm.leave.dto.response.AttendanceHistoryResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sm.leave.dto.response.AttendanceResponse;
import com.sm.leave.entity.Employee;
import com.sm.leave.entity.AttendanceRecord;
import com.sm.leave.exception.LeaveException;
import com.sm.leave.repository.AttendanceRecordRepository;
import com.sm.leave.repository.EmployeeRepository;
import com.sm.leave.service.AttendanceService;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@Transactional(readOnly = true)
public class AttendanceServiceImpl implements AttendanceService {

    private final AttendanceRecordRepository attendanceRecordRepository;
    private final EmployeeRepository employeeRepository;

    public AttendanceServiceImpl(AttendanceRecordRepository attendanceRecordRepository,
                                 EmployeeRepository employeeRepository) {
        this.attendanceRecordRepository = attendanceRecordRepository;
        this.employeeRepository = employeeRepository;

    }

    // 從 application.properties 讀取上班時間，預設 09:00
    @Value("${company.work-start-time:09:00}")
    private String workStartTimeStr;

    /**
     * 上班打卡
     */
    @Override
    @Transactional
    public AttendanceResponse clockIn(Long employeeId) {

        // 1. 驗證員工是否存在
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new LeaveException("找不到該員工，無法打卡"));

        // 2. 檢查離職員工不能打卡
        if ("INACTIVE".equalsIgnoreCase(employee.getStatus())) {
            throw new LeaveException("離職員工無法進行打卡");
        }

        LocalDateTime now = LocalDateTime.now();
        LocalDate today = now.toLocalDate();

        // 3. 檢查今天是否已打卡 (查詢當天 00:00 ~ 23:59 是否已有紀錄)
        LocalDateTime startOfDay = today.atStartOfDay();
        LocalDateTime endOfDay = today.atTime(LocalTime.MAX);

        boolean hasCheckedIn = attendanceRecordRepository.existsByEmployeeIdAndCheckInTimeBetween(employeeId,
                startOfDay, endOfDay);

        if (hasCheckedIn) {
            throw new LeaveException("今天已經完成上班打卡，請勿重複打卡");
        }

        // 4. 判斷是否遲到
        LocalTime checkInTime = now.toLocalTime();
        LocalTime standardStartTime = LocalTime.parse(workStartTimeStr); // 09:00

        String status = "NORMAL";
        if (checkInTime.isAfter(standardStartTime)) {
            status = "LATE";
        }

        // 5. 建立並儲存出勤紀錄
        AttendanceRecord attendance = AttendanceRecord.builder()
                .employee(employee)
                .workDate(today) // 建議多存一個純日期欄位，未來很好查
                .checkInTime(now)
                .status(status).build();

        AttendanceRecord savedAttendance = attendanceRecordRepository.save(attendance);

        // 6. 回傳 Response DTO
        return AttendanceResponse.builder()
                .attendanceId(savedAttendance.getId())
                .employeeName(employee.getName())
                .checkInTime(savedAttendance.getCheckInTime())
                .status(savedAttendance.getStatus())
                .message(status.equals("LATE") ? "打卡成功！您已遲到。" : "打卡成功，祝您有美好的一天！").build();
    }

    /**
     * 下班打卡
     */
    @Override
    @Transactional
    public AttendanceResponse clockOut(Long employeeId) {

        LocalDateTime now = LocalDateTime.now();
        LocalDate today = now.toLocalDate();

        // 1. 找今天的打卡紀錄 (當天 00:00 ~ 23:59)
        LocalDateTime startOfDay = today.atStartOfDay();
        LocalDateTime endOfDay = today.atTime(LocalTime.MAX);

        // 善用 Optional 處理「如果早上忘記打卡」的情況
        AttendanceRecord attendance = attendanceRecordRepository.findByEmployeeIdAndCheckInTimeBetween(employeeId, startOfDay, endOfDay)
                .orElseThrow(() -> new LeaveException("找不到您早上的上班打卡紀錄！請先聯繫人資補打卡。"));

        // 2. 檢查是否已經下班打卡過
        if (attendance.getCheckOutTime() != null) {
            throw new LeaveException("您今天已經完成下班打卡，請勿重複操作");
        }

        // 3. 計算工時 (使用 Java 8 Duration)
        LocalDateTime checkInTime = attendance.getCheckInTime();
        Duration duration = Duration.between(checkInTime, now);

        // 轉成分鐘數來計算，精確度較高
        long totalMinutes = duration.toMinutes();

        // 【企業邏輯補強】：是否扣除中午休息 60 分鐘？
        // 假設上班超過 5 小時，通常會跨過午休，自動扣除 60 分鐘
        if (totalMinutes > 300) {
            totalMinutes -= 60;
        }

        // 將分鐘換算成小時 (例如 510 分鐘 / 60 = 8.5 小時)
        BigDecimal workHours = BigDecimal.valueOf(totalMinutes)
                .divide(BigDecimal.valueOf(60), 1, RoundingMode.HALF_UP); // 保留一位小數

        // 4. 更新紀錄
        attendance.setCheckOutTime(now);
        attendance.setWorkHours(workHours);

        // 如果總工時小於時數（例如不滿 8 小時），也可以在這裡順便改狀態為「早退」，依公司規章而定

        AttendanceRecord savedAttendance = attendanceRecordRepository.save(attendance);

        // 5. 回傳 Response DTO
        return AttendanceResponse.builder()
                .attendanceId(savedAttendance.getId())
                .checkInTime(savedAttendance.getCheckInTime())
                .checkOutTime(savedAttendance.getCheckOutTime())
                .workHours(savedAttendance.getWorkHours())
                .message(String.format("下班打卡成功！您今日總工時為 %s 小時。辛苦了！", workHours))
                .build();
    }

    @Transactional(readOnly = true) // 唯讀查詢優化
    public List<AttendanceHistoryResponse> getMyAttendance(Long employeeId, YearMonth month) {

        // 1. 驗證員工是否存在
         employeeRepository.findById(employeeId)
                .orElseThrow(() -> new LeaveException("找不到該員工，無法查詢出勤紀錄"));

        // 2. 計算該 YearMonth 的當月起迄時間區間
        LocalDate firstDayOfMonth = month.atDay(1); // 2026-07-01
        LocalDate lastDayOfMonth = month.atEndOfMonth(); // 2026-07-31

        LocalDateTime startDateTime = firstDayOfMonth.atStartOfDay(); // 2026-07-01 00:00:00
        LocalDateTime endDateTime = lastDayOfMonth.atTime(LocalTime.MAX); // 2026-07-31 23:59:59.999...

        // 3. 查詢該員工在該時間區間內的出勤紀錄 (依時間正序排列，方便前端拉成月曆或表格)
        List<AttendanceRecord> attendances = attendanceRecordRepository
                .findByEmployeeIdAndCheckInTimeBetweenOrderByCheckInTimeAsc(employeeId, startDateTime, endDateTime);

        // 4. 將 Entity 轉換為 DTO 列表
        return attendances.stream()
                .map(this::convertToHistoryDto)
                .toList();
    }

    private AttendanceHistoryResponse convertToHistoryDto(AttendanceRecord attendance) {
        return AttendanceHistoryResponse.builder()
                .attendanceId(attendance.getId())
                .workDate(attendance.getWorkDate()) // 2026-07-07
                .checkInTime(attendance.getCheckInTime())
                .checkOutTime(attendance.getCheckOutTime()) // 下班可能為 null (表示尚未下班打卡)
                .workHours(attendance.getWorkHours())       // 尚未下班時為 null
                .status(attendance.getStatus())             // NORMAL / LATE
                .build();
    }

}
