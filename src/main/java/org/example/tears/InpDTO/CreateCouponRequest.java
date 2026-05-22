package org.example.tears.InpDTO;

import lombok.Data;
import org.example.tears.Enums.ServiceOption;

import java.time.LocalDate;
@Data
public class CreateCouponRequest {

    private String code;

    private Integer discountPercentage;

    private Integer fixedDiscount;

    private Integer usageLimit;

    private Integer minimumOrderPrice;

    private Integer maxDiscountAmount;

    private LocalDate expiryDate;

    private ServiceOption serviceOption;
}