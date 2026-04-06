package org.example.tears.Service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.example.tears.DTO.PartDto;
import org.example.tears.DTO.UpdatePartsDto;
import org.example.tears.Enums.WorkflowStage;
import org.example.tears.Model.CarServiceRequest;
import org.example.tears.Model.RequestApproval;
import org.example.tears.Model.RequestPart;
import org.example.tears.Repository.CarServiceRequestRepository;
import org.example.tears.Repository.RequestApprovalRepository;
import org.example.tears.Repository.RequestPartRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

    @Service
    @RequiredArgsConstructor
    public class RequestApprovalService {

        private final RequestApprovalRepository approvalRepo;
        private final RequestPartRepository partRepo;
        private final CarServiceRequestRepository requestRepo;
        private final NotificationService notificationService;


        // ===============================
        // الموظف يحدث القطع والتسعير
        // ===============================
        @Transactional
        public void updateParts(Integer requestId, UpdatePartsDto dto){

            CarServiceRequest request =
                    requestRepo.findById(requestId)
                            .orElseThrow(() ->
                                    new RuntimeException("Request not found"));


            // حذف القطع القديمة
            partRepo.deleteByRequestId(requestId);


            int total = dto.getLaborCost(); // أجرة اليد


            // إضافة القطع الجديدة
            for (PartDto p : dto.getParts()) {
                RequestPart part = new RequestPart();

                part.setName(p.getName());
                part.setType(p.getType());  // لو عندك نوع القطعة
                part.setQuantity(p.getQuantity());
                part.setEstimatedPrice(p.getEstimatedPrice());
                part.setRequest(request);

                partRepo.save(part);

                total += (p.getEstimatedPrice() != null ? p.getEstimatedPrice() : 0) *
                        (p.getQuantity() != null ? p.getQuantity() : 0);
            }



            // السعر النهائي
            request.setFinalPrice(total);

            // انتظار موافقة العميل
            request.setStage(WorkflowStage.WAITING_APPROVAL);

            requestRepo.save(request);


            // إنشاء طلب موافقة
            createApproval(request);


            // إشعار العميل
            notificationService.send(
                    request.getCustomer().getUser(),
                    "Your request #" + request.getId() + " is ready for approval"
            );
        }



        // ===============================
        // إنشاء سجل موافقة
        // ===============================
        private void createApproval(CarServiceRequest request){

            // إذا فيه موافقة قبل لا نكرر
            if (approvalRepo.findByRequest_Id(request.getId()).isPresent()){
                return;
            }

            RequestApproval approval = new RequestApproval();

            approval.setRequest(request);
            approval.setApproved(null);

            approvalRepo.save(approval);
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

            request.setStage(WorkflowStage.REPAIRING);

            requestRepo.save(request);


            // إشعار الموظف
            notificationService.send(
                    request.getCurrentEmployee().getUser(),
                    "Request #" + request.getId() + " approved by customer"
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

            request.setStage(WorkflowStage.INSPECTION);

            requestRepo.save(request);


            // إشعار الموظف
            notificationService.send(
                    request.getCurrentEmployee().getUser(),
                    "Request #" + request.getId() + " rejected by customer"
            );
        }

    }