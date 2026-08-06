package org.example.tears.DTO;

import lombok.Data;
import org.example.tears.Enums.CustomerReportStatus;
import org.example.tears.Enums.RequestState;

import java.time.LocalDateTime;
@Data
public class CustomerReportCardDto {

    private Integer requestId;

    private String orderNumber;

    private String serviceType;

    private CustomerReportStatus reportStatus;

    private RequestState requestState;

    private LocalDateTime reportDate;
}