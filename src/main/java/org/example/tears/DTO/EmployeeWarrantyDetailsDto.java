package org.example.tears.DTO;

import lombok.Data;
import org.example.tears.Enums.WarrantyStatus;

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


    private String carModelName;

    private String carModelNameAr;

    private String serviceOption;

    private String plateNumberArabic;

    private String plateNumberEnglish;

    private List<WarrantyImageResponseDto> images;

    private List<EmployeeWarrantyStatusHistoryDto> timeline;

    private List<RequestNoteDTO> notes;
}