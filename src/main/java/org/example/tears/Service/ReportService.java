package org.example.tears.Service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.example.tears.Api.ApiException;
import org.example.tears.DTO.CreateReportDto;
import org.example.tears.Enums.CustomerRequestStatus;
import org.example.tears.Enums.PricingStatus;
import org.example.tears.Enums.StaffRequestStatus;
import org.example.tears.Enums.WorkflowStage;
import org.example.tears.Model.*;
import org.example.tears.Repository.CarServiceRequestRepository;
import org.example.tears.Repository.RequestPartRepository;
import org.example.tears.Repository.RequestReportRepository;
import org.example.tears.Repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReportService {

        private final RequestReportRepository reportRepo;
        private final CarServiceRequestRepository requestRepo;
        private final NotificationService notificationService;
        private final RequestPartRepository partRepo;
         private final UserRepository userRepo;

    @Value("${app.upload-dir:uploads}")
    private String uploadsDir;

    @Transactional
    public void uploadReport(
            Integer requestId,
            Integer employeeId,
            String fileUrl,
            String description
    ) {

        CarServiceRequest req =
                requestRepo.findById(requestId)
                        .orElseThrow(() -> new RuntimeException("الطلب غير موجود"));

        if (req.getAssignedEmployee() == null ||
                !req.getAssignedEmployee().getId().equals(employeeId)) {
            throw new RuntimeException("غير مصرح");
        }

        RequestReport report = new RequestReport();
        report.setRequest(req);
        report.setCreatedAt(LocalDateTime.now());

        reportRepo.save(report);
    }

    @Transactional
    public void handleReportUpload(Integer requestId, Integer employeeId,
                                   MultipartFile file, String description) throws IOException {

        // 1️⃣ جلب الطلب
        CarServiceRequest req = requestRepo.findById(requestId)
                .orElseThrow(() -> new RuntimeException("الطلب غير موجود"));

        // 2️⃣ تحقق الموظف
        if (!req.getAssignedEmployee().getId().equals(employeeId)) {
            throw new RuntimeException("أنت غير مسموح لك برفع التقرير لهذا الطلب");
        }

        // 3️⃣ حفظ الملف
        File dir = new File(uploadsDir);
        if (!dir.exists()) dir.mkdirs();

        String filename = System.currentTimeMillis() + "_" + file.getOriginalFilename();
        File destination = new File(dir, filename);
        file.transferTo(destination);

        // 4️⃣ رفع التقرير في قاعدة البيانات
        RequestReport report = new RequestReport();
        report.setRequest(req);
        report.setCreatedAt(LocalDateTime.now());
        reportRepo.save(report);

        // 5️⃣ تحديث حالة الموظف والعميل
        req.setStaffStatus(StaffRequestStatus.RECEIVED);
        req.setCustomerStatus(CustomerRequestStatus.CAR_RECEIVED);
        requestRepo.save(req);

        // 6️⃣ إرسال إشعار للعميل
        notificationService.send(req.getCustomer().getUser(),
                "تم استلام سيارتك، وسيتم متابعة الطلب قريبًا.");    }

    public String generateReport(Integer requestId) {

        CarServiceRequest request =
                requestRepo.findById(requestId)
                        .orElseThrow(() ->
                                new ApiException("Request not found"));

        List<RequestPart> parts =
                partRepo.findByRequestId(requestId);

        StringBuilder report = new StringBuilder();

        report.append("===== تقرير فحص السيارة =====\n\n");

        report.append("رقم الطلب : ")
                .append(request.getOrderNumber())
                .append("\n");

        report.append("اسم العميل : ")
                .append(request.getCustomer().getUser().getFullName())
                .append("\n");

        report.append("رقم الجوال : ")
                .append(request.getCustomer().getUser().getPhoneNumber())
                .append("\n\n");

        report.append("السيارة : ")
                .append(request.getCar().getBrand().getNameAr())
                .append(" ")
                .append(request.getCar().getModel().getNameAr())
                .append("\n");

        report.append("اللوحة : ")
                .append(request.getCar().getPlateNumberArabic())
                .append("\n\n");

        report.append("المشكلة:\n");

        report.append(request.getProblemDescription());

        report.append("\n\n");

        report.append("القطع المطلوبة:\n\n");

        for(RequestPart p : parts){

            report.append("- ");

            report.append(p.getName());

            report.append(" | الكمية : ");

            report.append(p.getQuantity());

            report.append(" | السعر : ");

            report.append(p.getFinalPrice());

            report.append(" ريال");

            report.append("\n");
        }

        report.append("\n");

        report.append("الإجمالي : ");

        report.append(request.getFinalPrice());

        report.append(" ريال");

        return report.toString();

    }

    @Transactional
    public void createReport(
            Integer requestId,
            CreateReportDto dto
    ) {

        CarServiceRequest request = requestRepo.findById(requestId)
                .orElseThrow(() -> new ApiException("الطلب غير موجود"));

        if (request.getPricingStatus() != PricingStatus.PRICED) {
            throw new ApiException("يجب إنهاء التسعير أولاً");
        }

        RequestReport report = reportRepo.findByRequest_Id(requestId)
                .orElse(new RequestReport());

        report.setRequest(request);

        report.setInspectionResult(dto.getInspectionResult());
        report.setTechnicianNotes(dto.getTechnicianNotes());
        report.setRecommendations(dto.getRecommendations());

        if (report.getCreatedAt() == null) {
            report.setCreatedAt(LocalDateTime.now());
        }

        reportRepo.save(report);
    }
}
