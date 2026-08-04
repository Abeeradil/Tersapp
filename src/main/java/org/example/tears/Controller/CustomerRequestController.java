package org.example.tears.Controller;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.example.tears.Api.ApiResponse;
import org.example.tears.DTO.*;
import org.example.tears.Model.User;
import org.example.tears.OutDTO.RequestResponseDto;
import org.example.tears.Service.*;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
    @RequestMapping("/api/v1/tears/customer/requests")
    @RequiredArgsConstructor
    @PreAuthorize("hasRole(\"CUSTOMER\")")
    public class CustomerRequestController {

        private final AuthService authService;
        private final CarServiceRequestService requestService;
        private final RequestApprovalService requestApprovalService;
        private final PaymentIntentService paymentIntentService;
    private final WarrantyService warrantyService;



    // طلباتي
        @GetMapping("/my")
        public List<RequestResponseDto> myRequests(
                @AuthenticationPrincipal User user
        ) {
            return requestService
                    .getMyRequests(user.getCustomer().getId());
        }



    @GetMapping("/requests/{requestId}/report/download")
    public ResponseEntity<byte[]> downloadCustomerReport(
            @PathVariable Integer requestId,
            HttpServletRequest request
    ) throws Exception {

        User user = authService.getAuthenticatedUser(request);

        return requestApprovalService.downloadCustomerReport(
                requestId,
                user.getCustomer()
        );
    }

    @GetMapping("/requests/{requestId}/report")
    public ApiResponse previewCustomerReport(
            @PathVariable Integer requestId,
            HttpServletRequest request
    ){

        User user = authService.getAuthenticatedUser(request);

        return new ApiResponse(
                true,
                "تم جلب التقرير",
                requestApprovalService.getReport(
                        requestId,
                        user.getCustomer()
                )
        );
    }


        // الموافقة على التقرير
        @PutMapping("/{requestId}/approve")
        public ApiResponse approveReport(
                @PathVariable Integer requestId,
                @RequestParam(required = false) String note
        ){
            requestApprovalService.approve(requestId, note);

            return new ApiResponse(
                    true,
                    "تمت الموافقة على التقرير"
            );
        }

        @PostMapping("/{requestId}/final-payment")
        public CheckoutResponse createFinalPayment(
                @PathVariable Integer requestId,
                HttpServletRequest request
        ){

            return paymentIntentService.createFinalCheckout(
                    requestId,
                    request
            );
        }

    @PutMapping("/{requestId}/reject")
    public ApiResponse rejectReport(
            @PathVariable Integer requestId) {

        requestApprovalService.reject(requestId);

        return new ApiResponse(
                true,
                "تم رفض التقرير"
        );
    }

    @PutMapping("/{requestId}/modify")
    public ApiResponse modifyReport(
            @PathVariable Integer requestId,
            @RequestBody CustomerModifyReportDto dto) {

        requestApprovalService.requestModification(
                requestId,
                dto);

        return new ApiResponse(
                true,
                "تم إرسال طلب التعديل"
        );
    }

    @PutMapping("/{requestId}/delivery")
    public ApiResponse scheduleDelivery(
            @PathVariable Integer requestId,
            @RequestBody DeliveryRequestDto dto,
            HttpServletRequest request
    ){

        User user = authService.getAuthenticatedUser(request);

        requestApprovalService.chooseDelivery(
                requestId,
                dto,
                user.getCustomer()
        );

        return new ApiResponse(
                true,
                "تم تحديد موعد التسليم"
        );
    }

    @PostMapping("/{requestId}/warranty")
    public ApiResponse createWarranty(
            @PathVariable Integer requestId,
            @RequestPart WarrantyRequestDto dto,
            @RequestPart(required = false) List<MultipartFile> images,
            HttpServletRequest request
    ) {

        User user = authService.getAuthenticatedUser(request);

        warrantyService.createWarrantyRequest(
                requestId,
                dto,
                images,
                user.getCustomer()
        );

        return new ApiResponse(
                true,
                "تم إرسال طلب الضمان"
        );
    }


    @GetMapping("/warranty")
    public List<WarrantyResponseDto> myWarrantyRequests(
            HttpServletRequest request
    ){

        User user = authService.getAuthenticatedUser(request);

        return warrantyService.getCustomerWarrantyRequests(
                user.getCustomer()
        );
    }

    @GetMapping("/warranty/{id}")
    public WarrantyDetailsDto details(
            @PathVariable Integer id,
            HttpServletRequest request
    ){

        User user = authService.getAuthenticatedUser(request);

        return warrantyService.details(
                id,
                user.getCustomer()
        );
    }


}

