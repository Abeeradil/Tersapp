package org.example.tears.DTO;

import lombok.Data;

@Data
public class PartReportDto {
    private String name;

    private String type;

    private Integer quantity;

    private Integer unitPrice;

    private Integer totalPrice;

    private Integer laborCost;
}
