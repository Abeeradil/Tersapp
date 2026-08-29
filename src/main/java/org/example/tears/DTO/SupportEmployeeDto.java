package org.example.tears.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SupportEmployeeDto {

    private Integer id;
    private String fullName;
    private String employeeCode;
    private String email;
    private String phone;
    private String city;
    private String jobTitle;
}
