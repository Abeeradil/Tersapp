package org.example.tears.Controller;

import lombok.RequiredArgsConstructor;
import org.example.tears.Api.ApiResponse;
import org.example.tears.DTO.PricingRequestDto;
import org.example.tears.Model.Employee;
import org.example.tears.Model.User;
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

    @PutMapping("/requests/{requestId}/prising")
    public ApiResponse pricingRequest(
            @PathVariable Integer requestId,
            @RequestBody PricingRequestDto dto,
            @AuthenticationPrincipal User user
    ){

        requestPricingService.pricingRequest(
                requestId,
                dto,
                user.getEmployee()
        );

        return new ApiResponse(true,"تم حفظ التسعير");
    }
    }