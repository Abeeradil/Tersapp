package org.example.tears.Controller;

import lombok.RequiredArgsConstructor;
import org.example.tears.Api.ApiResponse;
import org.example.tears.DTO.PricingRequestCardDto;
import org.example.tears.DTO.PricingRequestDetailsDto;
import org.example.tears.DTO.PricingRequestDto;
import org.example.tears.Model.User;
import org.example.tears.Service.PricingQueryService;
import org.example.tears.Service.RequestPricingService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/tears/pricing")
@RequiredArgsConstructor
public class PricingController {

        private final RequestPricingService requestPricingService;
        private final PricingQueryService pricingQueryService;

    @GetMapping("/my/requests")
    public List<PricingRequestCardDto> myRequests(
            @AuthenticationPrincipal User user
    ) {
        return pricingQueryService.getMyRequests(
                user.getEmployee()
        );
    }


    @GetMapping("/requests/{id}/details")
    public PricingRequestDetailsDto getDetails(
            @PathVariable Integer id,
            @AuthenticationPrincipal User user
    ) {

        return pricingQueryService.getRequestDetails(
                id,
                user.getEmployee()
        );
    }


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