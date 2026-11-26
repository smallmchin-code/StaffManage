package com.sm.leave.controller;

import com.sm.leave.dto.request.ResignationRequest;
import com.sm.leave.dto.request.UpdatePasswordRequest;
import com.sm.leave.dto.response.ApiResponse;
import com.sm.leave.dto.response.EmployeeStatusResponse;
import com.sm.leave.entity.Employee;
import com.sm.leave.service.EmployeeService;
import com.sm.leave.util.SecurityUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/employee")
@RequiredArgsConstructor
public class EmployeeController {

    private static final Logger logger = LoggerFactory.getLogger(EmployeeController.class);

    private final EmployeeService employeeService;
    private final SecurityUtils securityUtils;

    @PostMapping("/updatePassword")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<?>> updatePassword(@Valid @RequestBody UpdatePasswordRequest request){
        Employee employee = securityUtils.getCurrentEmployee();

        logger.info("POST /api/employee/updatePassword 開始 - Email: {}", employee.getEmail());

        employeeService.updatePassword(employee.getEmail(),
                request.oldPassword(),
                request.newPassword());

        logger.info("POST /api/employee/updatePassword 完成 - Email: {}", employee.getEmail());
        return ResponseEntity.ok(ApiResponse.success("密碼更改成功"));
    }

    @PostMapping("/admin/updatePassword")
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    public ResponseEntity<ApiResponse<?>> resetPasswordByAdmin(@RequestParam String newPassword){
        Employee employee = securityUtils.getCurrentEmployee();

        logger.info("POST /api/employee/admin/updatePassword 開始 - 操作者 Email: {}", employee.getEmail());

        employeeService.resetPasswordByAdmin(employee.getEmail(),
                newPassword);

        logger.info("POST /api/employee/admin/updatePassword 完成 - 操作者 Email: {}", employee.getEmail());
        return ResponseEntity.ok(ApiResponse.success("密碼更改成功"));
    }

    @PostMapping("/resignation")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<EmployeeStatusResponse>> resignation (@Valid @RequestBody ResignationRequest request){
        Employee operator = securityUtils.getCurrentEmployee();

        logger.info("POST /api/employee/resignation 開始 - 操作者 ID: {}, 目標員工 ID: {}", operator.getId(), request.employeeId());

        EmployeeStatusResponse response = employeeService.updateEmployeeStatusToInactive(request.employeeId(), operator.getId(), request.reason());

        logger.info("POST /api/employee/resignation 完成 - 目標員工 ID: {}", request.employeeId());
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}