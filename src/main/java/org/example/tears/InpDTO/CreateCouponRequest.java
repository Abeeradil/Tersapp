package org.example.tears.InpDTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.tears.Enums.ServiceOption;

import java.time.LocalDate;
@Data
@AllArgsConstructor
@NoArgsConstructor
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