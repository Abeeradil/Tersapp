package org.example.tears.InpDTO;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AdminCreateEmployeeDTO {

    @NotBlank
    private String fullName;
    @NotBlank
    private String phoneNumber;
    @NotBlank
    private String jobTitle;

}
