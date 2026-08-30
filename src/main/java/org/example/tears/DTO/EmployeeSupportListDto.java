package org.example.tears.DTO;

import lombok.Data;

@Data

public class EmployeeSupportListDto {
    private Integer id;
    private String fullName;
    private String employeeCode;
    private String email;
    private String phone;
    private String city;
    private String jobTitle;
}
