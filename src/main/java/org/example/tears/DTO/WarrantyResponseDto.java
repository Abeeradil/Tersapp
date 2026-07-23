package org.example.tears.DTO;

import lombok.Data;
import org.example.tears.Enums.WarrantyProblemType;
import org.example.tears.Enums.WarrantyStatus;

import java.time.LocalDateTime;

@Data
public class WarrantyResponseDto {

    private Integer id;

    private String orderNumber;

    private WarrantyProblemType problemType;

    private WarrantyStatus status;

    private LocalDateTime createdAt;
}