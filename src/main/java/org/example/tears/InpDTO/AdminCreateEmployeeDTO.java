package org.example.tears.InpDTO;

import lombok.Data;
import org.example.tears.Enums.EmployeeCity;
import org.example.tears.Enums.EmployeeRole;

@Data
public class AdminCreateEmployeeDTO {

    private String fullName;

    private String phoneNumber;

    private String jobTitle;

    private EmployeeCity city;

    private EmployeeRole employeeRole;
}
