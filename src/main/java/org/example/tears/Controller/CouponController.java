package org.example.tears.Controller;

import lombok.RequiredArgsConstructor;
import org.example.tears.Enums.ServiceOption;
import org.example.tears.InpDTO.CreateCouponRequest;
import org.example.tears.InpDTO.UpdateCouponRequest;
import org.example.tears.InpDTO.ValidateCouponDto;
import org.example.tears.Model.Coupon;
import org.example.tears.Service.CouponService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/tears/coupons")
@RequiredArgsConstructor
public class CouponController {

        private final CouponService couponService;

        @PostMapping("/new")
        public Coupon create(@RequestBody CreateCouponRequest dto) {
            return couponService.create(dto);
        }

        @PatchMapping("/update/{id}")
        public Coupon update(@PathVariable Integer id,
                             @RequestBody UpdateCouponRequest dto) {
            return couponService.update(id, dto);
        }

        @GetMapping("/all")
        public List<Coupon> getAll() {
            return couponService.getAll();
        }
    @PostMapping("/validate")
    public ResponseEntity<?> validate(@RequestBody ValidateCouponDto dto) {

        ServiceOption option = ServiceOption.valueOf(dto.getServiceOption());

        // هنا لازم يكون عندك total من السعر
        int total = dto.getTotalPrice();

        Coupon coupon = couponService.validate(
                dto.getCouponCode(),
                total,
                option
        );

        int finalPrice = couponService.applyDiscount(coupon, total);

        return ResponseEntity.ok(finalPrice);
    }

        @PutMapping("/disable/{id}")
        public Coupon disable(@PathVariable Integer id) {
            return couponService.disable(id);
        }
}