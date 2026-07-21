package org.example.tears.Service;

import org.example.tears.Enums.*;
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

@Service
    @RequiredArgsConstructor
    public class RequestApprovalService {

        private final RequestApprovalRepository approvalRepo;
        private final RequestPartRepository partRepo;
        private final CarServiceRequestRepository requestRepo;
        private final RequestReportRepository reportRepo;
        private final NotificationService notificationService;
    private final RequestPricingService requestPricingService;
    private final AppointmentService appointmentService;
    private final LocationRepository locationRepository;



    public ResponseEntity<byte[]> downloadCustomerReport(
            Integer requestId,
            Customer customer
    ) throws Exception {

        CarServiceRequest serviceRequest =
                requestRepo.findById(requestId)
                        .orElseThrow(() ->
                                new ApiException("الطلب غير موجود"));

        if (!serviceRequest.getCustomer().getId().equals(customer.getId())) {
            throw new ApiException("غير مصرح لك");
        }

        return requestPricingService.downloadPricingReport(requestId);
    }

    @Transactional(readOnly = true)
        public ReportPreviewDto getReport(Integer requestId) {

            CarServiceRequest request =
                    requestRepo.findById(requestId)
                            .orElseThrow(() ->
                                    new ApiException("الطلب غير موجود"));

            List<RequestPart> parts =
                    partRepo.findByRequestId(requestId);

            ReportPreviewDto dto = new ReportPreviewDto();

            dto.setRequestId(request.getId());
            dto.setOrderNumber(request.getOrderNumber());
            dto.setProblemDescription(request.getProblemDescription());

        RequestApproval approval =
                approvalRepo.findByRequest_Id(requestId)
                        .orElse(null);

        if (approval != null) {
            dto.setCustomerApproved(
                    approval.getApproved()
            );
        }
            List<CustomerReportPartDto> list = new ArrayList<>();

            double grandTotal = 0;

            for (RequestPart part : parts) {

                CustomerReportPartDto p =
                        new CustomerReportPartDto();

                p.setPartId(part.getId());

                p.setName(part.getName());

                p.setQuantity(part.getQuantity());

                p.setFinalPrice(part.getFinalPrice());

                p.setLaborCost(part.getLaborCost());

                double total =
                        (part.getFinalPrice() * part.getQuantity())
                                + part.getLaborCost();

                p.setTotal(total);

                grandTotal += total;

                list.add(p);
            }

            dto.setParts(list);

            dto.setGrandTotal(grandTotal);

            return dto;
        }


    @Transactional
    public void approve(Integer requestId, String note) {

        RequestApproval approval =
                approvalRepo.findByRequest_Id(requestId)
                        .orElseThrow(() ->
                                new ApiException("لا يوجد تقرير للموافقة"));

        if (Boolean.TRUE.equals(approval.getApproved())) {
            throw new ApiException("تمت الموافقة مسبقاً");
        }

        approval.setDecisionAt(LocalDateTime.now());
        approval.setCustomerNote(note);

        approvalRepo.save(approval);

        CarServiceRequest request = approval.getRequest();

        request.setPaymentStatus(PaymentStatus.PENDING);
        request.setCustomerStatus(
                CustomerRequestStatus.WAITING_APPROVAL
        );

        request.setLastUpdated(LocalDateTime.now());

        requestRepo.save(request);

        notificationService.send(
                request.getCustomer().getUser(),
                "تمت الموافقة على التقرير، يمكنك الآن إتمام الدفع."
        );

        notificationService.send(
                request.getAssignedEmployee().getUser(),
                "وافق العميل على تقرير التسعير للطلب #"
                        + request.getOrderNumber()
                        + " وبانتظار سداد الدفعة النهائية."
        );
    }


    public void reject(Integer requestId, String note) {

        RequestApproval approval =
                approvalRepo.findByRequest_Id(requestId)
                        .orElseThrow(() ->
                                new RuntimeException("Approval not found"));

        approval.setApproved(false);
        approval.setDecisionAt(LocalDateTime.now());
        approval.setCustomerNote(note);

        approvalRepo.save(approval);

        CarServiceRequest request = approval.getRequest();

        request.setCustomerStatus(CustomerRequestStatus.READY_FOR_DELIVERY);

        request.setStaffStatus(StaffRequestStatus.DELIVERY_IN_PROGRESS);

        request.setLastUpdated(LocalDateTime.now());

        requestRepo.save(request);

        notificationService.send(
                request.getAssignedEmployee().getUser(),
                "رفض العميل تقرير التسعير للطلب #" +
                        request.getOrderNumber() +
                        "، يرجى تجهيز السيارة للتسليم."
        );
    }


    @Transactional
    public void requestModification(
            Integer requestId,
            CustomerModifyReportDto dto
    ){

        CarServiceRequest request =
                requestRepo.findById(requestId)
                        .orElseThrow(() ->
                                new ApiException("الطلب غير موجود"));

        RequestApproval approval =
                approvalRepo.findByRequest_Id(requestId)
                        .orElseThrow(() ->
                                new ApiException("لا يوجد تقرير"));

        approval.setApproved(false);
        approval.setCustomerNote(dto.getNote());
        approval.setDecisionAt(LocalDateTime.now());

        approvalRepo.save(approval);

        int totalPartsPrice = 0;
        int totalLabor = 0;

        for (CustomerPartDto item : dto.getParts()) {

            RequestPart part =
                    partRepo.findById(item.getPartId())
                            .orElseThrow(() ->
                                    new ApiException("القطعة غير موجودة"));

            if (!part.getRequest().getId().equals(requestId)) {
                throw new ApiException("القطعة لا تتبع هذا الطلب");
            }

            int oldQty = part.getQuantity();

            int newQty = item.getQuantity();

            if (newQty < 0) {
                throw new ApiException("الكمية غير صحيحة");
            }

            int oldLabor =
                    part.getLaborCost() == null ? 0 : part.getLaborCost();

            int laborPerPiece =
                    oldQty == 0 ? 0 : oldLabor / oldQty;

            part.setQuantity(newQty);

            part.setLaborCost(laborPerPiece * newQty);

            partRepo.save(part);

            int unitPrice =
                    part.getFinalPrice() == null ? 0 : part.getFinalPrice();

            totalPartsPrice += unitPrice * newQty;

            totalLabor += part.getLaborCost();
        }

        request.setFinalPrice(totalPartsPrice + totalLabor);

        request.setPricingStatus(PricingStatus.PRICED);

        request.setCustomerStatus(CustomerRequestStatus.WAITING_APPROVAL);

        request.setStaffStatus(StaffRequestStatus.REPORT_WRITING);

        request.setCurrentEmployee(request.getAssignedEmployee());

        request.setLastUpdated(LocalDateTime.now());

        RequestReport oldReport =
                reportRepo.findByRequest_IdAndLatestTrue(requestId)
                        .orElse(null);

        int version = 1;

        if (oldReport != null) {

            oldReport.setLatest(false);

            reportRepo.save(oldReport);

            version = oldReport.getVersion() + 1;
        }

        RequestReport report = new RequestReport();

        report.setRequest(request);

        report.setCreatedBy(request.getAssignedPricingEmployee());

        report.setCreatedAt(LocalDateTime.now());

        report.setVersion(version);

        report.setLatest(true);

        report.setSent(true);

        reportRepo.save(report);

        requestRepo.save(request);

        notificationService.send(
                request.getCustomer().getUser(),
                "تم تحديث تقرير التسعير، يرجى مراجعته مرة أخرى."
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

        if (request.getStaffStatus() != StaffRequestStatus.DELIVERY_IN_PROGRESS) {
            throw new ApiException("لا يمكن اختيار موعد التسليم حالياً");
        }

        if (dto.getDeliveryDate().getDayOfWeek() == DayOfWeek.FRIDAY) {
            throw new ApiException("لا يمكن اختيار يوم الجمعة");
        }

        if (dto.getDeliveryDate().isBefore(LocalDate.now())) {
            throw new ApiException("لا يمكن اختيار تاريخ سابق");
        }

        /**
        if (!AVAILABLE_TIMES.contains(dto.getDeliveryTime())) {
            throw new ApiException("وقت التسليم غير متاح");
        }
         **/


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

        notificationService.send(
                request.getCurrentEmployee().getUser(),
                "قام العميل بحجز موعد تسليم السيارة للطلب #"
                        + request.getOrderNumber()
        );
    }


}