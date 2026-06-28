package org.example.tears.DTO;

import lombok.Data;
import org.example.tears.Model.RequestPart;

@Data
public class PartDetailsDto {

    private Integer id;

    private String name;

    private RequestPart type;

    private Integer quantity;

    private Integer estimatedPrice;

    private Integer finalPrice;

    private Integer laborCost;

    private Integer totalPrice;
}