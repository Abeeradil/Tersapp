package org.example.tears.Controller;

import lombok.RequiredArgsConstructor;
import org.example.tears.Api.ApiResponse;
import org.example.tears.DTO.EmployeeRequestResponseDto;
import org.example.tears.DTO.PartDto;
import org.example.tears.DTO.ReportDto;
import org.example.tears.DTO.UpdateStatusDTO;
import org.example.tears.Enums.StaffRequestStatus;
import org.example.tears.Model.CarServiceRequest;
import org.example.tears.Model.User;
import org.example.tears.OutDTO.RequestResponseDto;
import org.example.tears.Service.PartsService;
import org.example.tears.Service.ReportService;
import org.example.tears.Service.RequestWorkflowService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

@RestController
    @RequestMapping("/api/v1/tears/employee/requests")
    @RequiredArgsConstructor
    @PreAuthorize("hasRole('EMPLOYEE')")
    public class EmployeeRequestController {

    private final RequestWorkflowService workflowService;
    private final PartsService partsService;
    private final ReportService reportService;

    // طلباتي
    @GetMapping("/my")
    public List<EmployeeRequestResponseDto> myRequests(
            @AuthenticationPrincipal User user
    ) {
        return workflowService
                .getEmployeeRequests(user.getCustomer().getId());
    }

    // طلبات موظف - للتجربة بدون توكن
    @GetMapping("/my-test")
    public ResponseEntity<?> myRequestsTest(@RequestParam Integer employeeId) {
        var requests = workflowService.getEmployeeRequests(employeeId);

        return ResponseEntity.ok(Map.of(
                "success", true,
                "data", requests
        ));
    }

    // تغيير حالة الطلب
    @PutMapping("/{id}/status")
    public ApiResponse updateStatus(
            @PathVariable Integer id,
            @RequestBody UpdateStatusDTO dto,
            @AuthenticationPrincipal User user
    ) {

        workflowService.updateStaffStatus(
                id,
                dto.getStatus(),
                user.getEmployee().getId(),
                dto.getNote()
        );

        return new ApiResponse("تم تحديث الحالة بنجاح");
    }

    // 👇 نسخة مؤقتة لتجربة تغيير الحالة بدون توكن
    @PutMapping("/test/{id}/status")
    public ApiResponse updateStatusTest(
            @PathVariable Integer id,
            @RequestBody UpdateStatusDTO dto
    ) {
        // مستخدم وهمي للتجربة
        User mockUser = new User();
        mockUser.setId(1); // حطي أي EmployeeId موجود
        mockUser.setEmployee(mockUser.getEmployee()); // أو أنشئي Employee وهمي بنفس الـ Id

        workflowService.updateStaffStatus(
                id,
                dto.getStatus(),
                mockUser.getEmployee().getId(),
                dto.getNote()
        );

        return new ApiResponse("تم تحديث الحالة بنجاح (Test)");
    }

    @PostMapping("/{id}/receive")
    public ApiResponse receiveCar(
            @PathVariable Integer id,
            @RequestParam(required = false) String note,
            @RequestParam("imageUrl") String imageUrl,
            @AuthenticationPrincipal User user
    ) {
        workflowService.updateStatus(
                id,
                StaffRequestStatus.RECEIVED,
                user.getId(),
                note,
                imageUrl
        );
        return new ApiResponse("تم استلام السيارة ورفع الصورة بنجاح");
    }

    @PostMapping("/test/{id}/receive")
    public ApiResponse receiveCarTest(
            @PathVariable Integer id,
            @RequestParam(required = false) String note,
            @RequestParam("imageUrl") String imageUrl
    ) {
        User mockUser = new User();
        mockUser.setId(1); // EmployeeId وهمي

        workflowService.updateStatus(
                id,
                StaffRequestStatus.RECEIVED,
                mockUser.getId(),
                note,
                imageUrl
        );

        return new ApiResponse("تم استلام السيارة ورفع الصورة بنجاح (Test)");
    }


    // إضافة قطعة
    @PostMapping("/{id}/parts")
    public ApiResponse addPart(
            @PathVariable Integer id,
            @RequestBody PartDto dto
    ) {

        partsService.addPart(id, dto);

        return new ApiResponse("تمت إضافة القطعة");
    }


    // رفع تقرير
        @PostMapping("/{requestId}/report")
        public ResponseEntity<?> uploadReport(
                @PathVariable Integer requestId,
                @RequestPart("file") MultipartFile file,
                @RequestPart("description") String description
        ) {
            try {
                // هنا نرفع التقرير
                String fileUrl = saveFileAndGetUrl(file); // دالة تحفظ الملف وترجع الرابط
                reportService.uploadReport(requestId, fileUrl, description);

                return ResponseEntity.ok(Map.of(
                        "success", true,
                        "message", "تم رفع التقرير بنجاح"
                ));
            } catch (RuntimeException e) {
                return ResponseEntity.status(403).body(Map.of(
                        "success", false,
                        "message", e.getMessage()
                ));
            }
        }

        // مثال بسيط لحفظ الملف (تقدر تعدل حسب مشروعك)
        private String saveFileAndGetUrl(MultipartFile file) {
            // حفظ الملف في السيرفر أو رفعه للسحابة
            // وإرجاع رابط الوصول له
            String filename = System.currentTimeMillis() + "_" + file.getOriginalFilename();
            Path path = Paths.get("uploads/" + filename);
            try {
                Files.createDirectories(path.getParent());
                file.transferTo(path);
            } catch (IOException e) {
                throw new RuntimeException("فشل رفع الملف");
            }
            return "/uploads/" + filename; // رابط للوصول للملف
        }
    @PostMapping(value = "/{requestId}/report", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> uploadReport(
            @PathVariable Integer requestId,
            @RequestPart("file") MultipartFile file,
            @RequestPart("description") String description,
            @RequestParam Integer employeeId   // ID الموظف
    ) {
        try {
            reportService.handleReportUpload(requestId, employeeId, file, description);
            return ResponseEntity.ok(Map.of("success", true, "message", "تم رفع التقرير وتحديث الحالة"));
        } catch (Exception e) {
            return ResponseEntity.status(500)
                    .body(Map.of("success", false, "message", e.getMessage()));
        }
    }
}
