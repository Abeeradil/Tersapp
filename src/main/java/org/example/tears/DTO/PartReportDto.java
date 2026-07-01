package org.example.tears.DTO;

import lombok.Data;

@Data
public class PartReportDto {

        private Integer partId;

        private String name;

        private String type;

        private Integer quantity;

        private Integer finalPrice;   // سعر الوحدة

        private Integer totalPrice;   // سعر الوحدة × العدد

        private Integer laborCost;

}
