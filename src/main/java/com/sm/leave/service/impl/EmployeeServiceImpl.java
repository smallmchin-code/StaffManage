package com.sm.leave.service.impl;

import com.sm.leave.dto.request.CreateEmployeeRequest;
import com.sm.leave.dto.response.CreateEmployeeResponse;
import com.sm.leave.exception.LeaveException;
import com.sm.leave.repository.EmployeeRepository;
import com.sm.leave.service.EmployeeService;
import com.sm.leave.entity.Employee;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.crypto.password.PasswordEncoder;

@Slf4j
@Service
@Transactional(readOnly = true)
public class EmployeeServiceImpl implements EmployeeService {

    private final EmployeeRepository employeeRepository;
    private final PasswordEncoder passwordEncoder; // 注入 Spring Security 的加密器

    public EmployeeServiceImpl(EmployeeRepository employeeRepository,
                               PasswordEncoder passwordEncoder) {
        this.employeeRepository = employeeRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public CreateEmployeeResponse createEmployee(CreateEmployeeRequest request) {
        // 1. 驗證 Email 是否重複
        if (employeeRepository.existsByEmail(request.getEmail())) {
            throw new LeaveException("該 Email 已經被註冊過");
        }

        // 2. 執行緒安全的自動生成員工編號 (EMP00001, EMP00002...)
        String nextEmployeeNo = generateNextEmployeeNo();

        // 3. 密碼加密
        String encodedPassword = passwordEncoder.encode(request.getPassword());

        // 4. 建立員工 Entity (設定預設狀態與角色)
        Employee employee = Employee.builder()
                .employeeNo(nextEmployeeNo)
                .name(request.getName())
                .email(request.getEmail())
                .password(encodedPassword)
                .status("ACTIVE")       // 預設為在職
                .role( request.getRole()) // 預設角色
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
                .role(savedEmployee.getRole())
                .message("員工帳號建立成功")
                .build();
    }

    /**
     * 生成下一個員工編號的私有方法
     */
    private synchronized String generateNextEmployeeNo() {
        // 從資料庫撈出目前最大的員工編號 (例如 "EMP00002")
        // 這裡加上 synchronized 可以防止單一伺服器內部的同時點擊衝突
        return employeeRepository.findMaxEmployeeNo()
                .map(maxNo -> {
                    // 拔掉 "EMP"，剩下 "00002"
                    String numberStr = maxNo.substring(3);
                    // 轉成數字 + 1 -> 3
                    int nextNumber = Integer.parseInt(numberStr) + 1;
                    // 重新格式化回 EMP00003
                    return String.format("EMP%05d", nextNumber);
                })
                .orElse("EMP00001"); // 如果資料庫完全沒資料，就從 EMP00001 開始
    }

}
