package org.example.tears.Repository;

import org.example.tears.Model.CarServiceRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

public interface CarServiceRequestRepository extends JpaRepository<CarServiceRequest,Integer> {
    List<CarServiceRequest> findByCustomerIdOrderByIdDesc(Integer customerId);

    boolean existsByAppointmentDateAndAppointmentTime(
            LocalDate appointmentDate,
            LocalTime appointmentTime
    );


    @Query("SELECT MAX(r.id) FROM CarServiceRequest r")
    Integer findMaxId();


    List<CarServiceRequest> findByAppointmentDate(LocalDate appointmentDate);

    List<CarServiceRequest> findAllByOrderByIdDesc();

    List<CarServiceRequest> findByAssignedEmployeeIdOrderByIdDesc(Integer employeeId);
    @Query("""
    SELECT ra.request FROM RequestAssignment ra
    WHERE ra.employee.id = :employeeId
      AND ra.status = 'ACTIVE'
""")
    List<CarServiceRequest> findAssignedTo(@Param("employeeId") Integer employeeId);

    @Query("""
    SELECT r.appointmentTime
    FROM CarServiceRequest r
    WHERE r.appointmentDate = :date
""")
    List<String> findBookedTimesByDate(String date);

    @Modifying
    @Query("""
            UPDATE CarServiceRequest r
            SET r.assignedEmployee = null,
                r.assignedPricingEmployee = null,
                r.currentEmployee = null
            WHERE r.assignedEmployee.id = :employeeId
               OR r.assignedPricingEmployee.id = :employeeId
               OR r.currentEmployee.id = :employeeId
            """)
    void clearEmployeeReferences(@Param("employeeId") Integer employeeId);

    List<CarServiceRequest> findByAssignedEmployeeId(Integer staffId);

    List<CarServiceRequest> findByCustomerId(Integer customerId);

    Optional<CarServiceRequest> findByInitialTransactionId(String transactionId);
}
