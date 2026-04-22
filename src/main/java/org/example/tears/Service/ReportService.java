package org.example.tears.Service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.example.tears.Enums.CustomerRequestStatus;
import org.example.tears.Enums.PricingStatus;
import org.example.tears.Enums.StaffRequestStatus;
import org.example.tears.Enums.WorkflowStage;
import org.example.tears.Model.*;
import org.example.tears.Repository.CarServiceRequestRepository;
import org.example.tears.Repository.RequestPartRepository;
import org.example.tears.Repository.RequestReportRepository;
import org.example.tears.Repository.UserRepository;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class ReportService {

        private final RequestReportRepository reportRepo;
        private final CarServiceRequestRepository requestRepo;
        private final NotificationService notificationService;
         private final UserRepository userRepo;


    @Transactional
        public void uploadReport(Integer requestId, String url, String desc) {

            // 1️⃣ جلب الطلب
            CarServiceRequest req = requestRepo.findById(requestId)
                    .orElseThrow(() -> new RuntimeException("الطلب غير موجود"));

            // 2️⃣ جلب المستخدم الحالي من Spring Security
            String username = SecurityContextHolder.getContext().getAuthentication().getName();

            User currentUser = userRepo.findByPhoneNumber(username) // أو email حسب ما تستخدم كاسم مستخدم
                    .orElseThrow(() -> new RuntimeException("المستخدم غير موجود"));

            if (currentUser.getEmployee() == null) {
                throw new RuntimeException("المستخدم الحالي ليس موظف");
            }

            Integer currentEmployeeId = currentUser.getEmployee().getId();

            // 3️⃣ تحقق أن الموظف مسند لهذا الطلب
            if (req.getAssignedEmployee() == null ||
                    !req.getAssignedEmployee().getId().equals(currentEmployeeId)) {
                throw new RuntimeException("غير مصرح لك برفع تقرير لهذا الطلب");
            }

            // 4️⃣ رفع التقرير
            RequestReport r = new RequestReport();
            r.setRequest(req);
            r.setFileUrl(url);
            r.setDescription(desc);
            r.setCreatedAt(LocalDateTime.now());

            reportRepo.save(r);
        }

    private final String uploadsDir = "C:\\Users\\progects\\Tears\\uploads\\forms\\";

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
        report.setFileUrl(destination.getAbsolutePath());
        report.setDescription(description);
        report.setCreatedAt(LocalDateTime.now());
        reportRepo.save(report);

        // 5️⃣ تحديث حالة الموظف والعميل
        req.setStaffStatus(StaffRequestStatus.RECEIVED);
        req.setCustomerStatus(CustomerRequestStatus.CAR_RECEIVED);
        requestRepo.save(req);

        // 6️⃣ إرسال إشعار للعميل
        notificationService.send(req.getCustomer().getUser(),
                "تم استلام سيارتك، وسيتم متابعة الطلب قريبًا.");    }
}
