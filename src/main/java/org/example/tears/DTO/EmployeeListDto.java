package org.example.tears.DTO;

import lombok.Data;

@Data
public class EmployeeListDto {

    private Integer id;

    private String fullName;

    private String phoneNumber;

    private String jobTitle;

    private String role;

    private String status;
}