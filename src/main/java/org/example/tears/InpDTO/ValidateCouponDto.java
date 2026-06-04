package org.example.tears.InpDTO;

import lombok.Data;

@Data
    public class ValidateCouponDto {

    private String couponCode;

    private Double totalPrice;

    private String serviceOption;

    private Boolean hydraulicTruck;
}