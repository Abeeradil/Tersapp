package org.example.tears.DTO;

import lombok.Data;
import org.example.tears.Enums.WarrantyCustomerStatus;
import org.example.tears.Enums.WarrantyReason;
import org.example.tears.InpDTO.LocationDto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Data
public class WarrantyDetailsDto {

    private Integer id;
    private Integer requestId;

    private String orderNumber;

    private WarrantyReason warrantyReason;

    private String serviceOption;    // نوع الخدمة

    private String description;

    private WarrantyCustomerStatus status;

    private String rejectReason;

    private LocalDateTime createdAt;

    private String customerName;

    private String carModelName;
    private String carModelNameAr;

    private String plateNumberArabic;
    private String plateNumberEnglish;

    private LocationDto receivingLocation;
    private LocalDate receivingDate;
    private LocalTime receivingTime;

    private LocationDto deliveryLocation;
    private LocalDate deliveryDate;
    private LocalTime deliveryTime;

    private boolean canChooseReceivingAppointment;
    private boolean canChooseDeliveryAppointment;
    private boolean vehicleReceived;

    private String warrantyDescription;
    private List<WarrantyImageResponseDto> images;
    private List<CustomerWarrantyStatusHistoryDto> timeline;

}