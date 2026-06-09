package org.example.tears.DTO;

import lombok.Data;
import org.example.tears.InpDTO.LocationDto;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
public class RequestDetailDto {

    private Integer id;
    private String orderNumber;

    private String status;

    // pricing
    private Double totalPrice;
    private Double originalPrice;
    private Double discount;
    private Double vatAmount;
    private Double remainingAmount;

    // payment
    private String paymentStatus;
    private String paymentMethod;
    private Double amountPaid;

    // appointment
    private LocalDate appointmentDate;
    private LocalTime appointmentTime;

    // car
    private String plateNumberArabic;
    private String plateNumberEnglish;

    // location
    private LocationDto location;

    // employee 👇
    private EmployeeSummaryDto assignedEmployee;

    private String pricingMessage;
}