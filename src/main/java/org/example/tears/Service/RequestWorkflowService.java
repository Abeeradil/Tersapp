package org.example.tears.Service;

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
    private final WarrantyStatusHistoryRepository warrantyHistoryRepos;
    private final NotificationService notificationService;
    private final RequestImageRepository imageRepo;
    private final SocketService socketService;
    private final FileStorageService fileStorageService;
    private final WarrantyRepository warrantyRepo;
    private final WarrantyService warrantyService;
    private final RequestApprovalService requestApprovalService;
    private final CarServiceRequestService carServiceRequestService;

    @Transactional
    public void updateStatus(
            Integer requestId,
            StaffRequestStatus status,
            Integer employeeId,
            String note) {

        CarServiceRequest req = requestRepo.findById(requestId)
                .orElseThrow(() ->
                        new ApiException("الطلب غير موجود"));

        if (req.getAssignedTechnician() == null ||
                !employeeId.equals(req.getAssignedTechnician().getId())) {

            throw new ApiException("غير مصرح لك");
        }

        if (status == StaffRequestStatus.PRICING) {
            throw new ApiException("هذه الحالة لها إجراء خاص");
        }

        if (req.getStaffStatus() == StaffRequestStatus.RECEIVED ||
                req.getStaffStatus() == StaffRequestStatus.REPORT_WRITING) {

            throw new ApiException("هذه الحالة لها إجراء خاص");
        }

        validateStatusTransition(
                req.getStaffStatus(),
                status
        );

        if (status == StaffRequestStatus.DELIVERED &&
                !Boolean.TRUE.equals(req.getCustomerSelectedDelivery())) {

            throw new ApiException(
                    "لا يمكن إنهاء الطلب قبل أن يحدد العميل موعد وطريقة التسليم"
            );
        }

        req.setStaffStatus(status);

        switch (status) {

            case DELIVERY_IN_PROGRESS ->
                    req.setCustomerStatus(
                            CustomerRequestStatus.READY_FOR_DELIVERY
                    );

            case DELIVERED -> {
                req.setCustomerStatus(
                        CustomerRequestStatus.DELIVERED
                );

                req.setStage(
                        WorkflowStage.DELIVERED
                );
            }

            default ->
                    req.setCustomerStatus(
                            mapCustomerStatus(status)
                    );
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

        updateStaffTimestamps(req, status);

        Employee employee =
                employeeRepo.findById(employeeId)
                        .orElseThrow(() ->
                                new ApiException("الموظف غير موجود"));

        saveNote(req, employee, note);

        saveHistory(req, employeeId);

        requestRepo.save(req);

        socketService.send(
                "/topic/current-orders/" +
                        req.getCustomer().getUser().getId(),
                carServiceRequestService.toCurrentDto(req)
        );

        socketService.send(
                "/topic/request/" + req.getId(),
                carServiceRequestService.toResponseDto(req)
        );

        notificationService.send(
                req.getCustomer().getUser(),
                "تم تحديث حالة طلبك رقم #" + req.getId()
        );
    }


    private void validateStatusTransition(
            StaffRequestStatus current,
            StaffRequestStatus next) {

        switch (current) {

            case NEW -> {
                if (next != StaffRequestStatus.RECEIVED) {
                    throw new ApiException("انتقال غير صحيح");
                }
            }

            case RECEIVED ->
                    throw new ApiException(
                            "استخدم زر استلام السيارة"
                    );

            case INSPECTION_IN_PROGRESS -> {
                if (next != StaffRequestStatus.PARTS_REGISTERING) {
                    throw new ApiException("انتقال غير صحيح");
                }
            }

            case PARTS_REGISTERING -> {
                if (next != StaffRequestStatus.PRICING) {
                    throw new ApiException("انتقال غير صحيح");
                }
            }

            case REPORT_WRITING -> {
                if (next != StaffRequestStatus.REPAIRING) {
                    throw new ApiException("انتقال غير صحيح");
                }
            }

            case REPAIRING -> {
                if (next != StaffRequestStatus.TESTING &&
                        next != StaffRequestStatus.PARTS_REGISTERING) {

                    throw new ApiException("انتقال غير صحيح");
                }
            }

            case TESTING -> {
                if (next != StaffRequestStatus.DELIVERY_IN_PROGRESS) {
                    throw new ApiException("انتقال غير صحيح");
                }
            }

            case DELIVERY_IN_PROGRESS -> {
                if (next != StaffRequestStatus.DELIVERED) {
                    throw new ApiException("انتقال غير صحيح");
                }
            }

            default ->
                    throw new ApiException(
                            "لا يمكن تحديث هذه الحالة"
                    );
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

        if (req.getAssignedTechnician() == null ||
                !employeeId.equals(req.getAssignedTechnician().getId())) {

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

        socketService.send(
                "/topic/current-orders/" +
                        req.getCustomer().getUser().getId(),
                carServiceRequestService.toCurrentDto(req)
        );

        socketService.send(
                "/topic/request/" + req.getId(),
                carServiceRequestService.toResponseDto(req)
        );

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

            case PARTS_REGISTERING ->
                    req.setPartsRegisteredAt(now);

            case REPORT_WRITING ->
                    req.setReportWrittenAt(now);

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
            Employee assignedTechnicialEmployee
    ) {

        CarServiceRequest request =
                requestRepo.findById(requestId)
                        .orElseThrow(() ->
                                new ApiException("الطلب غير موجود"));

        if (request.getCurrentEmployee() == null ||
                !request.getCurrentEmployee()
                        .getId()
                        .equals(assignedTechnicialEmployee.getId())) {

            throw new ApiException("الطلب غير مسند لك");
        }

        if (request.getPricingStatus() != PricingStatus.PRICED) {
            throw new ApiException("يجب إنهاء التسعير أولاً");
        }

        if (request.getFinalPrice() == null ||
                request.getFinalPrice() <= 0) {

            throw new ApiException(
                    "لا يمكن إرسال تقرير بدون سعر نهائي"
            );
        }

        RequestReport report =
                reportRepo.findByRequest_IdAndLatestTrue(requestId)
                        .orElseThrow(() ->
                                new ApiException("لا يوجد تقرير"));

        if (Boolean.TRUE.equals(report.isSent())) {
            throw new ApiException("تم إرسال التقرير مسبقاً");
        }

        report.setSent(true);

        request.setCustomerStatus(
                CustomerRequestStatus.WAITING_APPROVAL
        );

        request.setReportSentAt(
                LocalDateTime.now()
        );

        request.setLastUpdated(
                LocalDateTime.now()
        );

        reportRepo.save(report);
        requestRepo.save(request);

        // ==========================================
        // تحديث التقرير لحظياً
        // ==========================================

        socketService.send(
                "/topic/report/" + requestId,
                requestApprovalService.getReport(
                        requestId,
                        request.getCustomer()
                )
        );

        // ==========================================
        // تحديث الطلب لحظياً
        // ==========================================

        socketService.send(
                "/topic/current-orders/" +
                        request.getCustomer()
                                .getUser()
                                .getId(),
                carServiceRequestService.toCurrentDto(request)
        );

        // ==========================================
        // Notification
        // ==========================================

        notificationService.send(
                request.getCustomer().getUser(),
                "تم إرسال تقرير التسعير، بانتظار موافقتك."
        );
    }

    @Transactional
    public void updateWarrantyStatus(
            Integer warrantyId,
            WarrantyStatus newStatus,
            Integer employeeId
    ) {

        WarrantyRequest warranty =
                warrantyRepo.findById(warrantyId)
                        .orElseThrow(() ->
                                new ApiException("طلب الضمان غير موجود"));

        Employee employee =
                employeeRepo.findById(employeeId)
                        .orElseThrow(() ->
                                new ApiException("الموظف غير موجود"));

        if (warranty.getAssignedTechnician() == null ||
                !warranty.getAssignedTechnician()
                        .getId()
                        .equals(employeeId)) {

            throw new ApiException("طلب الضمان غير مسند إليك");
        }


        WarrantyStatus current = warranty.getStatus();

        if (current == newStatus) {
            throw new ApiException(
                    "طلب الضمان بالفعل في هذه الحالة"
            );
        }
        // ============================
        // APPROVED → RECEIVED
        // ============================

        if (current == WarrantyStatus.APPROVED &&
                newStatus == WarrantyStatus.RECEIVED) {

            if (warranty.getReceivingDate() == null ||
                    warranty.getReceivingTime() == null ||
                    warranty.getReceivingLocation() == null) {

                throw new ApiException(
                        "لم يحدد العميل موعد استلام السيارة"
                );
            }

            LocalDateTime now = LocalDateTime.now();

            warranty.setStatus(
                    WarrantyStatus.RECEIVED
            );

            warranty.setUpdatedAt(now);

            warrantyRepo.save(warranty);

            saveWarrantyHistory(
                    warranty,
                    WarrantyStatus.RECEIVED,
                    WarrantyCustomerStatus.VEHICLE_RECEIVED,
                    employee
            );

            // WebSocket
            socketService.send(
                    "/topic/warranty/" +
                            warranty.getCustomer()
                                    .getUser()
                                    .getId(),
                    warrantyService.toResponseDto(warranty)
            );

            socketService.send(
                    "/topic/warranty-details/" +
                            warranty.getId(),
                    warrantyService.toDetailsDto(warranty)
            );

            socketService.send(
                    "/topic/current-orders/" +
                            warranty.getCustomer()
                                    .getUser()
                                    .getId(),
                    carServiceRequestService.toCurrentDto(
                            warranty.getRequest()
                    )
            );

            notificationService.send(
                    warranty.getCustomer().getUser(),
                    "تم تأكيد موعد استلام السيارة لطلب الضمان."
            );

            return;
        }

        if (current == WarrantyStatus.RECEIVED) {
            throw new ApiException(
                    "هذه الحالة لها إجراء خاص، استخدم زر استلام السيارة"
            );
        }

        validateWarrantyTransition(current, newStatus);

        if (newStatus == WarrantyStatus.DELIVERED) {

            if (warranty.getDeliveryLocation() == null ||
                    warranty.getDeliveryDate() == null ||
                    warranty.getDeliveryTime() == null) {

                throw new ApiException(
                        "لا يمكن تسليم السيارة، العميل لم يحدد موقع وموعد التسليم"
                );
            }
        }

        LocalDateTime now = LocalDateTime.now();

        warranty.setStatus(newStatus);
        warranty.setUpdatedAt(now);

        switch (newStatus) {

            case REPAIRING ->
                    warranty.setRepairStartedAt(now);

            case DELIVERED ->
                    warranty.setDeliveredAt(now);

            default -> {
            }
        }

        warrantyRepo.save(warranty);

        saveWarrantyHistory(
                warranty,
                newStatus,
                WarrantyCustomerStatus.VEHICLE_RECEIVED,
                employee
        );

        if (current == WarrantyStatus.APPROVED &&
                newStatus == WarrantyStatus.RECEIVED) {

            if (warranty.getReceivingDate() == null ||
                    warranty.getReceivingTime() == null ||
                    warranty.getReceivingLocation() == null) {

                throw new ApiException(
                        "لم يحدد العميل موعد استلام السيارة"
                );
            }

            warranty.setStatus(WarrantyStatus.RECEIVED);
            warranty.setUpdatedAt(LocalDateTime.now());

            warrantyRepo.save(warranty);

            saveWarrantyHistory(
                    warranty,
                    WarrantyStatus.RECEIVED,
                    WarrantyCustomerStatus.VEHICLE_RECEIVED,
                    employee
            );

            socketService.send(
                    "/topic/warranty/" +
                            warranty.getCustomer()
                                    .getUser()
                                    .getId(),
                    warrantyService.toResponseDto(warranty)
            );

            socketService.send(
                    "/topic/warranty-details/" +
                            warranty.getId(),
                    warrantyService.toDetailsDto(warranty)
            );

            return;
        }

// ============================
// WebSocket
// ============================

        socketService.send(
                "/topic/warranty/" +
                        warranty.getCustomer()
                                .getUser()
                                .getId(),
                warrantyService.toResponseDto(warranty)
        );

        socketService.send(
                "/topic/warranty-details/" +
                        warranty.getId(),
                warrantyService.toDetailsDto(warranty)
        );

        socketService.send(
                "/topic/current-orders/" +
                        warranty.getCustomer()
                                .getUser()
                                .getId(),
                carServiceRequestService.toCurrentDto(
                        warranty.getRequest()
                )
        );

// ============================
// Notification
// ============================

        notificationService.send(
                warranty.getCustomer().getUser(),
                "تم تحديث حالة طلب الضمان رقم #"
                        + warranty.getId()
        );

    }


    private void validateWarrantyTransition(
            WarrantyStatus current,
            WarrantyStatus next
    ) {

        switch (current) {

            case INSPECTION -> {

                if (next != WarrantyStatus.REPAIRING) {
                    throw new ApiException("بعد الفحص يجب بدء الإصلاح");
                }
            }

            case REPAIRING -> {

                if (next != WarrantyStatus.TESTING) {
                    throw new ApiException("بعد الإصلاح يجب الانتقال للتجربة");
                }
            }

            case TESTING -> {

                if (next != WarrantyStatus.DELIVERY_IN_PROGRESS) {
                    throw new ApiException(
                            "بعد التجربة يجب تجهيز السيارة للتسليم"
                    );
                }
            }

            case DELIVERY_IN_PROGRESS -> {

                if (next != WarrantyStatus.DELIVERED) {
                    throw new ApiException("يجب تسليم السيارة");
                }
            }

            default ->
                    throw new ApiException(
                            "لا يمكن تحديث حالة الضمان الحالية"
                    );
        }
    }



    private void saveWarrantyHistory(
            WarrantyRequest warranty,
            WarrantyStatus employeeStatus,
            WarrantyCustomerStatus customerStatus,
            Employee employee
    ) {

        WarrantyStatusHistory history =
                new WarrantyStatusHistory();

        history.setWarrantyRequest(warranty);

        history.setEmployeeStatus(employeeStatus);

        history.setCustomerStatus(customerStatus);

        history.setChangedBy(employee);

        history.setChangedAt(LocalDateTime.now());

        warrantyHistoryRepos.save(history);
    }

    public List<WarrantyStatusHistoryDto> getWarrantyTimeline(
            Integer warrantyId
    ) {

        return warrantyHistoryRepos
                .findByWarrantyRequest_IdOrderByChangedAtAsc(warrantyId)
                .stream()
                .map(history -> {

                    WarrantyStatusHistoryDto dto =
                            new WarrantyStatusHistoryDto();

                    dto.setEmployeeStatus(history.getEmployeeStatus());
                    dto.setChangedAt(history.getChangedAt());

                        if (history.getChangedBy().getUser() != null) {
                            dto.setEmployeeName(
                                    history.getChangedBy()
                                            .getUser()
                                            .getFullName()
                            );
                        }

                    return dto;
                })
                .toList();
    }

    @Transactional
    public void receiveWarrantyCar(
            Integer warrantyId,
            Integer employeeId,
            List<MultipartFile> images
    ) {

        WarrantyRequest warranty =
                warrantyRepo.findById(warrantyId)
                        .orElseThrow(() ->
                                new ApiException("طلب الضمان غير موجود"));

        Employee employee =
                employeeRepo.findById(employeeId)
                        .orElseThrow(() ->
                                new ApiException("الموظف غير موجود"));

        if (warranty.getAssignedTechnician() == null ||
                !employeeId.equals(
                        warranty.getAssignedTechnician().getId()
                )) {

            throw new ApiException("غير مصرح لك");
        }

        if (warranty.getStatus() != WarrantyStatus.RECEIVED) {
            throw new ApiException(
                    "طلب الضمان ليس في حالة استلام السيارة"
            );
        }

        if (images == null || images.isEmpty()) {
            throw new ApiException(
                    "يجب إرفاق صورة واحدة على الأقل عند استلام السيارة"
            );
        }

        warrantyService.saveImages(
                warranty,
                images,
                WarrantyImageType.CAR_RECEIVED
        );

        LocalDateTime now = LocalDateTime.now();

        warranty.setStatus(WarrantyStatus.INSPECTION);
        warranty.setUpdatedAt(now);

        warrantyRepo.save(warranty);

        saveWarrantyHistory(
                warranty,
                WarrantyStatus.INSPECTION,
                WarrantyCustomerStatus.VEHICLE_RECEIVED,
                employee
        );

        socketService.send(
                "/topic/warranty/" +
                        warranty.getCustomer()
                                .getUser()
                                .getId(),
                warrantyService.toResponseDto(warranty)
        );

        socketService.send(
                "/topic/warranty-details/" +
                        warranty.getId(),
                warrantyService.toDetailsDto(warranty)
        );

        socketService.send(
                "/topic/current-orders/" +
                        warranty.getCustomer()
                                .getUser()
                                .getId(),
                carServiceRequestService.toCurrentDto(
                        warranty.getRequest()
                )
        );



        notificationService.send(
                warranty.getCustomer().getUser(),
                "تم استلام السيارة لطلب الضمان وبدأت مرحلة الفحص."
        );
    }


}
