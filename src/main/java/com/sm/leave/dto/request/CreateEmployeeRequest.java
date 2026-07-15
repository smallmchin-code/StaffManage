package com.sm.leave.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.time.LocalDate;

public record CreateEmployeeRequest(@NotBlank(message = "名字不能為空") String name,

                                    @NonNull Long roleId,

                                    @NotBlank(message = "信箱不能為空") String email,

                                    @NotBlank(message = "密碼不能為空") String password,

                                    @NotBlank(message = "入職時間不能為空") LocalDate hireDate) {


}
