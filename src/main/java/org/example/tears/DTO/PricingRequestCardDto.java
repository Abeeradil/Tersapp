package org.example.tears.DTO;

import lombok.Data;
import org.example.tears.Enums.PricingStatus;

import java.time.LocalDateTime;

@Data
public class PricingRequestCardDto {

    private Integer id;

    private String orderNumber;

    private String pricingStatus;

    private String customerName;

    private String carModelName;

    private String carModelNameAr;

    private String plateNumberArabic;

    private String plateNumberEnglish;

    private LocalDateTime createdAt;

}