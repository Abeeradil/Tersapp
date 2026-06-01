package org.example.tears.Service;

import lombok.RequiredArgsConstructor;
import org.example.tears.Api.ApiException;
import org.example.tears.Enums.PricingStatus;
import org.example.tears.Enums.ServiceOption;
import org.example.tears.Model.CarServiceRequest;
import org.example.tears.Model.Coupon;
import org.example.tears.Model.Employee;
import org.example.tears.Model.RequestPart;
import org.example.tears.Repository.CarServiceRequestRepository;
import org.example.tears.Repository.CouponRepository;
import org.example.tears.Repository.RequestPartRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class PricingCalculationService {

    private final CouponRepository couponRepository;
    private final CarServiceRequestRepository requestRepository;
    private final CouponService couponService;
    private final RequestPartRepository requestPartRepository;
    private final PricingService pricingService;

    private static final int HYDRAULIC_EXTRA = 100;

    public int calculatePreview(String serviceOption, boolean hydraulicTruck) {

        ServiceOption option = ServiceOption.valueOf(serviceOption);

        int total = option.getPrice();

        if (hydraulicTruck) {
            total += 100;
        }

        return total;
    }


        public int calculateFinal(String serviceOption,
                                  boolean hydraulicTruck,
                                  String couponCode) {

            int total = pricingService.calculatePreview(serviceOption, hydraulicTruck);

            if (couponCode != null && !couponCode.isBlank()) {

                Coupon coupon = couponService.validate(
                        couponCode,
                        total,
                        ServiceOption.valueOf(serviceOption)
                );

                total = applyDiscount(coupon, total);
            }

            return Math.max(total, 0);
        }

        private int applyDiscount(Coupon coupon, int total) {

            if (coupon.getDiscountPercentage() != null) {

                int discount = (total * coupon.getDiscountPercentage()) / 100;

                if (coupon.getMaxDiscountAmount() != null) {
                    discount = Math.min(discount, coupon.getMaxDiscountAmount());
                }

                total -= discount;
            }

            if (coupon.getFixedDiscount() != null) {
                total -= coupon.getFixedDiscount();
            }

            return total;
        }

    public void startPricing(Integer requestId, Employee employee) {

        CarServiceRequest request = requestRepository.findById(requestId)
                .orElseThrow(() -> new RuntimeException("Request not found"));

        request.setPricingStatus(PricingStatus.PRICING);
        request.setAssignedPricingEmployee(employee);
        requestRepository.save(request);
    }


}