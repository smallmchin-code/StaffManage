package com.sm.leave.util;

import com.sm.leave.entity.Employee;
import com.sm.leave.exception.LeaveException;
import com.sm.leave.repository.EmployeeRepository;
import com.sm.leave.security.CustomUserDetails;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Objects;

@Component
public class  SecurityUtils {

    private final EmployeeRepository employeeRepository;

    public SecurityUtils(EmployeeRepository employeeRepository) {
        this.employeeRepository = employeeRepository;
    }

    public Employee getCurrentEmployee() {
        Object principal = Objects.requireNonNull(SecurityContextHolder.getContext().getAuthentication()).getPrincipal();
        if (principal instanceof CustomUserDetails(Employee employee)) {
            return employee;
        }
        if (principal instanceof UserDetails userDetails) {
            return employeeRepository.findByEmail(userDetails.getUsername())
                    .orElseThrow(() -> new LeaveException("找不到該使用者"));
        }
        throw new LeaveException("未登入或身分驗證失效");
    }
}