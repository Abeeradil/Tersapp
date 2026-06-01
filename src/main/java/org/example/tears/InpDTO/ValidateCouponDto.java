package org.example.tears.InpDTO;

import lombok.Data;

@Data
    public class ValidateCouponDto {

    private String couponCode;

    private Integer totalPrice;

    private String serviceOption;

    private boolean hydraulicTruck;
}