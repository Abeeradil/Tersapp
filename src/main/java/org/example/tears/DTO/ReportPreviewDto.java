package org.example.tears.DTO;

import lombok.Data;

import java.util.List;
@Data
public class ReportPreviewDto {

    private Integer requestId;

    private String orderNumber;

    private String customerName;

    private String carModel;

    private String problemDescription;

    private Boolean customerApproved;

    private List<CustomerReportPartDto> parts;

    private Integer totalPartsPrice;

    private Integer totalLabor;

    private Double discount;

    private String serviceType;

    private Double grandTotal;

}