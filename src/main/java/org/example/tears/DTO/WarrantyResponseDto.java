package org.example.tears.DTO;

import lombok.Data;
import org.example.tears.Enums.WarrantyCustomerStatus;
import org.example.tears.Enums.WarrantyReason;

import java.time.LocalDateTime;

@Data
public class WarrantyResponseDto {

    private Integer id;

    private String orderNumber;

    private WarrantyReason warrantyReason;

    private WarrantyCustomerStatus customerStatus;

    private LocalDateTime createdAt;
}