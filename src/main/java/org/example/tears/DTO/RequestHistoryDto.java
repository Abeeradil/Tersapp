package org.example.tears.DTO;

import lombok.Data;
import org.example.tears.Enums.WarrantyCustomerStatus;
import org.example.tears.Enums.WarrantyEligibilityStatus;
import org.example.tears.Enums.WarrantyReason;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
public class RequestHistoryDto {

    private Integer id;

    private String orderNumber;
    private String serviceName;

    private Integer carId;

    private String plateNumberArabic;

    private String plateNumberEnglish;

    private String brandNameAr;

    private String modelNameAr;

    private Integer carYear;

    private String carImage;

    private LocalDate appointmentDate;

    private LocalTime appointmentTime;

    private Double totalPrice;

    private Boolean canReview;
    private Boolean reviewed;
    private Boolean warrantyRequest;
    private Integer warrantyId;
    private WarrantyReason warrantyReason;
    private WarrantyCustomerStatus warrantyStatus;
    private WarrantyEligibilityStatus warrantyEligibility;


    private LocalDate warrantyExpiryDate;
    private Long warrantyRemainingDays;
    private Boolean underWarranty;


    private String customerStatus;
    private String requestState;

}