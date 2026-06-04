package org.example.tears.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CouponCalculationResponse {

    private Boolean couponValid;

    private String message;

    private Double originalPrice;

    private Double vatAmount;

    private Double priceBeforeVat;

    private Double discountAmount;

    private Double finalPrice;

}