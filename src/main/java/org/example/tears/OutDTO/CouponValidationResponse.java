package org.example.tears.OutDTO;

import lombok.Data;

@Data
public class CouponValidationResponse {

    private boolean valid;

    private Integer originalPrice;

    private Integer finalPrice;

    private String message;
}