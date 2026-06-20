package org.example.tears.Service;

import lombok.RequiredArgsConstructor;
import org.example.tears.Api.ApiException;
import org.example.tears.DTO.EmployeeRequestResponseDto;
import org.example.tears.DTO.RequestSummaryDto;
import org.example.tears.Enums.StaffRequestStatus;
import org.example.tears.Mapper.RequestMapper;
import org.example.tears.Model.CarServiceRequest;
import org.example.tears.Model.Employee;
import org.example.tears.OutDTO.EmployeeRequestDetailsDto;
import org.example.tears.Repository.CarServiceRequestRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RequestQueryService {

    private final CarServiceRequestRepository requestRepo;
    private final RequestMapper requestMapper;

    public List<RequestSummaryDto> getAllRequests() {
        return requestRepo.findAllByOrderByIdDesc()
                .stream()
                .map(requestMapper::toSummaryDto)
                .toList();
    }

    public long getAllRequestsCount() {
        return requestRepo.count();
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
                .map(requestMapper::toEmployeeCardDto)
                .toList();
    }

    public long getMyRequestsCount(Employee employee){
        return requestRepo.countByAssignedEmployee_Id(employee.getId());
    }

    public List<EmployeeRequestResponseDto> getMyRequestsByStatus(Employee employee, StaffRequestStatus status) {
        return requestRepo.findByAssignedEmployeeAndStaffStatus(employee, status)
                .stream()
                .map(requestMapper::toEmployeeCardDto)
                .toList();
    }

    public EmployeeRequestDetailsDto getRequestDetails(
            Integer requestId,
            Employee employee
    ){

        CarServiceRequest request =
                requestRepo.findById(requestId)
                        .orElseThrow(() ->
                                new ApiException("الطلب غير موجود"));

        if(request.getAssignedEmployee()==null ||
                !request.getAssignedEmployee().getId().equals(employee.getId())){

            throw new ApiException("غير مصرح لك");
        }

        return requestMapper.toEmployeeDetailsDto(request);
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

    public List<RequestSummaryDto> searchMyRequests(
            Employee employee,
            String orderNumber,
            String plateArabic,
            String plateEnglish
    ) {

        return requestRepo.findByAssignedEmployee(employee)
                .stream()
                .filter(r ->
                        orderNumber == null ||
                                r.getOrderNumber().toLowerCase().contains(orderNumber.toLowerCase())
                )
                .filter(r ->
                        plateArabic == null ||
                                r.getCar().getPlateNumberArabic().replace(" ", "")
                                        .contains(plateArabic.replace(" ", ""))
                )
                .filter(r ->
                        plateEnglish == null ||
                                r.getCar().getPlateNumberEnglish().toLowerCase()
                                        .contains(plateEnglish.toLowerCase())
                )
                .map(requestMapper::toSummaryDto)
                .toList();
    }

}