package org.example.tears.DTO;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class ReportDto {

        private String orderNumber;

        private String customerName;

        private String carModel;

        private String problemDescription;

        private List<PartReportDto> parts;

        private Integer totalPartsPrice;

        private Integer totalLabor;

        private Integer grandTotal;

        private String inspectionResult;

        private String technicianNotes;

        private String recommendations;

        private LocalDateTime createdAt;

        private String reportNumber;

        private Integer version;
    }