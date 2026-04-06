package org.example.tears.DTO;

import lombok.Data;

@Data
public class PartDto {

        private String name;
        private String type;           // جديد
        private Integer quantity;      // بدل qty
        private Integer estimatedPrice; // بدل price
        private Integer laborCost;      // جديد
    }
