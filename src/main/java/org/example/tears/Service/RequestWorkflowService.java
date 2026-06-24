package org.example.tears.Service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.example.tears.Api.ApiException;
import org.example.tears.DTO.EmployeeRequestResponseDto;
import org.example.tears.DTO.EmployeeSummaryDto;
import org.example.tears.DTO.ReportDto;
import org.example.tears.DTO.RequestSummaryDto;
import org.example.tears.Enums.PricingStatus;
import org.example.tears.Enums.StaffRequestActionRule;
import org.example.tears.Enums.StaffRequestStatus;
import org.example.tears.Enums.WorkflowStage;
import org.example.tears.Model.*;
import org.example.tears.OutDTO.RequestResponseDto;
import org.example.tears.Repository.*;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RequestWorkflowService {

    private final CarServiceRequestRepository requestRepo;
    private final EmployeeRepository employeeRepo;
    private final RequestNoteRepository noteRepo;
    private final RequestReportRepository reportRepo;
    private final RequestStatusHistoryRepository historyRepo;
    private final NotificationService notificationService;
    private final RequestImageRepository imageRepo;
    private final RequestPartRepository partRepo;
    private final FileStorageService fileStorageService;

    @Transactional
    public void updateStatus(
            Integer requestId,
            StaffRequestStatus status,
            Integer employeeId,
            String note,
            String imageUrl
    ) {

        CarServiceRequest req = requestRepo.findById(requestId)
                .orElseThrow(() -> new RuntimeException("الطلب غير موجود"));

        if (req.getAssignedEmployee() == null ||
                !employeeId.equals(req.getAssignedEmployee().getId())) {
            throw new RuntimeException("غير مصرح لك");
        }

        StaffRequestActionRule rule =
                StaffRequestActionRule.fromStatus(status);

        // 📝 ملاحظة
        if (rule.isRequiresNote() &&
                (note == null || note.isBlank())) {
            throw new RuntimeException("يجب إضافة ملاحظة لهذه الحالة");
        }

        // 📸 صور
        if (imageUrl != null && !imageUrl.isBlank()) {

            long count = imageRepo.countByRequest_Id(req.getId());

            if (count >= 5) {
                throw new RuntimeException("لا يمكن رفع أكثر من 5 صور");
            }

            RequestImage image = new RequestImage();
            image.setRequest(req);
            image.setImageUrl(imageUrl);
            image.setUploadedAt(LocalDateTime.now());
            image.setUploadedAtStatus(status);

            imageRepo.save(image);

            if (status == StaffRequestStatus.RECEIVED
                    && req.getReceivedImageUrl() == null) {
                req.setReceivedImageUrl(imageUrl);
            }
        }

        // 🔄 الحالة
        req.setStaffStatus(status);
        req.setLastUpdated(LocalDateTime.now());

        if (status == StaffRequestStatus.PARTS_REGISTERING) {
            req.setPricingStatus(PricingStatus.PRICING);
        }

        updateStaffTimestamps(req, status);

        if (note != null && !note.isBlank()) {
            saveNote(req, employeeId, note);
        }

        saveHistory(req, employeeId);

        requestRepo.save(req);

        notificationService.send(
                req.getCustomer().getUser(),
                "تم تحديث حالة طلبك رقم #" + req.getId()
        );
    }





    // =========================
        // حفظ الأوقات
        // =========================
        private void updateStaffTimestamps(
                CarServiceRequest req,
                StaffRequestStatus status
        ) {
            LocalDateTime now = LocalDateTime.now();

            switch (status) {

                case RECEIVED -> req.setReceivedAt(now);

                case INSPECTION_IN_PROGRESS -> req.setInspectionAt(now);

                case TESTING -> req.setTestingAt(now);

                case PRICING -> req.setPricingAt(now);

                case REPAIRING -> req.setRepairAt(now);

                case DELIVERED -> req.setDeliveredAt(now);
            }
        }



        // =========================
        // حفظ ملاحظة
        // =========================
        private void saveNote(
                CarServiceRequest req,
                Integer empId,
                String note
        ) {

            RequestNote n = new RequestNote();

            n.setRequest(req);
            n.setEmployeeId(empId);
            n.setNote(note);
            n.setCreatedAt(LocalDateTime.now());

            noteRepo.save(n);
        }


        // =========================
        // حفظ History
        // =========================
        private void saveHistory(
                CarServiceRequest req,
                Integer empId
        ) {

            RequestStatusHistory h = new RequestStatusHistory();

            h.setRequest(req);
            h.setStaffStatus(req.getStaffStatus());
            h.setCustomerStatus(req.getCustomerStatus());
            h.setPricingStatus(req.getPricingStatus());

            h.setChangedBy(empId);
            h.setChangedAt(LocalDateTime.now());

            historyRepo.save(h);
        }


    public String mapStaffStatus(StaffRequestStatus status) {
        return switch (status) {
            case NEW -> "جديدة";
            case RECEIVED -> "تم الاستلام";
            case INSPECTION_IN_PROGRESS -> "جاري الفحص";
            case TESTING -> "قيد التجربة";
            case REPORT_WRITING -> "إرفاق التقرير";
            case PARTS_REGISTERING -> "تسجيل القطع";
            case PRICING -> "جاري تسعير القطع";
            case REPAIRING -> "جاري الإصلاح";
            case DELIVERED -> "تم التسليم";
        };
    }

    private String mapStageToArabic(WorkflowStage stage) {

            if (stage == null) return "غير محدد";

            return switch (stage) {

                case NEW_REQUEST -> "طلب جديد";
                case ASSIGNED -> "تم الإسناد";
                case RECEIVED -> "تم الاستلام";
                case INSPECTION_IN_PROGRESS -> "قيد الفحص";
                case TESTING -> "تجربة";
                case REPORT_WRITING -> "كتابة التقرير";
                case PARTS_REGISTERING -> "تسجيل القطع";
                case PRICING -> "قيد التسعير";
                case WAITING_APPROVAL -> "بانتظار الموافقة";
                case REPAIRING -> "قيد الإصلاح";
                case READY -> "جاهز";
                case DELIVERED -> "تم التسليم";
                case CANCELLED -> "ملغي";
            };
        }
    private CarServiceRequest getRequest(Integer id) {
        return requestRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("الطلب غير موجود"));
    }

    private void validateEmployee(CarServiceRequest req, Integer employeeId) {
        if (req.getAssignedEmployee() == null ||
                !req.getAssignedEmployee().getId().equals(employeeId)) {
            throw new RuntimeException("غير مصرح لك");
        }
    }

    @Transactional
    public void updateWorkshopStatus(
            Integer requestId,
            StaffRequestStatus status,
            Integer employeeId,
            String note,
            String imageUrl
    ) {

        CarServiceRequest req = getRequest(requestId);

        validateWorkshopEmployee(req, employeeId);

        req.setStaffStatus(status);
        req.setLastUpdated(LocalDateTime.now());

        switch (status) {

            case RECEIVED -> {
                if (imageUrl == null)
                    throw new RuntimeException("صورة مطلوبة");

                req.setReceivedImageUrl(imageUrl);
                req.setReceivedAt(LocalDateTime.now());
            }

            case INSPECTION_IN_PROGRESS -> req.setInspectionAt(LocalDateTime.now());

            case TESTING -> req.setTestingAt(LocalDateTime.now());

            case REPORT_WRITING -> {
                req.setLastUpdated(LocalDateTime.now());
            }

            case PARTS_REGISTERING -> {
                req.setLastUpdated(LocalDateTime.now());

                // 🔥 تحويل للتسعير
                req.setPricingStatus(PricingStatus.PRICING);

                assignPricingEmployee(req);
            }
        }

        if (note != null && !note.isBlank()) {
            saveNote(req, employeeId, note);
        }

        saveHistory(req, employeeId);
        requestRepo.save(req);

        notifyCustomer(req);
    }

    private void notifyCustomer(CarServiceRequest req) {

        if (req.getCustomer() == null) return;

        notificationService.send(
                req.getCustomer().getUser(),
                "تم تحديث طلبك #" + req.getOrderNumber()
        );
    }

    private void validateWorkshopEmployee(CarServiceRequest req, Integer employeeId) {

        if (req.getAssignedEmployee() == null ||
                !req.getAssignedEmployee().getId().equals(employeeId)) {
            throw new RuntimeException("غير مصرح لك");
        }
    }


    private void applyWorkshopLogic(
            CarServiceRequest req,
            StaffRequestStatus status,
            String imageUrl
    ) {

        LocalDateTime now = LocalDateTime.now();

        switch (status) {

            case RECEIVED -> {
                if (imageUrl == null) {
                    throw new RuntimeException("صورة الاستلام مطلوبة");
                }
                req.setReceivedImageUrl(imageUrl);
                req.setReceivedAt(now);
            }

            case INSPECTION_IN_PROGRESS -> req.setInspectionAt(now);

            case TESTING -> req.setTestingAt(now);

            case PARTS_REGISTERING -> {

                req.setRepairAt(now);

                // 🔥 تحويل للتسعير
                req.setPricingStatus(PricingStatus.PRICING);

                assignPricingEmployee(req);
            }

            case REPAIRING -> req.setRepairAt(now);

            case DELIVERED -> req.setDeliveredAt(now);
        }
    }

    private void validateTransition(StaffRequestStatus current, StaffRequestStatus next) {

        if (current == null) return;

        switch (current) {

            case NEW -> {
                if (next != StaffRequestStatus.RECEIVED)
                    throw new RuntimeException("يجب استلام السيارة أولاً");
            }

            case RECEIVED -> {
                if (next != StaffRequestStatus.INSPECTION_IN_PROGRESS)
                    throw new RuntimeException("ابدأ الفحص أولاً");
            }

            case INSPECTION_IN_PROGRESS -> {
                if (next != StaffRequestStatus.TESTING)
                    throw new RuntimeException("يجب التجربة أولاً");
            }

            case TESTING -> {
                if (next != StaffRequestStatus.PARTS_REGISTERING)
                    throw new RuntimeException("سجل القطع أولاً");
            }

            case PARTS_REGISTERING -> {
                throw new RuntimeException("الطلب انتقل للتسعير");
            }

            case REPAIRING -> {
                if (next != StaffRequestStatus.DELIVERED)
                    throw new RuntimeException("يجب إكمال الإصلاح");
            }

            case DELIVERED -> {
                throw new RuntimeException("الطلب مكتمل");
            }
        }
    }

    private void assignPricingEmployee(CarServiceRequest req) {
        List<Employee> employees =
                employeeRepo.findPricingEmployees();

        if(employees.isEmpty()){
            throw new RuntimeException("لا يوجد موظف تسعير");
        }

        Employee emp = employees.stream()
                .min(Comparator.comparingLong(
                        e -> requestRepo.countByAssignedEmployee_Id(e.getId())
                ))
                .orElseThrow();
    }

    public ReportDto preview(Integer requestId){

        RequestReport report =
                reportRepo.findByRequest_Id(requestId)
                        .orElseThrow(() ->
                                new ApiException("No report"));

        ReportDto dto = new ReportDto();

        dto.setContent(report.getReportContent());
        dto.setFileUrl(report.getFileUrl());
        dto.setCreatedAt(report.getCreatedAt());

        return dto;
    }

    @Transactional
    public void sendReport(Integer requestId){

        RequestReport report =
                reportRepo.findByRequest_Id(requestId)
                        .orElseThrow(() ->
                                new ApiException("No report"));

        CarServiceRequest request = report.getRequest();

        // 1. إرسال التقرير
        report.setSent(true);
        reportRepo.save(report);

        // 2. تحديث حالة الطلب
        request.setPricingStatus(PricingStatus.PRICED);

        requestRepo.save(request);

        // 3. إشعار العميل
        notificationService.send(
                request.getCustomer().getUser(),
                "تم إرسال تقرير الفحص لطلبك #" + request.getOrderNumber()
        );
    }


    private void saveNoteIfNeeded(CarServiceRequest req, Integer empId, String note) {
        if (note == null || note.isBlank()) return;

        RequestNote n = new RequestNote();
        n.setRequest(req);
        n.setEmployeeId(empId);
        n.setNote(note);
        n.setCreatedAt(LocalDateTime.now());

        noteRepo.save(n);
    }



}
