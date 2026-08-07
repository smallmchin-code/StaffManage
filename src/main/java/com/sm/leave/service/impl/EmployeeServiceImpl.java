package com.sm.leave.service.impl;

import com.sm.leave.dto.request.CreateEmployeeRequest;
import com.sm.leave.dto.response.CreateEmployeeResponse;
import com.sm.leave.dto.response.EmployeeStatusResponse;
import com.sm.leave.entity.Department;
import com.sm.leave.entity.LeaveRequest;
import com.sm.leave.entity.Role;
import com.sm.leave.exception.LeaveException;
import com.sm.leave.repository.DepartmentRepository;
import com.sm.leave.repository.EmployeeRepository;
import com.sm.leave.repository.LeaveRequestRepository;
import com.sm.leave.repository.RoleRepository;
import com.sm.leave.service.EmployeeService;
import com.sm.leave.entity.Employee;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EmployeeServiceImpl implements EmployeeService {

    private final DepartmentRepository departmentRepository;
    private final EmployeeRepository employeeRepository;
    private final RoleRepository roleRepository;
    private final LeaveRequestRepository leaveRequestRepository;
    private final PasswordEncoder passwordEncoder; // 注入 Spring Security 的加密器


    @Transactional
    public CreateEmployeeResponse createEmployee(CreateEmployeeRequest request) {
        // 1. 驗證 Email 是否重複
        if (employeeRepository.existsByEmail(request.email())) {
            throw new LeaveException("該 Email 已經被註冊過");
        }

        Department department = departmentRepository.findByName(request.departmentName())
                .orElseThrow(() -> new LeaveException("該部門不存在"));

        Role role = roleRepository.findById(request.roleId())
                .orElseThrow(() ->
                        new LeaveException("角色不存在"));

        // 2. 執行緒安全的自動生成員工編號 (EMP00001, EMP00002...)
        String nextEmployeeNo = generateNextEmployeeNo();

        // 3. 密碼加密
        String encodedPassword = passwordEncoder.encode(request.password());



        // 4. 建立員工 Entity (設定預設狀態與角色)
        Employee employee = Employee.builder()
                .employeeNo(nextEmployeeNo)
                .name(request.name())
                .email(request.email())
                .password(encodedPassword)
                .status("ACTIVE")       // 預設為在職
                .role(role) // 預設角色
                .department(department)
                .build();

        // 5. 存進 DB
        Employee savedEmployee = employeeRepository.save(employee);

        log.info("成功建立新員工！工號: {}, 姓名: {}", nextEmployeeNo, savedEmployee.getName());

        // 6. 回傳 Response DTO (千萬不要把加密後的密碼回傳給前端！)
        return CreateEmployeeResponse.builder()
                .id(savedEmployee.getId())
                .employeeNo(savedEmployee.getEmployeeNo())
                .name(savedEmployee.getName())
                .email(savedEmployee.getEmail())
                .status(savedEmployee.getStatus())
                .roleCode(savedEmployee.getRole().getCode())
                .departmentName(savedEmployee.getDepartment().getName())
                .message("員工帳號建立成功")
                .build();
    }

    @Transactional
    public void updatePassword(String email, String oldPassword, String newPassword) {
        Employee employee = findByEmail(email);

        // 驗證舊密碼是否正確
        if (!passwordEncoder.matches(oldPassword, employee.getPassword())) {
            throw new LeaveException("舊密碼輸入錯誤");
        }

        // 密碼加密並儲存
        employee.setPassword(passwordEncoder.encode(newPassword));
        employeeRepository.save(employee);
    }

    @Transactional
    public void resetPasswordByAdmin(String email, String tempPassword) {
        // 管理員強制重設密碼（不需驗證舊密碼）
        Employee employee = findByEmail(email);

        employee.setPassword(passwordEncoder.encode(tempPassword));
        employeeRepository.save(employee);
    }

    @Transactional
    public EmployeeStatusResponse updateEmployeeStatusToInactive(Long employeeId, Long operatorId, String reason) {

        // 1. 驗證操作者（HR/主管）是否存在與角色
        Employee operator = employeeRepository.findById(operatorId)
                .orElseThrow(() -> new LeaveException("找不到操作人員資訊"));

        // 假設只有  SYSTEM 級別可以辦理離職
        if (!"ADMIN".equalsIgnoreCase(operator.getRole().getCode())) {
            throw new LeaveException("權限不足：只有系統管理員可以辦理員工離職");
        }

        // 2. 驗證被操作的員工是否存在
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new LeaveException("找不到該員工，無法辦理離職"));

        // 安全把關：禁止自己幫自己辦離職
        if (employeeId.equals(operatorId)) {
            throw new LeaveException("違反安全規範：您無法為自己辦理離職處理");
        }

        // 3. 檢查是否已經是離職狀態
        if ("INACTIVE".equalsIgnoreCase(employee.getStatus())) {
            throw new LeaveException("該員工已經是離職(INACTIVE)狀態，請勿重複操作");
        }

        // 4. 【核心連動】清理該員工所有 PENDING (審核中) 的假單
        List<LeaveRequest> pendingLeaves = leaveRequestRepository.findByEmployeeIdAndStatus(employeeId, "PENDING");
        if (!pendingLeaves.isEmpty()) {
            log.info("將自動作廢員工(工號: {})的 {} 筆待審核假單", employee.getEmployeeNo(), pendingLeaves.size());
            for (LeaveRequest lr : pendingLeaves) {
                lr.setStatus("CANCELLED");
                // 這裡可以直接加上備註原因
                lr.setRejectReason("系統自動作廢：員工已辦理離職");
            }
            // 批次儲存更新後的假單狀態
            leaveRequestRepository.saveAll(pendingLeaves);
        }

        // 5. 變更員工狀態為離職
        employee.setStatus("INACTIVE");
        Employee updatedEmployee = employeeRepository.save(employee);

        log.info("員工離職手續辦理成功！操作者: {}, 被離職員工工號: {}, 姓名: {}, 原因: {}",
                operator.getName(), updatedEmployee.getEmployeeNo(), updatedEmployee.getName(), reason);

        // 6. 包裝 Response DTO 回傳
        return EmployeeStatusResponse.builder()
                .employeeId(updatedEmployee.getId())
                .employeeNo(updatedEmployee.getEmployeeNo())
                .name(updatedEmployee.getName())
                .status(updatedEmployee.getStatus()) // 回傳 INACTIVE
                .message("員工離職手續辦理完成，已自動作廢其所有待審核假單")
                .build();
    }

    /**
     * 透過 DB 悲觀鎖產生下一個員工編號。
     * 流程：
     *   1. SELECT employeeNo FROM employees ORDER BY employeeNo DESC LIMIT 1 FOR UPDATE
     *   2. 解析數字部分 + 1，重新格式化
     *   3. 若 DB 無任何員工，從 EMP00001 開始
     * 為何不用 synchronized：
     *   synchronized 只鎖 JVM，多台 server 或連線池多 thread 仍有衝突風險。
     *   FOR UPDATE 在 DB 層保證序列化，Transaction commit 後鎖才釋放。
     */
    private String generateNextEmployeeNo() {
        return employeeRepository.findMaxEmployeeNoForUpdate()
                .map(maxNo -> {
                    // maxNo 格式固定為 "EMP00002"，取 index 3 之後的數字字串
                    String numberStr = maxNo.substring(3);
                    int nextNumber = Integer.parseInt(numberStr) + 1;
                    return String.format("EMP%05d", nextNumber);
                })
                .orElse("EMP00001");
    }// 如果資料庫完全沒資料，就從 EMP00001 開始

    private Employee findByEmail(String email) {
        return employeeRepository.findByEmail(email)
                .orElseThrow(() -> new LeaveException("找不到工號為 " + email + " 的員工"));
    }

}
