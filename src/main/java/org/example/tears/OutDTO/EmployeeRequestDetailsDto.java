package org.example.tears.OutDTO;

import lombok.Data;
import org.example.tears.DTO.RequestImageDto;
import org.example.tears.DTO.TimelineItemDto;
import org.example.tears.Enums.ServiceOption;
import org.example.tears.InpDTO.LocationDto;

import java.time.LocalDateTime;
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
    private LocalDateTime lastUpdated;

    private String pricingEmployeeName;
    private String pricingEmployeePhone;

}