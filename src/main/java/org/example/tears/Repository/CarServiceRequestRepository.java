package org.example.tears.Repository;

import org.example.tears.Enums.CustomerRequestStatus;
import org.example.tears.Enums.StaffRequestStatus;
import org.example.tears.Model.CarServiceRequest;
import org.example.tears.Model.Employee;
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

    List<CarServiceRequest> findByAssignedEmployeeAndStaffStatus(
            Employee employee,
            StaffRequestStatus status
    );

    List<CarServiceRequest> findByAssignedEmployeeAndOrderNumberContainingIgnoreCase(
            Employee employee,
            String orderNumber
    );

    Optional<CarServiceRequest> findById(Integer id);

    List<CarServiceRequest> findByAssignedEmployee_Id(Integer employeeId);
    long countByCustomerIdAndCustomerStatus(Integer customerId, CustomerRequestStatus status);
    long countByAssignedEmployee_Id(Integer employeeId);

    long countByAssignedEmployee_IdAndStaffStatus(Integer employeeId, StaffRequestStatus status);

    boolean existsByAppointmentDateAndAppointmentTime(
            LocalDate appointmentDate,
            LocalTime appointmentTime
    );

    @Query("""
SELECT r
FROM CarServiceRequest r
JOIN r.car c
WHERE
(:orderNumber IS NULL OR r.orderNumber LIKE CONCAT(:orderNumber, '%'))
AND
(:plateArabic IS NULL OR c.plateNumberArabic LIKE CONCAT(:plateArabic, '%'))
AND
(:plateEnglish IS NULL OR c.plateNumberEnglish LIKE CONCAT(:plateEnglish, '%'))
""")
    List<CarServiceRequest> search(
            @Param("orderNumber") String orderNumber,
            @Param("plateArabic") String plateArabic,
            @Param("plateEnglish") String plateEnglish
    );


    List<CarServiceRequest> findByAssignedEmployee (Employee emp);
    List<CarServiceRequest> findByAssignedEmployeeIsNull();


    @Query("""
SELECT r FROM CarServiceRequest r
WHERE r.assignedEmployee IS NULL
""")
    List<CarServiceRequest> findUnassignedRequests();



    List<CarServiceRequest>
    findByCustomerIdAndCustomerStatusInOrderByIdDesc(
            Integer customerId,
            List<CustomerRequestStatus> statuses
    );

    List<CarServiceRequest>
    findByCustomerIdAndCustomerStatusNotInOrderByIdDesc(
            Integer customerId,
            List<CustomerRequestStatus> statuses
    );

    @Query("SELECT MAX(r.id) FROM CarServiceRequest r")
    Integer findMaxId();


    List<CarServiceRequest> findByAppointmentDate(LocalDate appointmentDate);

    List<CarServiceRequest> findAllByOrderByIdDesc();

    @Query("""
SELECT r.appointmentTime
FROM CarServiceRequest r
WHERE r.appointmentDate = :date
""")
    List<LocalTime> findBookedTimesByDate(
            @Param("date") LocalDate date
    );

}
