package org.example.tears.DTO;

import lombok.Data;
import org.example.tears.Enums.PricingStatus;

import java.util.List;

@Data
public class PartsDetailsDto {

    private String problemDescription;

    private List<PartReportDto> parts;
    private PricingStatus pricingStatus;

    private Integer totalParts;       // مجموع عدد القطع

    private Integer totalLabor;       // مجموع الأجور

    private Integer totalPartsPrice;  // مجموع أسعار القطع

    private Integer grandTotal;       // القطع + الأجور

    private Boolean priced = false;

}