package com.sm.leave.security;

import com.sm.leave.entity.Employee;
import com.sm.leave.repository.EmployeeRepository;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NullMarked;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final EmployeeRepository employeeRepository;

    /**
     * Spring Security 登入流程會自動呼叫這個 method。
     * username 在這個專案對應的是 Employee 的 email。
     * 回傳的 UserDetails 包含：
     *   - email（作為 username）
     *   - Bcrypt 密碼（Spring Security 會自行比對，不需要我們手動比）
     *   - 角色（格式必須是 "ROLE_" 前綴，例如 ROLE_ADMIN）
     *   - 帳號是否啟用（對應 employee.status）
     */
    @NullMarked
    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(@NonNull String email) throws UsernameNotFoundException {

        Employee employee = employeeRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("找不到此 Email 的使用者：" + email));

        // Spring Security 的角色慣例：必須加上 "ROLE_" 前綴
        // ADMIN → ROLE_ADMIN, MANAGER → ROLE_MANAGER, EMPLOYEE → ROLE_EMPLOYEE
        String roleCode = employee.getRole().getCode(); // "ADMIN" / "MANAGER" / "EMPLOYEE"
        SimpleGrantedAuthority authority = new SimpleGrantedAuthority("ROLE_" + roleCode);

        return User.builder()
                .username(employee.getEmail())
                .password(employee.getPassword())           // 已是 Bcrypt，Spring 會自動比對
                .authorities(List.of(authority))
                .accountExpired(false)
                .accountLocked(false)
                .credentialsExpired(false)
                .disabled("INACTIVE".equalsIgnoreCase(employee.getStatus())) // 離職員工登不進來
                .build();
    }
}