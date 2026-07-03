package org.example.tears.Service;

import lombok.RequiredArgsConstructor;
import org.example.tears.Api.ApiException;
import org.example.tears.DTO.PricingRequestCardDto;
import org.example.tears.DTO.PricingRequestDetailsDto;
import org.example.tears.Mapper.PricingRequestMapper;
import org.example.tears.Model.CarServiceRequest;
import org.example.tears.Model.Employee;
import org.example.tears.Repository.CarServiceRequestRepository;
import org.springframework.stereotype.Service;

import java.util.List;
@Service
@RequiredArgsConstructor
public class PricingQueryService {

    private final CarServiceRequestRepository requestRepo;
    private final PricingRequestMapper pricingRequestMapper;

    public List<PricingRequestCardDto> getMyRequests(Employee employee) {

        return requestRepo.findByAssignedPricingEmployee(employee)
                .stream()
                .map(pricingRequestMapper::toPricingCardDto)
                .toList();
    }


    public PricingRequestDetailsDto getRequestDetails(
            Integer requestId,
            Employee employee
    ) {

        CarServiceRequest request =
                requestRepo.findById(requestId)
                        .orElseThrow(() ->
                                new ApiException("الطلب غير موجود"));

        if (request.getAssignedPricingEmployee() == null ||
                !request.getAssignedPricingEmployee().getId().equals(employee.getId())) {

            throw new ApiException("غير مصرح لك");
        }

        PricingRequestDetailsDto dto =
                pricingRequestMapper.toPricingDetailsDto(request);

        return dto;
    }
}
