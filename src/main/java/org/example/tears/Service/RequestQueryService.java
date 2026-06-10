package org.example.tears.Service;

import lombok.RequiredArgsConstructor;
import org.example.tears.DTO.EmployeeRequestResponseDto;
import org.example.tears.DTO.RequestSummaryDto;
import org.example.tears.Mapper.RequestMapper;
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
    private final RequestMapper requestMapper;

    public List<RequestSummaryDto> getAllRequests() {
        return requestRepo.findAllByOrderByIdDesc()
                .stream()
                .map(requestMapper::toSummaryDto)
                .toList();
    }

    public List<RequestSummaryDto> getUnassigned() {
        return requestRepo.findByAssignedEmployeeIsNull()
                .stream()
                .map(requestMapper::toSummaryDto)
                .toList();
    }

    public List<EmployeeRequestResponseDto> getMyRequests(Employee employee) {
            return requestRepo.findByAssignedEmployee(employee)
                .stream()
                .map(requestMapper::toEmployeeDto)
                .toList();
    }


    public List<RequestSummaryDto> search(
            String orderNumber,
            String plateArabic,
            String plateEnglish
    ) {

        return requestRepo.search(
                        orderNumber,
                        plateArabic,
                        plateEnglish
                )
                .stream()
                .map(requestMapper::toSummaryDto)
                .toList();
    }

}