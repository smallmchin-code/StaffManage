package com.sm.leave.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.time.LocalDate;

public record CreateEmployeeRequest(@NotBlank(message = "名字不能為空") String name,

                                    @NotBlank(message = "部門不能為空") String departmentName,

                                    @NonNull Long roleId,

                                    @NotBlank(message = "信箱不能為空") String email,

                                    @NotBlank(message = "密碼不能為空" ) @Min(8) String password,

                                    @NotBlank(message = "入職時間不能為空") LocalDate hireDate) {


}
