package org.example.tears.Service;

import lombok.RequiredArgsConstructor;
import org.example.tears.Enums.ServiceOption;
import org.example.tears.Model.*;
import org.example.tears.OutDTO.PricingResponse;
import org.springframework.stereotype.Service;


@Service
@RequiredArgsConstructor
public class PricingCalculationService {

    private final CouponService couponService;

    private static final double VAT_PERCENTAGE = 0.15;
    private static final double HYDRAULIC_EXTRA = 100;

    private double calculateSubtotal(
            String serviceOption,
            boolean hydraulicTruck
    ) {

        ServiceOption option =
                ServiceOption.valueOf(serviceOption.toUpperCase());

        double subtotal = option.getPrice();

        if (hydraulicTruck) {
            subtotal += HYDRAULIC_EXTRA;
        }

        return subtotal;
    }


    public PricingResponse calculateFinal(
            String serviceOption,
            Boolean hydraulicTruck,
            String couponCode,
            Customer customer
    ) {

        // 1️⃣ سعر الخدمة قبل الضريبة
        double subtotal =
                calculateSubtotal(
                        serviceOption,
                        hydraulicTruck
                );

        ServiceOption option =
                ServiceOption.valueOf(
                        serviceOption.toUpperCase()
                );

        // 2️⃣ السعر الأساسي شامل الضريبة
        double originalPriceWithVat =
                subtotal + (subtotal * VAT_PERCENTAGE);

        // 3️⃣ حساب الخصم على السعر شامل الضريبة
        double discount = 0;
        boolean couponValid = true;
        String message = "Success";

        if (couponCode != null &&
                !couponCode.isBlank()) {

            try {

                Coupon coupon =
                        couponService.validate(
                                couponCode,
                                subtotal,
                                option,
                                customer);

                discount =
                        calculateDiscount(
                                coupon,
                                originalPriceWithVat
                        );

            } catch (Exception e) {

                couponValid = false;
                message = e.getMessage();
            }
        }

        // 4️⃣ الخصم من السعر شامل الضريبة
        double finalPrice =
                Math.max(
                        originalPriceWithVat - discount,
                        0
                );

        // 5️⃣ استخراج الضريبة الموجودة داخل السعر النهائي
        double vatAmount =
                finalPrice *
                        VAT_PERCENTAGE /
                        (1 + VAT_PERCENTAGE);

        PricingResponse response =
                new PricingResponse();

        response.originalPrice =
                round(originalPriceWithVat);

        response.discount =
                round(discount);

        response.vatAmount =
                round(vatAmount);

        response.finalPrice =
                round(finalPrice);

        response.couponValid =
                couponValid;

        response.message =
                message;

        return response;
    }

    private double calculateDiscount(
            Coupon coupon,
            double total
    ) {

        double discount = 0;

        if (coupon.getDiscountPercentage() != null) {

            discount =
                    total *
                            coupon.getDiscountPercentage()
                            / 100.0;

            if (coupon.getMaxDiscountAmount()
                    != null) {

                discount =
                        Math.min(
                                discount,
                                coupon.getMaxDiscountAmount()
                        );
            }
        }

        if (coupon.getFixedDiscount()
                != null) {

            discount +=
                    coupon.getFixedDiscount();
        }

        return Math.min(discount, total);
    }

    private double round(double value) {

        return Math.round(value * 100.0)
                / 100.0;
    }
}