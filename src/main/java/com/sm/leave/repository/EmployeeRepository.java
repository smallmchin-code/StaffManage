package com.sm.leave.repository;

import com.sm.leave.entity.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Long> {

    Optional<Employee> findByEmployeeNo(String employeeNo);

    boolean existsByEmail(String email);

    Optional<String> findMaxEmployeeNo();
}
