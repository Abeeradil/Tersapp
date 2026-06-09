package org.example.tears.Service;

import lombok.RequiredArgsConstructor;
import org.example.tears.DTO.EmployeeRequestResponseDto;
import org.example.tears.DTO.RequestSummaryDto;
import org.example.tears.Model.CarServiceRequest;
import org.example.tears.Model.Employee;
import org.example.tears.Repository.CarServiceRequestRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RequestQueryService {

    private final CarServiceRequestRepository requestRepo;
    private final AdminService adminService;

    public List<RequestSummaryDto> getAllRequests() {
        return requestRepo.findAllByOrderByIdDesc()
                .stream()
                .map(adminService::toSummaryDto)
                .toList();
    }

    public List<RequestSummaryDto> getUnassigned() {
        return requestRepo.findByAssignedEmployeeIsNull()
                .stream()
                .map(adminService::toSummaryDto)
                .toList();
    }

    public List<EmployeeRequestResponseDto> getMyRequests(Employee emp) {
        return requestRepo.findByAssignedEmployee(emp)
                .stream()
                .map(this::toEmployeeDto)
                .toList();
    }



    private EmployeeRequestResponseDto toEmployeeDto(CarServiceRequest r) {
        EmployeeRequestResponseDto dto = new EmployeeRequestResponseDto();

        dto.setId(r.getId());
        dto.setOrderNumber(r.getOrderNumber());
        dto.setStatus(r.getStage().name());
        dto.setProblemDescription(r.getProblemDescription());

        return dto;
    }
}