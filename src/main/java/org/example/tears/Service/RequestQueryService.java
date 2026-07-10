package org.example.tears.Service;

import lombok.RequiredArgsConstructor;
import org.example.tears.Api.ApiException;
import org.example.tears.DTO.EmployeeRequestResponseDto;
import org.example.tears.DTO.RequestImageDto;
import org.example.tears.DTO.RequestSummaryDto;
import org.example.tears.Enums.EmployeeRole;
import org.example.tears.Enums.PricingStatus;
import org.example.tears.Enums.StaffRequestStatus;
import org.example.tears.Mapper.RequestMapper;
import org.example.tears.Model.CarServiceRequest;
import org.example.tears.Model.Employee;
import org.example.tears.OutDTO.EmployeeRequestDetailsDto;
import org.example.tears.Repository.CarServiceRequestRepository;
import org.example.tears.Repository.RequestImageRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RequestQueryService {

    private final CarServiceRequestRepository requestRepo;
    private final RequestImageRepository imageRepo;
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

    public long getMyNewRequestsCount(Employee employee) {

        if (employee.getEmployeeRole() == EmployeeRole.PRICING) {

            return requestRepo.countByAssignedPricingEmployee_IdAndPricingStatus(
                    employee.getId(),
                    PricingStatus.PRICING
            );
        }

        return requestRepo.countByAssignedEmployee_IdAndStaffStatus(
                employee.getId(),
                StaffRequestStatus.NEW
        );
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

        EmployeeRequestDetailsDto dto =
                requestMapper.toEmployeeDetailsDto(request);

        dto.setImages(
                imageRepo.findByRequest_Id(requestId)
                        .stream()
                        .map(img -> {
                            RequestImageDto imageDto = new RequestImageDto();

                            imageDto.setId(img.getId());
                            imageDto.setImageUrl(img.getImageUrl());

                            if (img.getUploadedAtStatus() != null) {
                                imageDto.setStatus(
                                        img.getUploadedAtStatus().name()
                                );
                            }

                            imageDto.setUploadedAt(
                                    img.getUploadedAt()
                            );

                            return imageDto;
                        })
                        .toList()
        );

        return dto;
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

    public List<EmployeeRequestResponseDto> searchMyRequests(
            Employee employee,
            String orderNumber,
            String plateArabic,
            String plateEnglish
    ) {

        List<CarServiceRequest> requests;

        if (employee.getEmployeeRole() == EmployeeRole.PRICING) {

            requests = requestRepo.findByAssignedPricingEmployee(employee);

        } else {

            requests = requestRepo.findByAssignedEmployee(employee);
        }

        return requests.stream()
                .filter(r ->
                        orderNumber == null ||
                                r.getOrderNumber()
                                        .toLowerCase()
                                        .startsWith(orderNumber.toLowerCase())
                )
                .filter(r ->
                        plateArabic == null ||
                                r.getCar()
                                        .getPlateNumberArabic()
                                        .replace(" ", "")
                                        .startsWith(
                                                plateArabic.replace(" ", "")
                                        )
                )
                .filter(r ->
                        plateEnglish == null ||
                                r.getCar()
                                        .getPlateNumberEnglish()
                                        .toLowerCase()
                                        .startsWith(plateEnglish.toLowerCase())
                )
                .map(requestMapper::toEmployeeCardDto)
                .toList();
    }

}