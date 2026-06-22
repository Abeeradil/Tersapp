package org.example.tears.OutDTO;

import lombok.Data;
import org.example.tears.Enums.ServiceOption;
import org.example.tears.InpDTO.LocationDto;

import java.time.LocalDateTime;

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

    private String problemDescription;

    private LocalDateTime createdAt;
    private LocalDateTime lastUpdated;
}