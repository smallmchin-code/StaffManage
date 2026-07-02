package com.sm.leave.service;

import com.sm.leave.dto.request.CreateEmployeeRequest;
import com.sm.leave.dto.response.CreateEmployeeResponse;

public interface EmployeeService {
    CreateEmployeeResponse createEmployee(CreateEmployeeRequest request);
}
