package org.example.tears.DTO;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class VerifyChangePasswordDTO {
    @NotBlank
    private String phoneNumber;

    @NotBlank
    private String otp;

    @NotBlank
    private String newPassword;

    @NotBlank
    private String confirmPassword;

}
