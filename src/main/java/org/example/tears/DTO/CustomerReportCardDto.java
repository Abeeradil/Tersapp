package org.example.tears.DTO;

import lombok.Data;
import org.example.tears.Enums.CustomerReportStatus;

import java.time.LocalDateTime;
@Data
public class CustomerReportCardDto {

    private Integer requestId;

    private String orderNumber;

    private String serviceType;

    private CustomerReportStatus status;

    private LocalDateTime reportDate;
}