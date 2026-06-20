package org.example.tears.Repository;

import org.example.tears.Model.CarServiceRequest;
import org.example.tears.Model.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface EmployeeRepository extends JpaRepository<Employee , Integer> {

    List<Employee> findAll();

    @Query("""
SELECT e FROM Employee e
WHERE e.employeeRole = 'PRICING'
""")
    List<Employee> findPricingEmployees();
}
