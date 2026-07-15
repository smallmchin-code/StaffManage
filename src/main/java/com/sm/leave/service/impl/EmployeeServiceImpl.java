package com.sm.leave.service.impl;

import com.sm.leave.dto.request.CreateEmployeeRequest;
import com.sm.leave.dto.response.CreateEmployeeResponse;
import com.sm.leave.entity.Role;
import com.sm.leave.exception.LeaveException;
import com.sm.leave.repository.EmployeeRepository;
import com.sm.leave.repository.RoleRepository;
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
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder; // 注入 Spring Security 的加密器

    public EmployeeServiceImpl(EmployeeRepository employeeRepository,
                               RoleRepository roleRepository,
                               PasswordEncoder passwordEncoder) {
        this.employeeRepository = employeeRepository;
        this.roleRepository =  roleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public CreateEmployeeResponse createEmployee(CreateEmployeeRequest request) {
        // 1. 驗證 Email 是否重複
        if (employeeRepository.existsByEmail(request.email())) {
            throw new LeaveException("該 Email 已經被註冊過");
        }

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
                .message("員工帳號建立成功")
                .build();
    }

    /**
     * 透過 DB 悲觀鎖產生下一個員工編號。
     *
     * 流程：
     *   1. SELECT employeeNo FROM employees ORDER BY employeeNo DESC LIMIT 1 FOR UPDATE
     *   2. 解析數字部分 + 1，重新格式化
     *   3. 若 DB 無任何員工，從 EMP00001 開始
     *
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



}
