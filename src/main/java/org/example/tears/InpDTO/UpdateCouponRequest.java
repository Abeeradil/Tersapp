package org.example.tears.InpDTO;

import lombok.Data;

import java.time.LocalDate;

@Data
public class UpdateCouponRequest {

    private Integer discountPercentage;
    private Integer fixedDiscount;
    private Integer maxDiscountAmount;
    private Integer minimumOrderPrice;
    private Integer usageLimit;
    private LocalDate expiryDate;
    private Boolean active;
}