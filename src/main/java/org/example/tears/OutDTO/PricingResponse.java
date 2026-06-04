package org.example.tears.OutDTO;

import lombok.Data;

@Data
public class PricingResponse {

    public boolean couponValid;

    public String message;

    public double originalPrice;

    public double discount;

    public double vatAmount;

    public double finalPrice;
}