package org.example.tears.DTO;

import lombok.Data;
import org.example.tears.Enums.WarrantyCustomerStatus;
import org.example.tears.Enums.WarrantyStatus;

import java.time.LocalDateTime;

@Data
public class WarrantyStatusHistoryDto {


        private WarrantyStatus employeeStatus;

        private WarrantyCustomerStatus customerStatus;

        private LocalDateTime changedAt;

    private String employeeName;
}