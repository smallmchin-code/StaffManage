package com.sm.leave.controller;

import com.sm.leave.dto.request.CreateEmployeeRequest;
import com.sm.leave.dto.request.LoginRequest;
import com.sm.leave.dto.response.ApiResponse;
import com.sm.leave.dto.response.CreateEmployeeResponse;
import com.sm.leave.dto.response.LoginResponse;
import com.sm.leave.entity.Employee;
import com.sm.leave.repository.EmployeeRepository;
import com.sm.leave.service.EmployeeService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    private final EmployeeRepository employeeRepository;
    private final EmployeeService employeeService;
    private final AuthenticationManager authenticationManager;

    private final SecurityContextRepository securityContextRepository =
            new HttpSessionSecurityContextRepository();

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<CreateEmployeeResponse>> register(@Valid @RequestBody CreateEmployeeRequest request){
         return ResponseEntity.ok(ApiResponse.success(employeeService.createEmployee(request)));

    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<?>> login(
            @Valid @RequestBody LoginRequest loginRequest,
            HttpServletRequest request,
            HttpServletResponse response) {

        String email    = loginRequest.email();
        String password = loginRequest.password();
        try {
            Authentication auth = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(email, password)
            );

            // Spring Security 6：需明確將 SecurityContext 寫入 HTTP Session
            SecurityContext context = SecurityContextHolder.createEmptyContext();
            context.setAuthentication(auth);
            SecurityContextHolder.setContext(context);
            securityContextRepository.saveContext(context, request, response);

            Employee employee = employeeRepository.findByEmail(email)
                    .orElseThrow(() -> new IllegalStateException("使用者資料不存在"));

            logger.info("使用者登入成功: {}", employee.getName());

            LoginResponse loginResponse = LoginResponse.builder()
                    .employeeNo(employee.getEmployeeNo())
                    .name(employee.getName())
                    .roleCode(employee.getRole().getCode())
                    .build();

            return ResponseEntity.ok(ApiResponse.success(loginResponse));

        } catch (DisabledException e) {
            logger.warn("帳號已停用: {}", email);
            return ResponseEntity.ok(ApiResponse.error("帳號已停用 (disabled)", "403"));
        } catch (BadCredentialsException e) {
            logger.warn("帳號或密碼錯誤: {}", email);
            return ResponseEntity.ok(ApiResponse.error("message", "帳號或密碼錯誤"));
        }
    }

    @GetMapping("/me")
    public ResponseEntity<LoginResponse> me(@AuthenticationPrincipal UserDetails userDetails){
        if (userDetails == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();

        Employee user = employeeRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new IllegalStateException("使用者資料不存在"));


        return ResponseEntity.ok(
                LoginResponse.builder()
                        .name(user.getName())
                        .roleCode(user.getRole().getCode())
                        .employeeNo(user.getEmployeeNo())
                        .build()
        );
    }
}
