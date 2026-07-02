package com.sm.leave.dto.response;

import com.sm.leave.entity.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateEmployeeResponse {

    private Long id;
    private String employeeNo;
    private String name;
    private String email;
    private String status;
    private Role role;
    private String message;


}
