package org.example.tears.InpDTO;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

import java.time.LocalDate;

@Data
public class CustomerRegisterDTO {

    @NotBlank
    private String fullName;

    @NotBlank
    @Pattern(regexp = "^\\+9665\\d{8}$",
            message = "Phone number must be a valid Saudi number")
    private String phoneNumber;

    @Past
    private LocalDate dateOfBirth;
}
