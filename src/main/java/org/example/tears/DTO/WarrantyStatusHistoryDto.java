package org.example.tears.DTO;

import lombok.Data;
import org.example.tears.Enums.WarrantyStatus;

import java.time.LocalDateTime;

@Data
public class WarrantyStatusHistoryDto {

    private WarrantyStatus status;

    private LocalDateTime changedAt;

    private Integer employeeId;

    private String employeeName;
}