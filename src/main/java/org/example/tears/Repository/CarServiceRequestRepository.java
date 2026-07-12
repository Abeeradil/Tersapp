package org.example.tears.Repository;

import org.example.tears.Enums.CustomerRequestStatus;
import org.example.tears.Enums.PricingStatus;
import org.example.tears.Enums.StaffRequestStatus;
import org.example.tears.Model.CarServiceRequest;
import org.example.tears.Model.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

public interface CarServiceRequestRepository extends JpaRepository<CarServiceRequest,Integer> {
    List<CarServiceRequest> findByCustomerIdOrderByIdDesc(Integer customerId);
    List<CarServiceRequest> findByAssignedPricingEmployee(Employee employee);
    List<CarServiceRequest> findByAssignedEmployeeAndStaffStatus(
            Employee employee,
            StaffRequestStatus status
    );


    long countByAssignedPricingEmployee_IdAndPricingStatus(
            Integer employeeId,
            PricingStatus pricingStatus
    );

    long countByAssignedPricingEmployee_IdAndPricingStatusIn(
            Integer employeeId,
            List<PricingStatus> statuses
    );

    Optional<CarServiceRequest> findById(Integer id);

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

    List<CarServiceRequest> findByAppointmentDate(LocalDate appointmentDate);

    List<CarServiceRequest> findAllByOrderByIdDesc();

}
