package com.sm.leave.repository;

import com.sm.leave.entity.Employee;
import com.sm.leave.entity.LeaveRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface LeaveRequestRepository extends JpaRepository<LeaveRequest, Long> {
	
	List<LeaveRequest> findByEmployeeId(Long employeeId);
	boolean existsByEmployeeAndStatusInAndStartDateLessThanEqualAndEndDateGreaterThanEqual(
	                                                                                       Employee employee, 
	                                                                                       List<String> statuses, 
	                                                                                       LocalDate endDate, 
	                                                                                       LocalDate startDate
	                                                                               );

	List<LeaveRequest> findByEmployeeIdOrderByIdDesc(Long EmployeeId);

	@Query("SELECT lr FROM LeaveRequest lr " +
			"JOIN FETCH lr.employee e " +      // 一併撈出員工資料
			"JOIN FETCH lr.leaveType lt " +    // 一併撈出假別資料
			"WHERE e.manager.id = :managerId " + // 限制只能是該主管的下屬
			"AND lr.status = 'PENDING' " +       // 只抓待審核
			"ORDER BY lr.id DESC")               // 最新申請的排最上面
	List<LeaveRequest> findPendingLeavesByManagerId(@Param("managerId") Long managerId);
}
