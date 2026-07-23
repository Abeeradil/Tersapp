package org.example.tears.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.example.tears.Api.ApiException;
import org.example.tears.DTO.*;
import org.example.tears.Enums.*;
import org.example.tears.Model.*;
import org.example.tears.Repository.*;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

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
    private final FileStorageService fileStorageService;


    @Transactional
    public void updateStatus(
            Integer requestId,
            StaffRequestStatus status,
            Integer employeeId,
            String note) {

        CarServiceRequest req = requestRepo.findById(requestId)
                .orElseThrow(() -> new ApiException("الطلب غير موجود"));

        if (req.getAssignedEmployee() == null ||
                !employeeId.equals(req.getAssignedEmployee().getId())) {
            throw new ApiException("غير مصرح لك");
        }

        req.setCustomerStatus(
                mapCustomerStatus(status)
        );

        if (
                status == StaffRequestStatus.PRICING ) {
            throw new ApiException(
                    "هذه الحالة لها إجراء خاص"
            );
        }

        if (req.getStaffStatus() == StaffRequestStatus.RECEIVED ||
                req.getStaffStatus() == StaffRequestStatus.REPORT_WRITING
        ) {
            throw new ApiException(
                    "هذه الحالة لها إجراء خاص"
            );
        }

        StaffRequestActionRule rule =
                StaffRequestActionRule.fromStatus(status);

        if (
                status.ordinal()
                        <= req.getStaffStatus().ordinal()
        ) {
            throw new ApiException(
                    "لا يمكن الرجوع لحالة سابقة"
            );
        }

        // 📝 ملاحظة
        if (rule.isRequiresNote() &&
                (note == null || note.isBlank())) {
            throw new ApiException("يجب إضافة ملاحظة لهذه الحالة");
        }

        validateStatusTransition(
                req.getStaffStatus(),
                status
        );

        // 📸 صور

        // 🔄 الحالة
        req.setStaffStatus(status);
        switch (status) {

            case DELIVERY_IN_PROGRESS ->
                req.setCustomerStatus(CustomerRequestStatus.READY_FOR_DELIVERY);

            case DELIVERED -> {
                req.setCustomerStatus(CustomerRequestStatus.DELIVERED);
                req.setStage(WorkflowStage.DELIVERED);
            }

            default ->
                req.setCustomerStatus(mapCustomerStatus(status));

        }

        if (status == StaffRequestStatus.DELIVERY_IN_PROGRESS) {

            notificationService.send(
                    req.getCustomer().getUser(),
                    "تم تجهيز سيارتك للتسليم، وسيتم تسليمها في الموعد الذي اخترته."
            );
        }

        if (status == StaffRequestStatus.DELIVERED) {

            notificationService.send(
                    req.getCustomer().getUser(),
                    "تم تسليم سيارتك بنجاح، شكرًا لاستخدامك خدماتنا."
            );
        }

        req.setLastUpdated(LocalDateTime.now());

        if (status == StaffRequestStatus.PRICING) {

            Employee pricingEmployee =
                    getLeastBusyPricingEmployee();

            req.setAssignedPricingEmployee(pricingEmployee);
            req.setCurrentEmployee(pricingEmployee);

            req.setPricingStatus(PricingStatus.PRICING);

            notificationService.send(
                    pricingEmployee.getUser(),
                    "تم إسناد طلب جديد للتسعير رقم #" + req.getId()
            );
        }

        updateStaffTimestamps(req, status);

        Employee employee = employeeRepo.findById(employeeId)
                .orElseThrow(() -> new ApiException("الموظف غير موجود"));

        saveNote(req, employee, note);

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

        // الحالات التي لها Endpoints خاصة (RECEIVED أُزيلت — تُدخل عبر /status)
        if (next == StaffRequestStatus.REPAIRING) {

            throw new ApiException("هذه الحالة لها عملية خاصة");
        }

        switch (current) {

            case NEW -> {
                if (next != StaffRequestStatus.RECEIVED)
                    throw new ApiException("انتقال غير صحيح");
            }

            case RECEIVED ->
                    throw new ApiException("استخدم زر استلام السيارة");

            case INSPECTION_IN_PROGRESS -> {
                if (next != StaffRequestStatus.TESTING)
                    throw new ApiException("انتقال غير صحيح");
            }

            case TESTING -> {
                if (next != StaffRequestStatus.PARTS_REGISTERING)
                    throw new ApiException("انتقال غير صحيح");
            }

            case PARTS_REGISTERING -> {
                if (next != StaffRequestStatus.PRICING)
                    throw new ApiException("انتقال غير صحيح");
            }

            case PRICING ->
                throw new ApiException("استخدم أزرار المسعر");

            case REPORT_WRITING -> {
                if (next != StaffRequestStatus.REPAIRING)
                    throw new ApiException("انتقال غير صحيح");
            }

            case REPAIRING -> {
                if (next != StaffRequestStatus.DELIVERY_IN_PROGRESS &&
                        next != StaffRequestStatus.PARTS_REGISTERING) {

                    throw new ApiException("انتقال غير صحيح");
                }

            }
            case DELIVERY_IN_PROGRESS -> {
                if (next != StaffRequestStatus.DELIVERED)
                    throw new ApiException("انتقال غير صحيح");
            }

            default ->
                    throw new ApiException("لا يمكن تحديث هذه الحالة من هنا");
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
                .orElseThrow(() ->
                        new ApiException("الطلب غير موجود"));

        if (req.getAssignedEmployee() == null ||
                !employeeId.equals(req.getAssignedEmployee().getId())) {

            throw new ApiException("غير مصرح لك");
        }

        if (req.getStaffStatus() != StaffRequestStatus.RECEIVED) {
            throw new ApiException("الطلب ليس في حالة الاستلام");
        }

        // ===========================
        // Validation
        // ===========================

        long currentImages = imageRepo.countByRequest(req);

        long newImages = images == null ? 0 : images.size();

        if (currentImages + newImages > 5) {
            throw new ApiException("الحد الأقصى لصور الطلب هو 5 صور");
        }

        // ===========================
        // Upload Images
        // ===========================

        if (images != null) {

            for (MultipartFile file : images) {

                if (file.isEmpty()) {
                    throw new ApiException("يوجد ملف فارغ");
                }

                // 10MB
                if (file.getSize() > 10 * 1024 * 1024) {
                    throw new ApiException("حجم الملف لا يجب أن يتجاوز 10MB");
                }

                String contentType = file.getContentType();

                if (contentType == null ||
                        !(contentType.startsWith("image/")
                                || contentType.equals("application/pdf"))) {

                    throw new ApiException("يسمح فقط برفع الصور أو ملفات PDF");
                }

                String fileUrl =
                        fileStorageService.saveFile(file, "receipts");

                RequestImage image = new RequestImage();

                image.setRequest(req);
                image.setImageUrl(fileUrl);
                image.setUploadedAt(LocalDateTime.now());
                image.setUploadedAtStatus(StaffRequestStatus.RECEIVED);

                imageRepo.save(image);

                if (req.getReceivedImageUrl() == null) {
                    req.setReceivedImageUrl(fileUrl);
                }
            }
        }

        // ===========================
        // Update Request
        // ===========================

        req.setStaffStatus(StaffRequestStatus.INSPECTION_IN_PROGRESS);
        req.setCustomerStatus(CustomerRequestStatus.CAR_RECEIVED);
        req.setStage(WorkflowStage.RECEIVED);

        updateStaffTimestamps(
                req,
                StaffRequestStatus.INSPECTION_IN_PROGRESS
        );

        Employee employee = employeeRepo.findById(employeeId)
                .orElseThrow(() ->
                        new ApiException("الموظف غير موجود"));

        saveNote(req, employee, note);

        saveHistory(req, employeeId);

        requestRepo.save(req);

        // ===========================
        // Notification
        // ===========================

        notificationService.send(
                req.getCustomer().getUser(),
                "تم استلام السيارة وبدء مرحلة الفحص لطلب رقم #" + req.getId()
        );
    }


    public List<TimelineItemDto> getTimeline(Integer requestId){

        CarServiceRequest req = requestRepo.findById(requestId)
                .orElseThrow(() -> new ApiException("الطلب غير موجود"));

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


    // =========================
        // حفظ الأوقات
        // =========================
    private void updateStaffTimestamps(
            CarServiceRequest req,
            StaffRequestStatus status
    ) {

        LocalDateTime now = LocalDateTime.now();

        req.setLastUpdated(now);

        switch (status) {

            case RECEIVED ->
                    req.setReceivedAt(now);

            case INSPECTION_IN_PROGRESS ->
                req.setInspectionAt(now);

            case TESTING ->
                    req.setTestingAt(now);

            case PRICING ->
                    req.setPricingAt(now);

            case REPAIRING ->
                    req.setRepairAt(now);

            case DELIVERY_IN_PROGRESS -> {
                // إذا عندك حقل deliveryStartedAt أضيفيه هنا
                // req.setDeliveryStartedAt(now);
            }

            case DELIVERED ->
                    req.setDeliveredAt(now);
        }
    }



        // =========================
        // حفظ ملاحظة
        // =========================
        private void saveNote(
            CarServiceRequest req,
            Employee employee,
            String note
    ){

            RequestNote n = new RequestNote();

            n.setRequest(req);
            n.setEmployee(employee);
            n.setNote(note);
            n.setStep(req.getStaffStatus());
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




    private void validateWorkshopEmployee(CarServiceRequest req, Integer employeeId) {

        if (req.getAssignedEmployee() == null ||
                !req.getAssignedEmployee().getId().equals(employeeId)) {
            throw new ApiException("غير مصرح لك");
        }
    }


    public Employee getLeastBusyPricingEmployee() {

        List<Employee> pricingEmployees =
                employeeRepo.findByEmployeeRole(EmployeeRole.PRICING);

        if (pricingEmployees.isEmpty()) {
            throw new ApiException("لا يوجد موظف تسعير");
        }

        Employee selected = null;
        long min = Long.MAX_VALUE;

        for (Employee employee : pricingEmployees) {

            long count = requestRepo
                    .countByAssignedPricingEmployee_IdAndPricingStatusIn(
                            employee.getId(),
                            List.of(
                                    PricingStatus.NEW,
                                    PricingStatus.PRICING
                            )
                    );

            if (count < min) {
                min = count;
                selected = employee;
            }
        }

        return selected;
    }

//    public ReportDto preview(
//            Integer requestId,
//            Employee employee
//    ) {
//
//        RequestReport report =
//                getAccessibleReport(requestId, employee);
//
//        CarServiceRequest request = report.getRequest();
//
//        ReportDto dto = new ReportDto();
//
//        dto.setCustomerName(
//                request.getCustomer().getUser().getFullName()
//        );
//
//        dto.setOrderNumber(
//                request.getOrderNumber()
//        );
//
//        dto.setCarModel(
//                request.getCar().getModel().getName()
//        );
//
//        dto.setProblemDescription(
//                request.getProblemDescription()
//        );
//
//        dto.setReportNumber(
//                report.getReportNumber()
//        );
//
//        dto.setVersion(
//                report.getVersion()
//        );
//
//        return dto;
//    }

    @Transactional
    public void sendToCustomer(
            Integer requestId,
            Employee assignedEmployee
    ) {

        CarServiceRequest request =
                requestRepo.findById(requestId)
                        .orElseThrow(() ->
                                new ApiException("الطلب غير موجود"));

        if (request.getCurrentEmployee() == null ||
                !request.getCurrentEmployee().getId().equals(assignedEmployee.getId())) {

            throw new ApiException("الطلب غير مسند لك");
        }

        if (request.getPricingStatus() != PricingStatus.PRICED) {
            throw new ApiException("يجب إنهاء التسعير أولاً");
        }

        if (request.getFinalPrice() == null || request.getFinalPrice() <= 0) {
            throw new ApiException("لا يمكن إرسال تقرير بدون سعر نهائي");
        }

        RequestReport report =
                reportRepo.findByRequest_IdAndLatestTrue(requestId)
                        .orElseThrow(() ->
                                new ApiException("لا يوجد تقرير"));

        report.setSent(true);
        request.setCustomerStatus(CustomerRequestStatus.WAITING_APPROVAL);

        request.setLastUpdated(LocalDateTime.now());

        reportRepo.save(report);
        requestRepo.save(request);

        notificationService.send(
                request.getCustomer().getUser(),
                "تم إرسال تقرير التسعير، بانتظار موافقتك."
        );
    }





}
