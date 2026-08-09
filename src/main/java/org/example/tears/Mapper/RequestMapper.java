package org.example.tears.Mapper;

import lombok.AllArgsConstructor;
import org.example.tears.DTO.*;
import org.example.tears.Enums.*;
import org.example.tears.Model.*;
import org.example.tears.OutDTO.EmployeeRequestDetailsDto;
import org.example.tears.Repository.RequestApprovalRepository;
import org.example.tears.Repository.RequestReportRepository;
import org.example.tears.Repository.WarrantyRepository;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

@Component
@AllArgsConstructor
public class RequestMapper {

    private final RequestReportRepository reportRepo;
    private final RequestApprovalRepository approvalRepo;
    private final WarrantyRepository warrantyRepo;


    public RequestSummaryDto toSummaryDto(CarServiceRequest req) {

        RequestSummaryDto dto = new RequestSummaryDto();

        dto.setId(req.getId());
        dto.setOrderNumber(req.getOrderNumber());

        dto.setStatus(
                req.getStaffStatus() != null
                        ? req.getStaffStatus().name()
                        : null
        );

        dto.setStage(
                req.getStage() != null
                        ? req.getStage().name()
                        : null
        );

        dto.setCreatedAt(req.getCreatedAt());

        if (req.getCustomer() != null) {
            dto.setCustomerName(
                    req.getCustomer()
                            .getUser()
                            .getFullName()
            );
        }

        if (req.getAssignedTechnician() != null) {
            dto.setAssignedTechnician(
                    req.getAssignedTechnician()
                            .getUser()
                            .getFullName()
            );
        }

        return dto;
    }

    public EmployeeRequestResponseDto toEmployeeCardDto(CarServiceRequest r) {

        EmployeeRequestResponseDto dto = new EmployeeRequestResponseDto();

        dto.setId(r.getId());
        dto.setOrderNumber(r.getOrderNumber());

        if (r.getStaffStatus() != null){
            dto.setStatus(r.getStaffStatus().name());
        }


        dto.setWarrantyEligible(
                isWarrantyEligible(r)
        );

        Optional<WarrantyRequest> warranty =
                warrantyRepo.findByRequestId(r.getId());

        dto.setWarrantyRequest(warranty.isPresent());



        dto.setWarrantyDescription(
                warranty.map(WarrantyRequest::getDescription).orElse(null)
        );


        dto.setProblemDescription(r.getProblemDescription());

        if (r.getCar() != null && r.getCar().getModel() != null) {

            dto.setCarModelName(
                    r.getCar().getModel().getName()
            );

            dto.setCarModelNameAr(
                    r.getCar().getModel().getNameAr()
            );

            dto.setPlateNumberArabic(
                    formatArabicPlate(r.getCar().getPlateNumberArabic())
            );

            dto.setPlateNumberEnglish(
                    formatEnglishPlate(r.getCar().getPlateNumberEnglish())
            );
        }

        if (r.getLocation() != null) {
            dto.setAddress(r.getLocation().getAddress());
        }

        if (r.getServiceOption() != null) {
            dto.setServiceOption(r.getServiceOption().name());
        }

        dto.setRequestState(
                mapRequestState(r)
        );



        dto.setCreatedAt(r.getCreatedAt());

        return dto;
    }

    public String mapRequestState(
            CarServiceRequest req
    ) {

        if (req.getCustomerStatus()
                == CustomerRequestStatus.CANCELED) {

            return RequestState.CANCELLED.name();
        }

        if (req.getCustomerStatus()
                == CustomerRequestStatus.DELIVERED) {

            return RequestState.COMPLETED.name();
        }

        return RequestState.ACTIVE.name();
    }

    public EmployeeRequestDetailsDto toEmployeeDetailsDto(
            CarServiceRequest r
    ) {

        EmployeeRequestDetailsDto dto =
                new EmployeeRequestDetailsDto();

        // ===========================
        // Basic Data
        // ===========================

        dto.setId(r.getId());
        dto.setOrderNumber(r.getOrderNumber());

        dto.setCustomerSelectedDelivery(
                r.getCustomerSelectedDelivery()
        );

        dto.setDeliveryDate(
                r.getDeliveryDate()
        );

        dto.setDeliveryTime(
                r.getDeliveryTime()
        );

        if (r.getDeliveryDate() != null) {

            dto.setDeliveryDay(
                    r.getDeliveryDate()
                            .getDayOfWeek()
                            .getDisplayName(
                                    TextStyle.FULL,
                                    new Locale("ar")
                            )
            );
        }

        if (r.getDeliveryLocation() != null) {

            dto.setDeliveryLocation(
                    r.getDeliveryLocation().getAddress()
            );
        }

        // ===========================
        // Status
        // ===========================

        if (r.getStaffStatus() != null) {

            dto.setStatus(
                    r.getStaffStatus().name()
            );
        }

        if (r.getServiceOption() != null) {

            dto.setServiceOption(
                    r.getServiceOption().name()
            );
        }

        // ===========================
        // Timeline
        // ===========================

        RequestReport report =
                reportRepo.findByRequest_IdAndLatestTrue(r.getId())
                        .orElse(null);

        dto.setTimeline(
                buildTimeline(r, report)
        );

        // ===========================
        // Problem
        // ===========================

        dto.setProblemDescription(
                r.getProblemDescription()
        );

        // ===========================
        // Approval
        // ===========================

        RequestApproval approval =
                approvalRepo.findByRequest_Id(r.getId())
                        .orElse(null);

        dto.setCustomerApproved(
                approval == null
                        ? null
                        : approval.getApproved()
        );

        // ===========================
        // Customer
        // ===========================

        if (r.getCustomer() != null) {

            dto.setCustomerName(
                    r.getCustomer()
                            .getUser()
                            .getFullName()
            );

            dto.setCustomerPhone(
                    r.getCustomer()
                            .getUser()
                            .getPhoneNumber()
            );
        }

        // ===========================
        // Car
        // ===========================

        if (r.getCar() != null) {

            dto.setCarModelName(
                    r.getCar()
                            .getModel()
                            .getName()
            );

            dto.setCarModelNameAr(
                    r.getCar()
                            .getModel()
                            .getNameAr()
            );

            dto.setPlateNumberArabic(
                    formatArabicPlate(
                            r.getCar().getPlateNumberArabic()
                    )
            );

            dto.setPlateNumberEnglish(
                    formatEnglishPlate(
                            r.getCar().getPlateNumberEnglish()
                    )
            );

            if (r.getLocation() != null) {

                dto.setAddress(
                        r.getLocation().getAddress()
                );
            }

            // ===========================
            // Pricing Employee
            // ===========================

            if (r.getAssignedPricingEmployee() != null) {

                dto.setPricingEmployeeName(
                        r.getAssignedPricingEmployee()
                                .getUser()
                                .getFullName()
                );

                dto.setPricingEmployeePhone(
                        r.getAssignedPricingEmployee()
                                .getUser()
                                .getPhoneNumber()
                );
            }
        }

        dto.setCreatedAt(
                r.getCreatedAt()
        );

        return dto;
    }

    private List<TimelineItemDto> buildTimeline(
            CarServiceRequest r,
            RequestReport report
    ){

        List<TimelineItemDto> list = new ArrayList<>();

        list.add(new TimelineItemDto(
                "تم إنشاء الطلب",
                StaffRequestStatus.NEW,
                r.getCreatedAt(),
                r.getCreatedAt() != null,
                r.getStaffStatus() == StaffRequestStatus.NEW
        ));

        list.add(new TimelineItemDto(
                "تم استلام السيارة",
                StaffRequestStatus.RECEIVED,
                r.getReceivedAt(),
                r.getReceivedAt() != null,
                r.getStaffStatus() == StaffRequestStatus.RECEIVED
        ));

        list.add(new TimelineItemDto(
                "جاري الفحص",
                StaffRequestStatus.INSPECTION_IN_PROGRESS,
                r.getInspectionAt(),
                r.getInspectionAt() != null,
                r.getStaffStatus() == StaffRequestStatus.INSPECTION_IN_PROGRESS
        ));

        list.add(new TimelineItemDto(
                "تسجيل القطع",
                StaffRequestStatus.PARTS_REGISTERING,
                r.getPartsRegisteredAt(),
                r.getPartsRegisteredAt() != null,
                r.getStaffStatus() == StaffRequestStatus.PARTS_REGISTERING
        ));

        list.add(new TimelineItemDto(
                "جاري التسعير",
                StaffRequestStatus.PRICING,
                r.getPricingAt(),
                r.getPricingAt() != null,
                r.getPricingStatus() == PricingStatus.PRICING
        ));

        list.add(new TimelineItemDto(
                "تم التسعير",
                StaffRequestStatus.PRICING,
                report == null ? null : report.getCreatedAt(),
                r.getPricingStatus() == PricingStatus.PRICED,
                false
        ));

        list.add(new TimelineItemDto(
                "إنشاء التقرير",
                StaffRequestStatus.PRICING,
                report == null ? null : report.getCreatedAt(),
                report != null,
                false
        ));

        list.add(new TimelineItemDto(
                "إرفاق التقرير",
                StaffRequestStatus.REPORT_WRITING,
                r.getReportWrittenAt(),
                r.getStaffStatus().ordinal()
                        >= StaffRequestStatus.REPORT_WRITING.ordinal(),
                r.getStaffStatus() == StaffRequestStatus.REPORT_WRITING
        ));
        list.add(new TimelineItemDto(
                "جاري الإصلاح",
                StaffRequestStatus.REPAIRING,
                r.getRepairAt(),
                r.getRepairAt() != null,
                r.getStaffStatus() == StaffRequestStatus.REPAIRING
        ));

        list.add(new TimelineItemDto(
                "قيد التجربة",
                StaffRequestStatus.TESTING,
                r.getTestingAt(),
                r.getTestingAt() != null,
                r.getStaffStatus() == StaffRequestStatus.TESTING
        ));

        list.add(new TimelineItemDto(
                "جاهز للتسليم",
                StaffRequestStatus.DELIVERY_IN_PROGRESS,
                r.getLastUpdated(),
                r.getStaffStatus().ordinal()
                        >= StaffRequestStatus.DELIVERY_IN_PROGRESS.ordinal(),
                r.getStaffStatus() == StaffRequestStatus.DELIVERY_IN_PROGRESS
        ));

        list.add(new TimelineItemDto(
                "تم التسليم",
                StaffRequestStatus.DELIVERED,
                r.getDeliveredAt(),
                r.getDeliveredAt() != null,
                r.getStaffStatus() == StaffRequestStatus.DELIVERED
        ));

        return list;
    }



    public EmployeeListDto toEmployeeDto(Employee employee) {

        EmployeeListDto dto = new EmployeeListDto();

        dto.setId(employee.getId());

        dto.setFullName(
                employee.getUser().getFullName()
        );

        dto.setPhoneNumber(
                employee.getUser().getPhoneNumber()
        );

        dto.setJobTitle(
                employee.getJobTitle()
        );

        dto.setRole(
                employee.getEmployeeRole().name()
        );


        dto.setStatus(
                employee.getUser().getStatus().name()
        );

        return dto;
    }

    private String formatEnglishPlate(String plate) {

        if (plate == null || plate.length() < 4) {
            return plate;
        }

        String letters = plate.substring(0, 3);
        String numbers = plate.substring(3);

        return letters + "-" + numbers;
    }

    private String formatArabicPlate(String plate) {

        if (plate == null || plate.isBlank()) {
            return plate;
        }

        String[] parts = plate.trim().split("\\s+");

        if (parts.length == 4) {
            return parts[0] + " " + parts[1] + " " + parts[2] + " - " + parts[3];
        }

        return plate;
    }

    public boolean isWarrantyEligible(CarServiceRequest request) {

        return request.getDeliveredAt() != null
                && LocalDateTime.now().isBefore(
                request.getDeliveredAt().plusDays(30)
        );
    }

//        public CustomerRequestStatus toCustomer(WorkflowStage stage) {
//
//            return switch (stage) {
//
//                case NEW_REQUEST -> CustomerRequestStatus.REQUEST_CREATED;
//
//                case ASSIGNED, RECEIVED ->
//                        CustomerRequestStatus.CAR_RECEIVED;
//
//                case INSPECTION_IN_PROGRESS,
//                     TESTING,
//                     REPORT_WRITING,
//                     PARTS_REGISTERING,
//                     PRICING ->
//                        CustomerRequestStatus.CAR_INSPECTION;
//
//                case WAITING_APPROVAL ->
//                        CustomerRequestStatus.WAITING_APPROVAL;
//
//                case REPAIRING ->
//                        CustomerRequestStatus.UNDER_REPAIR;
//
//                case READY ->
//                        CustomerRequestStatus.READY_FOR_DELIVERY;
//
//                case DELIVERED ->
//                        CustomerRequestStatus.DELIVERED;
//
//                case CANCELLED ->
//                        CustomerRequestStatus.CANCELED;
//            };
//        }
//
//        public StaffRequestStatus toStaff(WorkflowStage stage) {
//
//            return switch (stage) {
//
//                case NEW_REQUEST -> StaffRequestStatus.NEW;
//
//                case ASSIGNED -> StaffRequestStatus.NEW;
//
//                case RECEIVED -> StaffRequestStatus.RECEIVED;
//
//                case INSPECTION_IN_PROGRESS -> StaffRequestStatus.INSPECTION_IN_PROGRESS;
//
//                case TESTING -> StaffRequestStatus.TESTING;
//
//                case REPORT_WRITING -> StaffRequestStatus.REPORT_WRITING;
//
//                case PARTS_REGISTERING -> StaffRequestStatus.PARTS_REGISTERING;
//
//                case PRICING -> StaffRequestStatus.PRICING;
//
//                case REPAIRING -> StaffRequestStatus.REPAIRING;
//
//                case READY -> StaffRequestStatus.REPAIRING;
//
//                case DELIVERED -> StaffRequestStatus.DELIVERED;
//
//                case WAITING_APPROVAL -> StaffRequestStatus.PRICING;
//
//                case CANCELLED -> StaffRequestStatus.DELIVERED; // أو CANCELLED إذا أضفتها
//            };
//        }
}