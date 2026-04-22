package org.example.tears.Repository;

import org.example.tears.Model.CarServiceRequest;
import org.example.tears.Model.Employee;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface EmployeeRepository extends JpaRepository<Employee , Integer> {

    List<Employee> findAll();

}
