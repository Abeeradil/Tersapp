package org.example.tears.DTO;

import lombok.Data;
import org.example.tears.Enums.WarrantyCustomerStatus;
import org.example.tears.Enums.WarrantyReason;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class WarrantyDetailsDto {

    private Integer id;

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


    private String warrantyDescription;
    private List<WarrantyImageResponseDto> images;
    private List<WarrantyStatusHistoryDto> timeline;
}