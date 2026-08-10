package org.example.tears.OutDTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AuthStatusDto {
    private boolean authenticated;
    private Integer userId;
    private Integer empId;
    private Integer custmId;
    private String name;
    private String role;
}