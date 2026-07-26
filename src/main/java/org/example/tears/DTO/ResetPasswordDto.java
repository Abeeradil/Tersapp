package org.example.tears.DTO;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

@Data
public class ResetPasswordDto {
    @NotEmpty
    private String resetToken;
    @NotEmpty
    private String newPassword;
    @NotEmpty
    private String confirmPassword;
}
