package org.example.tears.Repository;

import org.example.tears.Model.CarServiceRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CarServiceRequestRepository extends JpaRepository<CarServiceRequest,Integer> {
    List<CarServiceRequest> findByCustomerIdOrderByIdDesc(Integer customerId);

    Integer countByAppointmentDateAndAppointmentTime(String date, String time);

    List<CarServiceRequest> findAllByOrderByIdDesc();

    List<CarServiceRequest> findByAssignedEmployeeIdOrderByIdDesc(Integer employeeId);
    @Query("""
    SELECT ra.request FROM RequestAssignment ra
    WHERE ra.employee.id = :employeeId
      AND ra.status = 'ACTIVE'
""")
    List<CarServiceRequest> findAssignedTo(@Param("employeeId") Integer employeeId);



    List<CarServiceRequest> findByAssignedEmployeeId(Integer staffId);

    List<CarServiceRequest> findByCustomerId(Integer customerId);

}
