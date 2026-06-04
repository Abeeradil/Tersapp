package org.example.tears.DTO;

import lombok.Data;

@Data
public class CalculateCouponRequest {
    private String couponCode;

    private String serviceOption;

    private boolean hydraulicTruck;
}