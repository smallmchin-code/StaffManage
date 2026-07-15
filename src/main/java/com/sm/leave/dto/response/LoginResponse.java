package com.sm.leave.dto.response;

import lombok.Builder;

@Builder
public record LoginResponse(String employeeNo,
                            String name,
                            String roleCode) {
}
