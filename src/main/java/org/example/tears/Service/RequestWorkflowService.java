package org.example.tears.Service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.example.tears.Api.ApiException;
import org.example.tears.DTO.*;
import org.example.tears.Enums.*;
import org.example.tears.Model.*;
import org.example.tears.OutDTO.RequestResponseDto;
import org.example.tears.Repository.*;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import static org.example.tears.Enums.CustomerRequestStatus.UNDER_REPAIR;

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
            String note) {

        CarServiceRequest req = requestRepo.findById(requestId)
                .orElseThrow(() -> new RuntimeException("الطلب غير موجود"));

        if (req.getAssignedEmployee() == null ||
                !employeeId.equals(req.getAssignedEmployee().getId())) {
            throw new RuntimeException("غير مصرح لك");
        }

        req.setCustomerStatus(
                mapCustomerStatus(status)
        );

        if (
                status == StaffRequestStatus.RECEIVED ||
                        status == StaffRequestStatus.PARTS_REGISTERING ||
                        status == StaffRequestStatus.PRICING ||
                        status == StaffRequestStatus.REPAIRING||
                        status == StaffRequestStatus.DELIVERY_IN_PROGRESS
        ) {
            throw new RuntimeException(
                    "هذه الحالة لها إجراء خاص"
            );
        }        StaffRequestActionRule rule =
                StaffRequestActionRule.fromStatus(status);

        if(
                status.ordinal()
                        <= req.getStaffStatus().ordinal()
        ){
            throw new RuntimeException(
                    "لا يمكن الرجوع لحالة سابقة"
            );
        }

        // 📝 ملاحظة
        if (rule.isRequiresNote() &&
                (note == null || note.isBlank())) {
            throw new RuntimeException("يجب إضافة ملاحظة لهذه الحالة");
        }

        validateStatusTransition(
                req.getStaffStatus(),
                status
        );

        // 📸 صور

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
    private void validateStatusTransition(
            StaffRequestStatus current,
            StaffRequestStatus next
    ) {

        // هذه لها endpoints خاصة
        if (next == StaffRequestStatus.RECEIVED ||
                next == StaffRequestStatus.PARTS_REGISTERING ||
                next == StaffRequestStatus.PRICING ||
                next == StaffRequestStatus.REPAIRING) {

            throw new RuntimeException("هذه الحالة لها عملية خاصة");
        }

        switch (current){

            case NEW -> {
                if(next != StaffRequestStatus.INSPECTION_IN_PROGRESS)
                    throw new RuntimeException("انتقال غير صحيح");
            }

            case INSPECTION_IN_PROGRESS -> {
                if(next != StaffRequestStatus.TESTING)
                    throw new RuntimeException("انتقال غير صحيح");
            }

            case TESTING -> {
                if(next != StaffRequestStatus.REPORT_WRITING)
                    throw new RuntimeException("انتقال غير صحيح");
            }

            case REPORT_WRITING -> {
                if(next != StaffRequestStatus.DELIVERED)
                    throw new RuntimeException("انتقال غير صحيح");
            }

            case DELIVERED ->
                    throw new RuntimeException("تم إغلاق الطلب");

            default ->
                    throw new RuntimeException("لا يمكن تحديث هذه الحالة من هنا");
        }
    }

    @Transactional
    public void receiveCar(
            Integer requestId,
            Integer employeeId,
            String note,
            List<MultipartFile> images
    ) {

        CarServiceRequest req = requestRepo.findById(requestId)
                .orElseThrow(() -> new RuntimeException("الطلب غير موجود"));

        if (req.getAssignedEmployee() == null ||
                !employeeId.equals(req.getAssignedEmployee().getId())) {
            throw new RuntimeException("غير مصرح لك");
        }

        // حد أقصى 5 ملفات
        if (images != null && images.size() > 5) {
            throw new RuntimeException("الحد الأقصى 5 ملفات");
        }

        if (images != null) {

            for (MultipartFile file : images) {

                // التحقق من النوع
                String contentType = file.getContentType();

                if (contentType == null ||
                        !(contentType.startsWith("image/")
                                || contentType.equals("application/pdf"))) {

                    throw new RuntimeException("يسمح فقط بالصور أو PDF");
                }

                String fileUrl =
                        fileStorageService.saveFile(file, "receipts");

                RequestImage image = new RequestImage();
                image.setRequest(req);
                image.setImageUrl(fileUrl);
                image.setUploadedAt(LocalDateTime.now());
                image.setUploadedAtStatus(StaffRequestStatus.RECEIVED);

                imageRepo.save(image);

                // أول صورة نخزنها كصورة رئيسية
                if (req.getReceivedImageUrl() == null) {
                    req.setReceivedImageUrl(fileUrl);
                }
            }
        }



        req.setStaffStatus(StaffRequestStatus.RECEIVED);
        req.setReceivedAt(LocalDateTime.now());
        req.setCustomerStatus(CustomerRequestStatus.CAR_RECEIVED);
        req.setStage(WorkflowStage.RECEIVED);
        req.setLastUpdated(LocalDateTime.now());

        if (note != null && !note.isBlank()) {
            saveNote(req, employeeId, note);
        }

        saveHistory(req, employeeId);

        requestRepo.save(req);
    }


    public List<TimelineItemDto> getTimeline(Integer requestId){

        CarServiceRequest req = requestRepo.findById(requestId)
                .orElseThrow(() -> new RuntimeException("الطلب غير موجود"));

        List<TimelineItemDto> timeline = new ArrayList<>();

        timeline.add(new TimelineItemDto(
                "إنشاء الطلب",
                StaffRequestStatus.NEW,
                req.getCreatedAt(),
                req.getCreatedAt() != null,
                req.getStaffStatus() == StaffRequestStatus.NEW
        ));

        timeline.add(new TimelineItemDto(
                "تم استلام السيارة",
                StaffRequestStatus.RECEIVED,
                req.getReceivedAt(),
                req.getReceivedAt() != null,
                req.getStaffStatus() == StaffRequestStatus.RECEIVED
        ));

        timeline.add(new TimelineItemDto(
                "جاري الفحص",
                StaffRequestStatus.INSPECTION_IN_PROGRESS,
                req.getInspectionAt(),
                req.getInspectionAt() != null,
                req.getStaffStatus() == StaffRequestStatus.INSPECTION_IN_PROGRESS
        ));

        timeline.add(new TimelineItemDto(
                "قيد التجربة",
                StaffRequestStatus.TESTING,
                req.getTestingAt(),
                req.getTestingAt() != null,
                req.getStaffStatus() == StaffRequestStatus.TESTING
        ));

        timeline.add(new TimelineItemDto(
                "تسجيل القطع",
                StaffRequestStatus.PARTS_REGISTERING,
                req.getLastUpdated(),
                req.getStaffStatus().ordinal() >= StaffRequestStatus.PARTS_REGISTERING.ordinal(),
                req.getStaffStatus() == StaffRequestStatus.PARTS_REGISTERING
        ));

        timeline.add(new TimelineItemDto(
                "جاري التسعير",
                StaffRequestStatus.PRICING,
                req.getPricingAt(),
                req.getPricingAt() != null,
                req.getStaffStatus() == StaffRequestStatus.PRICING
        ));

        timeline.add(new TimelineItemDto(
                "جاري الإصلاح",
                StaffRequestStatus.REPAIRING,
                req.getRepairAt(),
                req.getRepairAt() != null,
                req.getStaffStatus() == StaffRequestStatus.REPAIRING
        ));

        timeline.add(new TimelineItemDto(
                "تم التسليم",
                StaffRequestStatus.DELIVERED,
                req.getDeliveredAt(),
                req.getDeliveredAt() != null,
                req.getStaffStatus() == StaffRequestStatus.DELIVERED
        ));

        return timeline;
    }

    private TimelineItemDto createTimeline(
            String title,
            StaffRequestStatus status,
            LocalDateTime date) {

        TimelineItemDto dto = new TimelineItemDto();

        dto.setTitle(title);
        dto.setStatus(status);
        dto.setCompleted(date != null);
        return dto;
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
    private CustomerRequestStatus mapCustomerStatus(StaffRequestStatus status) {

        return switch (status) {

            case NEW -> CustomerRequestStatus.REQUEST_CREATED;

            case RECEIVED -> CustomerRequestStatus.CAR_RECEIVED;

            case INSPECTION_IN_PROGRESS,
                 TESTING, PARTS_REGISTERING, PRICING -> CustomerRequestStatus.CAR_INSPECTION;

            case REPORT_WRITING -> CustomerRequestStatus.WAITING_APPROVAL;

            case REPAIRING -> UNDER_REPAIR;

            case DELIVERY_IN_PROGRESS -> CustomerRequestStatus.READY_FOR_DELIVERY;

            case DELIVERED -> CustomerRequestStatus.DELIVERED;
        };
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
            case DELIVERY_IN_PROGRESS -> "جاري التسليم";
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
        req.setAssignedPricingEmployee(emp);

        requestRepo.save(req);
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



}
