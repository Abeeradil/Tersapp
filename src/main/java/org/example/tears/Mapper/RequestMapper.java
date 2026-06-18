package org.example.tears.Mapper;

import org.example.tears.DTO.EmployeeListDto;
import org.example.tears.DTO.EmployeeRequestResponseDto;
import org.example.tears.DTO.RequestSummaryDto;
import org.example.tears.InpDTO.LocationDto;
import org.example.tears.Model.CarServiceRequest;
import org.example.tears.Model.Employee;
import org.example.tears.Model.Location;
import org.example.tears.OutDTO.EmployeeRequestDetailsDto;
import org.springframework.stereotype.Component;

@Component
public class RequestMapper {

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

        dto.setTotalPrice(req.getFinalPrice() != null
                        ? req.getFinalPrice().doubleValue()
                        : req.getEstimatedPrice()
        );

        dto.setCreatedAt(req.getCreatedAt());

        if (req.getCustomer() != null) {
            dto.setCustomerName(
                    req.getCustomer()
                            .getUser()
                            .getFullName()
            );
        }

        if (req.getAssignedEmployee() != null) {
            dto.setAssignedEmployee(
                    req.getAssignedEmployee()
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

        if (r.getStaffStatus() != null) {
            dto.setStatus(r.getStaffStatus().name());
        }

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
            dto.setLocation(mapLocation(r.getLocation()));
        }

        if (r.getServiceOption() != null) {
            dto.setServiceOption(r.getServiceOption().getDisplayName());
        }

        dto.setCreatedAt(r.getCreatedAt());

        return dto;
    }

    public EmployeeRequestDetailsDto toEmployeeDetailsDto(CarServiceRequest r){

        EmployeeRequestDetailsDto dto = new EmployeeRequestDetailsDto();

        dto.setId(r.getId());
        dto.setOrderNumber(r.getOrderNumber());

        if (r.getStaffStatus() != null){
            dto.setStatus(r.getStaffStatus().name());
        }

        if (r.getServiceOption() != null){
            dto.setServiceOption(r.getServiceOption().getDisplayName());
        }

        dto.setProblemDescription(r.getProblemDescription());

        if(r.getCustomer()!=null){

            dto.setCustomerName(
                    r.getCustomer().getUser().getFullName()
            );
        }

        if(r.getCar()!=null){

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

        dto.setLocation(mapLocation(r.getLocation()));

        dto.setCreatedAt(r.getCreatedAt());
        dto.setLastUpdated(r.getLastUpdated());


        return dto;
    }

    public LocationDto mapLocation(Location loc) {

        if (loc == null) {
            return null;
        }

        LocationDto dto = new LocationDto();

        dto.setId(loc.getId());

        dto.setLat(loc.getLat());

        dto.setLng(loc.getLng());

        dto.setAddress(loc.getAddress());

        dto.setTitle(loc.getTitle());

        return dto;
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