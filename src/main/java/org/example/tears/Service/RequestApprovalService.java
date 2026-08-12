package org.example.tears.Service;

import org.example.tears.Enums.*;
import org.example.tears.Mapper.RequestMapper;
import org.example.tears.Model.*;
import org.example.tears.Repository.*;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.example.tears.Api.ApiException;
import org.example.tears.DTO.*;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;


@Service
    @RequiredArgsConstructor
    public class RequestApprovalService {

        private final RequestApprovalRepository approvalRepo;
        private final RequestPartRepository partRepo;
        private final CarServiceRequestRepository requestRepo;
        private final RequestReportRepository reportRepo;
        private final NotificationService notificationService;
    private final RequestPricingService requestPricingService;
    private final LocationRepository locationRepository;
    private final SocketService socketService;
    private final RequestMapper requestMapper;
    private final WarrantyRepository warrantyRepo;
    private final CarServiceRequestService carServiceRequestService;
    private final WarrantyService warrantyService;
    private final AppointmentService appointmentService;
    private final RequestNoteRepository noteRepo;



    public ResponseEntity<byte[]> downloadCustomerReport(
            Integer requestId,
            Customer customer
    ) throws Exception {

        CarServiceRequest request =
                requestRepo.findById(requestId)
                        .orElseThrow(() ->
                                new ApiException("الطلب غير موجود"));

        if (!request.getCustomer().getId().equals(customer.getId())) {
            throw new ApiException("غير مصرح لك");
        }

        RequestReport report =
                reportRepo.findByRequest_IdAndLatestTrue(requestId)
                        .orElseThrow(() ->
                                new ApiException("لا يوجد تقرير"));

        return requestPricingService.generatePdf(report);
    }

    @Transactional(readOnly = true)
    public ReportPreviewDto getReport(
            Integer requestId,
            Customer customer
    ){

            CarServiceRequest request =
                    requestRepo.findById(requestId)
                            .orElseThrow(() ->
                                    new ApiException("الطلب غير موجود"));

        if (!request.getCustomer().getId().equals(customer.getId())) {
            throw new ApiException("غير مصرح لك");
        }


        RequestReport report =
                reportRepo.findByRequest_IdAndLatestTrue(requestId)
                        .orElseThrow(() ->
                                new ApiException("التقرير غير موجود"));

        List<RequestPart> parts =
                partRepo.findByReport_Id(report.getId());



            ReportPreviewDto dto = new ReportPreviewDto();

            dto.setRequestId(request.getId());
            dto.setOrderNumber(request.getOrderNumber());
            dto.setProblemDescription(request.getProblemDescription());
            dto.setServiceType(request.getServiceOption().name());

        RequestApproval approval =
                approvalRepo.findByRequest_Id(requestId)
                        .orElse(null);

        if (approval != null) {
            dto.setCustomerApproved(
                    approval.getApproved()
            );
        }
            List<CustomerReportPartDto> list = new ArrayList<>();
        int totalPartsPrice = 0;
        int totalLabor = 0;
        double grandTotal = 0;

        for (RequestPart part : parts) {

            CustomerReportPartDto p = new CustomerReportPartDto();

            p.setPartId(part.getId());
            p.setName(part.getName());
            p.setType(part.getType());

            p.setQuantity(part.getQuantity());
            p.setFinalPrice(part.getFinalPrice());
            p.setLaborCost(part.getLaborCost());

            int partsCost =
                    part.getFinalPrice() * part.getQuantity();

            int labor =
                    part.getLaborCost();

            double total =
                    partsCost + labor;

            p.setTotal(total);

            totalPartsPrice += partsCost;
            totalLabor += labor;
            grandTotal += total;

            list.add(p);

            }

        dto.setParts(list);

        dto.setTotalPartsPrice(totalPartsPrice);

        dto.setTotalLabor(totalLabor);

        dto.setDiscount(
                request.getDiscount() == null ? 0 : request.getDiscount()
        );

        dto.setGrandTotal(grandTotal);

        dto.setServiceType(
                request.getServiceOption().name()
        );

            return dto;
        }
    @Transactional(readOnly = true)
    public List<CustomerReportCardDto> getCustomerReports(
            Customer customer,
            ReportApprovalFilter status
    ) {

        List<RequestApproval> approvals;

        switch (status) {

            case APPROVED ->
                    approvals =
                            approvalRepo
                                    .findByRequest_CustomerAndApproved(
                                            customer,
                                            true
                                    );

            case REJECTED ->
                    approvals =
                            approvalRepo
                                    .findByRequest_CustomerAndApproved(
                                            customer,
                                            false
                                    );

            case PENDING ->
                    approvals =
                            approvalRepo
                                    .findByRequest_CustomerAndApprovedIsNull(
                                            customer
                                    );

            default ->
                    approvals =
                            approvalRepo
                                    .findByRequest_Customer(
                                            customer
                                    );
        }

        return approvals.stream()
                .map(approval -> {

                    CarServiceRequest request =
                            approval.getRequest();

                    CustomerReportCardDto dto =
                            new CustomerReportCardDto();

                    dto.setRequestId(
                            request.getId()
                    );

                    dto.setOrderNumber(
                            request.getOrderNumber()
                    );

                    dto.setServiceType(
                            request.getServiceOption()
                                    .name()
                    );

                    dto.setReportStatus(
                            mapStatus(approval)
                    );

                    dto.setRequestState(
                            RequestState.valueOf(
                                    requestMapper.mapRequestState(
                                            request
                                    )
                            )
                    );

                    RequestReport report =
                            reportRepo
                                    .findByRequest_IdAndLatestTrue(
                                            request.getId()
                                    )
                                    .orElse(null);

                    if (report != null) {

                        dto.setReportDate(
                                report.getCreatedAt()
                        );
                    }

                    return dto;

                })
                .toList();
    }

    private CustomerReportStatus mapStatus(RequestApproval approval) {

        if (approval == null || approval.getApproved() == null) {
            return CustomerReportStatus.PENDING;
        }

        return approval.getApproved()
                ? CustomerReportStatus.APPROVED
                : CustomerReportStatus.REJECTED;
    }

    @Transactional
    public void reject(
            Integer requestId,
            DeliveryRequestDto dto,
            Customer customer
    ) {

        CarServiceRequest request =
                requestRepo.findById(requestId)
                        .orElseThrow(() ->
                                new ApiException("الطلب غير موجود"));

        if (!request.getCustomer().getId().equals(customer.getId())) {
            throw new ApiException("غير مصرح لك");
        }

        RequestApproval approval =
                approvalRepo.findByRequest_Id(requestId)
                        .orElseThrow(() ->
                                new ApiException("لا يوجد تقرير"));

        if (Boolean.TRUE.equals(approval.getApproved())) {
            throw new ApiException("تمت معالجة التقرير مسبقاً");
        }

        approval.setApproved(false);
        approval.setDecisionAt(LocalDateTime.now());

        approvalRepo.save(approval);

        if (dto.getDeliveryDate().isBefore(LocalDate.now())) {
            throw new ApiException("لا يمكن اختيار تاريخ سابق");
        }

        if (dto.getDeliveryDate().getDayOfWeek() == DayOfWeek.FRIDAY) {
            throw new ApiException("لا يمكن اختيار يوم الجمعة");
        }

        Location location =
                locationRepository.findById(dto.getLocationId())
                        .orElseThrow(() ->
                                new ApiException("الموقع غير موجود"));

        request.setDeliveryLocation(location);
        request.setDeliveryDate(dto.getDeliveryDate());
        request.setDeliveryTime(dto.getDeliveryTime());

        request.setCustomerSelectedDelivery(true);

        request.setCustomerStatus(CustomerRequestStatus.READY_FOR_DELIVERY);

        request.setStaffStatus(StaffRequestStatus.DELIVERY_IN_PROGRESS);

        request.setLastUpdated(LocalDateTime.now());

        requestRepo.save(request);

        socketService.send(
                "/topic/current-orders/" +
                        request.getCustomer()
                                .getUser()
                                .getId(),
                carServiceRequestService.toCurrentDto(request)
        );
        socketService.send(
                "/topic/availability",
                appointmentService.getAllAvailability()
        );

        socketService.send(
                "/topic/report/" + requestId,
                getReport(
                        requestId,
                        request.getCustomer()
                )
        );

        notificationService.send(
                request.getAssignedTechnician().getUser(),
                "رفض العميل تقرير التسعير وحدد موعد استلام السيارة للطلب #"
                        + request.getOrderNumber()
        );
    }


    @Transactional
    public void requestModification(
            Integer requestId,
            CustomerModifyReportDto dto
    ) {

        CarServiceRequest request =
                requestRepo.findById(requestId)
                        .orElseThrow(() ->
                                new ApiException("الطلب غير موجود"));

        RequestApproval approval =
                approvalRepo.findByRequest_Id(requestId)
                        .orElseThrow(() ->
                                new ApiException("لا يوجد تقرير"));

        if (Boolean.TRUE.equals(approval.getApproved())) {
            throw new ApiException("تم اعتماد التقرير");
        }

        if (Boolean.FALSE.equals(approval.getApproved())) {
            throw new ApiException("تم رفض التقرير");
        }

        // ==========================================
        // التقرير الحالي الذي يراه العميل
        // ==========================================

        RequestReport oldReport =
                reportRepo.findByRequest_IdAndLatestTrue(requestId)
                        .orElseThrow(() ->
                                new ApiException("التقرير غير موجود"));

        // ==========================================
        // إنشاء تقرير جديد
        // ==========================================

        RequestReport newReport =
                new RequestReport();

        newReport.setRequest(request);

        newReport.setCreatedBy(
                request.getAssignedPricingEmployee()
        );

        newReport.setCreatedAt(
                LocalDateTime.now()
        );

        newReport.setVersion(
                oldReport.getVersion() + 1
        );

        newReport.setLatest(true);

        newReport.setSent(false);

        reportRepo.save(newReport);

        newReport.setReportNumber(
                "PR-" +
                        String.format(
                                "%06d",
                                newReport.getId()
                        )
        );

        reportRepo.save(newReport);

        // ==========================================
        // نسخ القطع الحالية من التقرير القديم
        // ==========================================

        List<RequestPart> oldParts =
                partRepo.findByReport_Id(
                        oldReport.getId()
                );

        Map<Integer, CustomerPartDto> selectedParts =
                dto.getParts()
                        .stream()
                        .collect(Collectors.toMap(
                                CustomerPartDto::getPartId,
                                Function.identity(),
                                (a, b) -> b
                        ));

        int totalPartsPrice = 0;
        int totalLabor = 0;

        // ==========================================
        // بناء القطع الجديدة
        // ==========================================

        for (RequestPart oldPart : oldParts) {

            CustomerPartDto selected =
                    selectedParts.get(oldPart.getId());

            // ======================================
            // العميل حذف القطعة
            // ======================================

            if (selected == null ||
                    selected.getQuantity() == null ||
                    selected.getQuantity() <= 0) {

                continue;
            }

            int quantity =
                    selected.getQuantity();

            int oldQuantity =
                    oldPart.getQuantity() == null
                            ? 1
                            : oldPart.getQuantity();

            int oldLabor =
                    oldPart.getLaborCost() == null
                            ? 0
                            : oldPart.getLaborCost();

            // تكلفة العمالة لكل قطعة
            int laborPerPiece =
                    oldQuantity > 0
                            ? oldLabor / oldQuantity
                            : 0;

            int newLabor =
                    laborPerPiece * quantity;

            // ======================================
            // إنشاء نسخة جديدة من القطعة
            // ======================================

            RequestPart newPart =
                    new RequestPart();

            newPart.setRequest(request);
            newPart.setReport(newReport);

            newPart.setName(
                    oldPart.getName()
            );

            newPart.setType(
                    oldPart.getType()
            );

            newPart.setQuantity(
                    quantity
            );

            newPart.setNotes(
                    oldPart.getNotes()
            );

            newPart.setFinalPrice(
                    oldPart.getFinalPrice()
            );

            newPart.setLaborCost(
                    newLabor
            );

            newPart.setPriced(
                    oldPart.getPriced()
            );

            partRepo.save(newPart);

            // ======================================
            // إعادة الحساب
            // ======================================

            int partPrice =
                    oldPart.getFinalPrice() == null
                            ? 0
                            : oldPart.getFinalPrice();

            totalPartsPrice +=
                    partPrice * quantity;

            totalLabor +=
                    newLabor;
        }

        // ==========================================
        // إعادة حساب السعر
        // ==========================================

        double subtotal =
                totalPartsPrice + totalLabor;

        double discount =
                request.getDiscount() == null
                        ? 0
                        : request.getDiscount();

        discount =
                Math.min(
                        discount,
                        subtotal
                );

        double afterDiscount =
                Math.max(
                        subtotal - discount,
                        0
                );

        double vat =
                afterDiscount * 0.15;

        double grandTotal =
                afterDiscount + vat;

        // ==========================================
        // تحديث الطلب بالقيم الجديدة
        // ==========================================

        request.setFinalPrice(
                (int) Math.round(grandTotal)
        );

        request.setVatAmount(vat);

        request.setDiscount(discount);

        request.setPricingStatus(
                PricingStatus.PRICED
        );

        request.setCustomerStatus(
                CustomerRequestStatus.WAITING_APPROVAL
        );

        request.setStaffStatus(
                StaffRequestStatus.REPORT_WRITING
        );

        request.setCurrentEmployee(
                request.getAssignedTechnician()
        );

        request.setLastUpdated(
                LocalDateTime.now()
        );

        requestRepo.save(request);

        // ==========================================
        // تحديث Approval
        // ==========================================

        approval.setApproved(null);

        approval.setDecisionAt(null);

        approval.setCustomerNote(null);

        approvalRepo.save(approval);

        // ==========================================
        // إشعار العميل
        // ==========================================

        notificationService.send(
                request.getCustomer().getUser(),
                "تم تحديث تقرير التسعير، يرجى مراجعته مرة أخرى."
        );

        // ==========================================
        // تحديث مباشر للواجهة
        // ==========================================

        socketService.send(
                "/topic/report/" + requestId,
                getReport(
                        requestId,
                        request.getCustomer()
                )
        );
    }


    @Transactional
    public void chooseDelivery(
            Integer requestId,
            DeliveryRequestDto dto,
            Customer customer
    ) {

        CarServiceRequest request =
                requestRepo.findById(requestId)
                        .orElseThrow(() ->
                                new ApiException("الطلب غير موجود"));

        if (!request.getCustomer().getId().equals(customer.getId())) {
            throw new ApiException("غير مصرح لك");
        }

        WarrantyRequest warranty =
                warrantyRepo.findByRequestId(requestId)
                        .orElse(null);

        boolean isWarrantyReceiving =
                warranty != null &&
                        warranty.getStatus() == WarrantyStatus.APPROVED;

        boolean isWarrantyDelivery =
                warranty != null &&
                        warranty.getStatus() == WarrantyStatus.DELIVERY_IN_PROGRESS;

        // ===========================
        // Warranty Receiving
        // ===========================

        if (isWarrantyReceiving) {

            appointmentService.validateAppointment(
                    dto.getDeliveryDate(),
                    dto.getDeliveryTime()
            );

            if (warranty.getReceivingDate() != null) {
                throw new ApiException(
                        "تم اختيار موعد استلام السيارة مسبقاً"
                );
            }

            Location location =
                    locationRepository.findById(dto.getLocationId())
                            .orElseThrow(() ->
                                    new ApiException("الموقع غير موجود"));

            warranty.setReceivingLocation(location);
            warranty.setReceivingDate(dto.getDeliveryDate());
            warranty.setReceivingTime(dto.getDeliveryTime());
            warranty.setUpdatedAt(LocalDateTime.now());

            warrantyRepo.save(warranty);

            socketService.send(
                    "/topic/warranty/" +
                            customer.getUser().getId(),
                    warrantyService.toResponseDto(warranty)
            );

            socketService.send(
                    "/topic/warranty-details/" +
                            warranty.getId(),
                    warrantyService.toDetailsDto(warranty)
            );

            socketService.send(
                    "/topic/availability",
                    appointmentService.getAllAvailability()
            );

        }

        // ===========================
        // Warranty Delivery
        // ===========================

        else if (isWarrantyDelivery) {

            appointmentService.validateAppointment(
                    dto.getDeliveryDate(),
                    dto.getDeliveryTime()
            );

            if (warranty.getDeliveryLocation() != null) {
                throw new ApiException(
                        "تم اختيار موعد تسليم الضمان مسبقاً"
                );
            }

            Location location =
                    locationRepository.findById(dto.getLocationId())
                            .orElseThrow(() ->
                                    new ApiException("الموقع غير موجود"));

            warranty.setDeliveryLocation(location);
            warranty.setDeliveryDate(dto.getDeliveryDate());
            warranty.setDeliveryTime(dto.getDeliveryTime());
            warranty.setUpdatedAt(LocalDateTime.now());

            warrantyRepo.save(warranty);

            socketService.send(
                    "/topic/warranty/" +
                            customer.getUser().getId(),
                    warrantyService.toResponseDto(warranty)
            );

            socketService.send(
                    "/topic/warranty-details/" +
                            warranty.getId(),
                    warrantyService.toDetailsDto(warranty)
            );

            socketService.send(
                    "/topic/availability",
                    appointmentService.getAllAvailability()
            );

            socketService.send(
                    "/topic/current-orders/" +
                            customer.getUser().getId(),
                    carServiceRequestService.toCurrentDto(request)
            );
        }

        // ===========================
        // Normal Request Delivery
        // ===========================
        else if (warranty != null) {

            throw new ApiException(
                    "لا يمكن اختيار موعد حالياً لطلب الضمان"
            );
        }

        else {

            if (request.getStaffStatus() !=
                    StaffRequestStatus.DELIVERY_IN_PROGRESS) {

                throw new ApiException(
                        "لا يمكن اختيار موعد التسليم حالياً"
                );
            }

            appointmentService.validateAppointment(
                    dto.getDeliveryDate(),
                    dto.getDeliveryTime()
            );

            if (Boolean.TRUE.equals(
                    request.getCustomerSelectedDelivery()
            )) {
                throw new ApiException(
                        "تم اختيار موعد التسليم مسبقاً"
                );
            }

            Location location =
                    locationRepository.findById(dto.getLocationId())
                            .orElseThrow(() ->
                                    new ApiException("الموقع غير موجود"));

            request.setDeliveryLocation(location);
            request.setDeliveryDate(dto.getDeliveryDate());
            request.setDeliveryTime(dto.getDeliveryTime());
            request.setCustomerSelectedDelivery(true);
            request.setLastUpdated(LocalDateTime.now());

            requestRepo.save(request);

            socketService.send(
                    "/topic/current-orders/" +
                            customer.getUser().getId(),
                    carServiceRequestService.toCurrentDto(request)
            );

            socketService.send(
                    "/topic/availability",
                    appointmentService.getAllAvailability()
            );

            socketService.send(
                    "/topic/request/" +
                            request.getId(),
                    carServiceRequestService.toResponseDto(request)
            );
        }

        // ===========================
        // Notification
        // ===========================

        if (request.getCurrentEmployee() != null) {

            notificationService.send(
                    request.getCurrentEmployee().getUser(),
                    isWarrantyReceiving
                            ? "قام العميل بحجز موعد استلام سيارة الضمان للطلب #"
                            + request.getOrderNumber()
                            : isWarrantyDelivery
                            ? "قام العميل بحجز موعد تسليم سيارة الضمان للطلب #"
                            + request.getOrderNumber()
                            : "قام العميل بحجز موعد تسليم السيارة للطلب #"
                            + request.getOrderNumber()
            );
        }
    }

    @Transactional
    public void addCustomerNote(
            Integer requestId,
            String note,
            Customer customer
    ) {

        CarServiceRequest request =
                requestRepo.findById(requestId)
                        .orElseThrow(() ->
                                new ApiException("الطلب غير موجود"));

        if (!request.getCustomer().getId().equals(customer.getId())) {
            throw new ApiException("غير مصرح لك");
        }

        if (note == null || note.isBlank()) {
            throw new ApiException("الملاحظة مطلوبة");
        }

        RequestNote requestNote = new RequestNote();

        requestNote.setRequest(request);
        requestNote.setCustomer(customer);
        requestNote.setEmployee(null);
        requestNote.setNote(note.trim());
        requestNote.setRequestStatus(
                request.getStaffStatus()
        );
        requestNote.setType(RequestNoteType.CUSTOMER);
        requestNote.setCreatedAt(LocalDateTime.now());

        noteRepo.save(requestNote);
    }


}