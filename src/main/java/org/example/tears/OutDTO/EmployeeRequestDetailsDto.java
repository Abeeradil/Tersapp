package org.example.tears.OutDTO;

import lombok.Data;
import org.example.tears.DTO.*;
import org.example.tears.Enums.WarrantyStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Data
public class EmployeeRequestDetailsDto {

    private Integer id;
    private String orderNumber;

    private String status;

    private String serviceOption;

    private String customerName;
    private String customerPhone;

    private String carModelName;
    private String carModelNameAr;

    private String plateNumberArabic;
    private String plateNumberEnglish;

    private String address;

    private List<RequestImageDto> images;
    private List<TimelineItemDto> timeline;
    private List<RequestNoteDTO> notes;


    private String problemDescription;

    private LocalDateTime createdAt;

    private Boolean customerSelectedDelivery;

    private LocalDate deliveryDate;
    private LocalTime deliveryTime;
    private String deliveryDay;
    private String deliveryLocation;

    private Boolean customerApproved;

    private String pricingEmployeeName;
    private String pricingEmployeePhone;

    private Boolean warrantyRequest;
    private WarrantyStatus warrantyStatus;

    private String warrantyDescription;
    private List<WarrantyImageResponseDto> warrantyImages;
    private List< EmployeeWarrantyStatusHistoryDto> warrantyTimeline;

}