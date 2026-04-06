package org.example.tears.Controller;

import lombok.RequiredArgsConstructor;
import org.example.tears.Api.ApiResponse;
import org.example.tears.Model.Employee;
import org.example.tears.Service.PricingService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/tears/pricing")
@RequiredArgsConstructor
@PreAuthorize("hasRole('PRICING')")
public class PricingController {

    private final PricingService pricingService;


    // استلام طلب للتسعير
    @PutMapping("/requests/{id}/start")
    public ApiResponse startPricing(
            @PathVariable Integer id,
            @AuthenticationPrincipal Employee pricingEmp
    ) {

        pricingService.startPricing(
                id,pricingEmp

        );

        return new ApiResponse("تم استلام الطلب للتسعير");
    }


    // تسعير قطعة
    @PutMapping("/parts/{id}/price")
    public ApiResponse setPartPrice(
            @PathVariable Integer id,
            @RequestParam Integer price
    ) {

        pricingService.setFinalPrice(id, price);

        return new ApiResponse("تم تحديث السعر");
    }


    // إنهاء التسعير
    @PutMapping("/requests/{id}/finish")
    public ApiResponse finishPricing(@PathVariable Integer id) {

        pricingService.finishPricing(id);

        return new ApiResponse("تم إنهاء التسعير");
    }
}
