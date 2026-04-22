package org.example.tears.DTO;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class VerifyOtpDTO {
    @NotBlank
    private String phoneNumber;

    @NotBlank
    private String otp;
}