package com.sm.leave.controller;

import com.sm.leave.dto.request.CreateEmployeeRequest;
import com.sm.leave.dto.request.LoginRequest;
import com.sm.leave.dto.response.ApiResponse;
import com.sm.leave.dto.response.CreateEmployeeResponse;
import com.sm.leave.dto.response.LoginResponse;
import com.sm.leave.entity.Employee;
import com.sm.leave.repository.EmployeeRepository;
import com.sm.leave.service.EmployeeService;
import com.sm.leave.util.SecurityUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class  AuthController {

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    private final EmployeeRepository    employeeRepository;
    private final EmployeeService       employeeService;
    private final AuthenticationManager authenticationManager;
    private final SecurityUtils         securityUtils;

    private final SecurityContextRepository securityContextRepository =
            new HttpSessionSecurityContextRepository();

    // ─────────────────────────────────────────────
    // 註冊
    // ─────────────────────────────────────────────
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<CreateEmployeeResponse>> register(
            @Valid @RequestBody CreateEmployeeRequest request) {

        logger.info("POST /api/auth/register 開始 - Email: {}, 姓名: {}", request.email(), request.name());

        CreateEmployeeResponse response = employeeService.createEmployee(request);

        logger.info("POST /api/auth/register 完成 - 新員工工號: {}", response.employeeNo());
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    // ─────────────────────────────────────────────
    // 登入
    // ─────────────────────────────────────────────
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<?>> login(
            @Valid @RequestBody LoginRequest loginRequest,
            HttpServletRequest request,
            HttpServletResponse response) {

        String email    = loginRequest.email();
        String password = loginRequest.password();

        logger.info("POST /api/auth/login 開始 - Email: {}", email);

        try {
            Authentication auth = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(email, password)
            );

            SecurityContext context = SecurityContextHolder.createEmptyContext();
            context.setAuthentication(auth);
            SecurityContextHolder.setContext(context);
            securityContextRepository.saveContext(context, request, response);

            Employee employee = employeeRepository.findByEmail(email)
                    .orElseThrow(() -> new IllegalStateException("使用者資料不存在"));

            LoginResponse loginResponse = LoginResponse.builder()
                    .employeeNo(employee.getEmployeeNo())
                    .name(employee.getName())
                    .roleCode(employee.getRole().getCode())
                    .build();

            logger.info("POST /api/auth/login 完成 - 員工: {}({}) 登入成功，角色: {}",
                    employee.getName(), employee.getEmployeeNo(), employee.getRole().getCode());

            return ResponseEntity.ok(ApiResponse.success(loginResponse));

        } catch (DisabledException e) {
            logger.warn("POST /api/auth/login 失敗 - 帳號已停用: {}", email);
            return ResponseEntity.ok(ApiResponse.error("帳號已停用 (disabled)", "403"));

        } catch (BadCredentialsException e) {
            logger.warn("POST /api/auth/login 失敗 - 帳號或密碼錯誤: {}", email);
            return ResponseEntity.ok(ApiResponse.error("帳號或密碼錯誤", "401"));
        }
    }

    // ─────────────────────────────────────────────
    // 登出
    // ─────────────────────────────────────────────
    @PostMapping("/logout")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Void>> logout(HttpServletRequest request) {

        String employeeName = "unknown";
        try {
            employeeName = securityUtils.getCurrentEmployee().getName();
        } catch (Exception ignored) {}

        logger.info("POST /api/auth/logout 開始 - 員工: {}", employeeName);

        // 清除 Spring Security Context
        SecurityContextHolder.clearContext();

        // 讓 HTTP Session 失效，清除伺服器端的 Session 資料
        HttpSession session = request.getSession(false); // false = 不建立新 Session
        if (session != null) {
            session.invalidate();
        }

        logger.info("POST /api/auth/logout 完成 - 員工: {} 已登出", employeeName);
        return ResponseEntity.ok(ApiResponse.success("登出成功"));
    }

    // ─────────────────────────────────────────────
    // 取得當前登入者資訊
    // ─────────────────────────────────────────────
    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<LoginResponse>> me() {

        logger.info("GET /api/auth/me 開始");

        Employee user = securityUtils.getCurrentEmployee();

        LoginResponse loginResponse = LoginResponse.builder()
                .name(user.getName())
                .roleCode(user.getRole().getCode())
                .employeeNo(user.getEmployeeNo())
                .build();

        logger.info("GET /api/auth/me 完成 - 員工: {}({})", user.getName(), user.getEmployeeNo());
        return ResponseEntity.ok(ApiResponse.success(loginResponse));
    }
}