package org.example.tears.OutDTO;

import lombok.Data;
import org.example.tears.DTO.RequestImageDto;
import org.example.tears.DTO.TimelineItemDto;

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

    private String problemDescription;

    private LocalDateTime createdAt;

    private LocalDate deliveryDate;

    private LocalTime deliveryTime;

    private String deliveryLocation;

    private Boolean customerApproved;

    private String pricingEmployeeName;
    private String pricingEmployeePhone;

}