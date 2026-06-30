package org.example.tears.Controller;

import lombok.RequiredArgsConstructor;
import org.example.tears.Api.ApiResponse;
import org.example.tears.DTO.PricingRequestDto;
import org.example.tears.Model.Employee;
import org.example.tears.Service.PricingCalculationService;
import org.example.tears.Service.RequestPartService;
import org.example.tears.Service.RequestPricingService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/tears/pricing")
@RequiredArgsConstructor
public class PricingController {


        private final RequestPricingService requestPricingService;
        private final RequestPartService requestPartService;

        // =========================
        // Start Pricing
        // =========================
        @PutMapping("/requests/{requestId}/pricing")
        public ApiResponse pricingRequest(
                @PathVariable Integer requestId,
                @RequestBody PricingRequestDto dto
        ){

            requestPricingService.pricingParts(requestId, dto);

            return new ApiResponse(true,"تم حفظ الأسعار");
        }

        // =========================
        // Set Part Price
        // =========================
        @PutMapping("/parts/{id}/price")
        public ApiResponse setPartPrice(
                @PathVariable Integer id,
                @RequestParam Integer price
        ) {

            requestPartService.setFinalPrice(id, price);

            return new ApiResponse(true, "تم تحديث السعر");
        }

        // =========================
        // Finish Pricing
        // =========================
        @PutMapping("/requests/{id}/finish")
        public ApiResponse finishPricing(
                @PathVariable Integer id
        ) {

            requestPricingService.finishPricing(id);

            return new ApiResponse(true, "تم إنهاء التسعير");
        }
    }