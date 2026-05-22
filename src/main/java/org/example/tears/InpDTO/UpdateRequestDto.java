package org.example.tears.InpDTO;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
public class UpdateRequestDto {

    private Integer carId;

    private String serviceOption;

    private Boolean hydraulicTruck;

    private String problemDescription;

    private Integer locationId;

    private LocalDate appointmentDate;

    private LocalTime appointmentTime;

    private String couponCode;

    private String paymentMethod;
}