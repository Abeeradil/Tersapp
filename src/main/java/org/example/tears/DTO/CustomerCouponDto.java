package org.example.tears.DTO;

import lombok.Data;
import org.example.tears.Enums.ServiceOption;

import java.time.LocalDate;

@Data
public class CustomerCouponDto {

    private Integer id;

    private String code;

    private String title;

    private String description;

    private Integer discountPercentage;

    private Integer fixedDiscount;

    private Integer maxDiscountAmount;

    private LocalDate expiryDate;

    private ServiceOption serviceOption;
}