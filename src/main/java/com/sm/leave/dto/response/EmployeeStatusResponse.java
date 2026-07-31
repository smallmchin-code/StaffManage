package com.sm.leave.dto.response;

import lombok.Builder;

@Builder
public record EmployeeStatusResponse(
         Long employeeId,
         String employeeNo,
         String name,
         String status, // INACTIVE
         String message) {

}
