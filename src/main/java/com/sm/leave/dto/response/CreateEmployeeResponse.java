package com.sm.leave.dto.response;

import com.sm.leave.entity.Role;
import lombok.Builder;

@Builder
public record CreateEmployeeResponse(Long   id,
                                     String employeeNo,
                                     String name,
                                     String email,
                                     String status,
                                     String roleCode ,
                                     String message) {


}
