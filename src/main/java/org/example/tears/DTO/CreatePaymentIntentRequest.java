package org.example.tears.DTO;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
public class CreatePaymentIntentRequest {
    Integer carId;
    String serviceOption;
    String problemDescription;
    LocalDate appointmentDate;
    LocalTime appointmentTime;
    Boolean hydraulicTruck;
    String couponCode;
}
