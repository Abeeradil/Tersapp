package org.example.tears.Mapper;

import org.example.tears.DTO.EmployeeListDto;
import org.example.tears.DTO.EmployeeRequestResponseDto;
import org.example.tears.DTO.RequestSummaryDto;
import org.example.tears.Model.CarServiceRequest;
import org.example.tears.Model.Employee;
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

    public EmployeeRequestResponseDto toEmployeeDto(CarServiceRequest r) {

        EmployeeRequestResponseDto dto = new EmployeeRequestResponseDto();

        dto.setId(r.getId());
        dto.setOrderNumber(r.getOrderNumber());

        if (r.getStage() != null) {
            dto.setStatus(r.getStage().name());
        }

        dto.setProblemDescription(r.getProblemDescription());

        if (r.getCar() != null) {

            dto.setCarModel(r.getCar().getModel());

            dto.setPlateNumberArabic(
                    r.getCar().getPlateNumberArabic()
            );

            dto.setPlateNumberEnglish(
                    r.getCar().getPlateNumberEnglish()
            );
        }

        if (r.getLocation() != null) {
            dto.setAddress(r.getLocation().getAddress());
        }

        dto.setCreatedAt(r.getCreatedAt());

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
}