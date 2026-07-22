package org.example.tears.DTO;

import lombok.Data;

@Data
public class ReportSnapshotPartDto {

    private Integer partId;

    private String name;

    private String type;

    private Integer quantity;

    private Integer finalPrice;

    private Integer laborCost;

    private Integer totalPrice;

    private String problemDescription;

}