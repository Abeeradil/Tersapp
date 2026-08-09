package org.example.tears.DTO;

import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.Data;
import org.example.tears.Enums.WarrantyStatus;
import org.example.tears.Model.Employee;

import java.time.LocalDateTime;

@Data
public class WarrantyStatusHistoryDto {

    private WarrantyStatus status;

    private LocalDateTime changedAt;

    @ManyToOne
    @JoinColumn(name = "changed_by_employee_id")
    private Employee changedBy;

    private String employeeName;
}