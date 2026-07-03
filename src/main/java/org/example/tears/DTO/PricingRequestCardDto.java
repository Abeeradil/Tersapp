package org.example.tears.DTO;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class PricingRequestCardDto {

    private Integer id;

    private String orderNumber;

    private String pricingStatus;

    // نوع الخدمة
    private String serviceOption;

    // موقع الاستلام
    private String address;

    // السيارة
    private String carModelName;

    private String carModelNameAr;

    // اللوحة
    private String plateNumberArabic;

    private String plateNumberEnglish;

    private LocalDateTime createdAt;

}