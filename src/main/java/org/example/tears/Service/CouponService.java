package org.example.tears.Service;

import lombok.RequiredArgsConstructor;
import org.example.tears.Api.ApiException;
import org.example.tears.Enums.ServiceOption;
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

        // ================= CREATE =================
        public Coupon create(Coupon coupon) {
            coupon.setCreatedAt(LocalDateTime.now());
            coupon.setUsedCount(0);
            return couponRepository.save(coupon);
        }

        // ================= GET ALL =================
        public List<Coupon> getAll() {
            return couponRepository.findAll();
        }

        // ================= DISABLE =================
        public void disable(Integer id) {

            Coupon coupon = couponRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("الكوبون غير موجود"));

            coupon.setActive(false);
            couponRepository.save(coupon);
        }

        // ================= VALIDATE =================
        public Coupon validate(String code, int totalPrice, ServiceOption option) {

            Coupon coupon = couponRepository.findByCodeIgnoreCase(code)
                    .orElseThrow(() -> new ApiException("الكوبون غير موجود"));

            if (!coupon.isActive())
                throw new ApiException("الكوبون غير مفعل");

            if (coupon.getExpiryDate() != null &&
                    coupon.getExpiryDate().isBefore(LocalDate.now()))
                throw new ApiException("الكوبون منتهي");

            if (coupon.getUsageLimit() != null &&
                    coupon.getUsedCount() >= coupon.getUsageLimit())
                throw new ApiException("تم استهلاك الكوبون");

            if (coupon.getMinimumOrderPrice() != null &&
                    totalPrice < coupon.getMinimumOrderPrice())
                throw new ApiException("الحد الأدنى للطلب غير متحقق");

            if (coupon.getServiceOption() != null &&
                    coupon.getServiceOption() != option)
                throw new ApiException("الكوبون غير صالح لهذه الخدمة");

            return coupon;
        }

    public int applyDiscount(Coupon coupon, int total) {

        int discount = 0;

        if (coupon.getDiscountPercentage() != null) {
            discount += (total * coupon.getDiscountPercentage()) / 100;

            if (coupon.getMaxDiscountAmount() != null &&
                    discount > coupon.getMaxDiscountAmount()) {
                discount = coupon.getMaxDiscountAmount();
            }
        }

        if (coupon.getFixedDiscount() != null) {
            discount += coupon.getFixedDiscount();
        }

        return Math.max(total - discount, 0);
    }

    public void markUsed(Coupon coupon) {
        coupon.setUsedCount(coupon.getUsedCount() + 1);
        couponRepository.save(coupon);
    }
}