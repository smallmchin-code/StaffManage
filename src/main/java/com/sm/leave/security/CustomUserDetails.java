package com.sm.leave.security;

import com.sm.leave.entity.Employee;
import org.jspecify.annotations.NullMarked;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

/**
 * @param employee 保持純淨的 Employee 參照
 */
public record CustomUserDetails(Employee employee) implements UserDetails {

    // Spring Security 的角色慣例：必須加上 "ROLE_" 前綴
    // ADMIN → ROLE_ADMIN, MANAGER → ROLE_MANAGER, EMPLOYEE → ROLE_EMPLOYEE
    @NullMarked
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        String roleCode = employee.getRole().getCode();
        return List.of(new SimpleGrantedAuthority("ROLE_" + roleCode));
    }

    @Override
    public String getPassword() {
        return employee.getPassword();
    }

    @NullMarked
    @Override
    public String getUsername() {
        return employee.getEmail();
    }

    @NullMarked
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @NullMarked
    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @NullMarked
    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return !"INACTIVE".equalsIgnoreCase(employee.getStatus());
    }
}