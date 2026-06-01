package org.example.tears.Service;

import lombok.RequiredArgsConstructor;
import org.example.tears.Api.ApiException;
import org.example.tears.Enums.ServiceOption;
import org.example.tears.InpDTO.CreateCouponRequest;
import org.example.tears.InpDTO.UpdateCouponRequest;
import org.example.tears.InpDTO.ValidateCouponDto;
import org.example.tears.Model.Coupon;
import org.example.tears.Repository.CouponRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CouponService {

        private final CouponRepository couponRepository;

        public Coupon create(CreateCouponRequest dto) {

            if (couponRepository.findByCodeIgnoreCase(dto.getCode()).isPresent()) {
                throw new ApiException("الكوبون موجود مسبقًا");
            }

            Coupon c = new Coupon();
            c.setCode(dto.getCode().toUpperCase());
            c.setActive(true);
            c.setDiscountPercentage(dto.getDiscountPercentage());
            c.setFixedDiscount(dto.getFixedDiscount());
            c.setUsageLimit(dto.getUsageLimit());
            c.setMinimumOrderPrice(dto.getMinimumOrderPrice());
            c.setMaxDiscountAmount(dto.getMaxDiscountAmount());
            c.setExpiryDate(dto.getExpiryDate());
            c.setServiceOption(dto.getServiceOption());

            return couponRepository.save(c);
        }

        public Coupon update(Integer id, UpdateCouponRequest dto) {

            Coupon c = couponRepository.findById(id)
                    .orElseThrow(() -> new ApiException("الكوبون غير موجود"));

            if (dto.getDiscountPercentage() != null)
                c.setDiscountPercentage(dto.getDiscountPercentage());

            if (dto.getFixedDiscount() != null)
                c.setFixedDiscount(dto.getFixedDiscount());

            if (dto.getUsageLimit() != null)
                c.setUsageLimit(dto.getUsageLimit());

            if (dto.getMinimumOrderPrice() != null)
                c.setMinimumOrderPrice(dto.getMinimumOrderPrice());

            if (dto.getMaxDiscountAmount() != null)
                c.setMaxDiscountAmount(dto.getMaxDiscountAmount());

            if (dto.getExpiryDate() != null)
                c.setExpiryDate(dto.getExpiryDate());

            if (dto.getActive() != null)
                c.setActive(dto.getActive());

            return couponRepository.save(c);
        }

        public Coupon validate(String code, int total, ServiceOption option) {

            Coupon coupon = couponRepository.findByCodeIgnoreCase(code)
                    .orElseThrow(() -> new RuntimeException("Coupon not found"));

            if (!coupon.isActive())
                throw new RuntimeException("Coupon inactive");

            if (coupon.getExpiryDate() != null &&
                    coupon.getExpiryDate().isBefore(LocalDate.now()))
                throw new RuntimeException("Coupon expired");

            if (coupon.getServiceOption() != null &&
                    coupon.getServiceOption() != option)
                throw new RuntimeException("Coupon not valid for this service");

            if (coupon.getMinimumOrderPrice() != null &&
                    total < coupon.getMinimumOrderPrice())
                throw new RuntimeException("Minimum order not met");

            return coupon;
        }

        public int applyDiscount(Coupon c, int total) {

            if (c.getDiscountPercentage() != null) {
                int discount = (total * c.getDiscountPercentage()) / 100;

                if (c.getMaxDiscountAmount() != null &&
                        discount > c.getMaxDiscountAmount()) {
                    discount = c.getMaxDiscountAmount();
                }

                total -= discount;
            }

            if (c.getFixedDiscount() != null) {
                total -= c.getFixedDiscount();
            }

            return Math.max(total, 0);
        }

        public List<Coupon> getAll() {
            return couponRepository.findAll();
        }

        public Coupon disable(Integer id) {

            Coupon c = couponRepository.findById(id)
                    .orElseThrow(() -> new ApiException("غير موجود"));

            c.setActive(false);
            return couponRepository.save(c);
        }
    }