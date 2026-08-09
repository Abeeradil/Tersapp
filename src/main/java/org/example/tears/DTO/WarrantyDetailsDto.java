package org.example.tears.DTO;

import lombok.Data;
import org.example.tears.Enums.WarrantyProblemType;
import org.example.tears.Enums.WarrantyStatus;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class WarrantyDetailsDto {

    private Integer id;

    private String orderNumber;

    private WarrantyProblemType problemType;

    private String description;

    private WarrantyStatus status;

    private String rejectReason;

    private LocalDateTime createdAt;

    private String customerName;

    private String carModel;

    private String plateNumber;

    private String warrantyDescription;
    private List<WarrantyImageResponseDto> images;
    private List<WarrantyStatusHistoryDto> timeline;
}