package org.example.tears.InpDTO;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Data
public class CreateRequestStepDto {

        private Integer carId;

        @NotBlank
        private String serviceOption;

        private boolean hydraulicTruck;

        @NotBlank
        private String problemDescription;

        @NotNull
        private LocalDate appointmentDate;

        @NotNull
        private LocalTime appointmentTime;

        private Integer locationId;

        private String couponCode;

        private String paymentMethod;
    }