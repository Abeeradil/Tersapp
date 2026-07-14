package org.example.tears.Controller;

import lombok.RequiredArgsConstructor;
import org.example.tears.Api.ApiResponse;
import org.example.tears.DTO.CustomerModifyReportDto;
import org.example.tears.DTO.ReportPreviewDto;
import org.example.tears.DTO.UpdatePartsDto;
import org.example.tears.Model.User;
import org.example.tears.OutDTO.RequestResponseDto;
import org.example.tears.Service.CarServiceRequestService;
import org.example.tears.Service.RequestApprovalService;
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

        private final CarServiceRequestService requestService;
        private final RequestApprovalService requestApprovalService;


        // طلباتي
        @GetMapping("/my")
        public List<RequestResponseDto> myRequests(
                @AuthenticationPrincipal User user
        ) {
            return requestService
                    .getMyRequests(user.getCustomer().getId());
        }

    @GetMapping("/requests/{requestId}/report")
    public ReportPreviewDto getReport(
            @PathVariable Integer requestId
    ) {
        return requestApprovalService.getReport(requestId);
    }


        // الموافقة على التقرير
        @PutMapping("/{requestId}/approve")
        public ApiResponse approveReport(
                @PathVariable Integer requestId,
                @RequestParam(required = false) String note
        ) {

            requestApprovalService.approve(requestId, note);

            return new ApiResponse(
                    true,
                    "تمت الموافقة على التقرير"
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

