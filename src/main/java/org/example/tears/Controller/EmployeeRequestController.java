package org.example.tears.Controller;

import lombok.RequiredArgsConstructor;
import org.example.tears.Api.ApiResponse;
import org.example.tears.DTO.*;
import org.example.tears.Enums.StaffRequestStatus;
import org.example.tears.Model.User;
import org.example.tears.OutDTO.EmployeeRequestDetailsDto;
import org.example.tears.Service.*;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;

@RestController
    @RequestMapping("/api/v1/tears/employee/requests")
    @RequiredArgsConstructor
    @PreAuthorize("hasRole('EMPLOYEE')")
    public class EmployeeRequestController {

    private final RequestWorkflowService workflowService;
    private final PartsService partsService;
    private final RequestQueryService requestQueryService;

    @GetMapping("/my/requests")
    public List<EmployeeRequestResponseDto> myRequests(
            @AuthenticationPrincipal User user
    ) {
        return requestQueryService.getMyRequests(user.getEmployee());
    }

    @GetMapping("/details/{id}")
    public EmployeeRequestDetailsDto getRequestDetails(
            @PathVariable Integer id,
            @AuthenticationPrincipal User user
    ){

        return requestQueryService.getRequestDetails(
                id,
                user.getEmployee()
        );

    }

    @GetMapping("/requests/count/all")
    public ResponseEntity<Long> getAllCount() {
        return ResponseEntity.ok(requestQueryService.getAllRequestsCount());
    }

    @GetMapping("/my/requests/count")
    public long myRequestsCount(@AuthenticationPrincipal User user) {
        return requestQueryService.getMyNewRequestsCount(user.getEmployee());
    }


    @GetMapping("/requests/search")
    public List<RequestSummaryDto> search(

            @RequestParam(required = false)
            String orderNumber,

            @RequestParam(required = false)
            String plateArabic,

            @RequestParam(required = false)
            String plateEnglish
    ) {

        return requestQueryService.search(
                orderNumber,
                plateArabic,
                plateEnglish
        );
    }

    @GetMapping("/my/search")
    public List<EmployeeRequestResponseDto> searchMyRequests(
            @AuthenticationPrincipal User user,
            @RequestParam(required = false) String orderNumber,
            @RequestParam(required = false) String plateArabic,
            @RequestParam(required = false) String plateEnglish
    ) {

        return requestQueryService.searchMyRequests(
                user.getEmployee(),
                orderNumber,
                plateArabic,
                plateEnglish
        );
    }

    @GetMapping("/my/status/requests")
    public List<EmployeeRequestResponseDto> myRequests(
            @AuthenticationPrincipal User user,
            @RequestParam(required = false) StaffRequestStatus status
    ) {

        if (status == null) {
            return requestQueryService.getMyRequests(user.getEmployee());
        }

        return requestQueryService.getMyRequestsByStatus(user.getEmployee(), status);
    }




    @PutMapping("/{id}/status")
    public ApiResponse updateStatus(
            @PathVariable Integer id,
            @RequestBody UpdateStatusDTO dto,
            @AuthenticationPrincipal User user
    ) {
        workflowService.updateStatus(
                id,
                dto.getStatus(),
                user.getEmployee().getId(),
                dto.getNote());

        return new ApiResponse(true, "تم تحديث الحالة بنجاح");
    }


    @PostMapping(value = "/{id}/receive", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse receiveCar(
            @PathVariable Integer id,
            @RequestParam(required = false) String note,
            @RequestPart("images") List<MultipartFile> images,
            @AuthenticationPrincipal User user
    ) {

        workflowService.receiveCar(
                id,
                user.getEmployee().getId(),
                note,
                images
        );

        return new ApiResponse(true, "تم استلام السيارة");
    }

    @GetMapping("/{id}/timeline")
    public ApiResponse getTimeline(@PathVariable Integer id){

        return new ApiResponse(
                true,
                "تم جلب التسلسل الزمني",
                workflowService.getTimeline(id)
        );
    }



    // إضافة قطعة
    @PostMapping("/{id}/add-parts")
    public ApiResponse addParts(
            @PathVariable Integer id,
            @RequestBody AddPartsDto dto
    ) {

        partsService.addParts(id, dto);

        return new ApiResponse(true, "تم تسجيل القطع بنجاح");
    }


    @GetMapping("/{id}/get-parts")
    public ApiResponse getParts(@PathVariable Integer id){

        return new ApiResponse(
                true,
                "تم جلب القطع",
                partsService.getParts(id)
        );
    }

        // معاينة التقرير
        @GetMapping("/requests/{requestId}/report")
        public ApiResponse preview(
                @PathVariable Integer requestId
        ) {

            return new ApiResponse(
                    true,
                    "تم جلب التقرير",
                    workflowService.preview(requestId)
            );
        }


        // إرسال التقرير للعميل
    @PutMapping("/requests/{requestId}/send-to-customer")
    public ApiResponse sendToCustomer(
            @PathVariable Integer requestId,
            @AuthenticationPrincipal User user
    ){

        workflowService.sendToCustomer(
                requestId,
                user.getEmployee()
        );

        return new ApiResponse(true,"تم إرسال التقرير للعميل");
    }

}
