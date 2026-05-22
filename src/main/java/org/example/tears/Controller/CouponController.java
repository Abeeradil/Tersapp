package org.example.tears.Controller;

import lombok.RequiredArgsConstructor;
import org.example.tears.Model.Coupon;
import org.example.tears.Repository.CouponRepository;
import org.example.tears.Service.CouponService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/tears/coupons")
@RequiredArgsConstructor
public class CouponController {

    private final CouponService couponService;

    @PostMapping("/new")
    public Coupon create(@RequestBody Coupon coupon) {
        return couponService.create(coupon);
    }

    @GetMapping("/all")
    public List<Coupon> getAll() {
        return couponService.getAll();
    }

    @PutMapping("/update/{id}")
    public Coupon update(
            @PathVariable Integer id,
            @RequestBody Coupon coupon
    ) {
        return couponService.update(id, coupon);
    }


    @PutMapping("/{id}/disable")
    public void disable(@PathVariable Integer id) {
        couponService.disable(id);
    }
}