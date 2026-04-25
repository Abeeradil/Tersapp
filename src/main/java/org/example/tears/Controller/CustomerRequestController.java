package org.example.tears.Controller;

import lombok.RequiredArgsConstructor;
import org.example.tears.Api.ApiResponse;
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
        private final RequestApprovalService approvalService;


        // طلباتي
        @GetMapping("/my")
        public List<RequestResponseDto> myRequests(
                @AuthenticationPrincipal User user
        ) {
            return requestService
                    .getMyRequests(user.getCustomer().getId());
        }


        // الموافقة على التقرير
        @PutMapping("/{id}/approve")
        public ApiResponse approveReport(
                @PathVariable Integer id
                ,String note
        ) {

            approvalService.approve(id,note);

            return new ApiResponse(true,"تمت الموافقة على التقرير");
        }


        // تعديل القطع
        @PutMapping("/{id}/parts")
        public ApiResponse updateParts(
                @PathVariable Integer id,
                @RequestBody UpdatePartsDto dto
        ) {

            approvalService.updateParts(id, dto);

            return new ApiResponse(true,"تم تحديث القطع");
        }
    }

