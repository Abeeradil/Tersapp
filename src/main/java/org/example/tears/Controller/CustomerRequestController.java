package org.example.tears.Controller;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.example.tears.Api.ApiResponse;
import org.example.tears.DTO.CheckoutResponse;
import org.example.tears.DTO.CustomerModifyReportDto;
import org.example.tears.DTO.ReportPreviewDto;
import org.example.tears.DTO.UpdatePartsDto;
import org.example.tears.Model.User;
import org.example.tears.OutDTO.RequestResponseDto;
import org.example.tears.Service.AuthService;
import org.example.tears.Service.CarServiceRequestService;
import org.example.tears.Service.PaymentIntentService;
import org.example.tears.Service.RequestApprovalService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

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


        // طلباتي
        @GetMapping("/my")
        public List<RequestResponseDto> myRequests(
                @AuthenticationPrincipal User user
        ) {
            return requestService
                    .getMyRequests(user.getCustomer().getId());
        }

    @GetMapping("/requests/{requestId}/report/download")
    public ResponseEntity<byte[]> downloadReport(
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
    public ReportPreviewDto getReport(
            @PathVariable Integer requestId
    ) {
        return requestApprovalService.getReport(requestId);
    }


        // الموافقة على التقرير
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
            @PathVariable Integer requestId,
            @RequestParam(required = false) String note
    ) {

        requestApprovalService.reject(requestId, note);

        return new ApiResponse(
                true,
                "تم رفض التقرير"
        );
    }

    @PutMapping("/{requestId}/modify")
    public ApiResponse modifyReport(
            @PathVariable Integer requestId,
            @RequestBody CustomerModifyReportDto dto
    ) {

        requestApprovalService.requestModification(
                requestId,
                dto
        );

        return new ApiResponse(
                true,
                "تم إرسال طلب التعديل"
        );
    }


    }

