package org.example.tears.DTO;

import lombok.Data;
import org.example.tears.Enums.WarrantyStatus;
import org.example.tears.InpDTO.LocationDto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
@Data
public class EmployeeWarrantyDetailsDto {

    private Integer warrantyId;

    private Integer requestId;

    private String orderNumber;

    private WarrantyStatus status;

    private String description;

    private String customerName;
    private String customerPhone;

    private String address;

    private String carModelName;
    private String carModelNameAr;

    private String serviceOption;

    private String plateNumberArabic;
    private String plateNumberEnglish;

    private LocalDateTime createdAt;

    private LocationDto receivingLocation;
    private LocalDate receivingDate;
    private LocalTime receivingTime;

    private LocationDto deliveryLocation;
    private LocalDate deliveryDate;
    private LocalTime deliveryTime;

    private List<WarrantyImageResponseDto> images;

    private List<EmployeeWarrantyStatusHistoryDto> timeline;

    private List<RequestNoteDTO> notes;
}