package org.example.tears.DTO;

import lombok.Data;
import org.example.tears.Enums.ReportStatus;

@Data
public class CustomerReportListDto {

    private Integer requestId;

    private String orderNumber;

    private String serviceOption;

    private String reportName;

    private ReportStatus status;

    private Boolean active;

}