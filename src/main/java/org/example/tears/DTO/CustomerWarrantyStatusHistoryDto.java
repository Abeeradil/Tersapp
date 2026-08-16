package org.example.tears.DTO;

import lombok.Data;
import org.example.tears.Enums.WarrantyCustomerStatus;

import java.time.LocalDateTime;

@Data
public class CustomerWarrantyStatusHistoryDto {

    private WarrantyCustomerStatus status;

    private LocalDateTime changedAt;
}
