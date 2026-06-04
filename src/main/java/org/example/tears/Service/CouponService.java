package org.example.tears.Service;

import lombok.RequiredArgsConstructor;
import org.example.tears.Api.ApiException;
import org.example.tears.Enums.ServiceOption;
import org.example.tears.InpDTO.CreateCouponRequest;
import org.example.tears.InpDTO.UpdateCouponRequest;
import org.example.tears.Model.Coupon;
import org.example.tears.Repository.CouponRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CouponService {

    private final CouponRepository couponRepository;

    public Coupon create(CreateCouponRequest dto) {

        if (couponRepository.findByCodeIgnoreCase(dto.getCode()).isPresent()) {
            throw new ApiException("الكوبون موجود مسبقًا");
        }

        Coupon coupon = new Coupon();

        coupon.setCode(dto.getCode().toUpperCase());
        coupon.setActive(true);
        coupon.setDiscountPercentage(dto.getDiscountPercentage());
        coupon.setFixedDiscount(dto.getFixedDiscount());
        coupon.setUsageLimit(dto.getUsageLimit());
        coupon.setMinimumOrderPrice(dto.getMinimumOrderPrice());
        coupon.setMaxDiscountAmount(dto.getMaxDiscountAmount());
        coupon.setExpiryDate(dto.getExpiryDate());
        coupon.setServiceOption(dto.getServiceOption());

        return couponRepository.save(coupon);
    }

    public Coupon update(Integer id, UpdateCouponRequest dto) {

        Coupon coupon = couponRepository.findById(id)
                .orElseThrow(() -> new ApiException("الكوبون غير موجود"));

        if (dto.getDiscountPercentage() != null) {
            coupon.setDiscountPercentage(dto.getDiscountPercentage());
        }

        if (dto.getFixedDiscount() != null) {
            coupon.setFixedDiscount(dto.getFixedDiscount());
        }

        if (dto.getUsageLimit() != null) {
            coupon.setUsageLimit(dto.getUsageLimit());
        }

        if (dto.getMinimumOrderPrice() != null) {
            coupon.setMinimumOrderPrice(dto.getMinimumOrderPrice());
        }

        if (dto.getMaxDiscountAmount() != null) {
            coupon.setMaxDiscountAmount(dto.getMaxDiscountAmount());
        }

        if (dto.getExpiryDate() != null) {
            coupon.setExpiryDate(dto.getExpiryDate());
        }

        if (dto.getActive() != null) {
            coupon.setActive(dto.getActive());
        }

        return couponRepository.save(coupon);
    }

    public Coupon validate(
            String code,
            Double total,
            ServiceOption option
    ) {

        Coupon coupon = couponRepository.findByCodeIgnoreCase(code)
                .orElseThrow(() -> new ApiException("Coupon not found"));

        if (!coupon.isActive()) {
            throw new ApiException("Coupon inactive");
        }

        if (coupon.getExpiryDate() != null &&
                coupon.getExpiryDate().isBefore(LocalDate.now())) {
            throw new ApiException("Coupon expired");
        }

        if (coupon.getServiceOption() != null &&
                coupon.getServiceOption() != option) {
            throw new ApiException("Coupon not valid for this service");
        }

        if (coupon.getMinimumOrderPrice() != null &&
                total < coupon.getMinimumOrderPrice()) {
            throw new ApiException("Minimum order not met");
        }

        return coupon;
    }

    public Double calculateDiscount(
            Coupon coupon,
            Double total
    ) {

        double discount = 0;

        if (coupon.getDiscountPercentage() != null) {
            discount = total * coupon.getDiscountPercentage() / 100.0;

            if (coupon.getMaxDiscountAmount() != null) {
                discount = Math.min(discount, coupon.getMaxDiscountAmount());
            }
        }

        if (coupon.getFixedDiscount() != null) {
            discount += coupon.getFixedDiscount();
        }

        return Math.min(discount, total);
    }

    public List<Coupon> getAll() {
        return couponRepository.findAll();
    }

    public Coupon disable(Integer id) {

        Coupon coupon = couponRepository.findById(id)
                .orElseThrow(() -> new ApiException("غير موجود"));

        coupon.setActive(false);

        return couponRepository.save(coupon);
    }
}