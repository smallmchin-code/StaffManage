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
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
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
    // 2. UserDetailsService + 加密器
    // ─────────────────────────────────────────────
    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    // ─────────────────────────────────────────────
    // 3. AuthenticationManager
    // ─────────────────────────────────────────────
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    // ─────────────────────────────────────────────
    // 4. CORS 設定必須在 Security 層處理
    //    原本放在 WebConfig.addCorsMappings 的設定
    //    在 Security filter 之前就被擋掉，這裡才是正確位置
    // ─────────────────────────────────────────────
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(List.of(
                "http://localhost:4200",
                "http://172.20.10.4:5173"
        ));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS", "HEAD"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true); // Session Cookie 需要這個

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

    // ─────────────────────────────────────────────
    // 5. URL 層級的權限規則
    //
    // 優先順序：由上到下，第一個匹配的規則生效
    // ─────────────────────────────────────────────
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource())) // 使用上方的 CORS Bean
                .csrf(AbstractHttpConfigurer::disable)
                .authenticationProvider(authenticationProvider())
                .authorizeHttpRequests(auth -> auth

                        // ════════════════════════════════
                        // 前端靜態資源與 SPA 路由（無須登入）
                        // ════════════════════════════════
                        .requestMatchers(
                                "/",
                                "/index.html",
                                "/register",
                                "/login",
                                "/assets/**",
                                "/static/**",
                                "/*.js",
                                "/*.css",
                                "/*.ico"
                        ).permitAll()

                        // ════════════════════════════════
                        // 公開 API 端點（無須登入）
                        // ════════════════════════════════
                        .requestMatchers(
                                "/api/auth/login",
                                "/api/auth/register"
                        ).permitAll()

                        // ════════════════════════════════
                        // ADMIN 限定
                        // ════════════════════════════════
                        .requestMatchers(
                                "/api/admin/**",
                                "/api/auth/admin/**"
                        ).hasRole("ADMIN")

                        // ════════════════════════════════
                        // ADMIN 或 MANAGER 限定
                        // ════════════════════════════════
//                        .requestMatchers(
//                                "/api/leave/approval",
//                                "/api/leave/pending"
//                        ).hasAnyRole("ADMIN", "MANAGER")

                        // ════════════════════════════════
                        // 所有已登入角色（ADMIN / MANAGER / EMPLOYEE）
                        // ════════════════════════════════
                        .requestMatchers(
                                "/api/auth/me",
                                "/api/auth/logout",
                                "/api/auth/updatePassword",
                                "/api/auth/resignation"
                        ).hasAnyRole("ADMIN", "MANAGER", "EMPLOYEE")

                        .requestMatchers("/api/leave/**")
                        .hasAnyRole("ADMIN", "MANAGER", "EMPLOYEE")

                        .requestMatchers("/api/attendance/**")
                        .hasAnyRole("ADMIN", "MANAGER", "EMPLOYEE")

                        .requestMatchers("/api/notifications/**")
                        .hasAnyRole("ADMIN", "MANAGER", "EMPLOYEE")

                        // ════════════════════════════════
                        // 其餘全部擋掉
                        // ════════════════════════════════
                        .anyRequest().denyAll()
                )
                .sessionManagement(session -> session
                        .maximumSessions(1)
                );

        return http.build();
    }
}