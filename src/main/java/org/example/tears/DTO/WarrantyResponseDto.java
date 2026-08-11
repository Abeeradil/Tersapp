package org.example.tears.DTO;

import lombok.Data;
import org.example.tears.Enums.WarrantyReason;
import org.example.tears.Enums.WarrantyStatus;

import java.time.LocalDateTime;

@Data
public class WarrantyResponseDto {

    private Integer id;

    private String orderNumber;

    private WarrantyReason warrantyReason;

    private WarrantyStatus status;

    private LocalDateTime createdAt;
}