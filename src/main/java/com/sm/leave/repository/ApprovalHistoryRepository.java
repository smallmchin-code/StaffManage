package com.sm.leave.repository;

import com.sm.leave.entity.ApprovalHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ApprovalHistoryRepository extends JpaRepository<ApprovalHistory, Integer> {

}
