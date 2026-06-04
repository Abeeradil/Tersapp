package org.example.tears.Service;

import lombok.RequiredArgsConstructor;
import org.example.tears.Api.ApiException;
import org.example.tears.Enums.PricingStatus;
import org.example.tears.Enums.ServiceOption;
import org.example.tears.Model.CarServiceRequest;
import org.example.tears.Model.Coupon;
import org.example.tears.Model.Employee;
import org.example.tears.Model.RequestPart;
import org.example.tears.OutDTO.PricingResponse;
import org.example.tears.Repository.CarServiceRequestRepository;
import org.example.tears.Repository.CouponRepository;
import org.example.tears.Repository.RequestPartRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class PricingCalculationService {

    private final CarServiceRequestRepository requestRepository;
    private final CouponService couponService;
    private final RequestPartRepository requestPartRepository;
    private final PricingService pricingService;

    private static final double VAT_PERCENTAGE = 0.15;
    private static final double HYDRAULIC_EXTRA = 100;

    public double calculatePreview(
            String serviceOption,
            boolean hydraulicTruck
    ) {

        ServiceOption option =
                ServiceOption.valueOf(serviceOption.toUpperCase());

        double total = option.getPrice();

        if (hydraulicTruck) {
            total += HYDRAULIC_EXTRA;
        }

        return total;
    }

    public PricingResponse calculateFinal(
            String serviceOption,
            Boolean hydraulicTruck,
            String couponCode
    ) {

        double originalPrice =
                calculatePreview(serviceOption, hydraulicTruck);

        ServiceOption option =
                ServiceOption.valueOf(serviceOption.toUpperCase());

        double discount = 0;

        boolean couponValid = true;

        String message = "Success";

        if (couponCode != null &&
                !couponCode.isBlank()) {

            try {

                Coupon coupon =
                        couponService.validate(
                                couponCode,
                                originalPrice,
                                option
                        );

                discount =
                        calculateDiscount(
                                coupon,
                                originalPrice
                        );

            } catch (Exception e) {

                couponValid = false;

                message = e.getMessage();
            }
        }

        double afterDiscount =
                Math.max(
                        originalPrice - discount,
                        0
                );

        double vatAmount =
                afterDiscount * VAT_PERCENTAGE;

        double finalPrice =
                afterDiscount + vatAmount;

        PricingResponse response =
                new PricingResponse();

        response.originalPrice =
                round(originalPrice);

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