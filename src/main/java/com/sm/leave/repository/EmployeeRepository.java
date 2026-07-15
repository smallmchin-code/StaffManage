package com.sm.leave.repository;

import com.sm.leave.entity.Employee;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Long> {

    Optional<Employee> findByEmployeeNo(String employeeNo);

    boolean existsByEmail(String email);


    @EntityGraph(attributePaths = {"role"})
    Optional<Employee> findByEmail(String email);

    /**
     * 取得目前 DB 中最大的員工編號。
     * FOR UPDATE：在同一個 Transaction 內對這筆資料加行鎖，
     * 確保其他 Transaction 必須等待，避免同時產生相同編號。
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT e.employeeNo FROM Employee e ORDER BY e.employeeNo DESC LIMIT 1 ")
    Optional<String> findMaxEmployeeNoForUpdate();
}
