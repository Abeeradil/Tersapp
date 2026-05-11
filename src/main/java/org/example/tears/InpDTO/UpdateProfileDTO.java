package org.example.tears.InpDTO;

import lombok.Data;

import java.time.LocalDate;

@Data
public class UpdateProfileDTO {
    private String firstName;
    private String middleName;
    private String lastName;
    private LocalDate dateOfBirth;
}