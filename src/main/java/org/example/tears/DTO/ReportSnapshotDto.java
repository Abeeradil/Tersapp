package org.example.tears.DTO;

import lombok.Data;

import java.util.List;

@Data
public class ReportSnapshotDto {

    private Integer requestId;

    private String orderNumber;

    private String customerName;

    private String carModel;

    private String serviceOption;

    private String problemDescription;

    private List<ReportSnapshotPartDto> parts;

    private Integer totalPartsPrice;

    private Integer totalLabor;

    private Integer vat;

    private Integer discount;

    private Integer grandTotal;

}