package com.sm.leave.service;

import com.sm.leave.dto.request.CreateEmployeeRequest;
import com.sm.leave.dto.response.CreateEmployeeResponse;
import com.sm.leave.dto.response.EmployeeStatusResponse;

public interface EmployeeService {
    CreateEmployeeResponse createEmployee(CreateEmployeeRequest request);
    void updatePassword(String email, String oldPassword, String newPassword);
    void resetPasswordByAdmin(String email, String tempPassword);
    EmployeeStatusResponse updateEmployeeStatusToInactive(Long employeeId, Long operatorId, String reason);
}
