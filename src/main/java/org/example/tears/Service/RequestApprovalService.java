package org.example.tears.Service;

import org.example.tears.Enums.*;
import org.example.tears.Model.Customer;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.example.tears.Api.ApiException;
import org.example.tears.DTO.*;
import org.example.tears.Model.CarServiceRequest;
import org.example.tears.Model.RequestApproval;
import org.example.tears.Model.RequestPart;
import org.example.tears.Repository.CarServiceRequestRepository;
import org.example.tears.Repository.RequestApprovalRepository;
import org.example.tears.Repository.RequestPartRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
    @RequiredArgsConstructor
    public class RequestApprovalService {

        private final RequestApprovalRepository approvalRepo;
        private final RequestPartRepository partRepo;
        private final CarServiceRequestRepository requestRepo;
        private final NotificationService notificationService;
    private final RequestPricingService requestPricingService;


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


        // ===============================
        // العميل يوافق
        // ===============================
        public void approve(Integer requestId, String note){

            RequestApproval approval =
                    approvalRepo.findByRequest_Id(requestId)
                            .orElseThrow(() ->
                                    new RuntimeException("Approval not found"));

            approval.setApproved(true);
            approval.setDecisionAt(LocalDateTime.now());
            approval.setCustomerNote(note);

            approvalRepo.save(approval);

            CarServiceRequest request = approval.getRequest();

            request.setCustomerStatus(
                    CustomerRequestStatus.WAITING_APPROVAL
            );
            request.setPaymentStatus(PaymentStatus.PENDING);

            request.setLastUpdated(LocalDateTime.now());

            requestRepo.save(request);

            notificationService.send(
                    request.getAssignedEmployee().getUser(),
                    "وافق العميل على تقرير التسعير للطلب #" + request.getOrderNumber()
            );
        }




        // ===============================
        // العميل يرفض
        // ===============================
        public void reject(Integer requestId, String note){

            RequestApproval approval =
                    approvalRepo.findByRequest_Id(requestId)
                            .orElseThrow(() ->
                                    new RuntimeException("Approval not found"));


            approval.setApproved(false);
            approval.setDecisionAt(LocalDateTime.now());
            approval.setCustomerNote(note);

            approvalRepo.save(approval);



            CarServiceRequest request = approval.getRequest();

            request.setCustomerStatus(CustomerRequestStatus.WAITING_APPROVAL);

            request.setStaffStatus(StaffRequestStatus.REPORT_WRITING);

            request.setLastUpdated(LocalDateTime.now());
            requestRepo.save(request);


            // إشعار الموظف
            notificationService.send(
                    request.getCurrentEmployee().getUser(),
                    "Request #" + request.getId() + " rejected by customer"
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

            for(CustomerPartDto item : dto.getParts()){

                RequestPart part =
                        partRepo.findById(item.getPartId())
                                .orElseThrow(() ->
                                        new ApiException("القطعة غير موجودة"));

                if(!part.getRequest().getId().equals(requestId)){

                    throw new ApiException("القطعة لا تتبع هذا الطلب");
                }

                part.setQuantity(item.getQuantity());

                part.setPriced(false);

                partRepo.save(part);
            }

            request.setPricingStatus(PricingStatus.PRICING);

            request.setCustomerStatus(CustomerRequestStatus.WAITING_APPROVAL);

            request.setStaffStatus(StaffRequestStatus.REPORT_WRITING);

            request.setCurrentEmployee(request.getAssignedEmployee());

            request.setLastUpdated(LocalDateTime.now());

            requestRepo.save(request);

            notificationService.send(
                    request.getAssignedEmployee().getUser(),
                    "قام العميل بطلب تعديل على التقرير رقم #" +
                            request.getOrderNumber()
            );
        }



    }