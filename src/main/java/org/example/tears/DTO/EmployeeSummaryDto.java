package org.example.tears.DTO;

import lombok.Data;
import org.example.tears.Enums.EmployeeRole;

@Data
public class EmployeeSummaryDto {

    private Integer id;
    
    private String name;

    private EmployeeRole role;

    private String assignedEmployee;

    private String jobTitle;
}
