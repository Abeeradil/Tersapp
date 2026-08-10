package org.example.tears.DTO;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class WarrantyRequestSummaryDto {

    private Integer requestId;

    private String orderNumber;

    private String plateNumberArabic;

    private String plateNumberEnglish;

    private String serviceType;

    private String problemDescription;

    private LocalDateTime createdAt;
}
