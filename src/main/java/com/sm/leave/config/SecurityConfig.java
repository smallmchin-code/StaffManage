package com.sm.leave.config;

import com.sm.leave.security.UserDetailsServiceImpl;
import lombok.RequiredArgsConstructor;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity          // 啟用 @PreAuthorize 等 Method 層級的權限控管
@RequiredArgsConstructor
public class SecurityConfig {

    private final UserDetailsServiceImpl userDetailsService;

    // ─────────────────────────────────────────────
    // 1. 密碼加密器
    // ─────────────────────────────────────────────
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // ─────────────────────────────────────────────
    // 2. 告訴 Spring Security 用哪個 UserDetailsService + 哪個加密器
    // ─────────────────────────────────────────────
    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    // ─────────────────────────────────────────────
    // 3. AuthenticationManager（Controller 的 login 方法需要注入這個）
    // ─────────────────────────────────────────────
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    // ─────────────────────────────────────────────
    // 4. URL 層級的權限規則
    // ─────────────────────────────────────────────
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)   // REST API 不需要 CSRF（若有前端 form 提交請斟酌）
                .authenticationProvider(authenticationProvider())
                .authorizeHttpRequests(auth -> auth

                        // ── 公開端點（不需登入）──
                        .requestMatchers("/api/auth/login", "/api/auth/register").permitAll()

                        // ── 只有 ADMIN 可以進來 ──
                        .requestMatchers("/api/admin/**").hasRole("ADMIN")

                        // ── ADMIN 或 MANAGER 可以進來（例如審核假單）──
                        .requestMatchers("/api/manager/**").hasAnyRole("ADMIN", "MANAGER")

                        // ── 所有登入過的人都可以（ADMIN、MANAGER、EMPLOYEE 三種角色）──
                        // hasAnyRole("ADMIN","MANAGER","EMPLOYEE") 等同於 authenticated()，
                        .requestMatchers("/api/leave/**").hasAnyRole("ADMIN", "MANAGER", "EMPLOYEE")

                        // ── 其餘全部擋掉 ──
                        .anyRequest().denyAll()
                )
                .sessionManagement(session -> session
                        // Session 由 Spring Security 管理（AuthController 的 login 會存 Session）
                        .maximumSessions(1)   // 同一帳號最多一個 Session（可依需求移除）
                );

        return http.build();
    }
}