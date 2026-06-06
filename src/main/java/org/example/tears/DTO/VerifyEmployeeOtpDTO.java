package org.example.tears.DTO;

import lombok.Data;

@Data
public class VerifyEmployeeOtpDTO {

    private String emailOrPhone;
    private String otp;
}