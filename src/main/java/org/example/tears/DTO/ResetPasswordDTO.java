package org.example.tears.DTO;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ResetPasswordDTO {

    @NotBlank
    private String phoneNumber;

    @NotBlank
    private String newPassword;

    @NotBlank
    private String confirmPassword;
}
