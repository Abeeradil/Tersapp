package org.example.tears.DTO;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class PhoneNumberDTO {
    @NotBlank
    private String phoneNumber;

}
