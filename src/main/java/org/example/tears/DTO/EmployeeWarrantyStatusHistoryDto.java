package org.example.tears.DTO;

import lombok.Data;
import org.example.tears.Enums.WarrantyStatus;

import java.time.LocalDateTime;

@Data
public class EmployeeWarrantyStatusHistoryDto {

    private WarrantyStatus status;

    private LocalDateTime changedAt;

    private String employeeName;
}