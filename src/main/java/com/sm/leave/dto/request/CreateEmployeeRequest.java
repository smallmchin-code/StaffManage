package com.sm.leave.dto.request;

import com.sm.leave.entity.Role;
import lombok.*;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Getter
public class CreateEmployeeRequest {
    private String name;

    private Role role;

    private String email;

    private String password;

    private LocalDate hireDate;

}
