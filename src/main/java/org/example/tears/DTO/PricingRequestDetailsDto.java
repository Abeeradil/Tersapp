package org.example.tears.DTO;

import lombok.Data;

import java.util.List;
@Data
public class PricingRequestDetailsDto {

        private Integer id;

        private String orderNumber;

        private String pricingStatus;

        private String customerName;

        private String customerPhone;

        private String problemDescription;

        private String carModelName;

        private String carModelNameAr;

        private String plateNumberArabic;

        private String plateNumberEnglish;

        private List<PartReportDto> parts;

        private Integer totalParts;

        private Integer totalLabor;

        private Integer totalPartsPrice;

        private Integer grandTotal;
    }