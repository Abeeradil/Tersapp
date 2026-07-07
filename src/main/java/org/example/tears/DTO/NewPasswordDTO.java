package org.example.tears.DTO;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

@Data
public class NewPasswordDTO {

    @NotEmpty
    private String newPassword;

    @NotEmpty
    private String confirmPassword;
}