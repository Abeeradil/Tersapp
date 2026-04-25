package org.example.tears.InpDTO;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Past;
import lombok.Data;

import java.time.LocalDate;

@Data
public class CustomerRegisterDTO {

    @NotBlank
    private String fullName;

    @NotBlank
    private String phoneNumber;

    @Past
    private LocalDate dateOfBirth;
}
