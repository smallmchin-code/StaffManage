package com.sm.leave.dto.request;

import com.sm.leave.entity.Role;
import lombok.*;

import java.time.LocalDate;

public record CreateEmployeeRequest(String name,

                                    Long roleId,

                                    String email,

                                    String password,

                                    LocalDate hireDate) {


}
