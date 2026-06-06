package org.example.tears.InpDTO;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.example.tears.Enums.EmployeeRole;

@Data
public class AdminCreateEmployeeDTO {

    private String fullName;

    private String phoneNumber;

    private String jobTitle;

    private EmployeeRole employeeRole;
}
