package org.example.tears.DTO;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

@Data
public class SendOtpDto {
    @NotEmpty
    private String phoneNumber;
}
