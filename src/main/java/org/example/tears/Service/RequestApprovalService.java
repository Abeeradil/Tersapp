package org.example.tears.Service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.example.tears.DTO.PartDto;
import org.example.tears.DTO.UpdatePartsDto;
import org.example.tears.Enums.CustomerRequestStatus;
import org.example.tears.Enums.StaffRequestStatus;
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

            request.setCustomerStatus(CustomerRequestStatus.UNDER_REPAIR);

            request.setStaffStatus(StaffRequestStatus.REPAIRING);

            request.setStage(WorkflowStage.REPAIRING);

            request.setRepairAt(LocalDateTime.now());

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

    }