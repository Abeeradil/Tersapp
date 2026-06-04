package org.example.tears.Controller;

import lombok.RequiredArgsConstructor;
import org.example.tears.Enums.ServiceOption;
import org.example.tears.InpDTO.CreateCouponRequest;
import org.example.tears.InpDTO.UpdateCouponRequest;
import org.example.tears.InpDTO.ValidateCouponDto;
import org.example.tears.Model.Coupon;
import org.example.tears.OutDTO.PricingResponse;
import org.example.tears.Service.CouponService;
import org.example.tears.Service.PricingCalculationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/tears/coupons")
@RequiredArgsConstructor
public class CouponController {

    private final CouponService couponService;
    private final PricingCalculationService pricingCalculationService;

    @PostMapping("/new")
    public Coupon create(@RequestBody CreateCouponRequest dto) {
        return couponService.create(dto);
    }

    @PatchMapping("/update/{id}")
    public Coupon update(
            @PathVariable Integer id,
            @RequestBody UpdateCouponRequest dto
    ) {
        return couponService.update(id, dto);
    }

    @GetMapping("/all")
    public List<Coupon> getAll() {
        return couponService.getAll();
    }

    @PostMapping("/validate")
    public ResponseEntity<PricingResponse> validate(
            @RequestBody ValidateCouponDto dto
    ) {
        PricingResponse response =
                pricingCalculationService.calculateFinal(
                        dto.getServiceOption(),
                        dto.getHydraulicTruck(),
                        dto.getCouponCode()
                );

        return ResponseEntity.ok(response);
    }

    @PutMapping("/disable/{id}")
    public Coupon disable(@PathVariable Integer id) {
        return couponService.disable(id);
    }
}