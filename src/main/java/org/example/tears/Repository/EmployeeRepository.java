package org.example.tears.Repository;

import org.example.tears.Enums.EmployeeRole;
import org.example.tears.Model.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface EmployeeRepository extends JpaRepository<Employee , Integer> {

    List<Employee> findAll();

    List<Employee> findByEmployeeRole(EmployeeRole employeeRole);
}