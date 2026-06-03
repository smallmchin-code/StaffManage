package com.sm.leave.repository;

import com.sm.leave.entity.Employee;
import com.sm.leave.entity.LeaveRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface LeaveRequestRepository extends JpaRepository<LeaveRequest, Integer> {
	
	List<LeaveRequest> findByEmployeeId(Long employeeId);
	boolean existsByEmployeeAndStatusInAndStartDateLessThanEqualAndEndDateGreaterThanEqual(
	                                                                                       Employee employee, 
	                                                                                       List<String> statuses, 
	                                                                                       LocalDate endDate, 
	                                                                                       LocalDate startDate
	                                                                               );
}
