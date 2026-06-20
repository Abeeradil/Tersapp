package org.example.tears.Controller;

import lombok.RequiredArgsConstructor;
import org.example.tears.Api.ApiResponse;
import org.example.tears.DTO.EmployeeRequestResponseDto;
import org.example.tears.DTO.PartDto;
import org.example.tears.DTO.UpdateStatusDTO;
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
    private final ReportService reportService;
    private final RequestQueryService requestQueryService;
    private final FileStorageService fileStorageService;

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
        return requestQueryService.getMyRequestsCount(user.getEmployee());
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


    @PostMapping("/{id}/report")
    public ApiResponse uploadReport(
            @PathVariable Integer id,
            @RequestParam MultipartFile file,
            @RequestParam String description,
            @AuthenticationPrincipal User user
    ) {

        String url = fileStorageService.saveFile(file, "reports");

        reportService.uploadReport(
                id,
                user.getEmployee().getId(),
                url,
                description
        );

        return new ApiResponse(true, "تم رفع التقرير");
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
                dto.getNote(),
                dto.getImageUrl() // ممكن null
        );

        return new ApiResponse(true, "تم تحديث الحالة بنجاح");
    }

    @PostMapping(value = "/{id}/receive", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse receiveCar(
            @PathVariable Integer id,
            @RequestParam(required = false) String note,
            @RequestParam("image") MultipartFile image,
            @AuthenticationPrincipal User user
    ) {

        String imageUrl = fileStorageService.saveFile(image, "receipts");

        workflowService.updateStatus(
                id,
                StaffRequestStatus.RECEIVED,
                user.getEmployee().getId(),
                note,
                imageUrl
        );

        return new ApiResponse(true, "تم استلام السيارة ورفع الصورة بنجاح");
    }



    // إضافة قطعة
    @PostMapping("/{id}/parts")
    public ApiResponse addPart(
            @PathVariable Integer id,
            @RequestBody PartDto dto
    ) {

        partsService.addPart(id, dto);

        return new ApiResponse(true,"تمت إضافة القطعة");
    }


}
