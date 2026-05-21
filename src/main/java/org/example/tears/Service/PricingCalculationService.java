package org.example.tears.Service;

import lombok.RequiredArgsConstructor;
import org.example.tears.Enums.PricingStatus;
import org.example.tears.Enums.ServiceOption;
import org.example.tears.Model.CarServiceRequest;
import org.example.tears.Model.Employee;
import org.example.tears.Model.RequestPart;
import org.example.tears.Repository.CarServiceRequestRepository;
import org.example.tears.Repository.CouponRepository;
import org.example.tears.Repository.RequestPartRepository;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PricingCalculationService {

    private final CouponRepository couponRepository;
    private final CarServiceRequestRepository requestRepository;
    private final RequestPartRepository requestPartRepository;

    private static final int HYDRAULIC_EXTRA = 100;
    public void startPricing(Integer requestId, Employee employee) {

        CarServiceRequest request = requestRepository.findById(requestId)
                .orElseThrow(() -> new RuntimeException("Request not found"));

        request.setPricingStatus(PricingStatus.PRICING);
        request.setAssignedPricingEmployee(employee);
        requestRepository.save(request);
    }

    public void setFinalPrice(Integer partId, Integer price) {

        RequestPart part = requestPartRepository.findById(partId)
                .orElseThrow(() -> new RuntimeException("Part not found"));

        part.setFinalPrice(price);

        requestPartRepository.save(part);
    }

    public void finishPricing(Integer requestId) {

        CarServiceRequest request = requestRepository.findById(requestId)
                .orElseThrow(() -> new RuntimeException("Request not found"));

        request.setPricingStatus(PricingStatus.PRICED);

        requestRepository.save(request);
    }

    // ================= CALCULATION =================

    public int calculatePreview(String serviceOption, boolean hydraulicTruck) {

        ServiceOption option = ServiceOption.valueOf(serviceOption);

        int price = option.getPrice();

        if (hydraulicTruck) {
            price += HYDRAULIC_EXTRA;
        }

        return price;
    }

    public int calculateFinal(String serviceOption, boolean hydraulicTruck, String couponCode) {

        ServiceOption option = ServiceOption.valueOf(serviceOption);

        int price = option.getPrice();

        if (hydraulicTruck) {
            price += HYDRAULIC_EXTRA;
        }

        if (couponCode != null && !couponCode.isBlank()) {

            var coupon = couponRepository.findByCodeAndActiveTrue(couponCode);

            if (coupon.isPresent()) {
                price = Math.max(0, price - coupon.get().getDiscount());
            }
        }

        return price;
    }
}